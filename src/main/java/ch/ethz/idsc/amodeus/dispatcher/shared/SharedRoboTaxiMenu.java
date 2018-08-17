/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.data.Request;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/** Object containing list of pickup and dropoff planned
 * for an AV.
 * 
 * @author Nicolo Ormezzano, Lukas Sieber */
public class SharedRoboTaxiMenu {
    private final List<SharedRoboTaxiCourse> roboTaxiMenu = new ArrayList<>();

    public SharedRoboTaxiMenu() {
    }

    private SharedRoboTaxiMenu(SharedRoboTaxiMenu sharedAVMenu) {
        sharedAVMenu.roboTaxiMenu.stream() // SharedAVCourse is immutable
                .forEach(roboTaxiMenu::add);
    }

    // **************************************************
    // ADDING COURSES
    // **************************************************

    public void addAVCourseAtIndex(SharedRoboTaxiCourse avCourse, int courseIndex) {
        GlobalAssert.that(0 <= courseIndex && courseIndex <= roboTaxiMenu.size());
        roboTaxiMenu.add(courseIndex, avCourse);
    }

    public void addAVCourseAsStarter(SharedRoboTaxiCourse avCourse) {
        roboTaxiMenu.add(0, avCourse);
    }

    public void addAVCourseAsDessert(SharedRoboTaxiCourse avCourse) {
        roboTaxiMenu.add(roboTaxiMenu.size(), avCourse);
    }

    // **************************************************
    // MOVING COURSES
    // **************************************************

    public boolean moveAVCourseToPrev(SharedRoboTaxiCourse sharedAVCourse) {
        GlobalAssert.that(containsCourse(sharedAVCourse));
        int i = getIndexOf(sharedAVCourse);
        boolean swap = 0 < i && i < roboTaxiMenu.size();
        if (swap)
            Collections.swap(roboTaxiMenu, i, i - 1);
        return swap;
    }

    public boolean moveAVCourseToNext(SharedRoboTaxiCourse sharedAVCourse) {
        GlobalAssert.that(containsCourse(sharedAVCourse));
        int i = getIndexOf(sharedAVCourse);
        boolean swap = 0 <= i && i < roboTaxiMenu.size() - 1;
        if (swap)
            Collections.swap(roboTaxiMenu, i, i + 1);
        return swap;
    }

    /** Replaces the all the courses of this menu with the course order of the new
     * menu. It is required that the new menu contains exactly the same courses as
     * the old one.
     * 
     * @param sharedAVMenu */
    public void replaceWith(SharedRoboTaxiMenu sharedAVMenu) {
        GlobalAssert.that(containsSameCourses(sharedAVMenu));
        clearWholeMenu();
        GlobalAssert.that(roboTaxiMenu.isEmpty());
        roboTaxiMenu.addAll(sharedAVMenu.getCourses());
    }

    // **************************************************
    // REMOVING COURSES
    // **************************************************

    public void removeStarterCourse() {
        GlobalAssert.that(hasStarter());
        removeAVCourse(0);
    }

    public void removeAVCourse(int courseIndex) {
        GlobalAssert.that(roboTaxiMenu.size() > courseIndex);
        roboTaxiMenu.remove(courseIndex);
    }

    public void removeAVCourse(SharedRoboTaxiCourse sharedAVCourse) {
        GlobalAssert.that(containsCourse(sharedAVCourse));
        roboTaxiMenu.remove(sharedAVCourse);
    }

    public void clearWholeMenu() {
        roboTaxiMenu.clear();
    }

    // **************************************************
    // GET COURSES
    // **************************************************

    /** Gets the next course of the menu.
     * 
     * @return */
    public SharedRoboTaxiCourse getStarterCourse() {
        return roboTaxiMenu.isEmpty() ? null : roboTaxiMenu.get(0);
    }

    /** Gets the complete List of Courses in this Menu
     * 
     * @return */
    public List<SharedRoboTaxiCourse> getCourses() {
        return Collections.unmodifiableList(roboTaxiMenu);
        // return roboTaxiMenu;
    }

    /** Get the position of the course in the menu. 0 is the next course (called
     * Starter). see {@link getStarterCourse}.
     * 
     * @param course
     * @return */
    public int getIndexOf(SharedRoboTaxiCourse course) {
        return roboTaxiMenu.indexOf(course);
    }

    /** Gets A deep Copy of this Menu
     * 
     * @return */
    public SharedRoboTaxiMenu copy() {
        return new SharedRoboTaxiMenu(this);
    }

    /** Gets the indices of the give SharedAVMealType.
     * 
     * @param sharedRoboTaxiMealType
     * @return */
    public List<Integer> getPickupOrDropOffCoursesIndeces(SharedRoboTaxiMealType sharedRoboTaxiMealType) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < roboTaxiMenu.size(); i++) {
            if (roboTaxiMenu.get(i).getMealType().equals(sharedRoboTaxiMealType)) {
                indices.add(i);
            }
        }
        return indices;
    }

    public Set<Id<Request>> getUniqueAVRequests() {
        Set<Id<Request>> ids = new HashSet<>();
        roboTaxiMenu.forEach(savc -> ids.add(savc.getRequestId()));
        return ids;
    }

    public void printMenu() {
        roboTaxiMenu.forEach(course -> System.out.println(course.getRequestId().toString() + ":\t" + course.getMealType().name()));
    }

    // **************************************************
    // CHECK FUNCTIONS
    // **************************************************

    /** @return true if the menu has entries */
    public boolean hasStarter() {
        return !roboTaxiMenu.isEmpty();
    }

    /** Checks if the given sharedAvCourse is contained in the menu
     * 
     * @param sharedAVCourse
     * @return */
    public boolean containsCourse(SharedRoboTaxiCourse sharedAVCourse) {
        return roboTaxiMenu.contains(sharedAVCourse);
    }

    /** @return false if any dropoff occurs after pickup in the menu */
    public boolean checkNoPickupAfterDropoffOfSameRequest() {
        for (SharedRoboTaxiCourse course : roboTaxiMenu) {
            if (course.getMealType().equals(SharedRoboTaxiMealType.PICKUP)) {
                int pickupIndex = getIndexOf(course);
                SharedRoboTaxiCourse dropoffCourse = getCorrespDropoff(course);
                if (Objects.nonNull(dropoffCourse)) {
                    int dropofIndex = getIndexOf(dropoffCourse);
                    if (pickupIndex > dropofIndex) {
                        System.err.println("The SharedRoboTaxiMenu contains a pickup after its dropoff. Stopping Execution.");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /** @param pickupCourse
     * @return corresponding {@link SharedRoboTaxiCourse} where dropoff takes place or
     *         null if not found */
    public SharedRoboTaxiCourse getCorrespDropoff(SharedRoboTaxiCourse pickupCourse) {
        GlobalAssert.that(pickupCourse.getMealType().equals(SharedRoboTaxiMealType.PICKUP));
        for (SharedRoboTaxiCourse course : roboTaxiMenu) {
            if (course.getRequestId().equals(pickupCourse.getRequestId())) {
                if (course.getMealType().equals(SharedRoboTaxiMealType.DROPOFF)) {
                    return course;
                }
            }
        }
        return null;
    }

    /** Checks if the menu contains exactly the same courses as the inputed menu.
     * 
     * @param sharedAVMenu
     * @return true if the the two menus contain the same courses */
    public boolean containsSameCourses(SharedRoboTaxiMenu sharedAVMenu) {
        return roboTaxiMenu.size() == sharedAVMenu.getCourses().size() && //
                sharedAVMenu.getCourses().containsAll(roboTaxiMenu);
    }

    public boolean checkAllCoursesAppearOnlyOnce() {
        return new HashSet<>(roboTaxiMenu).size() == roboTaxiMenu.size();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof SharedRoboTaxiMenu) {
            SharedRoboTaxiMenu sharedAVMenu = (SharedRoboTaxiMenu) object;
            List<SharedRoboTaxiCourse> otherMenu = sharedAVMenu.getCourses();
            // TODO LUXURY there is an easier way to check for equality
            if (otherMenu.size() == roboTaxiMenu.size()) {
                for (int i = 0; i < roboTaxiMenu.size(); i++)
                    if (!roboTaxiMenu.get(i).equals(sharedAVMenu.getCourses().get(i)))
                        return false;
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        // TODO SHARED not yet implemented
        throw new RuntimeException();
    }

}
