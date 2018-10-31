/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

/** {@link EmptyDirective} is assigned to a vehicle that already is in the
 * desired state but should not be available to be assigned yet another
 * Directive within the iteration. */
/* package */ class StayToInfinityDirective implements AbstractDirective {
    RoboTaxi roboTaxi;

    StayToInfinityDirective(RoboTaxi roboTaxi) {
        this.roboTaxi = roboTaxi;
    }

    @Override
    public void execute() {

        ScheduleUtils.makeWhole(roboTaxi, 0.0, Double.POSITIVE_INFINITY, roboTaxi.getDivertableLocation());
        
    }

}
