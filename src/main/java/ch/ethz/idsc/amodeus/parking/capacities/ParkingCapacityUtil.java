package ch.ethz.idsc.amodeus.parking.capacities;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;

public enum ParkingCapacityUtil {
    ;

    /** @return entire number of parking spaces in the @param network with
     *         {@link ParkingCapacity} @param parkingCapacity */
    public static Scalar getTotal(Network network, ParkingCapacity parkingCapacity) {
        Scalar totalCap = RealScalar.ZERO;
        for (Link link : network.getLinks().values()) {
            totalCap = totalCap.add(RationalScalar.of(parkingCapacity.getSpatialCapacity(link.getId()), 1));
        }
        return totalCap;
    }
}
