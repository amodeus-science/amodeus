package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseListUtils;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMenu;

/** This class covers the static functions on a Shared Menu.
 * 
 * @author Lukas Sieber */
/* package */ enum SharedMenuUtils {
    ;

    // **************************************************
    // Get Functions
    // **************************************************
    /** Gets the next course of the menu.
     * 
     * @return */
    public static Optional<SharedCourse> getStarterCourse(SharedMenu sharedMenu) {
        return SharedCourseListUtils.getStarterCourse(sharedMenu.getRoboTaxiMenu());
    }

    public static long getNumberCustomersOnBoard(SharedMenu sharedMenu) {
        return SharedCourseListUtils.getNumberCustomersOnBoard(sharedMenu.getRoboTaxiMenu());
    }

    // **************************************************
    // Check Menus
    // **************************************************
    public static boolean containSameCourses(SharedMenu sharedMenu1, SharedMenu sharedMenu2) {
        return sharedMenu1.getRoboTaxiMenu().size() == sharedMenu2.getRoboTaxiMenu().size() && //
                sharedMenu1.getRoboTaxiMenu().containsAll(sharedMenu2.getRoboTaxiMenu());
    }

    public static boolean checkAllCoursesAppearOnlyOnce(SharedMenu sharedMenu) {
        return SharedCourseListUtils.checkAllCoursesAppearOnlyOnce(sharedMenu.getRoboTaxiMenu());
    }

    public static boolean checkNoPickupAfterDropoffOfSameRequest(SharedMenu sharedMenu) {
        return SharedCourseListUtils.checkNoPickupAfterDropoffOfSameRequest(sharedMenu.getRoboTaxiMenu());
    }

    public static boolean checkMenuDoesNotPlanToPickUpMoreCustomersThanCapacity(SharedMenu sharedMenu, int roboTaxiCapacity) {
        return SharedCourseListUtils.checkMenuDoesNotPlanToPickUpMoreCustomersThanCapacity(sharedMenu.getRoboTaxiMenu(), roboTaxiCapacity);
    }

    /** @return true if the menu has entries */
    public static boolean hasStarter(SharedMenu sharedMenu) {
        return SharedCourseListUtils.hasStarter(sharedMenu.getRoboTaxiMenu());
    }

    // **************************************************
    // ADDING COURSES
    // **************************************************
    public static SharedMenu addAVCoursesAsStarter(SharedMenu sharedMenu, SharedCourse... avCourses) {
        return applyFunction(sharedMenu, SharedCourseListUtils::addAVCourseAsStarter, avCourses);
    }

    public static SharedMenu addAVCoursesAsDessert(SharedMenu sharedMenu, SharedCourse... avCourses) {
        return applyFunction(sharedMenu, SharedCourseListUtils::addAVCourseAsDessert, avCourses);
    }

    public static SharedMenu addAVCoursesAtIndex(SharedMenu sharedMenu, int courseIndex, SharedCourse... avCourses) {
        List<SharedCourse> list = SharedCourseListUtils.copy(sharedMenu.getRoboTaxiMenu());
        for (SharedCourse sharedCourse : avCourses) {
            SharedCourseListUtils.addAVCourseAtIndex(list, sharedCourse, courseIndex);
        }
        return SharedMenu.of(list);
    }

    // **************************************************
    // MOVING COURSES
    // **************************************************

    public static SharedMenu moveAVCourseToPrev(SharedMenu sharedMenu, SharedCourse sharedAVCourse) {
        return applyFunction(sharedMenu, SharedCourseListUtils::moveAVCourseToPrev, sharedAVCourse);
    }

    public static SharedMenu moveAVCourseToNext(SharedMenu sharedMenu, SharedCourse sharedAVCourse) {
        return applyFunction(sharedMenu, SharedCourseListUtils::moveAVCourseToNext, sharedAVCourse);
    }

    // **************************************************
    // REMOVING COURSES
    // **************************************************

    public static SharedMenu removeStarterCourse(SharedMenu sharedMenu) {
        return applyFunction(sharedMenu, SharedCourseListUtils::removeStarterCourse);
    }

    public static SharedMenu removeAVCourse(SharedMenu sharedMenu, int courseIndex) {
        return applyFunction(sharedMenu, courseIndex, SharedCourseListUtils::removeAVCourse);
    }

    public static SharedMenu removeAVCourses(SharedMenu sharedMenu, SharedCourse... sharedAVCourses) {
        return applyFunction(sharedMenu, SharedCourseListUtils::removeAVCourse, sharedAVCourses);
    }

    // **************************************************
    // APPLY FUNCTIONS TO THE MENU
    // **************************************************

    private static SharedMenu applyFunction(SharedMenu sharedMenu, Consumer<List<SharedCourse>> listFunction) {
        List<SharedCourse> list = SharedCourseListUtils.copy(sharedMenu.getRoboTaxiMenu());
        listFunction.accept(list);
        return SharedMenu.of(list);
    }

    private static SharedMenu applyFunction(SharedMenu sharedMenu, BiConsumer<List<SharedCourse>, SharedCourse> listFunction, SharedCourse... sharedCourses) {
        List<SharedCourse> list = SharedCourseListUtils.copy(sharedMenu.getRoboTaxiMenu());
        for (SharedCourse sharedCourse : sharedCourses) {
            listFunction.accept(list, sharedCourse);
        }
        return SharedMenu.of(list);
    }

    private static SharedMenu applyFunction(SharedMenu sharedMenu, Integer integer, BiConsumer<List<SharedCourse>, Integer> listFunction) {
        List<SharedCourse> list = SharedCourseListUtils.copy(sharedMenu.getRoboTaxiMenu());
        listFunction.accept(list, integer);
        return SharedMenu.of(list);
    }

}
