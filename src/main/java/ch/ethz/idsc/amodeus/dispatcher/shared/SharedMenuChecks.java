package ch.ethz.idsc.amodeus.dispatcher.shared;

public enum SharedMenuChecks {
;
    public static boolean checkMenuDoesNotPlanToPickUpMoreCustomersThanCapacity(SharedMenu sharedMenu, int roboTaxiCapacity) {
        long futureNumberCustomers = SharedMenuUtils.getNumberCustomersOnBoard(sharedMenu);
        for (SharedCourse sharedAVCourse : sharedMenu.getRoboTaxiMenu()) {
            if (sharedAVCourse.getMealType().equals(SharedMealType.PICKUP)) {
                futureNumberCustomers++;
            } else if (sharedAVCourse.getMealType().equals(SharedMealType.DROPOFF)) {
                futureNumberCustomers--;
            } else if (sharedAVCourse.getMealType().equals(SharedMealType.REDIRECT)) {
                // --
            } else {
                throw new IllegalArgumentException("Unknown SharedAVMealType -- please specify it !!!--");
            }
            if (futureNumberCustomers > roboTaxiCapacity) {
                return false;
            }
        }
        return true;
    }
}
