package com.roms.enums;

/**
 * Status of an offer letter in the recruitment workflow
 */
public enum OfferLetterStatus {
    /**
     * Offer letter is being prepared, not yet issued to candidate
     */
    DRAFT,
    
    /**
     * Offer letter has been issued to the candidate and awaiting signature
     */
    ISSUED,
    
    /**
     * Offer letter has been signed by the candidate
     */
    SIGNED,
    
    /**
     * Offer letter was withdrawn or cancelled
     */
    WITHDRAWN
}
