package br.com.microservices.choreographed.orderservice.core.consumer;

import br.com.microservices.choreographed.orderservice.core.service.EventService;
import br.com.microservices.choreographed.orderservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class EventConsumer {

    private final JsonUtil jsonUtil;
    private final EventService service;

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.notify-ending}"
    )
    public void consumerNotifyEndingEvent(String payload) {
        log.info("Receiving event {} from notify-ending topic", payload);
        var event = jsonUtil.toEvent(payload);
        service.notifyEnd(event);
    }
}
