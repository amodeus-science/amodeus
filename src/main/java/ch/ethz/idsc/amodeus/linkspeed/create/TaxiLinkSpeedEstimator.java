/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed.create;

import ch.ethz.idsc.amodeus.linkspeed.LinkSpeedDataContainer;

public interface TaxiLinkSpeedEstimator {

    /** @return the {@link LinkSpeedDatacontainer} produced by the {@link TaxiLinkSpeedEstimator} */
    public LinkSpeedDataContainer getLsData();
}
