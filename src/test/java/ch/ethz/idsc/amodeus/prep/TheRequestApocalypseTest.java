/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.io.File;

import org.matsim.api.core.v01.population.Population;

import ch.ethz.idsc.amodeus.aido.CleanAidoScenarios;
import ch.ethz.idsc.amodeus.util.io.FileDelete;
import ch.ethz.idsc.tensor.RationalScalar;
import junit.framework.TestCase;

public class TheRequestApocalypseTest extends TestCase {

    public void testSimple() throws Exception {
        /** download a scenario */
        StaticHelper.setupScenario();

        /** prepare a scenario */
        Preparer preparer = new Preparer();
        Population population = preparer.population;

        PopulationCutter populationCutter = preparer.scenOpt.getPopulationCutter();
        populationCutter.cut(population, preparer.network, preparer.config);

        /** reduce the number of legs */
        int numReqDes = 5000;
        long seed = 1234;
        TheRequestApocalypse.reducesThe(population).toNoMoreThan(RationalScalar.of(numReqDes, 1), seed);

        /** ensure testing worked correctly */
        assertEquals(numReqDes, LegCount.of(population, "av").number().intValue());

        /** clean scenario */
        CleanAidoScenarios.now();

    }

    @Override
    public void tearDown() throws Exception {
        FileDelete.of(new File("preparedNetwork.xml.gz"), 1, 1);
        FileDelete.of(new File("network.xml.gz"), 1, 1);
        FileDelete.of(new File("population.xml.gz"), 1, 1);
    }
}
