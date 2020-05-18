package ch.ethz.matsim.av.financial;

import ch.ethz.matsim.av.config.modal.AmodeusPricingConfig;

public class StaticPriceCalculator implements PriceCalculator {
    private final AmodeusPricingConfig pricingConfig;

    public StaticPriceCalculator(AmodeusPricingConfig pricingConfig) {
        this.pricingConfig = pricingConfig;
    }

    @Override
    public double calculatePrice(double travelDistance_m, double traveTime_s) {
        double billableDistance = Math.max(1, Math.ceil(travelDistance_m / pricingConfig.getSpatialBillingInterval())) * pricingConfig.getSpatialBillingInterval();

        double billableTravelTime = Math.max(1, Math.ceil(traveTime_s / pricingConfig.getTemporalBillingInterval())) * pricingConfig.getTemporalBillingInterval();

        double price = 0.0;

        price += (billableDistance / 1000.0) * pricingConfig.getPricePerKm();
        price += (billableTravelTime / 60.0) * pricingConfig.getPricePerMin();
        price += pricingConfig.getPricePerTrip();

        return price;
    }
}
