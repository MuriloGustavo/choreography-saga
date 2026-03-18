package br.com.microservices.choreographed.paymentservice.core.saga;

import br.com.microservices.choreographed.paymentservice.core.dto.Event;
import br.com.microservices.choreographed.paymentservice.core.producer.KafkaProducer;
import br.com.microservices.choreographed.paymentservice.core.utils.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

@Slf4j
@Component
@RequiredArgsConstructor
public class SagaExecutionController {

    public static final String SAGA_LOG_ID = "ORDER ID: %s | TRANSACTION ID: %s | EVENT ID: %s";

    @Value("${spring.kafka.topic.inventory-success}")
    private String inventorySuccessTopic;

    @Value("${spring.kafka.topic.payment-fail}")
    private String paymentFailTopic;

    @Value("${spring.kafka.topic.product-validation-fail}")
    private String productValidationFailTopic;

    private final JsonUtil jsonUtil;
    private final KafkaProducer producer;

    public void handleSaga(Event event) {
        switch (event.getStatus()) {
            case SUCCESS -> handleSuccess(event);
            case ROLLBACK_PENDING -> handleRollbackPending(event);
            case FAIL -> handleFail(event);
        }
    }

    private void handleSuccess(Event event) {
        log.info("### CURRENT SAGA: {} | SUCCESS | NEXT TOPIC {} | {} ",
                event.getSource(), inventorySuccessTopic, createSagaId(event));
        sendEvent(event, inventorySuccessTopic);
    }

    private void handleRollbackPending(Event event) {
        log.info("### CURRENT SAGA: {} | SEND TO ROLLBACK CURRENT SERVICE | NEXT TOPIC {} | {} ",
                event.getSource(), paymentFailTopic, createSagaId(event));
        sendEvent(event, paymentFailTopic);
    }

    private void handleFail(Event event) {
        log.info("### CURRENT SAGA: {} | SEND TO ROLLBACK PREVIOUS SERVICE | NEXT TOPIC {} | {} ",
                event.getSource(), productValidationFailTopic, createSagaId(event));
        sendEvent(event, productValidationFailTopic);
    }

    private void sendEvent(Event event, String topic) {
        producer.sendEvent(jsonUtil.toJson(event), topic);
    }

    private String createSagaId(Event event) {
        return format(SAGA_LOG_ID, event.getPayload().getId(), event.getTransactionId(), event.getId());
    }
}
