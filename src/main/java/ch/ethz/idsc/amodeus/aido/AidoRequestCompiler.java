/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import java.util.Collection;

import ch.ethz.idsc.amodeus.net.MatsimStaticDatabase;
import ch.ethz.idsc.amodeus.net.TensorCoords;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ enum AidoRequestCompiler {
    ;

    public static Tensor compile(Collection<AVRequest> requests) {
        return Tensor.of(requests.stream().map(AidoRequestCompiler::of));
    }

    private static Tensor of(AVRequest request) {
        // id
        Tensor info = Tensors.vector(MatsimStaticDatabase.INSTANCE.getRequestIndex(request));
        // submission time
        info.append(RealScalar.of(request.getSubmissionTime()));
        // from location
        info.append(TensorCoords.toTensor(MatsimStaticDatabase.INSTANCE.referenceFrame.coords_toWGS84().transform(//
                request.getFromLink().getCoord())));
        // to location
        info.append(TensorCoords.toTensor(MatsimStaticDatabase.INSTANCE.referenceFrame.coords_toWGS84().transform(//
                request.getToLink().getCoord())));
        return info;
    }
}
