/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.report;

@FunctionalInterface
//TODO @marcalbert improve documentation of class
public interface TotalValueIdentifier {
    /** @return string key in map that stores values of total quantities in analysis */
    String getIdentifier();
}
