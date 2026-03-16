package br.com.microservices.choreographed.orderservice.core.service;

import br.com.microservices.choreographed.orderservice.core.document.Order;
import br.com.microservices.choreographed.orderservice.core.dto.OrderRequest;
import br.com.microservices.choreographed.orderservice.core.producer.SagaProducer;
import br.com.microservices.choreographed.orderservice.core.repository.OrderRepository;
import br.com.microservices.choreographed.orderservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class OrderService {

    public static final String TRANSACTION_ID_PATTERN = "%s_%s";

    private final JsonUtil jsonUtil;
    private final SagaProducer producer;
    private final EventService eventService;
    private final OrderRepository repository;

    public Order createOrder(OrderRequest orderRequest) {
        var order = Order
                .builder()
                .products(orderRequest.getProducts())
                .createdAt(LocalDateTime.now())
                .transactionId(generateTransactionId())
                .build();
        repository.save(order);
        producer.sendEvent(jsonUtil.toJson(eventService.createEvent(order)));
        return order;
    }

    private static String generateTransactionId() {
        return String.format(TRANSACTION_ID_PATTERN, Instant.now().toEpochMilli(), UUID.randomUUID());
    }
}
