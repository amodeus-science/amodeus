package ch.ethz.matsim.av.price_model;

import java.util.Optional;

import org.matsim.facilities.Facility;

import ch.ethz.matsim.av.analysis.FleetInformationListener;
import ch.ethz.matsim.av.analysis.FleetInformationListener.FleetInformation;

public class DynamicPriceModel implements PriceModel {
    private final FleetInformationListener listener;

    private final double costPerDistance_km;
    private final double costPerVehicle;
    private final double costPerTrip;

    private final double baseFare;

    public DynamicPriceModel(FleetInformationListener listener, double costPerDistance_km, double costPerVehicle, double costPerTrip, double baseFare) {
        this.listener = listener;

        this.costPerDistance_km = costPerDistance_km;
        this.costPerVehicle = costPerVehicle;
        this.costPerTrip = costPerTrip;
        this.baseFare = baseFare;
    }

    @Override
    public Optional<Double> calculatePrice(double departureTime, Facility pickupFacility, Facility dropoffFacility, double travelDistance, double traveTime) {
        FleetInformation information = listener.getInformation();

        double cost = 0.0;
        cost += costPerDistance_km * information.vehicleDistance_m / 1000.0;
        cost += costPerVehicle * information.numberOfVehicles;
        cost += costPerTrip * information.numberOfRequests;

        double baseRevenue = baseFare * information.numberOfRequests;
        double remainingCost = cost - baseRevenue;

        if (remainingCost < 0.0) {
            return Optional.of(baseFare);
        } else {
            double pricePerDistance = remainingCost / (information.passengerDistance_m / 1000.0);
            return Optional.of(baseFare + pricePerDistance * travelDistance);
        }
    }
}
