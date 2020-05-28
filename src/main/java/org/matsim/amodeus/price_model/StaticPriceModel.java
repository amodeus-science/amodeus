package org.matsim.amodeus.price_model;

import java.util.Optional;

import org.matsim.amodeus.config.modal.PricingConfig;
import org.matsim.facilities.Facility;

public class StaticPriceModel implements PriceModel {
    private final PricingConfig pricingConfig;

    public StaticPriceModel(PricingConfig pricingConfig) {
        this.pricingConfig = pricingConfig;
    }

    @Override
    public Optional<Double> calculatePrice(double departureTime, Facility pickupFacility, Facility dropoffFacility, double travelDistance, double traveTime) {
        double billableDistance = Math.max(1, Math.ceil(travelDistance / pricingConfig.getSpatialBillingInterval())) * pricingConfig.getSpatialBillingInterval();
        double billableTravelTime = Math.max(1, Math.ceil(traveTime / pricingConfig.getTemporalBillingInterval())) * pricingConfig.getTemporalBillingInterval();

        double price = 0.0;

        price += (billableDistance / 1000.0) * pricingConfig.getPricePerKm();
        price += (billableTravelTime / 60.0) * pricingConfig.getPricePerMin();
        price += pricingConfig.getPricePerTrip();

        return Optional.of(price);
    }
}
