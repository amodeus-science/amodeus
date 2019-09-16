/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.net.VehicleContainer;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Array;

/** utility class that is used in LinkLayer
 * 
 * sioux falls has ~3k roads
 * zurich scenario has ~200k roads */
/* package */ class LoadStats {

    private static final RoboTaxiStatus[] INTERP = new RoboTaxiStatus[] { //
            RoboTaxiStatus.DRIVEWITHCUSTOMER, //
            RoboTaxiStatus.DRIVETOCUSTOMER, //
            RoboTaxiStatus.REBALANCEDRIVE };

    private final int width;

    final Map<Integer, Tensor> linkTensor = new HashMap<>();

    public LoadStats(int width) {
        this.width = width;
    }

    public void feed(SimulationObject ref, int ofs) {
        Map<Integer, List<VehicleContainer>> map = ref.vehicles.stream() //
                .collect(Collectors.groupingBy(this::indexFrom));

        for (Entry<Integer, List<VehicleContainer>> entry : map.entrySet()) {
            final int index = entry.getKey();
            final List<VehicleContainer> list = entry.getValue();

            final long total = list.stream().filter(vc -> vc.roboTaxiStatus.isDriving()).count();
            if (0 < total) {
                final Tensor array;
                if (linkTensor.containsKey(index))
                    array = linkTensor.get(index);
                else {
                    array = Array.zeros(width, 2);
                    linkTensor.put(index, array);
                }
                Map<RoboTaxiStatus, List<VehicleContainer>> classify = //
                        list.stream().collect(Collectors.groupingBy(vc -> vc.roboTaxiStatus));
                int[] counts = new int[3];
                for (RoboTaxiStatus avStatus : INTERP)
                    counts[avStatus.ordinal()] = classify.containsKey(avStatus) ? classify.get(avStatus).size() : 0;
                final int customers = counts[0];
                final int carsEmpty = counts[1] + counts[2];
                array.set(RealScalar.of(customers), ofs, 0);
                array.set(RealScalar.of(carsEmpty), ofs, 1);
            }
        }
    }

    private int indexFrom(VehicleContainer vc) {
        return vc.linkIndex[vc.linkIndex.length-1];
    }
}
