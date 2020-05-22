package ch.ethz.matsim.av.financial;

public interface PriceCalculator {
    double calculatePrice(double travelDistance_m, double traveTime_s);
}
