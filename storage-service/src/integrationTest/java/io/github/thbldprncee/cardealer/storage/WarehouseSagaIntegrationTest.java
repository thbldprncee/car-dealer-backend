package io.github.thbldprncee.cardealer.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.thbldprncee.cardealer.events.ApprovalResult;
import io.github.thbldprncee.cardealer.events.OrderApprovalResultEvent;
import io.github.thbldprncee.cardealer.events.OrderCanceledEvent;
import io.github.thbldprncee.cardealer.events.OrderSentForApprovalEvent;
import io.github.thbldprncee.cardealer.events.OrderType;
import io.github.thbldprncee.cardealer.storage.domain.enums.AssemblyOrderStatus;
import io.github.thbldprncee.cardealer.storage.domain.model.AssemblyOrder;
import io.github.thbldprncee.cardealer.storage.domain.model.Car;
import io.github.thbldprncee.cardealer.storage.repository.AssemblyOrderRepository;
import io.github.thbldprncee.cardealer.storage.repository.CarRepository;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.lifecycle.Startables;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * The warehouse half of the order approval saga, end to end: events go through a real Kafka
 * broker, state lands in a real PostgreSQL, and replies leave through the transactional outbox.
 */
@SpringBootTest(properties = "app.outbox.fixed-delay=200")
class WarehouseSagaIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");
    static final KafkaContainer KAFKA = new KafkaContainer("apache/kafka:3.7.2");

    static {
        Startables.deepStart(POSTGRES, KAFKA).join();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("grpc.server.port", WarehouseSagaIntegrationTest::freePort);
    }

    private static int freePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private static final UUID RESERVED_CAR_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-ccccccccccc2");
    private static final UUID UNTOUCHED_CAR_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-ccccccccccc1");
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    @Autowired private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CarRepository carRepository;
    @Autowired private AssemblyOrderRepository assemblyOrderRepository;

    private KafkaConsumer<String, String> results;

    @BeforeEach
    void subscribeToResults() {
        results = new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class));
        results.subscribe(List.of("order.approval-result"));
    }

    @AfterEach
    void closeConsumer() {
        results.close();
    }

    @Test
    void reservesTheCarAndReleasesItWhenTheOrderIsCanceled() throws Exception {
        UUID orderId = UUID.randomUUID();

        send("order.sent-for-approval", orderId, new OrderSentForApprovalEvent(UUID.randomUUID(), orderId,
                OrderType.IN_STOCK, RESERVED_CAR_ID, null, null, List.of(), "trace-1", Instant.now()));

        assertThat(awaitResult(orderId).result()).isEqualTo(ApprovalResult.APPROVED);
        assertThat(car(RESERVED_CAR_ID).isAvailable()).isFalse();

        send("order.canceled", orderId, new OrderCanceledEvent(UUID.randomUUID(), orderId,
                OrderType.IN_STOCK, RESERVED_CAR_ID, "trace-1", Instant.now()));

        await().atMost(TIMEOUT).until(() -> car(RESERVED_CAR_ID).isAvailable());
        assertThat(assemblyOrder(orderId).getStatus()).isEqualTo(AssemblyOrderStatus.CANCELED);
    }

    @Test
    void rejectsAnApprovalRequestThatArrivesAfterTheCancellation() throws Exception {
        UUID orderId = UUID.randomUUID();

        send("order.canceled", orderId, new OrderCanceledEvent(UUID.randomUUID(), orderId,
                OrderType.IN_STOCK, UNTOUCHED_CAR_ID, "trace-2", Instant.now()));
        await().atMost(TIMEOUT)
                .until(() -> assemblyOrderRepository.findBySourceOrderIdAndRemovedFalse(orderId).isPresent());

        send("order.sent-for-approval", orderId, new OrderSentForApprovalEvent(UUID.randomUUID(), orderId,
                OrderType.IN_STOCK, UNTOUCHED_CAR_ID, null, null, List.of(), "trace-2", Instant.now()));

        OrderApprovalResultEvent result = awaitResult(orderId);
        assertThat(result.result()).isEqualTo(ApprovalResult.REJECTED);
        assertThat(result.reason()).isEqualTo("Order was canceled");
        assertThat(car(UNTOUCHED_CAR_ID).isAvailable()).isTrue();
    }

    @Test
    void redeliveredApprovalRequestReservesOnlyOnce() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID carId = UUID.fromString("cccccccc-cccc-cccc-cccc-ccccccccccc1");
        OrderSentForApprovalEvent event = new OrderSentForApprovalEvent(UUID.randomUUID(), orderId,
                OrderType.CUSTOM, null, UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1"), null,
                List.of(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1")), "trace-3", Instant.now());

        send("order.sent-for-approval", orderId, event);
        send("order.sent-for-approval", orderId, event);

        assertThat(awaitResult(orderId).result()).isEqualTo(ApprovalResult.APPROVED);
        assertThat(assemblyOrderRepository.findAllByRemovedFalse())
                .filteredOn(order -> order.getSourceOrderId().equals(orderId))
                .hasSize(1);
        assertThat(car(carId).isAvailable()).isTrue();
    }

    private void send(String topic, UUID key, Object event) throws Exception {
        kafkaTemplate.send(topic, key.toString(), objectMapper.writeValueAsString(event)).get();
    }

    private Car car(UUID id) {
        return carRepository.findByIdAndRemovedFalse(id).orElseThrow();
    }

    private AssemblyOrder assemblyOrder(UUID orderId) {
        return assemblyOrderRepository.findBySourceOrderIdAndRemovedFalse(orderId).orElseThrow();
    }

    private OrderApprovalResultEvent awaitResult(UUID orderId) {
        List<OrderApprovalResultEvent> received = new ArrayList<>();
        await().atMost(TIMEOUT).until(() -> {
            results.poll(Duration.ofMillis(500)).forEach(record -> {
                try {
                    OrderApprovalResultEvent event = objectMapper.readValue(record.value(), OrderApprovalResultEvent.class);
                    if (event.orderId().equals(orderId)) {
                        received.add(event);
                    }
                } catch (Exception exception) {
                    throw new IllegalStateException(exception);
                }
            });
            return !received.isEmpty();
        });
        return received.get(0);
    }
}
