package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.Optional;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;

public enum RoboTaxiUtils {
;
    public static boolean canPickupNewCustomer(RoboTaxi roboTaxi) {
        // TODO why does the number of customers has to be larger for a pick up?
        return roboTaxi.getCurrentNumberOfCustomersOnBoard() >= 0 && roboTaxi.getCurrentNumberOfCustomersOnBoard() < roboTaxi.getCapacity();
    }
    
    
    public static boolean checkMenuConsistency(RoboTaxi roboTaxi) {
        return SharedMenuChecks.checkMenuDoesNotPlanToPickUpMoreCustomersThanCapacity(roboTaxi.getCopyOfMenu(), roboTaxi.getCapacity());
    }
    
    public static boolean hasNextCourse(RoboTaxi roboTaxi) {
        return SharedMenuUtils.hasStarter(roboTaxi.getCopyOfMenu());
    }
    
    // TODO Refactor to more meaningfull name
    public static Optional<SharedCourse> getStarterCourse(RoboTaxi roboTaxi) {
        return SharedMenuUtils.getStarterCourse(roboTaxi.getCopyOfMenu());
    }
    
    public static boolean nextCourseIsOfType(RoboTaxi roboTaxi, SharedMealType sharedMealType) {
        Optional<SharedCourse> nextcourse = getStarterCourse(roboTaxi);
        if (nextcourse.isPresent()) {
            return nextcourse.get().getMealType().equals(sharedMealType);
        }
        return false;
    }
}
