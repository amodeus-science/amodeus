package ch.ethz.idsc.amodeus.scenario.trips;

import org.matsim.api.core.v01.Coord;

import java.util.Date;

public class Trip implements Comparable<Trip> {
    public final Integer Id;
    public final Integer TaxiId;
    public final Coord PickupLoc;
    public final Coord DropoffLoc;
    public final Double Distance; // meters
    public final Double WaitTime;
    // ---
    public Date PickupDate;
    public Date DropoffDate;
    public long Duration; // seconds

    public Trip(Integer id, Integer taxiId, Date pickupDate, Date dropoffDate, Coord pickupLoc, Coord dropoffLoc, //
                long duration, Double distance, Double waitTime) {
        this.Id = id;
        this.TaxiId = taxiId;
        this.PickupDate = pickupDate;
        this.DropoffDate = dropoffDate;
        this.PickupLoc = pickupLoc;
        this.DropoffLoc = dropoffLoc;
        this.Duration = duration;
        this.Distance = distance;
        this.WaitTime = waitTime;
    }

    @Override
    public int compareTo(Trip trip) {
        return this.PickupDate.compareTo(trip.PickupDate);
    }

    @Override
    public String toString() {
        return PickupDate + "\t:\tTrip " + Id + "\t(Taxi " + TaxiId + ")";
    }
}
