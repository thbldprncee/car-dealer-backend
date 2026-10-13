package io.github.thbldprncee.cardealer.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaStringPayloadConfigTest {

    @Test
    void kafkaUsesStringPayloadSerializers() {
        Properties properties = loadApplicationProperties();

        assertThat(properties.getProperty("spring.kafka.producer.key-serializer"))
                .isEqualTo("org.apache.kafka.common.serialization.StringSerializer");
        assertThat(properties.getProperty("spring.kafka.producer.value-serializer"))
                .isEqualTo("org.apache.kafka.common.serialization.StringSerializer");
        assertThat(properties.getProperty("spring.kafka.consumer.key-deserializer"))
                .isEqualTo("org.apache.kafka.common.serialization.StringDeserializer");
        assertThat(properties.getProperty("spring.kafka.consumer.value-deserializer"))
                .isEqualTo("org.apache.kafka.common.serialization.StringDeserializer");
        assertThat(properties.getProperty("spring.kafka.consumer.group-id")).isEqualTo("order-service");
        assertThat(properties.getProperty("app.outbox.max-retries")).isEqualTo("5");
    }

    private Properties loadApplicationProperties() {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(new ClassPathResource("application.yml"));
        return factory.getObject();
    }
}
