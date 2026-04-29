package com.byteentropy.clearance_core.repository;

import com.byteentropy.clearance_core.model.ClearingBatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClearingBatchRepository extends JpaRepository<ClearingBatch, Long> {
}