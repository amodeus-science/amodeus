/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

public enum RoboTaxiStatus {
    DRIVEWITHCUSTOMER(true, "dwc", "with customer"), //
    DRIVETOCUSTOMER(true, "d2c", "pickup"), //
    REBALANCEDRIVE(true, "reb", "rebalance"), //
    STAY(false, "sty", "stay"), //
    PARKING(true, "par", "parking"), //
    OFFSERVICE(false, "off", "off service"),;

    private final String tag;
    private final String description;
    private final boolean isDriving;

    private RoboTaxiStatus(boolean isDriving, String xmlTag, String description) {
        this.isDriving = isDriving;
        this.tag = xmlTag;
        this.description = description;
    }

    public boolean isDriving() {
        return isDriving;
    }

    public String tag() {
        return tag;
    }

    public String description() {
        return description;
    }
}
