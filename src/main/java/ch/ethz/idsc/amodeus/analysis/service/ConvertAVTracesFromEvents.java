package ch.ethz.idsc.amodeus.analysis.service;

import java.io.File;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsReaderXMLv1;
import org.matsim.core.events.EventsUtils;

import ch.ethz.matsim.av.schedule.AVTransitEventMapper;

public enum ConvertAVTracesFromEvents {
    ;
    public static void write(Network network, String outputPath, String eventsPath) {
        AVTraceWriter writer = new AVTraceWriter(new File(outputPath));
        AVTraceListener listener = new AVTraceListener(network, writer);

        EventsManager eventsManager = EventsUtils.createEventsManager();
        eventsManager.addHandler(listener);

        EventsReaderXMLv1 reader = new EventsReaderXMLv1(eventsManager);
        reader.addCustomEventMapper("AVTransit", new AVTransitEventMapper());
        reader.readFile(eventsPath);

        listener.finish();
        writer.close();
    }
}
