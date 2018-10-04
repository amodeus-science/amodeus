/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import java.util.List;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.net.TensorCoords;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.io.StringScalar;
import ch.ethz.idsc.tensor.qty.Boole;

/* package */ class AidoRoboTaxiCompiler {
    private final MatsimAmodeusDatabase db;

    public AidoRoboTaxiCompiler(MatsimAmodeusDatabase db) {
        this.db = db;
    }

    public Tensor compile(List<RoboTaxi> roboTaxis) {
        return Tensor.of(roboTaxis.stream().map(rt -> this.ofTaxi(rt)));
    }

    private Tensor ofTaxi(RoboTaxi roboTaxi) {
        // id
        Tensor info = Tensors.vector(db.getVehicleIndex(roboTaxi));
        // divertable location
        info.append(TensorCoords.toTensor(db.referenceFrame.coords_toWGS84().transform( //
                roboTaxi.getDivertableLocation().getCoord())));
        // status
        info.append(StringScalar.of(roboTaxi.getStatus().name()));
        // divertable?
        info.append(Boole.of(roboTaxi.isDivertable()));
        return info;
    }
}
