package br.com.microservices.choreographed.orderservice.core.service;

import br.com.microservices.choreographed.orderservice.config.exception.ValidationException;
import br.com.microservices.choreographed.orderservice.core.document.Event;
import br.com.microservices.choreographed.orderservice.core.document.History;
import br.com.microservices.choreographed.orderservice.core.document.Order;
import br.com.microservices.choreographed.orderservice.core.dto.EventFilters;
import br.com.microservices.choreographed.orderservice.core.repository.EventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static br.com.microservices.choreographed.orderservice.core.enums.ESagaStatus.SUCCESS;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
@AllArgsConstructor
public class EventService {

    private static final String CURRENT_SOURCE = "PAYMENT_SERVICE";

    private final EventRepository repository;

    public void notifyEnd(Event event) {
        event.setSource(CURRENT_SOURCE);
        event.setOrderId(event.getOrderId());
        event.setCreatedAt(LocalDateTime.now());
        setEndingHistory(event);
        save(event);
        log.info("Order {} with saga notified! TransactionId: {}", event.getOrderId(), event.getTransactionId());
    }

    public List<Event> findAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public Event findByFilters(EventFilters filters) {
        validateEmptyFilters(filters);
        if (!isEmpty(filters.getOrderId())) {
            return findByOrderId(filters.getOrderId());
        } else {
            return findByTransactionId(filters.getTransactionId());
        }
    }

    private void setEndingHistory(Event event) {
        if (SUCCESS.equals(event.getStatus())) {
            log.info("SAGA FINISHED SUCCESSFULLY FOR EVENT {}", event.getId());
            addHistory(event, "Saga finished successfully.");
        } else {
            log.info("SAGA FINISHED WITH ERRORS FOR EVENT {}", event.getId());
            addHistory(event, "Saga finished with errors.");
        }
    }

    private void validateEmptyFilters(EventFilters filters) {
        if (isEmpty(filters.getOrderId()) && isEmpty(filters.getTransactionId())) {
            throw new ValidationException("OrderId or TransactionId must be informed.");
        }
    }

    private Event findByOrderId(String orderId) {
        return repository
                .findTop1ByOrderIdOrderByCreatedAtDesc(orderId)
                .orElseThrow(() -> new ValidationException("Event not found by orderId."));
    }

    private Event findByTransactionId(String transactionId) {
        return repository
                .findTop1ByTransactionIdOrderByCreatedAtDesc(transactionId)
                .orElseThrow(() -> new ValidationException("Event not found by transactionId."));
    }

    private Event save(Event event) {
        return repository.save(event);
    }

    public Event createEvent(Order order) {
        var event = Event
                .builder()
                .source(CURRENT_SOURCE)
                .status(SUCCESS)
                .orderId(order.getId())
                .transactionId(order.getTransactionId())
                .payload(order)
                .createdAt(LocalDateTime.now())
                .build();
        addHistory(event, "Saga started");
        return save(event);
    }

    private void addHistory(Event event, String message) {
        var history = History
                .builder()
                .source(event.getSource())
                .status(event.getStatus())
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();
        event.addToHistory(history);
    }
}
