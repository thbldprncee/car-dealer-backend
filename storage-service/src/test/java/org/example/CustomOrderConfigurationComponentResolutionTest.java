package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.domain.enums.ComponentType;
import org.example.domain.model.CarConfiguration;
import org.example.domain.model.ComponentOption;
import org.example.events.OrderSentForApprovalEvent;
import org.example.events.OrderType;
import org.example.repository.AssemblyOrderRepository;
import org.example.repository.CarConfigurationRepository;
import org.example.repository.CarModelRepository;
import org.example.repository.CarRepository;
import org.example.repository.ComponentOptionRepository;
import org.example.repository.ProcessedEventRepository;
import org.example.service.OrderSentForApprovalListener;
import org.example.service.OutboxEventService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomOrderConfigurationComponentResolutionTest {

    @Test
    @SuppressWarnings("unchecked")
    void customOrderComponentsAreResolvedFromConfigurationIdWhenEventListIsEmpty() {
        UUID configurationId = UUID.randomUUID();
        UUID componentOptionId = UUID.randomUUID();
        CarConfigurationRepository configurationRepository = mock(CarConfigurationRepository.class);
        CarConfiguration configuration = mock(CarConfiguration.class);
        ComponentOption componentOption = mock(ComponentOption.class);
        when(componentOption.getId()).thenReturn(componentOptionId);
        when(configuration.getSelectedComponents()).thenReturn(Map.of(ComponentType.WHEELS, componentOption));
        when(configurationRepository.findByIdAndRemovedFalse(configurationId)).thenReturn(Optional.of(configuration));

        OrderSentForApprovalListener listener = new OrderSentForApprovalListener(
                new ObjectMapper(),
                mock(ProcessedEventRepository.class),
                mock(AssemblyOrderRepository.class),
                mock(CarRepository.class),
                mock(CarModelRepository.class),
                configurationRepository,
                mock(ComponentOptionRepository.class),
                mock(OutboxEventService.class)
        );
        OrderSentForApprovalEvent event = new OrderSentForApprovalEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                OrderType.CUSTOM,
                null,
                UUID.randomUUID(),
                configurationId,
                List.of(),
                "trace-id",
                Instant.now()
        );

        List<UUID> componentOptionIds = (List<UUID>) ReflectionTestUtils.invokeMethod(
                listener,
                "resolveComponentOptionIds",
                event
        );

        assertThat(componentOptionIds).containsExactly(componentOptionId);
    }
}
