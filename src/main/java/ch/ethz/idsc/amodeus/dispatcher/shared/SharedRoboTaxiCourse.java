/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.Objects;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.Request;

/** Middle level class in SharedRoboTaxi functionality, a {@link SharedRoboTaxiMenu} is
 * composed of {@link SharedRoboTaxiCourse}s which internally have a {@link SharedRoboTaxiMealType}s */
public class SharedRoboTaxiCourse {

    private final Id<Request> requestId;
    private final Link link;
    private final SharedRoboTaxiMealType sharedRoboTaxiMealType;

    //TODO after implementing tests, carefully check if requestID and link can be replaced with AVRequest
    public SharedRoboTaxiCourse(Id<Request> requestId, Link link, SharedRoboTaxiMealType sharedAVMealType) {
        this.link = link;
        this.requestId = requestId;
        this.sharedRoboTaxiMealType = sharedAVMealType;
    }

    public SharedRoboTaxiMealType getMealType() {
        return sharedRoboTaxiMealType;
    }

    public Id<Request> getRequestId() {
        return requestId;
    }

    public Link getLink() {
        return link;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof SharedRoboTaxiCourse) {
            SharedRoboTaxiCourse sharedAVCourse = (SharedRoboTaxiCourse) object;
            return sharedAVCourse.getRequestId().equals(requestId) && //
                    sharedAVCourse.getLink().equals(link) && //
                    sharedAVCourse.getMealType().equals(sharedRoboTaxiMealType);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId.toString(), sharedRoboTaxiMealType);
    }
}
