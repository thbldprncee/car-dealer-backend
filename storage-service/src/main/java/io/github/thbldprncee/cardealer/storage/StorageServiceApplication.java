package io.github.thbldprncee.cardealer.storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableKafka
@EnableScheduling
@ConfigurationPropertiesScan(basePackages = "io.github.thbldprncee.cardealer.storage.config.properties")
@EntityScan(basePackages = "io.github.thbldprncee.cardealer.storage.domain.model")
@EnableJpaRepositories(basePackages = "io.github.thbldprncee.cardealer.storage.repository")
@SpringBootApplication(scanBasePackages = "io.github.thbldprncee.cardealer.storage")
public class StorageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StorageServiceApplication.class, args);
    }
}
