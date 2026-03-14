package br.com.microservices.choreographed.orderservice.core.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SagaProducer {

    @Value("${spring.kafka.topic.start-saga}")
    private String startSagaTopic;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendEvent(String payload) {
        try {
            log.info("Sending event to topic {} with data {}", startSagaTopic, payload);
            kafkaTemplate.send(startSagaTopic, payload);
        } catch (Exception ex) {
            log.error("Error trying to send data to topic {} with data {}", startSagaTopic, payload, ex);
        }
    }
}
