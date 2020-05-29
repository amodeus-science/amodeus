package org.matsim.amodeus.analysis;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.matsim.amodeus.analysis.passengers.PassengerAnalysisListener;
import org.matsim.amodeus.analysis.passengers.PassengerAnalysisWriter;
import org.matsim.amodeus.analysis.vehicles.VehicleAnalysisListener;
import org.matsim.amodeus.analysis.vehicles.VehicleAnalysisWriter;
import org.matsim.amodeus.config.AmodeusConfigGroup;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.groups.ControlerConfigGroup;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.controler.listener.ShutdownListener;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AnalysisOutputListener implements IterationStartsListener, IterationEndsListener, ShutdownListener {
    private static final String PASSENGER_RIDES_FILE_NAME = "amodeus_passenger_rides.csv";
    private static final String VEHICLE_MOVEMENTS_FILE_NAME = "amodeus_vehicle_movements.csv";
    private static final String VEHICLE_ACTIVITIES_FILE_NAME = "amodeus_vehicle_activities.csv";

    private final OutputDirectoryHierarchy outputDirectory;
    private final int lastIteration;

    private final int passengerAnalysisInterval;
    private final PassengerAnalysisListener passengerAnalysisListener;

    private final int vehicleAnalysisInterval;
    private final VehicleAnalysisListener vehicleAnalysisListener;

    private boolean isPassengerAnalysisActive = false;
    private boolean isVehicleAnalysisActive = false;

    @Inject
    public AnalysisOutputListener(AmodeusConfigGroup config, ControlerConfigGroup controllerConfig, OutputDirectoryHierarchy outputDirectory, Network network) {
        this.outputDirectory = outputDirectory;
        this.lastIteration = controllerConfig.getLastIteration();

        this.passengerAnalysisInterval = config.getPassengerAnalysisInterval();
        this.vehicleAnalysisInterval = config.getVehicleAnalysisInterval();

        LinkFinder linkFinder = new LinkFinder(network);

        this.passengerAnalysisListener = new PassengerAnalysisListener(linkFinder);
        this.vehicleAnalysisListener = new VehicleAnalysisListener(linkFinder);
    }

    @Override
    public void notifyIterationStarts(IterationStartsEvent event) {
        if (passengerAnalysisInterval > 0 && event.getIteration() % passengerAnalysisInterval == 0) {
            isPassengerAnalysisActive = true;
            event.getServices().getEvents().addHandler(passengerAnalysisListener);
        }

        if (vehicleAnalysisInterval > 0 && event.getIteration() % vehicleAnalysisInterval == 0) {
            isVehicleAnalysisActive = true;
            event.getServices().getEvents().addHandler(vehicleAnalysisListener);
        }
    }

    @Override
    public void notifyIterationEnds(IterationEndsEvent event) {
        try {
            if (isPassengerAnalysisActive) {
                event.getServices().getEvents().removeHandler(passengerAnalysisListener);

                String path = outputDirectory.getIterationFilename(event.getIteration(), PASSENGER_RIDES_FILE_NAME);
                new PassengerAnalysisWriter(passengerAnalysisListener).writeRides(new File(path));
            }

            if (isVehicleAnalysisActive) {
                event.getServices().getEvents().removeHandler(vehicleAnalysisListener);

                String movementsPath = outputDirectory.getIterationFilename(event.getIteration(), VEHICLE_MOVEMENTS_FILE_NAME);
                new VehicleAnalysisWriter(vehicleAnalysisListener).writeMovements(new File(movementsPath));

                String activitiesPath = outputDirectory.getIterationFilename(event.getIteration(), VEHICLE_ACTIVITIES_FILE_NAME);
                new VehicleAnalysisWriter(vehicleAnalysisListener).writeActivities(new File(activitiesPath));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void notifyShutdown(ShutdownEvent event) {
        try {
            File iterationPath = new File(outputDirectory.getIterationFilename(lastIteration, PASSENGER_RIDES_FILE_NAME));
            File outputPath = new File(outputDirectory.getOutputFilename(PASSENGER_RIDES_FILE_NAME));
            Files.copy(iterationPath.toPath(), outputPath.toPath());
        } catch (IOException e) {
        }

        try {
            File iterationPath = new File(outputDirectory.getIterationFilename(lastIteration, VEHICLE_MOVEMENTS_FILE_NAME));
            File outputPath = new File(outputDirectory.getOutputFilename(VEHICLE_MOVEMENTS_FILE_NAME));
            Files.copy(iterationPath.toPath(), outputPath.toPath());
        } catch (IOException e) {
        }

        try {
            File iterationPath = new File(outputDirectory.getIterationFilename(lastIteration, VEHICLE_ACTIVITIES_FILE_NAME));
            File outputPath = new File(outputDirectory.getOutputFilename(VEHICLE_ACTIVITIES_FILE_NAME));
            Files.copy(iterationPath.toPath(), outputPath.toPath());
        } catch (IOException e) {
        }
    }
}
