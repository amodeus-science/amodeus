/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.service;

import java.io.File;
import java.io.IOException;

import org.matsim.amodeus.analysis.LinkFinder;
import org.matsim.amodeus.analysis.passengers.PassengerAnalysisListener;
import org.matsim.amodeus.analysis.passengers.PassengerAnalysisWriter;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;

public enum ConvertAVServicesFromEvents {
    ;

    public static void write(Network network, String outputPath, String eventsPath) throws IOException {
        System.out.println("Read File from " + eventsPath);

        LinkFinder linkFinder = new LinkFinder(network);
        PassengerAnalysisListener listener = new PassengerAnalysisListener(linkFinder);

        EventsManager eventsManager = EventsUtils.createEventsManager();
        eventsManager.addHandler(listener);
        new MatsimEventsReader(eventsManager).readFile(eventsPath);

        new PassengerAnalysisWriter(listener).writeRides(new File(outputPath));

        System.out.println("Exported " + outputPath);
    }
}
