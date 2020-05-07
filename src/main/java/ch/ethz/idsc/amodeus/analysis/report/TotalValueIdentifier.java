/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.report;

@FunctionalInterface
/** Functional interface used in Analysis to identify (as string) parameters
 * analyzed in the simulation analysis components */
public interface TotalValueIdentifier {
    /** @return string key in map that stores values of total quantities in analysis */
    String getIdentifier();
}
