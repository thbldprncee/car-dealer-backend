package org.example;

import org.example.storage.StorageServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.assertj.core.api.Assertions.assertThat;

class StorageServiceApplicationSchedulingTest {

    @Test
    void schedulingIsEnabledForStorageOutboxPublisher() {
        assertThat(StorageServiceApplication.class.isAnnotationPresent(EnableScheduling.class)).isTrue();
    }
}
