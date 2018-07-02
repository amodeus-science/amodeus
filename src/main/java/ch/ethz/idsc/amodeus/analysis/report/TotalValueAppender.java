/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.report;

import java.util.Map;

/**
 * A total Value Appender can be used to append total values of a simulation to the total Values properties file in Amodeus.
 * To add the values defined in the getTotalValues() function the Appender has to be added with the Analysis.addTotalValue() function.
 * @author Lukas Sieber
 *
 */
public interface TotalValueAppender {
/**
 * defines pairs of a total values with a total value identifier. 
 * @return Map with entries of an identifier and a corresponding value as string. 
 */
    Map<TotalValueIdentifier, String> getTotalValues();
}
