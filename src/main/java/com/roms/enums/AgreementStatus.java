package com.roms.enums;

/**
 * Status of Agency Commission Agreement
 */
public enum AgreementStatus {
    /**
     * Agreement is active and payments can be made
     */
    ACTIVE,
    
    /**
     * All commission payments completed
     */
    COMPLETED,
    
    /**
     * Agreement cancelled (by SUPER_ADMIN only)
     */
    CANCELLED
}
