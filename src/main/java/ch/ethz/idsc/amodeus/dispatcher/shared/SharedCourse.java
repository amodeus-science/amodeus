/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.Objects;

import org.matsim.api.core.v01.network.Link;

/** Middle level class in SharedRoboTaxi functionality, a {@link SharedMenu} is
 * composed of {@link SharedCourse}s which internally have a {@link SharedMealType}s */
public class SharedCourse {

    private final String requestId;
    private final Link link;
    private final SharedMealType sharedRoboTaxiMealType;

    // TODO after implementing tests, carefully check if requestID and link can be replaced with AVRequest
    public SharedCourse(String requestId, Link link, SharedMealType sharedAVMealType) {
        this.link = link;
        this.requestId = requestId;
        this.sharedRoboTaxiMealType = sharedAVMealType;
    }

    public SharedMealType getMealType() {
        return sharedRoboTaxiMealType;
    }

    public String getRequestId() {
        return requestId;
    }

    public Link getLink() {
        return link;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof SharedCourse) {
            SharedCourse sharedAVCourse = (SharedCourse) object;
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
