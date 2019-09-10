/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed.create;

import java.util.Objects;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.linkspeed.LinkSpeedDataContainer;
import ch.ethz.idsc.amodeus.linkspeed.LinkSpeedTimeSeries;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.tensor.Scalar;

/** This class is used to complete modified link speeds in a network at a certain time
 * when no recording was made at that time. The principal idea is that a kernel
 * is used to identify neighbors and then the change of the speed of that link is the
 * same as the average change of its neighbors.
 * 
 * @author clruch */
public class LinkSpeedDataInterpolation {

    private final NeighborKernel kernel;
    private final Network network;
    private final LinkSpeedDataContainer lsData;
    private final MatsimAmodeusDatabase db;

    public LinkSpeedDataInterpolation(Network network, NeighborKernel filterKernel, //
            LinkSpeedDataContainer lsData, MatsimAmodeusDatabase db) {
        this.network = network;
        this.kernel = filterKernel;
        this.lsData = lsData;
        this.db = db;
        completeSpeeds();
    }

    private void completeSpeeds() {
        /** identify all times for which the {@link LinkSpeedDataContainer} contains recordings */
        Set<Integer> recordedTimes = lsData.getRecordedTimes();

        /** for every link in the network, complete recordings based on local average
         * if not present. */
        for (Link link : network.getLinks().values()) {
            Objects.requireNonNull(link);
            /** some recordings exist for link */
            if (lsData.getLinkSet().containsKey(db.getLinkIndex(link))) {
                LinkSpeedTimeSeries timeSeries = lsData.getLinkSet().get(db.getLinkIndex(link));
                for (Integer time : recordedTimes) {
                    if (timeSeries.getRecordedTimes().contains(time)) {
                        // -- don-t do anything, time was recorded on that link
                    } else {
                        Scalar speed = MeanLinkSpeed.neighborAverage(kernel.getNeighbors(link), time, link, //
                                db, lsData);
                        int linkID = Integer.parseInt(link.getId().toString());
                        lsData.addData(linkID, time, speed.number().doubleValue());
                    }
                }
            } else { /** no recordings exist for link */
                for (Integer time : recordedTimes) {
                    Scalar speed = MeanLinkSpeed.neighborAverage(kernel.getNeighbors(link), time, link, //
                            db, lsData);
                    int linkID = Integer.parseInt(link.getId().toString());
                    lsData.addData(linkID, time, speed.number().doubleValue());
                }
            }
        }
    }
}
