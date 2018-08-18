/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.io.File;
import java.io.IOException;

import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.population.io.PopulationWriter;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.util.io.GZHandler;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public enum PopulationPreparer {
    ;
    /** @param network
     * @param population
     * @param scenOptions
     * @param config
     * @param seed for random number generator that influences the population filtering
     * @throws Exception */
    public static void run( //
            Network network, Population population, ScenarioOptions scenOptions, Config config, long seed) throws Exception {
        System.out.println("++++++++++++++++++++++++ POPULATION PREPARER ++++++++++++++++++++++++++++++++");
        System.out.println("Original population size: " + population.getPersons().values().size());

        PopulationCutter populationCutter = scenOptions.getPopulationCutter();
        populationCutter.cut(population, network, config);
        System.out.println("Population size after cutting: " + population.getPersons().values().size());

        TheApocalypse.reducesThe(population).toNoMoreThan(scenOptions.getMaxPopulationSize()).people();
        TheApocalypse.reducesThe(population).toNoMoreThan(scenOptions.getMaxPopulationSize(), seed);
        System.out.println("Population after decimation:" + population.getPersons().values().size());
        GlobalAssert.that(0 < population.getPersons().size());

        final File fileExportGz = new File(scenOptions.getPreparedPopulationName() + ".xml.gz");
        final File fileExport = new File(scenOptions.getPreparedPopulationName() + ".xml");

        {
            // write the modified population to file
            PopulationWriter pw = new PopulationWriter(population);
            pw.write(fileExportGz.toString());
        }

        // extract the created .gz file
        try {
            GZHandler.extract(fileExportGz, fileExport);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
    }

}
