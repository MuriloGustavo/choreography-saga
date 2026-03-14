package br.com.microservices.choreographed.productvalidationservice.core.consumer;

import br.com.microservices.choreographed.productvalidationservice.core.utils.JsonUtil;
import br.com.microservices.choreographed.productvalidationservice.core.service.ProductValidationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class ProductValidationConsumer {

    private final JsonUtil jsonUtil;
    private final ProductValidationService productValidationService;

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.product-validation-success}"
    )
    public void consumerSuccessEvent(String payload) {
        log.info("Receiving success event {} from product-validation-success topic", payload);
        var event = jsonUtil.toEvent(payload);
        productValidationService.validateExistingProducts(event);
    }

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.product-validation-fail}"
    )
    public void consumerFailEvent(String payload) {
        log.info("Receiving rollback event {} from product-validation-fail topic", payload);
        var event = jsonUtil.toEvent(payload);
        productValidationService.rollBackEvent(event);
    }
}
