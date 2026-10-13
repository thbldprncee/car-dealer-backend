package io.github.thbldprncee.cardealer.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableKafka
@EnableScheduling
@ConfigurationPropertiesScan(basePackages = "io.github.thbldprncee.cardealer.order.config.properties")
@EntityScan(basePackages = "io.github.thbldprncee.cardealer.order.domain.model")
@EnableJpaRepositories(basePackages = "io.github.thbldprncee.cardealer.order.repository")
@SpringBootApplication(scanBasePackages = "io.github.thbldprncee.cardealer.order")
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
