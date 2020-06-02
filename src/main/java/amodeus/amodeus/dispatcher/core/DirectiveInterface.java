/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

/** interface for all directives that are collected during vehicle dispatching */
@FunctionalInterface
/* package */ interface DirectiveInterface {
    void execute();
}
