package ch.ethz.matsim.av.financial;

import java.util.Map;

import org.matsim.api.core.v01.Id;

import ch.ethz.matsim.av.config.operator.PricingConfig;
import ch.ethz.matsim.av.data.AVOperator;

public class StaticPriceCalculator implements PriceCalculator {
    private final Map<Id<AVOperator>, PricingConfig> pricingConfigs;

    public StaticPriceCalculator(Map<Id<AVOperator>, PricingConfig> pricingConfigs) {
        this.pricingConfigs = pricingConfigs;
    }

    @Override
    public double calculatePrice(Id<AVOperator> operatorId, double travelDistance_m, double traveTime_s) {
        PricingConfig priceStructure = pricingConfigs.get(operatorId);

        double billableDistance = Math.max(1, Math.ceil(travelDistance_m / priceStructure.getSpatialBillingInterval())) * priceStructure.getSpatialBillingInterval();

        double billableTravelTime = Math.max(1, Math.ceil(traveTime_s / priceStructure.getTemporalBillingInterval())) * priceStructure.getTemporalBillingInterval();

        double price = 0.0;

        price += (billableDistance / 1000.0) * priceStructure.getPricePerKm();
        price += (billableTravelTime / 60.0) * priceStructure.getPricePerMin();
        price += priceStructure.getPricePerTrip();

        return price;
    }
}
