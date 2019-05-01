/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.routing;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.router.costcalculators.OnlyTimeDependentTravelDisutility;
import org.matsim.core.router.util.TravelTime;

/** A Travel Cost Calculator that uses the travel times as travel disutility.
 * 
 * @author cdobler */
public class OnlyTimeDependentTravelDisutilityFixed extends OnlyTimeDependentTravelDisutility {

    public OnlyTimeDependentTravelDisutilityFixed(TravelTime travelTime) {
        super(travelTime);
    }

    @Override
    public double getLinkMinimumTravelDisutility(final Link link) {
        return this.travelTime.getLinkTravelTime(link, 0, null, null);
    }

}