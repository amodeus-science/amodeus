/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

/** {@link EmptyDirective} is assigned to a vehicle that already is in the
 * desired state but should not be available to be assigned yet another
 * Directive within the iteration. */
/* package */ enum EmptyDirective implements DirectiveInterface {
    INSTANCE;

    @Override
    public void execute() {
        // intentionally blank
    }

}
