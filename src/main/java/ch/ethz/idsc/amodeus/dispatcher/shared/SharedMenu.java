/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/** Object containing list of shared Courses (pickup, dropoff, rebalance) planned
 * for an RoboTaxi. If the menu */
public class SharedMenu {
    /** Unmodifiable List of Shared Courses */
    private final List<SharedCourse> roboTaxiMenu;

    /** Creates a Shared Menu which is consistent in itself (e.g. no coureses appear twice, for each request it is secured that the dropoff happens after the pickup

     * 
     * @param list
     * @return */
    public static SharedMenu of(List<SharedCourse> list) {
        GlobalAssert.that(SharedCourseListUtils.consistencyCheck(list));
        return new SharedMenu(list);
    }

    public static SharedMenu empty() {
        return new SharedMenu(null);
    }

    private SharedMenu(List<SharedCourse> list) {
        roboTaxiMenu = Collections.unmodifiableList((Objects.isNull(list)) ? new ArrayList<>() : list);
    }

    /** Two ways how to get the Courses in the Menu:
     * this function returns an unmodifiable view of the menu.
     * 
     * @return */
    public List<SharedCourse> getRoboTaxiMenu() {
        return roboTaxiMenu;
    }

    /** Two ways how to get the Courses in the Menu:
     * this function returns an deep copy of the courses in the menu.
     * 
     * @return */
    public List<SharedCourse> getModifiableCopyOfMenu() {
        return SharedCourseListUtils.copy(roboTaxiMenu);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof SharedMenu) {
            SharedMenu sharedAVMenu = (SharedMenu) object;
            boolean simpleCheck = roboTaxiMenu.equals(sharedAVMenu.getRoboTaxiMenu());
            List<SharedCourse> otherMenu = sharedAVMenu.roboTaxiMenu;
            // TODO LUXURY there is an easier way to check for equality

            if (otherMenu.size() == roboTaxiMenu.size()) {
                for (int i = 0; i < roboTaxiMenu.size(); i++)
                    if (!roboTaxiMenu.get(i).equals(sharedAVMenu.roboTaxiMenu.get(i))) {
                        GlobalAssert.that(!simpleCheck);
                        return false;
                    }
                GlobalAssert.that(simpleCheck);
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        // TODO Lukas SHARED not yet implemented
        throw new RuntimeException();
    }

}
