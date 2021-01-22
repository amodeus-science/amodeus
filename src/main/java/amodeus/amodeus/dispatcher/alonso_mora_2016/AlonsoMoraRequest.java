package amodeus.amodeus.dispatcher.alonso_mora_2016;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.optimizer.Request;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;

public class AlonsoMoraRequest {
    private final PassengerRequest request;

    private final double latestDropoffTime;
    private final double latestPickupTime;

    private final double directDropoffTime;

    private double activePickupTime;
    private double activeDropoffTime;

    public AlonsoMoraRequest(PassengerRequest request, double latestPickupTime, double latestDropoffTime, double directDropoffTime) {
        this.request = request;
        this.latestDropoffTime = latestDropoffTime;
        this.activeDropoffTime = latestDropoffTime;

        this.latestPickupTime = latestPickupTime;
        this.activePickupTime = latestPickupTime;

        this.directDropoffTime = directDropoffTime;
    }

    public Id<Request> getId() {
        return request.getId();
    }

    public double getLatestDropoffTime() {
        return latestDropoffTime;
    }

    public double getLatestPickupTime() {
        return latestPickupTime;
    }

    public PassengerRequest getRequest() {
        return request;
    }

    public double getActivePickupTime() {
        return activePickupTime;
    }

    public void setActivePickupTime(double activePickupTime) {
        this.activePickupTime = activePickupTime;
    }

    public double getActiveDropoffTime() {
        return activeDropoffTime;
    }

    public void setActiveDropoffTime(double activeDropoffTime) {
        this.activeDropoffTime = activeDropoffTime;
    }

    public double getDirectDropoffTime() {
        return directDropoffTime;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof AlonsoMoraRequest) {
            AlonsoMoraRequest otherRequest = (AlonsoMoraRequest) other;
            return otherRequest.request.equals(request);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return request.hashCode();
    }
}
