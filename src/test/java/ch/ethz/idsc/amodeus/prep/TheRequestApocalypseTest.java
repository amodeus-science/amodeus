/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import ch.ethz.idsc.amodeus.util.io.FileDelete;
import junit.framework.TestCase;

public class TheRequestApocalypseTest extends TestCase {

    public void testSimple() throws Exception {
        // TODO Joel reinstate test
        // /** download a scenario */
        // StaticHelper.setupScenario();
        //
        // /** prepare a scenario */
        // Preparer preparer = new Preparer();
        // Population population = preparer.population;
        //
        // PopulationCutter populationCutter = preparer.scenOpt.getPopulationCutter();
        // populationCutter.cut(population, preparer.network, preparer.config);
        //
        // /** reduce the number of legs */
        // int numReqDes = 5000;
        // long seed = 1234;
        // TheRequestApocalypse.reducesThe(population).toNoMoreThan(RationalScalar.of(numReqDes, 1), seed);
        //
        // /** ensure testing worked correctly */
        // assertEquals(numReqDes, LegCount.of(population, "av").number().intValue());

        /** clean scenario */
        // TODO Joel clean different way
        // CleanAidoScenarios.now();

    }

    @Override
    public void tearDown() throws Exception {
        List<File> list = Arrays.asList(//
                new File("preparedNetwork.xml.gz"), //
                new File("network.xml.gz"), //
                new File("population.xml.gz"));
        for (File file : list)
            if (file.exists())
                FileDelete.of(file, 1, 1);
    }
}
