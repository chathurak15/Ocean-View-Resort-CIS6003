package com.oceanview.service.billing;

// Factory Pattern, creates the correct BillingStrategy at runtime
public class BillingStrategyFactory {

    public enum BillingType {
        FLAT,
        SURCHARGE
    }

    // create billing strategy based on type
    public static BillingStrategy create(BillingType type) {
        if (type == null) {
            throw new IllegalArgumentException("Billing type must not be null");
        }
        return switch (type) {
            case FLAT -> new FlatRateBillingStrategy();
            case SURCHARGE -> new RoomTypeSurchargeBillingStrategy();
        };
    }
}
