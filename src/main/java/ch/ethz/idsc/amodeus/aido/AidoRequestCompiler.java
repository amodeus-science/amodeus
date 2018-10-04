/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import java.util.Collection;

import ch.ethz.idsc.amodeus.net.MatsimStaticDatabase;
import ch.ethz.idsc.amodeus.net.TensorCoords;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ class AidoRequestCompiler {
    private final MatsimStaticDatabase db;

    public AidoRequestCompiler(MatsimStaticDatabase db) {
        this.db = db;
    }

    public Tensor compile(Collection<AVRequest> requests) {
        return Tensor.of(requests.stream().map(r -> this.of(r)));
    }

    private Tensor of(AVRequest request) {
        // id
        Tensor info = Tensors.vector(db.getRequestIndex(request));
        // submission time
        info.append(RealScalar.of(request.getSubmissionTime()));
        // from location
        info.append(TensorCoords.toTensor(db.referenceFrame.coords_toWGS84().transform(//
                request.getFromLink().getCoord())));
        // to location
        info.append(TensorCoords.toTensor(db.referenceFrame.coords_toWGS84().transform(//
                request.getToLink().getCoord())));
        return info;
    }
}
