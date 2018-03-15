/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.data;

import org.matsim.api.core.v01.Coord;

/** the location spec help to make the ScenarioViewer
 * zoom in on the correct location in the map. */
public interface LocationSpec {
    /** @return reference frame of simulation */
    ReferenceFrame referenceFrame();

    /** @return coordinate of center of simulation
     *         the scenario viewer starts at that location */
    Coord center();

    /** @return identified that matches the string used in the
     *         specifications AmodeusOptions.properties */
    String name();
}
