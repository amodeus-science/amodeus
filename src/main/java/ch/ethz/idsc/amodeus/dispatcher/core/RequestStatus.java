/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import org.matsim.amodeus.dvrp.request.AVRequest;

public enum RequestStatus {
    /** REGULAR REQUEST PROCESS */
    /** Request entered system. */
    REQUESTED("req", "requested"), //
    /** Robotaxi has been assigned to the open request. */
    ASSIGNED("asd", "assigned"), //
    /** Robotaxi is on pickup drive. */
    PICKUPDRIVE("otw", "on the way"), //
    /** Pickup is taking place (1 time step only). */
    PICKUP("pup", "pickup"), //
    /** Robotaxi driving with customer. */
    DRIVING("drv", "driving"), //
    /** Dropoff is taking place (1 time step only). */
    DROPOFF("dof", "dropoff"), //

    /** IRREGULAR REQEUST STATII */
    /** Request cancelled before pickup. */
    CANCELLED("can", "cancelled"), //
    /** Used to label invalid requests in datasets. */
    INVALID("inv", "invalid"), //
    /** Used to label invalid requests in datasets. */
    EMPTY("emp", "empty"), //
    ;

    public final String tag;
    public final String description;

    RequestStatus(String xmlTag, String description) {
        this.tag = xmlTag;
        this.description = description;
    }

    public String tag() {
        return tag;
    }

    /** @return true if the customer is not yet served, false otherwise.
     *         A {@link AVRequest} is counted as served as soon as the "moment of impatience"
     *         for the customer is over and the customer is together with the car, i.e.,
     *         in pickup process, driving or during dropoff */
    public boolean isUnserved() {
        return !(this.equals(RequestStatus.DRIVING) || //
                this.equals(RequestStatus.PICKUP) || //
                this.equals(RequestStatus.DROPOFF));
    }
}
