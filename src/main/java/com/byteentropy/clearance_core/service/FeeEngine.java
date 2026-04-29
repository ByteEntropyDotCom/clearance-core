package com.byteentropy.clearance_core.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class FeeEngine {
    private static final BigDecimal FEE_PERCENTAGE = new BigDecimal("0.015"); // 1.5%

    public BigDecimal calculateFee(BigDecimal amount) {
        if (amount == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        
        // Use HALF_UP (standard commercial rounding) and scale of 2 for currency
        return amount.multiply(FEE_PERCENTAGE).setScale(2, RoundingMode.HALF_UP);
    }
}