package org.matsim.amodeus.price_model;

import java.util.Optional;

import org.matsim.facilities.Facility;

public interface PriceModel {
    Optional<Double> calculatePrice(double departureTime, Facility pickupFacility, Facility dropoffFacility, double travelDistance, double traveTime);
}
