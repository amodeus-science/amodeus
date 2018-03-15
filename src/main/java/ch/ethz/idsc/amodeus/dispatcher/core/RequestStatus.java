/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

public enum RequestStatus {
    REQUESTED("req", "requested"), //
    /** For roboTaxis: Open Request assigned to Vehicle */
    ASSIGNED("asd", "assigned"), //
    PICKUPDRIVE("otw", "on the way"), //
    PICKUP("pup", "pickup"), //
    DRIVING("drv", "driving"), //
    CANCELLED("can", "cancelled"), //
    DROPOFF("dof", "dropoff"), //
    EMPTY("noc", "no customer"), //
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

    public boolean unServiced() {
        return !equals(RequestStatus.DRIVING) && !equals(RequestStatus.DROPOFF);
    }

}
