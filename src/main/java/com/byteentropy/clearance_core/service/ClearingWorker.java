package com.byteentropy.clearance_core.service;

import com.byteentropy.clearance_core.client.BankNetworkClient;
import com.byteentropy.clearance_core.model.*;
import com.byteentropy.clearance_core.repository.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Service
public class ClearingWorker {

    private static final Logger logger = Logger.getLogger(ClearingWorker.class.getName());

    private final PaymentRepository paymentRepo;
    private final ClearingBatchRepository batchRepo;
    private final FeeEngine feeEngine;
    private final BankNetworkClient bankClient;

    public ClearingWorker(PaymentRepository paymentRepo, ClearingBatchRepository batchRepo, 
                          FeeEngine feeEngine, BankNetworkClient bankClient) {
        this.paymentRepo = paymentRepo;
        this.batchRepo = batchRepo;
        this.feeEngine = feeEngine;
        this.bankClient = bankClient;
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void processClearing() {
        List<PaymentEntity> pendingPayments = paymentRepo.findByStatus("AUTHORIZED");
        
        if (pendingPayments.isEmpty()) return;

        logger.info("Starting clearing process for " + pendingPayments.size() + " payments.");

        ClearingBatch batch = new ClearingBatch();
        batch.setCreatedAt(LocalDateTime.now());
        batch.setStatus(BatchStatus.PROCESSING);
        batch.setTotalAmount(BigDecimal.ZERO);
        batch.setTotalFees(BigDecimal.ZERO);
        // Set currency from the first payment in the batch
        batch.setCurrency(pendingPayments.get(0).getCurrency()); 
        
        batch = batchRepo.save(batch);

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalFees = BigDecimal.ZERO;

        try {
            for (PaymentEntity payment : pendingPayments) {
                BigDecimal fee = feeEngine.calculateFee(payment.getAmount());
                totalAmount = totalAmount.add(payment.getAmount());
                totalFees = totalFees.add(fee);

                bankClient.sendCaptureRequest(payment.getTransactionReference(), payment.getAmount());

                payment.setStatus("CLEARED");
                paymentRepo.save(payment);
            }

            batch.setTotalAmount(totalAmount);
            batch.setTotalFees(totalFees);
            batch.setStatus(BatchStatus.COMPLETED);
            batchRepo.save(batch);
            
            logger.info("Batch " + batch.getId() + " [" + batch.getCurrency() + "] completed successfully.");

        } catch (Exception e) {
            logger.severe("CRITICAL: Clearing failed. Batch ID: " + batch.getId() + ". Error: " + e.getMessage());
            batch.setStatus(BatchStatus.FAILED);
            batchRepo.save(batch);
            throw new RuntimeException("Rollback triggered", e);
        }
    }
}