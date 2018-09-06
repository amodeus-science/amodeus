/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.Objects;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.matsim.av.passenger.AVRequest;

/** Middle level class in SharedRoboTaxi functionality, a {@link SharedMenu} is
 * composed of {@link SharedCourse}s which internally have a {@link SharedMealType}s */
public class SharedCourse {

    /** fast access functions */
    public static SharedCourse pickupCourse(AVRequest avRequest) {
        return new SharedCourse(avRequest.getId().toString(), avRequest.getFromLink(), SharedMealType.PICKUP);
    }

    public static SharedCourse dropoffCourse(AVRequest avRequest) {
        return new SharedCourse(avRequest.getId().toString(), avRequest.getToLink(), SharedMealType.DROPOFF);
    }

    public static SharedCourse redirectCourse(Link link, String id) {
        return new SharedCourse(id, link, SharedMealType.REDIRECT);
    }
    
    public static SharedCourse waitingCourse(Link link, String id) {
        return new SharedCourse(id, link, SharedMealType.WAITFORCUSTOMER);
    }

    /** class implementation */
    private final String requestID;
    private final Link link;
    private final SharedMealType sharedRoboTaxiMealType;

    // TODO after implementing tests, carefully check if requestID and link can be replaced with AVRequest ?
    /** @param for {@link SharedMealType} PICKUP and DROPOFF the requestID must be the
     *            id of the {@link AVRequest}, otherwise a self-chosen id to distinguish different
     *            {@link SharedMealType} tasks of type REDIRECT
     * @param link
     * @param sharedAVMealType */
    private SharedCourse(String requestID, Link link, SharedMealType sharedAVMealType) {
        this.link = link;
        this.requestID = requestID;
        this.sharedRoboTaxiMealType = sharedAVMealType;
    }

    public SharedMealType getMealType() {
        return sharedRoboTaxiMealType;
    }

    public String getRequestId() {
        return requestID;
    }

    public Link getLink() {
        return link;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof SharedCourse) {
            SharedCourse sharedAVCourse = (SharedCourse) object;
            return sharedAVCourse.getRequestId().equals(requestID) && //
                    sharedAVCourse.getLink().equals(link) && //
                    sharedAVCourse.getMealType().equals(sharedRoboTaxiMealType);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestID.toString(), sharedRoboTaxiMealType);
    }
}
