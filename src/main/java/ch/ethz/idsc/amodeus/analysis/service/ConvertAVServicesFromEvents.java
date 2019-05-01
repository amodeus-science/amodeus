package ch.ethz.idsc.amodeus.analysis.service;

import java.io.File;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsReaderXMLv1;
import org.matsim.core.events.EventsUtils;

import ch.ethz.matsim.av.schedule.AVTransitEventMapper;

public enum ConvertAVServicesFromEvents {
    ;
    public static void write(Network network, String outputPath, String eventsPath) {

        AVServiceWriter writer = new AVServiceWriter(new File(outputPath));
        AVServiceListener listener = new AVServiceListener(network, writer);

        EventsManager eventsManager = EventsUtils.createEventsManager();
        eventsManager.addHandler(listener);

        EventsReaderXMLv1 reader = new EventsReaderXMLv1(eventsManager);
        reader.addCustomEventMapper("AVTransit", new AVTransitEventMapper());
        System.out.println("Read File from " + eventsPath);
        reader.readFile(eventsPath);

        writer.close();
        System.out.println("Exported " + outputPath);

    }
}
