/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.Optional;

/** This class covers the static functions on a Shared Menu.
 * 
 * @author Lukas Sieber */
public enum SharedMenuUtils {
    ;

    // TODO Lukas Rmove this file as it is not really required. use the list utils directly
    // **************************************************
    // Get Functions
    // **************************************************
    /** Gets the next course of the menu.
     * 
     * @return */
    public static Optional<SharedCourse> getStarterCourse(SharedMenu sharedMenu) {
        return SharedCourseListUtils.getStarterCourse(sharedMenu.getRoboTaxiMenu());
    }
    
    /** @return true if the menu has entries */
    public static boolean hasStarter(SharedMenu sharedMenu) {
        return SharedCourseListUtils.hasStarter(sharedMenu.getRoboTaxiMenu());
    }


    // **************************************************
    // Check Menus
    // **************************************************
    // TODO naming is wrong, this is only a one-sided inclusion check. Name accordingly.
    public static boolean containSameCourses(SharedMenu sharedMenu1, SharedMenu sharedMenu2) {
        return sharedMenu1.getRoboTaxiMenu().size() == sharedMenu2.getRoboTaxiMenu().size() && //
                sharedMenu1.getRoboTaxiMenu().containsAll(sharedMenu2.getRoboTaxiMenu());
    }

    public static boolean checkNoPickupAfterDropoffOfSameRequest(SharedMenu sharedMenu) {
        return SharedMenuCheck.checkNoPickupAfterDropoffOfSameRequest(sharedMenu.getRoboTaxiMenu());
    }

    public static boolean checkMenuConsistencyWithRoboTaxi(SharedMenu sharedMenu, int roboTaxiCapacity) {
        return SharedMenuCheck.checkMenuConsistency(sharedMenu.getRoboTaxiMenu(), roboTaxiCapacity);
    }

    public static boolean checkMenuDoesNotPlanToPickUpMoreCustomersThanCapacity(SharedMenu sharedMenu, int roboTaxiCapacity) {
        return Compatibility.of(sharedMenu.getRoboTaxiMenu()).forCapacity(roboTaxiCapacity);
    }




}
