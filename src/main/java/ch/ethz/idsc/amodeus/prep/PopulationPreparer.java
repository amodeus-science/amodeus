/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.Route;
import org.matsim.core.config.Config;
import org.matsim.core.population.io.PopulationWriter;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.util.io.GZHandler;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public enum PopulationPreparer {
	;
	public static void run(Network network, Population population, ScenarioOptions scenOptions, Config config)
			throws Exception {
		System.out.println("++++++++++++++++++++++++ POPULATION PREPARER ++++++++++++++++++++++++++++++++");
		System.out.println("Original population size: " + population.getPersons().values().size());

		PopulationCutter populationCutter = scenOptions.getPopulationCutter();
		populationCutter.cut(population, network, config);
		System.out.println("Population size after cutting: " + population.getPersons().values().size());

		TheApocalypse.reducesThe(population).toNoMoreThan(scenOptions.getMaxPopulationSize()).people();
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

	public static void checkRouteType(Population population) {
		Iterator<? extends Person> itPerson = population.getPersons().values().iterator();
		Person person = null;
		while (itPerson.hasNext()) {
			person = itPerson.next();
		}
		for (Plan plan : person.getPlans()) {
			for (PlanElement pE1 : plan.getPlanElements()) {
				if (pE1 instanceof Leg) {
					Leg leg = (Leg) pE1;
					Route route = leg.getRoute();
					System.out.println("RouteType of last person: " + route.getRouteType());
				}
			}
		}
	}
}
