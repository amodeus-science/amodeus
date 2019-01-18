package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ class InsertionCheck {

    public InsertionCheck(RoboTaxi roboTaxi, AVRequest request, double maxPickupDelay, double maxDrpoffDelay) {
        // TODO
    }

    /** @return null if the request cannot be reached before maxPickupDelay or
     *         the request cannot be dropped of before reaching maxDrpoffDelay. Otherwise
     *         returns the additional necessary distance to pickup the request. */
    public Double getAddDistance() {
        // TODO
        return null;
    }
    
    public void insert(){
        
    }
    
    public RoboTaxi getRoboTaxi(){
        
    }

}
