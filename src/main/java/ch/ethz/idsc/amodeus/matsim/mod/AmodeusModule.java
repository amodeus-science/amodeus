/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.mod;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.trafficmonitoring.FreeSpeedTravelTime;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.Vehicles;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import ch.ethz.matsim.av.config.AVConfigGroup;
import ch.ethz.matsim.av.config.operator.GeneratorConfig;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.framework.AVModule;

public class AmodeusModule extends AbstractModule {
    private final Logger logger = Logger.getLogger(AmodeusModule.class);

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

        installQSimModule(new AmodeusQSimModule());

        addControlerListenerBinding().toInstance(new WarningListener());
    }

    @Provides
    @Singleton
    public Map<Id<AVOperator>, VehicleType> provideVehicleTypes(AVConfigGroup config, Vehicles vehicles) {
        /* This provides overwrites the standard way how the av package determines vehicle types. By default
         * it uses the 'vehicleType' attribute of each operator to look up a certain predefined vehicle type
         * from MATSim / from the MATSim vehicles input file. If the vehicle type cannot be found, the standard
         * MATSim vehicle type is returned. Here, we now add another layer on top for Amodeus: If the 'numberOfSeats'
         * attribute is set for a generator, we create a new 'amodeusVehicleType:N' on the fly with N seats. */

        Map<Id<AVOperator>, VehicleType> vehicleTypes = new AVModule().provideVehicleTypes(config, vehicles);
        Map<Integer, VehicleType> amodeusTypes = new HashMap<>();

        for (OperatorConfig operatorConfig : config.getOperatorConfigs().values()) {
            GeneratorConfig generatorConfig = operatorConfig.getGeneratorConfig();

            String vehicleTypeName = generatorConfig.getVehicleType();
            String numberOfSeatsParameter = generatorConfig.getParams().get("numberOfSeats");

            if (Objects.nonNull(numberOfSeatsParameter)) {
                if (Objects.nonNull(vehicleTypeName))
                    throw new IllegalStateException(String.format( //
                            "Both vehicleType and numberOfSeats are set for operator %s. This is amiguous. "
                                    + "Option one is to define 'numberOfSeats' alone. In this case Amodeus will create an internal "
                                    + "vehicle type with the specified number of seats automatically. Option two is to explicitly "
                                    + "define a 'vehicleType' alone and register it through the possible ways that MATSim prvoides "
                                    + "(either manually adding a VehicleType to the Vehicles container after loeading the scenario, or providing a vehicles.xml.gz file).",
                            operatorConfig.getId()));
                int numberOfSeats = Integer.parseInt(numberOfSeatsParameter);
                if (!amodeusTypes.containsKey(numberOfSeats)) {
                    logger.info(String.format("Creating an on-the-fly vehicle type for Amodeus with %d seats", numberOfSeats));
                    VehicleType vehicleType = vehicles.getFactory().createVehicleType(Id.create(String.format("amodeus:%d", numberOfSeats), VehicleType.class));
                    amodeusTypes.put(numberOfSeats, vehicleType);
                }
                vehicleTypes.put(operatorConfig.getId(), amodeusTypes.get(numberOfSeats));
            }
        }
        return vehicleTypes;
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
