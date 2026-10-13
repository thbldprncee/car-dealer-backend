package io.github.thbldprncee.cardealer.order.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * A listener failure is retried three times with a one-second pause; after that the record is
 * published to {@code <topic>.DLT} so one bad message cannot stall the partition. Malformed JSON
 * goes there immediately, since retrying it can never succeed.
 */
@Slf4j
@Configuration
public class KafkaErrorHandlingConfig {

    @Bean
    public CommonErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3));
        handler.addNotRetryableExceptions(JsonProcessingException.class);
        handler.setRetryListeners((record, exception, attempt) ->
                log.warn("Kafka listener failed topic={}, offset={}, attempt={}",
                        record.topic(), record.offset(), attempt, exception));
        return handler;
    }
}
