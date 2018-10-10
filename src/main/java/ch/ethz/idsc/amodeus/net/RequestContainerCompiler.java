/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import java.util.EnumSet;
import java.util.Objects;

import ch.ethz.idsc.amodeus.dispatcher.core.RequestStatus;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

enum RequestContainerCompiler {
    ;
    /** @param avRequest
     * @param db
     * @param requestStatus
     * @return {@link RequestContainer} with information for storage and later viewing in
     *         {@link SimulationObject} */
    public static RequestContainer compile( //
            AVRequest avRequest, //
            MatsimAmodeusDatabase db, //
            RequestStatus requestStatus) {
        GlobalAssert.that(Objects.nonNull(avRequest));

        // In future versions this can be removed, because it will be checked in the AV package already
        GlobalAssert.that(Objects.nonNull(avRequest.getFromLink()));
        GlobalAssert.that(Objects.nonNull(avRequest.getToLink()));

        RequestContainer requestContainer = new RequestContainer();
        requestContainer.requestIndex = db.getRequestIndex(avRequest);
        requestContainer.fromLinkIndex = db.getLinkIndex(avRequest.getFromLink());
        requestContainer.submissionTime = avRequest.getSubmissionTime();
        requestContainer.toLinkIndex = db.getLinkIndex(avRequest.getToLink());
        requestContainer.requestStatus = EnumSet.of(requestStatus);
        return requestContainer;
    }
}
