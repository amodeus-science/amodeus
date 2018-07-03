/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import java.util.List;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.net.MatsimStaticDatabase;
import ch.ethz.idsc.amodeus.net.TensorCoords;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.io.StringScalar;

/* package */ enum AidoRoboTaxiCompiler {
    ;

    public static Tensor compile(List<RoboTaxi> roboTaxis) {
        return Tensor.of(roboTaxis.stream().map(AidoRoboTaxiCompiler::ofTaxi));

    }

    private static Tensor ofTaxi(RoboTaxi roboTaxi) {
        // id
        Tensor info = Tensors.vector(MatsimStaticDatabase.INSTANCE.getVehicleIndex(roboTaxi));
        // divertable location
        info.append(TensorCoords.toTensor(MatsimStaticDatabase.INSTANCE.referenceFrame.coords_toWGS84().transform(//
                roboTaxi.getDivertableLocation().getCoord())));
        // status
        info.append(StringScalar.of(roboTaxi.getStatus().name()));
        // divertable?
        info.append(roboTaxi.isDivertable() ? RealScalar.ONE : RealScalar.ZERO);
        return info;
    }
}
