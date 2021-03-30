/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.prep;

import java.io.File;

import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.api.core.v01.population.Population;

import amodeus.amodeus.test.ScenarioExecutionTest;
import amodeus.amodeus.test.TestFileHandling;
import amodeus.amodeus.util.io.Locate;
import amodeus.amodeus.util.io.MultiFileTools;
import amodeus.amodeus.util.math.GlobalAssert;
import junit.framework.TestCase;

public class TheRequestApocalypseTest extends TestCase {

    public void testSimple() throws Exception {
        File workingDirectory = MultiFileTools.getDefaultWorkingDirectory();

        /** download a scenario */
        File scenarioDirectory = //
                new File(Locate.repoFolder(ScenarioExecutionTest.class, "amodeus"), "resources/testScenario");
        GlobalAssert.that(workingDirectory.isDirectory());
        TestFileHandling.copyScnearioToMainDirectory(scenarioDirectory.getAbsolutePath(), //
                workingDirectory.getAbsolutePath());

        /** prepare a scenario */
        Preparer preparer = new Preparer(workingDirectory);
        Population population = preparer.population;

        PopulationCutter populationCutter = preparer.scenOpt.getPopulationCutter();
        populationCutter.cut(population, preparer.network, preparer.config);

        /** reduce the number of legs */
        int numReqDes = 3000;
        long seed = 1234;
        TheRequestApocalypse.reducesThe(population).toNoMoreThan(numReqDes, seed);

        /** ensure testing worked correctly */
        assertEquals(numReqDes, (int) LegCount.of(population, AmodeusModeConfig.DEFAULT_MODE));
    }

    @Override
    public void tearDown() throws Exception {
        TestFileHandling.removeGeneratedFiles();
    }
}
