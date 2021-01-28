/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.analysis.service;

import java.io.File;
import java.io.IOException;

import org.matsim.amodeus.analysis.LinkFinder;
import org.matsim.amodeus.analysis.vehicles.VehicleAnalysisListener;
import org.matsim.amodeus.analysis.vehicles.VehicleAnalysisWriter;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;

/* package */ enum ConvertAVTracesFromEvents {
    ;

    public static void write(Network network, String movementsOutputPath, String activitiesOutputPath, String eventsPath) throws IOException {
        LinkFinder linkFinder = new LinkFinder(network);
        VehicleAnalysisListener listener = new VehicleAnalysisListener(linkFinder);

        EventsManager eventsManager = EventsUtils.createEventsManager();
        eventsManager.addHandler(listener);
        
        eventsManager.initProcessing();
        new MatsimEventsReader(eventsManager).readFile(eventsPath);
        eventsManager.finishProcessing();
        
        new VehicleAnalysisWriter(listener).writeMovements(new File(movementsOutputPath));
        new VehicleAnalysisWriter(listener).writeActivities(new File(activitiesOutputPath));
    }
}
