package ch.ethz.matsim.av.financial;

import org.matsim.api.core.v01.Id;

import ch.ethz.matsim.av.data.AVOperator;

public interface PriceCalculator {
    double calculatePrice(Id<AVOperator> operatorId, double travelDistance_m, double traveTime_s);
}
