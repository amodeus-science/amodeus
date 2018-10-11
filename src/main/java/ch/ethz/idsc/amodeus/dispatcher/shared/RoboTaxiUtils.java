package ch.ethz.idsc.amodeus.dispatcher.shared;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;

public enum RoboTaxiUtils {
;
    public static boolean canPickupNewCustomer(RoboTaxi roboTaxi) {
        // TODO why does the number of customers has to be larger for a pick up?
        return roboTaxi.getCurrentNumberOfCustomersOnBoard() >= 0 && roboTaxi.getCurrentNumberOfCustomersOnBoard() < roboTaxi.getCapacity();
    }
    
    public static boolean canDropOffCustomer(RoboTaxi roboTaxi) {
        // TODO why does the number of customers has to be larger for a pick up?
        return roboTaxi.getCurrentNumberOfCustomersOnBoard() > 0 && roboTaxi.getCurrentNumberOfCustomersOnBoard() < roboTaxi.getCapacity();
    }
    
    public static boolean checkMenuConsistency(RoboTaxi roboTaxi) {
        return SharedMenuChecks.checkMenuDoesNotPlanToPickUpMoreCustomersThanCapacity(roboTaxi.getCopyOfMenu(), roboTaxi.getCapacity());
    }
    
}
