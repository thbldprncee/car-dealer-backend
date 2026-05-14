package org.example;

import org.example.order.OrderServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.assertj.core.api.Assertions.assertThat;

class OrderServiceApplicationSchedulingTest {

    @Test
    void schedulingIsEnabledForOutboxPublisher() {
        assertThat(OrderServiceApplication.class.isAnnotationPresent(EnableScheduling.class)).isTrue();
    }
}
