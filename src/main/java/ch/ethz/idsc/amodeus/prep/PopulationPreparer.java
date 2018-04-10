/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import static org.matsim.pt.PtConstants.TRANSIT_ACTIVITY_TYPE;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
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
    public static void run(Network network, Population population, ScenarioOptions scenOptions, Config config) throws Exception {
        System.out.println("++++++++++++++++++++++++ POPULATION PREPARER ++++++++++++++++++++++++++++++++");
        System.out.println("Original population size: " + population.getPersons().values().size());

        PopulationCutters populationCutters = scenOptions.getPopulationCutter();
        populationCutters.cut(population, network, scenOptions, config);
        System.out.println("Population size after cutting: " + population.getPersons().values().size());

        TheApocalypse.reducesThe(population).toNoMoreThan(scenOptions.getMaxPopulationSize()).people();
        System.out.println("Population after decimation:" + population.getPersons().values().size());
        GlobalAssert.that(0 < population.getPersons().size());
        changePtModesToAv(population);

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

    public static void changePtModesToAv(Population population) {
        int counter = 0;
        for (Person person : population.getPersons().values()) {
            for (Plan plan : person.getPlans()) {
                reducetransitToOneLeg(plan);
                for (PlanElement planElement : plan.getPlanElements()) {
                    if (planElement instanceof Leg) {
                        Leg leg = (Leg) planElement;
                        if (leg.getMode().equals("pt")) {
                            leg.setMode("av");
                            leg.setRoute(null);
                            counter++;
                        }
                    }
                }
                consistencyOfDepatrureTimes(plan);
            }
        }
        System.out.print(counter + " Legs were changed to AV Mode\n");
    }

    private static void reducetransitToOneLeg(Plan plan) {
        Iterator<PlanElement> it = plan.getPlanElements().iterator();

        boolean isOnPtTrip = false;
        long numberOfPtTripElements = 0;
        Leg ptTripChainStart = null;

        while (it.hasNext()) {
            PlanElement pE = it.next();

            if (pE instanceof Leg) {
                Leg leg = (Leg) pE;

                if (!isOnPtTrip) {
                    if (leg.getMode().equals("pt") || leg.getMode().equals("transit_walk")) {
                        isOnPtTrip = true;
                        leg.setRoute(null);
                        numberOfPtTripElements = 1;
                        ptTripChainStart = leg;
                    }
                } else {
                    it.remove();
                    numberOfPtTripElements++;
                }
            }

            if (pE instanceof Activity && isOnPtTrip) {
                Activity act = (Activity) pE;

                if (!act.getType().equals(TRANSIT_ACTIVITY_TYPE)) {
                    isOnPtTrip = false;

                    if (numberOfPtTripElements == 1 && ptTripChainStart.getMode().equals("transit_walk")) {
                        ptTripChainStart.setMode("walk");
                    } else {
                        ptTripChainStart.setMode("pt");
                    }
                } else {
                    it.remove();
                }
            }
        }
    }

    private static void consistencyOfDepatrureTimes(Plan plan) {
        // double endTimeActivity = 0;
        Leg prevLeg = null;
        Activity prevActivity = null;
        for (PlanElement pE1 : plan.getPlanElements()) {
            if (pE1 instanceof Activity) {
                Activity act = (Activity) pE1;
                if (prevLeg != null && prevActivity != null) {
                    prevLeg.setTravelTime(act.getStartTime() - prevActivity.getEndTime());
                }
                prevActivity = act;
            }
            if (pE1 instanceof Leg) {
                Leg leg = (Leg) pE1;
                leg.setDepartureTime(prevActivity.getEndTime());
                prevLeg = leg;
            }
        }

    }
}
