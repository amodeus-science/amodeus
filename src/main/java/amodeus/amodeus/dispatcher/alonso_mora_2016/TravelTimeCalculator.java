package amodeus.amodeus.dispatcher.alonso_mora_2016;

import org.matsim.api.core.v01.network.Link;

public interface TravelTimeCalculator {
    double getTravelTime(double departureTime, Link originLink, Link destinationLink);

    void clear();
}
