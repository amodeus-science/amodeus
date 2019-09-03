/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.fleetconvert;

import java.io.File;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.scenario.DataOperator;

public interface FleetConverter {

    public void run(File processingDir, File tripFile, DataOperator<?> dataOperator,//
            ScenarioOptions scenarioOptions, Network network, String tripId) throws Exception;

}
