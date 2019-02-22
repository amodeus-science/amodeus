package ch.ethz.idsc.amodeus.scenario.fleetconvert;


import java.io.File;

import ch.ethz.idsc.amodeus.scenario.chicago.DataOperator;

public interface FleetConverter {

    void run(File processingDir, File tripFile, DataOperator dataOperator) throws Exception;

}
