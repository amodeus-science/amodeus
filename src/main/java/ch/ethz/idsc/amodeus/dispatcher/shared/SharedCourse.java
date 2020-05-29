/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.Objects;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;

/** Middle level class in SharedRoboTaxi functionality, a {@link SharedMenu} is
 * composed of {@link SharedCourse}s which internally have a {@link SharedMealType}s
 * A {@link SharedCourse} has the following attributes:
 * - 1 {@link PassengerRequest} (unless it is a redirect course)
 * - 1 {@link Link}
 * - 1 {@link String} unique ID
 * - 1 {@link SharedMealType} either PICKUP, DROPOFF or REDIRECT}
 * 
 * There are three different possible {@link SharedCourse}s which can be instantiated
 * with the static functions below. */
public class SharedCourse {
    protected static final PassengerRequest STANDARD_REDIRECT_AVREQUEST = null;

    /** @return a pickup course, which is a {@link SharedCourse} during which the
     *         {@link PassengerRequest} @param avRequest should be picked up at its origin. */
    public static SharedCourse pickupCourse(PassengerRequest avRequest) {
        Objects.requireNonNull(avRequest);
        return new SharedCourse(avRequest, avRequest.getFromLink(), avRequest.getId().toString(), SharedMealType.PICKUP);
    }

    /** @return a dropoff course, which is a {@link SharedCourse} during which the
     *         {@link PassengerRequest} @param avRequest should be dropped off at its destination. */
    public static SharedCourse dropoffCourse(PassengerRequest avRequest) {
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
    private final PassengerRequest avRequest;
    private final Link link;
    private final String courseId;
    private final SharedMealType sharedRoboTaxiMealType;

    /** @param for {@link SharedMealType} PICKUP and DROPOFF the requestID must be the
     *            id of the {@link PassengerRequest}, otherwise a self-chosen id to distinguish different
     *            {@link SharedMealType} tasks of type REDIRECT
     * @param link
     * @param sharedAVMealType */
    protected SharedCourse(PassengerRequest avRequest, Link link, String courseId, SharedMealType sharedAVMealType) {
        this.avRequest = avRequest;
        this.link = Objects.requireNonNull(link);
        this.courseId = Objects.requireNonNull(courseId);
        this.sharedRoboTaxiMealType = Objects.requireNonNull(sharedAVMealType);
    }

    public SharedMealType getMealType() {
        return sharedRoboTaxiMealType;
    }

    public final String getCourseId() {
        return courseId;
    }

    public final Link getLink() {
        return link;
    }

    public final PassengerRequest getAvRequest() {
        return avRequest;
    }

    @Override
    public String toString() {
        return sharedRoboTaxiMealType.toString() + " of " + avRequest.getId().toString() + //
                " on " + link.getId().toString() + ", id:" + courseId;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof SharedCourse) {
            SharedCourse sharedAVCourse = (SharedCourse) object;
            /** avRequest not used in the comparison because
             * the avRequest can be null as well (In the Redirect case). */
            return sharedAVCourse.getCourseId().equals(courseId) && //
                    sharedAVCourse.getLink().equals(link) && //
                    sharedAVCourse.getMealType().equals(sharedRoboTaxiMealType);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId, sharedRoboTaxiMealType, link);
    }
}
