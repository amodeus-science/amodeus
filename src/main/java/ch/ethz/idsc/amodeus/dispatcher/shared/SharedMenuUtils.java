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
        return SharedCourseListUtils.getStarterCourse(sharedMenu.getCourseList());
    }
    
    /** @return true if the menu has entries */
    public static boolean hasStarter(SharedMenu sharedMenu) {
        return SharedCourseListUtils.hasStarter(sharedMenu.getCourseList());
    }


    // **************************************************
    // Check Menus
    // **************************************************
    // TODO naming is wrong, this is only a one-sided inclusion check. Name accordingly.
//    public static boolean containSameCourses(SharedMenu sharedMenu1, SharedMenu sharedMenu2) {
//        return sharedMenu1.getCourseList().size() == sharedMenu2.getCourseList().size() && //
//                sharedMenu1.getCourseList().containsAll(sharedMenu2.getCourseList());
//    }
//
//    public static boolean checkNoPickupAfterDropoffOfSameRequest(SharedMenu sharedMenu) {
//        return SharedMenuCheck.checkNoPickupAfterDropoffOfSameRequest(sharedMenu.getCourseList());
//    }
//
//    public static boolean checkMenuConsistencyWithRoboTaxi(SharedMenu sharedMenu, int roboTaxiCapacity) {
//        return SharedMenuCheck.checkMenuConsistency(sharedMenu.getCourseList(), roboTaxiCapacity);
//    }
//
//    public static boolean checkMenuDoesNotPlanToPickUpMoreCustomersThanCapacity(SharedMenu sharedMenu, int roboTaxiCapacity) {
//        return Compatibility.of(sharedMenu.getCourseList()).forCapacity(roboTaxiCapacity);
//    }




}
