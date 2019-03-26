/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.dataclean;

import java.io.File;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;

public class StandardDataCorrector implements DataCorrector {

    @Override
    public File correctFile(File taxiData, MatsimAmodeusDatabase db) throws Exception {
        return taxiData;
    }
}
