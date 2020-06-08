/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.matsim;

import java.util.Objects;

import org.apache.log4j.Logger;
import org.matsim.amodeus.config.AmodeusConfigGroup;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.amodeus.config.modal.GeneratorConfig;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.StartupListener;

public class CompatibilityModule extends AbstractModule {
    private final static Logger logger = Logger.getLogger(CompatibilityModule.class);

    @Override
    public void install() {
        addControlerListenerBinding().toInstance(new WarningListener());

        // Update vehicle caapacities by Amodeus configuration

        for (AmodeusModeConfig operatorConfig : AmodeusConfigGroup.get(getConfig()).getModes().values()) {
            GeneratorConfig generatorConfig = operatorConfig.getGeneratorConfig();
            String numberOfSeatsParameter = generatorConfig.getParams().get("numberOfSeats");

            if (Objects.nonNull(numberOfSeatsParameter)) {
                int numberOfSeats = Integer.parseInt(numberOfSeatsParameter);

                if (numberOfSeats != generatorConfig.getCapacity()) {
                    generatorConfig.setCapacity(numberOfSeats);
                    logger.warn(String.format("Overriding 'capacity' attribute of the generator for mode '%s' with the deprecated 'numberOfSeats' attribute.", //
                            operatorConfig.getMode()));
                } else {
                    logger.warn(
                            String.format("Your are using the deprecated 'numberOfSeats' attribute in the generator for mode '%s'. " //
                                            + "Please replace with the new 'capacity' attribute.", operatorConfig.getMode()));
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
                        "Since the last update of Amodeus it is necessary that each activity in a MATSim popuatlion" //
                                + " has a coordinate and not only a link ID to determine its location. You can either modify the way" //
                                + " you generate your population or use the AddCoordinatesToActivities tool to automatically " //
                                + "assign to each activity the coordinate of the currently associated link.");
        }
    }
}
