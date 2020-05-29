/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;

public enum DispatcherUtils {
    ;

    /** function call leaves the state of the {@link UniversalDispatcher} unchanged. successive
     * calls to the function return the identical collection.
     * 
     * @return list of {@link PassengerRequest}s grouped by link */
    public static Map<Link, List<PassengerRequest>> getPassengerRequestsAtLinks(Collection<PassengerRequest> avRequests) {
        return avRequests.stream() // <- intentionally not parallel to guarantee ordering of requests
                .collect(Collectors.groupingBy(PassengerRequest::getFromLink));
    }
}
