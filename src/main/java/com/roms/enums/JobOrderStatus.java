package com.roms.enums;

public enum JobOrderStatus {
    PENDING_APPROVAL,  // Job order pending admin approval
    OPEN,              // Approved and accepting applications
    IN_PROGRESS,       // Actively recruiting
    FILLED,            // All positions filled
    CLOSED,            // No longer accepting applications
    CANCELLED,         // Job order cancelled
    ON_HOLD            // Temporarily paused
}
