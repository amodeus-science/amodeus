/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import java.util.Arrays;
import java.util.HashSet;
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
    public static RequestContainer compile(AVRequest avRequest, MatsimStaticDatabase db, //
            RequestStatus requestStatus) {
        GlobalAssert.that(Objects.nonNull(avRequest));

        // In future versions this can be removed, because it will be checked in the AV package already
        GlobalAssert.that(Objects.nonNull(avRequest.getFromLink()));
        GlobalAssert.that(Objects.nonNull(avRequest.getToLink()));

        RequestContainer requestContainer = new RequestContainer();
        requestContainer.requestIndex = db.getRequestIndex(avRequest);
        requestContainer.fromLinkIndex = db.getLinkIndex_id(avRequest.getFromLink().getId().toString()); // TODO changed due to MatsimStaticDatabase problem
        requestContainer.submissionTime = avRequest.getSubmissionTime();
        requestContainer.toLinkIndex = db.getLinkIndex_id(avRequest.getToLink().getId().toString()); // TODO changed due to MatsimStaticDatabase problem
        requestContainer.requestStatus = new HashSet<>(Arrays.asList(requestStatus));
        return requestContainer;
    }
}
