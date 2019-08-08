/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

/** base class for all directives that are collected during vehicle dispatching */
@FunctionalInterface
/* package */ interface DirectiveInterface {
    void execute();
}
