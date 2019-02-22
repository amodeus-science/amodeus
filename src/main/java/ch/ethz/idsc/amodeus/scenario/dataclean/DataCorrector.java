package ch.ethz.idsc.amodeus.scenario.dataclean;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;

import java.io.File;

public interface DataCorrector {

    File correctFile(File taxiData, MatsimAmodeusDatabase db) throws Exception;

}
