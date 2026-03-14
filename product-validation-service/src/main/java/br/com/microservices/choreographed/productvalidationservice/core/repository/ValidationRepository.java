package br.com.microservices.choreographed.productvalidationservice.core.repository;

import br.com.microservices.choreographed.productvalidationservice.core.model.Validation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ValidationRepository extends JpaRepository<Validation, Integer> {

    Boolean existsByOrderIdAndTransactionId(String orderId, String transactionId);
    
    Optional<Validation> findByOrderIdAndTransactionId(String orderId, String transactionId);
}
