package io.github.thbldprncee.cardealer.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.thbldprncee.cardealer.events.ApprovalResult;
import io.github.thbldprncee.cardealer.events.OrderApprovalResultEvent;
import io.github.thbldprncee.cardealer.events.OrderType;
import io.github.thbldprncee.cardealer.order.domain.enums.InStockOrderStatus;
import io.github.thbldprncee.cardealer.order.domain.enums.OutboxEventStatus;
import io.github.thbldprncee.cardealer.order.domain.model.InStockCarOrder;
import io.github.thbldprncee.cardealer.order.repository.ClientRepository;
import io.github.thbldprncee.cardealer.order.repository.InStockCarOrderRepository;
import io.github.thbldprncee.cardealer.order.repository.OutboxEventRepository;
import io.github.thbldprncee.cardealer.order.repository.SalesManagerRepository;
import io.github.thbldprncee.cardealer.order.service.OutboxEventService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.lifecycle.Startables;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * The order-service half of the saga against real PostgreSQL and Kafka: warehouse decisions move
 * orders, and events written to the outbox reach the broker exactly once.
 */
@SpringBootTest(properties = "app.outbox.fixed-delay=200")
class OrderSagaIntegrationTest {

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
    }

    private static final UUID CLIENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID MANAGER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID SEED_CAR_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-ccccccccccc1");
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    @Autowired private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ClientRepository clientRepository;
    @Autowired private SalesManagerRepository salesManagerRepository;
    @Autowired private InStockCarOrderRepository inStockCarOrderRepository;
    @Autowired private OutboxEventService outboxEventService;
    @Autowired private OutboxEventRepository outboxEventRepository;
    @Autowired private TransactionTemplate transactionTemplate;

    @Test
    void warehouseApprovalMakesThePaidOrderReadyForDelivery() throws Exception {
        UUID orderId = paidOrder();

        send(result(orderId, ApprovalResult.APPROVED, null));
        send(result(orderId, ApprovalResult.APPROVED, null)); // a redelivery must not break anything

        await().atMost(TIMEOUT).until(() -> status(orderId) == InStockOrderStatus.READY_FOR_DELIVERY);
    }

    @Test
    void warehouseRejectionCancelsThePaidOrder() throws Exception {
        UUID orderId = paidOrder();

        send(result(orderId, ApprovalResult.REJECTED, "Car is not available"));

        await().atMost(TIMEOUT).until(() -> status(orderId) == InStockOrderStatus.CANCELED);
    }

    @Test
    void outboxEventsArePublishedToKafkaAndMarkedSent() {
        UUID aggregateId = UUID.randomUUID();
        String topic = "outbox-it-" + UUID.randomUUID();

        transactionTemplate.executeWithoutResult(status -> outboxEventService.saveEvent(
                aggregateId, "InStockCarOrder", "TestEvent", topic, Map.of("hello", "kafka"), "trace-it"));

        try (KafkaConsumer<String, String> consumer = consumer(topic)) {
            List<String> keys = new ArrayList<>();
            await().atMost(TIMEOUT).until(() -> {
                consumer.poll(Duration.ofMillis(500)).forEach(record -> keys.add(record.key()));
                return !keys.isEmpty();
            });
            assertThat(keys).containsExactly(aggregateId.toString());
        }
        await().atMost(TIMEOUT).until(() -> outboxEventRepository.findAll().stream()
                .anyMatch(event -> event.getAggregateId().equals(aggregateId)
                        && event.getStatus() == OutboxEventStatus.SENT));
    }

    private UUID paidOrder() {
        InStockCarOrder order = new InStockCarOrder(
                UUID.randomUUID(),
                clientRepository.findByIdAndRemovedFalse(CLIENT_ID).orElseThrow(),
                salesManagerRepository.findByIdAndRemovedFalse(MANAGER_ID).orElseThrow(),
                SEED_CAR_ID,
                "BMW 320i / VIN-IT",
                InStockOrderStatus.PAID
        );
        return inStockCarOrderRepository.save(order).getId();
    }

    private InStockOrderStatus status(UUID orderId) {
        return inStockCarOrderRepository.findByIdAndRemovedFalse(orderId).orElseThrow().getStatus();
    }

    private OrderApprovalResultEvent result(UUID orderId, ApprovalResult result, String reason) {
        return new OrderApprovalResultEvent(UUID.randomUUID(), orderId, OrderType.IN_STOCK, result, reason,
                "trace-it", Instant.now());
    }

    private void send(OrderApprovalResultEvent event) throws Exception {
        kafkaTemplate.send("order.approval-result", event.orderId().toString(),
                objectMapper.writeValueAsString(event)).get();
    }

    private KafkaConsumer<String, String> consumer(String topic) {
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class));
        consumer.subscribe(List.of(topic));
        return consumer;
    }
}
