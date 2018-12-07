/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

/** This class gives some functionalities on modifiable Lists of Shared AV Courses. It extends normal List functionalities with some Tests */
public enum SharedCourseListUtils {
    ;

    public static List<SharedCourse> copy(List<SharedCourse> courses) {
        return new ArrayList<>(courses);
    }

    // **************************************************
    // Get Functions
    // **************************************************

    public static Set<AVRequest> getOnBoardRequests(List<? extends SharedCourse> courses) {
        Set<AVRequest> pickups = getRequestsWithMealType(courses, SharedMealType.PICKUP);
        Set<AVRequest> dropoffs = getRequestsWithMealType(courses, SharedMealType.DROPOFF);
        for (AVRequest avRequestIDpickup : pickups) {
            boolean removeOk = dropoffs.remove(avRequestIDpickup);
            GlobalAssert.that(removeOk);
        }
        GlobalAssert.that(getNumberCustomersOnBoard(courses) == dropoffs.size());
        return dropoffs;
    }

    private static Set<AVRequest> getRequestsWithMealType(List<? extends SharedCourse> courses, SharedMealType sharedMealType) {
        return courses.stream().filter(sc -> sc.getMealType().equals(sharedMealType)).map(sc -> sc.getAvRequest()).collect(Collectors.toSet());
    }

    public static long getNumberPickups(List<? extends SharedCourse> courses) {
        return getNumberSharedMealType(courses, SharedMealType.PICKUP);
    }

    public static long getNumberDropoffs(List<? extends SharedCourse> courses) {
        return getNumberSharedMealType(courses, SharedMealType.DROPOFF);
    }

    public static long getNumberRedirections(List<? extends SharedCourse> courses) {
        return getNumberSharedMealType(courses, SharedMealType.REDIRECT);
    }

    private static long getNumberSharedMealType(List<? extends SharedCourse> courses, SharedMealType sharedMealType) {
        return courses.stream().filter(sc -> sc.getMealType().equals(sharedMealType)).count();
    }

    public static long getNumberCustomersOnBoard(List<? extends SharedCourse> courses) {
        return getNumberDropoffs(courses) - getNumberPickups(courses);
    }

    public static Set<AVRequest> getUniqueAVRequests(List<? extends SharedCourse> courses) {
        return courses.stream().filter(sc -> !sc.getMealType().equals(SharedMealType.REDIRECT)).map(sc -> sc.getAvRequest()).collect(Collectors.toSet());//
    }

    /** Gets the next course of the menu.
     * 
     * @return */
    public static Optional<SharedCourse> getStarterCourse(List<? extends SharedCourse> courses) {
        return Optional.ofNullable((hasStarter(courses)) ? courses.get(0) : null);
    }

    public static Optional<SharedCourse> getSecondCourse(List<SharedCourse> courses) {
        return Optional.ofNullable((hasSecondCourse(courses)) ? courses.get(1) : null);
    }

    // **************************************************
    // Check Shared Course List
    // **************************************************

    public static boolean hasStarter(List<? extends SharedCourse> courses) {
        return !courses.isEmpty();
    }

    public static boolean hasSecondCourse(List<SharedCourse> courses) {
        if (hasStarter(courses)) {
            return courses.size() >= 2;
        }
        return false;
    }

    public static boolean consistencyCheck(List<? extends SharedCourse> courses) {
        return checkAllCoursesAppearOnlyOnce(courses) && checkNoPickupAfterDropoffOfSameRequest(courses);
    }

    public static boolean checkAllCoursesAppearOnlyOnce(List<? extends SharedCourse> courses) {
        return new HashSet<>(courses).size() == courses.size();
    }

    public static boolean checkMenuConsistency(List<? extends SharedCourse> courses, int capacity) {
        return checkMenuDoesNotPlanToPickUpMoreCustomersThanCapacity(courses, capacity);
    }

    /** @return false if any dropoff occurs after pickup in the menu.
     *         If no dropoff ocurs for one pickup an exception is thrown as this is not allowed in a {@link SharedMenu} */
    public static boolean checkNoPickupAfterDropoffOfSameRequest(List<? extends SharedCourse> courses) {
        for (SharedCourse course : courses) {
            if (course.getMealType().equals(SharedMealType.PICKUP)) {
                int pickupIndex = courses.indexOf(course);
                SharedCourse dropoffCourse = SharedCourse.dropoffCourse(course.getAvRequest());
                GlobalAssert.that(courses.contains(dropoffCourse));
                int dropofIndex = courses.indexOf(dropoffCourse);
                if (pickupIndex > dropofIndex) {
                    System.err.println("The SharedRoboTaxiMenu contains a pickup after its dropoff. Stopping Execution.");
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean checkMenuDoesNotPlanToPickUpMoreCustomersThanCapacity(List<? extends SharedCourse> courses, int roboTaxiCapacity) {
        long futureNumberCustomers = SharedCourseListUtils.getNumberCustomersOnBoard(courses);
        for (SharedCourse sharedAVCourse : courses) {
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

    // **************************************************
    // ADDING COURSES
    // **************************************************
    public static void addAVCourseAsStarter(List<SharedCourse> courses, SharedCourse avCourse) {
        addAVCourseAtIndex(courses, avCourse, 0);
    }

    public static void addAVCourseAsDessert(List<SharedCourse> courses, SharedCourse avCourse) {
        addAVCourseAtIndex(courses, avCourse, courses.size());
    }

    public static void addAVCourseAtIndex(List<SharedCourse> courses, SharedCourse avCourse, int courseIndex) {
        GlobalAssert.that(0 <= courseIndex && courseIndex <= courses.size());
        courses.add(courseIndex, avCourse);
    }

    // **************************************************
    // MOVING COURSES
    // **************************************************

    public static boolean moveAVCourseToPrev(List<SharedCourse> courses, SharedCourse sharedAVCourse) {
        GlobalAssert.that(courses.contains(sharedAVCourse));
        int i = courses.indexOf(sharedAVCourse);
        if (0 < i && i < courses.size()) {
            Collections.swap(courses, i, i - 1);
            return true;
        }
        System.out.println("Swaping Failed!!");
        return false;
    }

    public static boolean moveAVCourseToNext(List<SharedCourse> courses, SharedCourse sharedAVCourse) {
        GlobalAssert.that(courses.contains(sharedAVCourse));
        int i = courses.indexOf(sharedAVCourse);
        if (0 <= i && i < courses.size() - 1) {
            Collections.swap(courses, i, i + 1);
            return true;
        }
        System.out.println("Swaping Failed!!");
        return false;
    }

    // **************************************************
    // REMOVING COURSES
    // **************************************************

    public static void removeStarterCourse(List<SharedCourse> courses) {
        GlobalAssert.that(!courses.isEmpty());
        courses.remove(0);
    }

    public static void removeAVCourse(List<SharedCourse> courses, int courseIndex) {
        GlobalAssert.that(courses.size() > courseIndex);
        courses.remove(courseIndex);
    }

    public static void removeAVCourse(List<SharedCourse> courses, SharedCourse sharedAVCourse) {
        GlobalAssert.that(courses.contains(sharedAVCourse));
        courses.remove(sharedAVCourse);
    }

}
