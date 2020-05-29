/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import java.util.EnumSet;
import java.util.Objects;

import org.matsim.amodeus.dvrp.request.AVRequest;

import ch.ethz.idsc.amodeus.dispatcher.core.RequestStatus;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

enum RequestContainerCompiler {
    ;

    /** @param avRequest {@link AVRequest}
     * @param requestStatus {@link RequestStatus}
     * @return {@link RequestContainer} with information for storage and later viewing in
     *         {@link SimulationObject} */
    public static RequestContainer compile( //
            AVRequest avRequest, //
            RequestStatus requestStatus) {
        GlobalAssert.that(Objects.nonNull(avRequest));

        // In future versions this can be removed, because it will be checked in the AV package already
        GlobalAssert.that(Objects.nonNull(avRequest.getFromLink()));
        GlobalAssert.that(Objects.nonNull(avRequest.getToLink()));

        RequestContainer requestContainer = new RequestContainer();
        requestContainer.requestIndex = avRequest.getId().index();
        requestContainer.fromLinkIndex = avRequest.getFromLink().getId().index();
        requestContainer.submissionTime = avRequest.getSubmissionTime();
        requestContainer.toLinkIndex = avRequest.getToLink().getId().index();
        requestContainer.requestStatus = EnumSet.of(requestStatus);
        return requestContainer;
    }
}
