package io.github.thbldprncee.cardealer.storage;

import io.github.thbldprncee.cardealer.storage.StorageServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.assertj.core.api.Assertions.assertThat;

class StorageServiceApplicationSchedulingTest {

    @Test
    void schedulingIsEnabledForStorageOutboxPublisher() {
        assertThat(StorageServiceApplication.class.isAnnotationPresent(EnableScheduling.class)).isTrue();
    }
}
