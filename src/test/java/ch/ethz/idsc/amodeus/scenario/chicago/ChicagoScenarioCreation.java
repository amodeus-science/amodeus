package ch.ethz.idsc.amodeus.scenario.chicago;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import ch.ethz.idsc.amodeus.util.io.MultiFileTools;

public class ChicagoScenarioCreation {
    @Test
    public void test() throws IOException, Exception {
        File workingDir = MultiFileTools.getDefaultWorkingDirectory();
        StaticHelper.setupTest(workingDir);
        CreateChicagoScenario.run(workingDir);
        // TODO add some tests, e.g., running the scenario
        assertTrue(true);
        StaticHelper.cleanUpTest(workingDir);
    }
}
