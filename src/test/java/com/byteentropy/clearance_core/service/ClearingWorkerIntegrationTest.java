package com.byteentropy.clearance_core.service;

import com.byteentropy.clearance_core.model.BatchStatus;
import com.byteentropy.clearance_core.model.ClearingBatch;
import com.byteentropy.clearance_core.model.PaymentEntity;
import com.byteentropy.clearance_core.repository.ClearingBatchRepository;
import com.byteentropy.clearance_core.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ClearingWorkerIntegrationTest {

    @Autowired
    private ClearingWorker clearingWorker;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ClearingBatchRepository batchRepository;

    @BeforeEach
    void setUp() {
        batchRepository.deleteAll();
        paymentRepository.deleteAll();
    }

    @Test
    @DisplayName("Should process authorized payments and create a completed batch with correct fees and currency")
    void testProcessClearingSuccess() {
        // Arrange
        PaymentEntity p1 = new PaymentEntity();
        p1.setTransactionReference("TXN-001");
        p1.setAmount(new BigDecimal("100.00"));
        p1.setCurrency("USD"); // Added currency
        p1.setStatus("AUTHORIZED");

        PaymentEntity p2 = new PaymentEntity();
        p2.setTransactionReference("TXN-002");
        p2.setAmount(new BigDecimal("200.00"));
        p2.setCurrency("USD"); // Added currency
        p2.setStatus("AUTHORIZED");

        paymentRepository.saveAll(List.of(p1, p2));

        // Act
        clearingWorker.processClearing();

        // Assert
        List<ClearingBatch> batches = batchRepository.findAll();
        assertEquals(1, batches.size());
        
        ClearingBatch batch = batches.get(0);
        assertEquals(BatchStatus.COMPLETED, batch.getStatus());
        assertEquals("USD", batch.getCurrency()); // VERIFY CURRENCY
        
        // Check sums (using compareTo for BigDecimal)
        assertEquals(0, new BigDecimal("300.00").compareTo(batch.getTotalAmount()));
        assertEquals(0, new BigDecimal("4.50").compareTo(batch.getTotalFees()));
    }

    @Test
    @DisplayName("Should verify financial rounding precision for complex amounts")
    void testRoundingPrecision() {
        // Arrange
        PaymentEntity p1 = new PaymentEntity();
        p1.setTransactionReference("TXN-ROUND");
        p1.setAmount(new BigDecimal("10.55")); 
        p1.setCurrency("USD");
        p1.setStatus("AUTHORIZED");
        paymentRepository.save(p1);

        // Act
        clearingWorker.processClearing();

        // Assert
        ClearingBatch batch = batchRepository.findAll().get(0);
        // 10.55 * 0.015 = 0.15825 -> Round Half Up to 2 decimal places = 0.16
        assertEquals(0, new BigDecimal("0.16").compareTo(batch.getTotalFees()));
        assertEquals("USD", batch.getCurrency());
    }

    @Test
    @DisplayName("Should not create a batch if no authorized payments exist")
    void testNoProcessWhenNoPayments() {
        PaymentEntity p1 = new PaymentEntity();
        p1.setTransactionReference("TXN-ALREADY-DONE");
        p1.setAmount(new BigDecimal("100.00"));
        p1.setCurrency("USD");
        p1.setStatus("CLEARED");
        paymentRepository.save(p1);

        clearingWorker.processClearing();

        assertEquals(0, batchRepository.count());
    }
}