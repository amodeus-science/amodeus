/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

/**
 * RoboTaxiStatus describes the various states in which a Robotaxi can be found in.
 *
 * DRIVEWITHCUSTOMER - at least one passenger on board
 * DRIVETOCUSTOMER - no passenger on board, on the way to pick-up a passenger
 * REBALANCEDRIVE - on a rebalancing mission
 * STAY - RoboTaxi is out of missions, available with no task outstanding
 * OFFSERVICE - off service, cannot be assigned a any task
 */
public enum RoboTaxiStatus {
    DRIVEWITHCUSTOMER(true, "dwc", "with customer"), //
    DRIVETOCUSTOMER(true, "d2c", "pickup"), //
    REBALANCEDRIVE(true, "reb", "rebalance"), //
    STAY(false, "sty", "stay"), //
    OFFSERVICE(false, "off", "off service"),;

    private final String tag;
    private final String description;
    private final boolean isDriving;

    RoboTaxiStatus(boolean isDriving, String xmlTag, String description) {
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
