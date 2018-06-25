package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/** @author onicoloLsieber
 *         Object containing list of pickup and dropoff planned for an AV. */
public class SharedAVMenu {

    private final List<SharedAVCourse> roboTaxiMenu = new ArrayList<>();

    public void addAVCourseWithIndex(SharedAVCourse avCourse, int courseIndex) {
        GlobalAssert.that(courseIndex >= 0 && courseIndex <= roboTaxiMenu.size());
        roboTaxiMenu.add(courseIndex, avCourse);
    }

    public void addAVCourseAsaStarter(SharedAVCourse avCourse) {
        roboTaxiMenu.add(0, avCourse);
    }

    public void addAVCourseAsaDessert(SharedAVCourse avCourse) {
        roboTaxiMenu.add(roboTaxiMenu.size(), avCourse);
    }

    public void removeAVCourse(int courseIndex) {
        roboTaxiMenu.remove(courseIndex);
    }

    public int getIndexOf(SharedAVCourse course) {
        return roboTaxiMenu.indexOf(course);
    }

    public List<SharedAVCourse> getCurrentSharedAVMenu() {
        return (List<SharedAVCourse>) Collections.unmodifiableCollection(roboTaxiMenu);
    }

    public SharedAVCourse getSharedAVStarter() {
        return roboTaxiMenu.isEmpty() ? null : roboTaxiMenu.get(0);
    }

    public void clearSharedAVMenu() {
        roboTaxiMenu.clear();
    }
}
