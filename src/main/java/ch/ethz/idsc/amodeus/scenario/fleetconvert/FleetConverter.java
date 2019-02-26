/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.fleetconvert;

import java.io.File;

import ch.ethz.idsc.amodeus.scenario.DataOperator;

public interface FleetConverter {

    void run(File processingDir, File tripFile, DataOperator dataOperator) throws Exception;

}
