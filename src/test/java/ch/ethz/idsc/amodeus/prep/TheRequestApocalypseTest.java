/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.io.File;

import ch.ethz.idsc.amodeus.test.ScenarioExecutionTest;
import ch.ethz.idsc.amodeus.test.TestFileHandling;
import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import junit.framework.TestCase;
import org.matsim.api.core.v01.population.Population;

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
        assertEquals(numReqDes, (int) LegCount.of(population, "av"));
    }

    @Override
    public void tearDown() throws Exception {
        TestFileHandling.removeGeneratedFiles();
    }
}
