/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.report;

import java.util.Map;

// TODO DOC document member functions
public interface TotalValueAppender {
    Map<TotalValueIdentifier, String> getTotalValues();
}
