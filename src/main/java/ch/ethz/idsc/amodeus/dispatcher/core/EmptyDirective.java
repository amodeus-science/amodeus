/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

/** {@link StayToInfinityDirective} is assigned to a vehicle that already is in the
 * desired state but should not be available to be assigned yet another
 * Directive within the iteration. */
/* package */ enum EmptyDirective implements AbstractDirective {
    INSTANCE;

    @Override
    public void execute() {
        // intentionally blank
    }

}
