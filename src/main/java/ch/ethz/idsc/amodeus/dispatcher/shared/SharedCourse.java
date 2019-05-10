/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.Objects;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.matsim.av.passenger.AVRequest;

/** Middle level class in SharedRoboTaxi functionality, a {@link SharedMenu} is
 * composed of {@link SharedCourse}s which internally have a {@link SharedMealType}s
 * A {@link SharedCourse} has the following attributes:
 * - 1 {@link AVRequest} (unless it is a redirect course)
 * - 1 {@link Link}
 * - 1 {@link String} unique ID
 * - 1 {@link SharedMealType} either PICKUP, DROPOFF or REDIRECT}
 * 
 * There are three different possible {@link SharedCourse}s which can be instantiated
 * with the static functions below. */
public class SharedCourse {
    protected static final AVRequest STANDARD_REDIRECT_AVREQUEST = null;

    /** @return a pickup course, which is a {@link SharedCourse} during which the
     *         {@link AVRequest} @param avRequest should be picked up at its origin. */
    public static SharedCourse pickupCourse(AVRequest avRequest) {
        Objects.requireNonNull(avRequest);
        return new SharedCourse(avRequest, avRequest.getFromLink(), avRequest.getId().toString(), SharedMealType.PICKUP);
    }

    /** @return a dropoff course, which is a {@link SharedCourse} during which the
     *         {@link AVRequest} @param avRequest should be dropped off at its destination. */
    public static SharedCourse dropoffCourse(AVRequest avRequest) {
        Objects.requireNonNull(avRequest);
        return new SharedCourse(avRequest, avRequest.getToLink(), avRequest.getId().toString(), SharedMealType.DROPOFF);
    }

    /** @return a redirect course which is a shared course to a location without a request pickup or
     *         dropoff, e.g., if one wants to have the {@link RoboTaxi} visit an area where a pickup
     *         is expected but not confirmed. The {@link SharedCourse} requires a {@link Link} @param link
     *         and a @param courseId which is a unique identifier, e.g., use roboTaxi.getId() + link.getId() + time **/
    public static SharedCourse redirectCourse(Link link, String courseId) {
        return new SharedCourse(STANDARD_REDIRECT_AVREQUEST, link, courseId, SharedMealType.REDIRECT);
    }

    // ---
    /** class implementation */
    private final AVRequest avRequest;
    private final Link link;
    private final String courseID;
    private final SharedMealType sharedRoboTaxiMealType;

    /** @param for {@link SharedMealType} PICKUP and DROPOFF the requestID must be the
     *            id of the {@link AVRequest}, otherwise a self-chosen id to distinguish different
     *            {@link SharedMealType} tasks of type REDIRECT
     * @param link
     * @param sharedAVMealType */
    protected SharedCourse(AVRequest avRequest, Link link, String courseId, SharedMealType sharedAVMealType) {
        this.avRequest = avRequest;
        this.link = Objects.requireNonNull(link);
        this.courseID = Objects.requireNonNull(courseId);
        this.sharedRoboTaxiMealType = Objects.requireNonNull(sharedAVMealType);
    }

    public SharedMealType getMealType() {
        return sharedRoboTaxiMealType;
    }

    // TODO make final and allow direct access ?
    public String getCourseId() {
        return courseID;
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
            /** avRequest not used in the comparison because
             * the avRequest can be null as well (In the Redirect case). */
            return sharedAVCourse.getCourseId().equals(courseID) && //
                    sharedAVCourse.getLink().equals(link) && //
                    sharedAVCourse.getMealType().equals(sharedRoboTaxiMealType);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseID, sharedRoboTaxiMealType, link);
    }
}
