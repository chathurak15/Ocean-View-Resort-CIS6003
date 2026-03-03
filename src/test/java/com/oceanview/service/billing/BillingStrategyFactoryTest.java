package com.oceanview.service.billing;

import org.junit.Test;
import static org.junit.Assert.*;

public class BillingStrategyFactoryTest {

    // FLAT rate strategy created successfully
    @Test
    public void create_shouldReturnFlatRateStrategy_whenTypeIsFlat() {
        BillingStrategy strategy = BillingStrategyFactory.create(BillingStrategyFactory.BillingType.FLAT);
        assertNotNull(strategy);
        assertEquals("FlatRateBillingStrategy", strategy.getStrategyName());
    }

    // SURCHARGE strategy created successfully
    @Test
    public void create_shouldReturnSurchargeStrategy_whenTypeIsSurcharge() {
        BillingStrategy strategy = BillingStrategyFactory.create(BillingStrategyFactory.BillingType.SURCHARGE);
        assertNotNull(strategy);
        assertEquals("RoomTypeSurchargeBillingStrategy", strategy.getStrategyName());
    }

    // Each call returns a new instance (no shared state)
    @Test
    public void create_shouldReturnNewInstanceOnEachCall() {
        BillingStrategy s1 = BillingStrategyFactory.create(BillingStrategyFactory.BillingType.FLAT);
        BillingStrategy s2 = BillingStrategyFactory.create(BillingStrategyFactory.BillingType.FLAT);
        assertNotSame(s1, s2);
    }

    // Null type throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void create_shouldThrowException_whenTypeIsNull() {
        BillingStrategyFactory.create(null);
    }
}
