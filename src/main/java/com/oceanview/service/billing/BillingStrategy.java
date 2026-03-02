package com.oceanview.service.billing;

import com.oceanview.model.Reservation;

public interface BillingStrategy {
    void applyPricing(Reservation reservation);

    String getStrategyName();
}
