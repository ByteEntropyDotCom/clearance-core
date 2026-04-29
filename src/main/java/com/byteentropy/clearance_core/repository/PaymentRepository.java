package com.byteentropy.clearance_core.repository;

import com.byteentropy.clearance_core.model.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    List<PaymentEntity> findByStatus(String status);
}