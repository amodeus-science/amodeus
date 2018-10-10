package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** This class covers the static functions on a Shared Menu.
 * 
 * @author Lukas Sieber */
public enum SharedMenuUtils {
    ;


    // **************************************************
    // Check Menus
    // **************************************************
    public static boolean containSamecourses(SharedMenu sharedMenu1, SharedMenu sharedMenu2) {
        
    }

    // **************************************************
    // ADDING COURSES
    // **************************************************
    public static SharedMenu addAVCourseAsStarter(SharedMenu sharedMenu, SharedCourse avCourse) {
        return applyFunction(sharedMenu, avCourse, SharedCourseListUtils::addAVCourseAsStarter);
    }

    public static SharedMenu addAVCourseAsDessert(SharedMenu sharedMenu, SharedCourse avCourse) {
        return applyFunction(sharedMenu, avCourse, SharedCourseListUtils::addAVCourseAsDessert);
    }

    public static SharedMenu addAVCourseAtIndex(SharedMenu sharedMenu, SharedCourse avCourse, int courseIndex) {
        // TODO implement it as well with an Apply function
        List<SharedCourse> list = sharedMenu.getModifiableCopyOfMenu();
        SharedCourseListUtils.addAVCourseAtIndex(list, avCourse, courseIndex);
        return SharedMenu.of(list);
    }

    // **************************************************
    // MOVING COURSES
    // **************************************************

    public static SharedMenu moveAVCourseToPrev(SharedMenu sharedMenu, SharedCourse sharedAVCourse) {
        return applyFunction(sharedMenu, sharedAVCourse, SharedCourseListUtils::moveAVCourseToPrev);
    }

    public static SharedMenu moveAVCourseToNext(SharedMenu sharedMenu, SharedCourse sharedAVCourse) {
        return applyFunction(sharedMenu, sharedAVCourse, SharedCourseListUtils::moveAVCourseToNext);
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

    public static SharedMenu removeAVCourse(SharedMenu sharedMenu, SharedCourse sharedAVCourse) {
        return applyFunction(sharedMenu, sharedAVCourse, SharedCourseListUtils::removeAVCourse);
    }
    

    // **************************************************
    // APPLY FUNCTIONS TO THE MENU
    // **************************************************

    
    private static SharedMenu applyFunction(SharedMenu sharedMenu, Consumer<List<SharedCourse>> listFunction) {
        List<SharedCourse> list = sharedMenu.getModifiableCopyOfMenu();
        listFunction.accept(list);
        return SharedMenu.of(list);
    }

    private static SharedMenu applyFunction(SharedMenu sharedMenu, SharedCourse sharedAVCourse, BiConsumer<List<SharedCourse>, SharedCourse> listFunction) {
        List<SharedCourse> list = sharedMenu.getModifiableCopyOfMenu();
        listFunction.accept(list, sharedAVCourse);
        return SharedMenu.of(list);
    }

    private static SharedMenu applyFunction(SharedMenu sharedMenu, Integer integer, BiConsumer<List<SharedCourse>, Integer> listFunction) {
        List<SharedCourse> list = sharedMenu.getModifiableCopyOfMenu();
        listFunction.accept(list, integer);
        return SharedMenu.of(list);
    }

}
