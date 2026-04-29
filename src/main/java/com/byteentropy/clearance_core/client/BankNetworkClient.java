package com.byteentropy.clearance_core.client;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class BankNetworkClient {
    // Simulates an API call to the Bank/Visa/Mastercard network
    public String sendCaptureRequest(String ref, java.math.BigDecimal amount) {
        System.out.println("External Call: Capturing " + amount + " for Ref: " + ref);
        return "BANK_ACK_" + UUID.randomUUID().toString().substring(0, 8);
    }
}