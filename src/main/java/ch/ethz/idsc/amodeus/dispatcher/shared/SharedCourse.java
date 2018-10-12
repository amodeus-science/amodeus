/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.Objects;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

/** Middle level class in SharedRoboTaxi functionality, a {@link SharedMenu} is
 * composed of {@link SharedCourse}s which internally have a {@link SharedMealType}s */
public class SharedCourse {

    
    /** fast access functions */
    public static SharedCourse pickupCourse(AVRequest avRequest) {
        return new SharedCourse(avRequest, avRequest.getFromLink(), SharedMealType.PICKUP);
    }

    public static SharedCourse dropoffCourse(AVRequest avRequest) {
        return new SharedCourse(avRequest, avRequest.getToLink(), SharedMealType.DROPOFF);
    }

    // FIXME Whats the meaning of this ID? maybe null might be a possibility as it is ment for the AV Request. 
    public static SharedCourse redirectCourse(Link link) {
        return new SharedCourse(STANDARD_REDIRECT_AVREQUEST, link, SharedMealType.REDIRECT);
    }

    /** class implementation */
//    private final String requestID;
    private final Link link;
    private final SharedMealType sharedRoboTaxiMealType;
    private final AVRequest avRequest;
    private static final AVRequest STANDARD_REDIRECT_AVREQUEST = null;

    // TODO Lukas after implementing tests, carefully check if requestID and link can be replaced with AVRequest ?
    /** @param for {@link SharedMealType} PICKUP and DROPOFF the requestID must be the
     *            id of the {@link AVRequest}, otherwise a self-chosen id to distinguish different
     *            {@link SharedMealType} tasks of type REDIRECT
     * @param link
     * @param sharedAVMealType */
    private SharedCourse(AVRequest avRequest, Link link, SharedMealType sharedAVMealType) {
        this.link = link;
        this.avRequest = avRequest;
        this.sharedRoboTaxiMealType = sharedAVMealType;
    }

    public SharedMealType getMealType() {
        return sharedRoboTaxiMealType;
    }

    public String getRequestId() {
        GlobalAssert.that(!avRequest.equals(STANDARD_REDIRECT_AVREQUEST));
        return avRequest.getId().toString();
    }

    public Link getLink() {
        return link;
    }

    public AVRequest getAvRequest() {
        return avRequest;
    }
    
    @Override
    public boolean equals(Object object) {
        if (object instanceof SharedCourse) {
            SharedCourse sharedAVCourse = (SharedCourse) object;
            // TODO IT might as well Work with comparing the avRequest itself... 
            return sharedAVCourse.getRequestId().equals(getRequestId()) && //
                    sharedAVCourse.getLink().equals(link) && //
                    sharedAVCourse.getMealType().equals(sharedRoboTaxiMealType);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRequestId(), sharedRoboTaxiMealType);
    }
}
