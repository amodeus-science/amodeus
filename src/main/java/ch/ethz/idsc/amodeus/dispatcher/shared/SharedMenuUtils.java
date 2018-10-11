package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/** This class covers the static functions on a Shared Menu.
 * 
 * @author Lukas Sieber */
public enum SharedMenuUtils {
    ;

    // **************************************************
    // Get Functions
    // **************************************************
    /** Gets the next course of the menu.
     * 
     * @return */
    public static SharedCourse getStarterCourse(SharedMenu sharedMenu) {
        GlobalAssert.that(hasStarter(sharedMenu));
        return sharedMenu.getRoboTaxiMenu().get(0);
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

    /** @return true if the menu has entries */
    public static boolean hasStarter(SharedMenu sharedMenu) {
        return !sharedMenu.getRoboTaxiMenu().isEmpty();
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
        List<SharedCourse> list = sharedMenu.getModifiableCopyOfMenu();
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

    public static SharedMenu removeAVCourses(SharedMenu sharedMenu, SharedCourse...sharedAVCourses) {
        return applyFunction(sharedMenu, SharedCourseListUtils::removeAVCourse, sharedAVCourses);
    }

    // **************************************************
    // APPLY FUNCTIONS TO THE MENU
    // **************************************************

    private static SharedMenu applyFunction(SharedMenu sharedMenu, Consumer<List<SharedCourse>> listFunction) {
        List<SharedCourse> list = sharedMenu.getModifiableCopyOfMenu();
        listFunction.accept(list);
        return SharedMenu.of(list);
    }

    private static SharedMenu applyFunction(SharedMenu sharedMenu, BiConsumer<List<SharedCourse>, SharedCourse> listFunction, SharedCourse... sharedCourses) {
        List<SharedCourse> list = sharedMenu.getModifiableCopyOfMenu();
        for (SharedCourse sharedCourse : sharedCourses) {
            listFunction.accept(list, sharedCourse);
        }
        return SharedMenu.of(list);
    }

    private static SharedMenu applyFunction(SharedMenu sharedMenu, Integer integer, BiConsumer<List<SharedCourse>, Integer> listFunction) {
        List<SharedCourse> list = sharedMenu.getModifiableCopyOfMenu();
        listFunction.accept(list, integer);
        return SharedMenu.of(list);
    }

}
