/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.mod;

import java.util.Objects;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.trafficmonitoring.FreeSpeedTravelTime;

import ch.ethz.matsim.av.config.AmodeusConfigGroup;
import ch.ethz.matsim.av.config.AmodeusModeConfig;
import ch.ethz.matsim.av.config.modal.GeneratorConfig;

public class AmodeusModule extends AbstractModule {
    private final static Logger logger = Logger.getLogger(AmodeusModule.class);

    @Override
    public void install() {
        /* This has been added after upgrading Amodeus' dependency to MATSim:0.10.1. In the mean time, DVRP got a new "initial travel time" which resembles
         * better what
         * QSim produces (since the time-step (seconds) based QSim produces travel times that are "rounded" to the next integer). This is now considered in DVRP
         * 0.10.1,
         * but it was not when Amodeus' unit tests had been written. Hence, to leave the unit tests intact, we add here the old FreeSpeedTravelTime. For
         * reference, the
         * MATSim commit, that introduced the new TravelTime implementation in DVRP is be380cfc72e1c29d840fcc6b71a4bebeb3e567c1 . */
        addTravelTimeBinding(DvrpTravelTimeModule.DVRP_INITIAL).toInstance(new FreeSpeedTravelTime());

        addControlerListenerBinding().toInstance(new WarningListener());

        // Update vehicle caapacities by Amodeus configuration

        for (AmodeusModeConfig operatorConfig : AmodeusConfigGroup.get(getConfig()).getModes().values()) {
            GeneratorConfig generatorConfig = operatorConfig.getGeneratorConfig();
            String numberOfSeatsParameter = generatorConfig.getParams().get("numberOfSeats");

            if (Objects.nonNull(numberOfSeatsParameter)) {
                int numberOfSeats = Integer.parseInt(numberOfSeatsParameter);

                if (numberOfSeats != generatorConfig.getCapacity()) {
                    generatorConfig.setCapacity(numberOfSeats);
                    logger.warn(String.format("Overriding 'capacity' attribute of the generator for mode '%s' with the deprecated 'numberOfSeats' attribute.",
                            operatorConfig.getMode()));
                }
            }
        }
    }

    private class WarningListener implements StartupListener {
        @Override
        public void notifyStartup(StartupEvent event) {
            boolean anyWarnings = false;
            for (Person person : event.getServices().getScenario().getPopulation().getPersons().values()) {
                boolean skip = false;
                for (Plan plan : person.getPlans()) {
                    if (skip)
                        break;
                    for (PlanElement element : plan.getPlanElements()) {
                        if (skip)
                            break;
                        if (element instanceof Activity) {
                            Activity activity = (Activity) element;
                            if (activity.getCoord() == null) {
                                logger.error(String.format("Agent '%s' has activity without coordiantes", person.getId()));
                                anyWarnings = true;
                                skip = true;
                            }
                        }
                    }
                }
            }
            if (anyWarnings)
                throw new RuntimeException(//
                        "Since the last update of Amodeus it is necessary that each activity in a MATSim popuatlion"
                                + " has a coordinate and not only a link ID to determine its location. You can either modify the way"
                                + " you generate your population or use the AddCoordinatesToActivities tool to automatically "
                                + "assign to each activity the coordinate of the currently associated link.");
        }
    }
}
