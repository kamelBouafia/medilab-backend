package com.medilab.enums;

public enum AgreementStatus {
    PENDING, // Initial request, awaiting partner review
    COUNTER_OFFER, // Partner modified prices, awaiting main lab confirmation
    APPROVED, // Both labs agreed, can be activated
    REJECTED, // Partner rejected the request
    CANCELLED, // Requesting lab cancelled the request
    INACTIVE, // Agreement terminated
    EXPIRED // Validity period has passed
}
