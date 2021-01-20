package amodeus.amodeus.dispatcher.alonso_mora_2016.sequence;

import java.util.Iterator;
import java.util.List;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;

import amodeus.amodeus.dispatcher.core.schedule.directives.StopDirective;

public interface SequenceGeneratorFactory {
    Iterator<List<StopDirective>> create(Link startLink, List<PassengerRequest> existingRequests, List<PassengerRequest> addedRequests);
}
