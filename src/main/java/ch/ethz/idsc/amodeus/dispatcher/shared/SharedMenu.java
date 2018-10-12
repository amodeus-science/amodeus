/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/** Top level class in SharedRoboTaxi functionality, a {@link SharedMenu} is
 * composed of {@link SharedCourse}s which internally have a {@link SharedMealType}s
 * A Menu contains a list of shared Courses (pickup, dropoff, rebalance) planned
 * for an RoboTaxi. The List of shared Couses can not be null. It is empty instead.
 * 
 *  Important: the List of Shared Courses is final and not modifiable.
 *  Thus only a View on the current menu can be received and changes are not permitted*/
public class SharedMenu {
    /** Unmodifiable List of Shared Courses */
    private final List<SharedCourse> roboTaxiMenu;

    /** Creates a Shared Menu which is consistent in itself (e.g. no coureses appear twice, for each request it is secured that the dropoff happens after the pickup
     * 
     * @param list of {@link SharedCourse}
     * @return */
    public static SharedMenu of(List<SharedCourse> list) {
        GlobalAssert.that(SharedCourseListUtils.consistencyCheck(list));
        return new SharedMenu(list);
    }

    /** Creates an empty Menu. It has no next course. It can be used for example for idling {@link RoboTaxi}
     * 
     * @return {@link SharedMenu} with no courses planed. */
    public static SharedMenu empty() {
        return new SharedMenu(null);
    }

    private SharedMenu(List<SharedCourse> list) {
        roboTaxiMenu = Collections.unmodifiableList((Objects.isNull(list)) ? new ArrayList<>() : list);
    }

    /**
     * 
     * @return an unmodifiable view of the menu*/
    public List<SharedCourse> getRoboTaxiMenu() {
        return roboTaxiMenu;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof SharedMenu) {
            SharedMenu sharedAVMenu = (SharedMenu) object;
            boolean simpleCheck = roboTaxiMenu.equals(sharedAVMenu.getRoboTaxiMenu());
            List<SharedCourse> otherMenu = sharedAVMenu.roboTaxiMenu;
            // TODO Lukas Remove soon the part below and use the result of above.
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
        // IF this wants to be used please implement
        throw new RuntimeException();
    }

}
