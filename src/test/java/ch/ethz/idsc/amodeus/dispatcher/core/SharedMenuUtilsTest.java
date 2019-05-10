/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Arrays;
import java.util.List;

import ch.ethz.idsc.amodeus.dispatcher.shared.Compatibility;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseMove;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMenu;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMenuCheck;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMenuUtils;
import junit.framework.TestCase;

public class SharedMenuUtilsTest extends TestCase {
    public void testSimple() {
        ArtificialScenarioCreator artificialScenarioCreator = new ArtificialScenarioCreator();
        SharedCourse pickupCourse1 = SharedCourse.pickupCourse(artificialScenarioCreator.avRequest1);
        assertEquals(pickupCourse1.getLink(), artificialScenarioCreator.avRequest1.getFromLink());
        assertEquals(pickupCourse1.getMealType(), SharedMealType.PICKUP);
        assertEquals(pickupCourse1.getAvRequest(), artificialScenarioCreator.avRequest1);
        assertEquals(pickupCourse1.getCourseId(), artificialScenarioCreator.avRequest1.getId().toString());

        SharedCourse dropoffCourse1 = SharedCourse.dropoffCourse(artificialScenarioCreator.avRequest1);
        assertEquals(dropoffCourse1.getLink(), artificialScenarioCreator.avRequest1.getToLink());
        assertEquals(dropoffCourse1.getMealType(), SharedMealType.DROPOFF);
        assertEquals(dropoffCourse1.getAvRequest(), artificialScenarioCreator.avRequest1);
        assertEquals(dropoffCourse1.getCourseId(), artificialScenarioCreator.avRequest1.getId().toString());

        SharedCourse pickupCourse2 = SharedCourse.pickupCourse(artificialScenarioCreator.avRequest2);
        SharedCourse dropoffCourse2 = SharedCourse.dropoffCourse(artificialScenarioCreator.avRequest2);

        List<SharedCourse> list1 = Arrays.asList(pickupCourse1, dropoffCourse1, pickupCourse2, dropoffCourse2);
        SharedMenu menu1 = SharedMenu.of(list1);

        assertEquals(SharedMenuUtils.getStarterCourse(menu1).get(), pickupCourse1);
        assertTrue(Compatibility.of(menu1.getCourseList()).forCapacity(1));
        assertTrue(SharedMenuCheck.eachPickupAfterDropoff(menu1.getCourseList()));
        assertTrue(SharedMenuCheck.coursesAppearOnce(menu1.getCourseList()));

        SharedMenu menu2 = SharedCourseMove.moveAVCourseToNext(menu1, dropoffCourse1);
        assertTrue(SharedMenuCheck.containSameCourses(menu1, menu2));
        SharedMenu menu2Check = SharedMenu.of(Arrays.asList(pickupCourse1, pickupCourse2, dropoffCourse1, dropoffCourse2));
        assertFalse(menu1.getCourseList().equals(menu2.getCourseList()));
        assertTrue(menu2.getCourseList().equals(menu2Check.getCourseList()));

        List<SharedCourse> listInvalid = Arrays.asList(dropoffCourse1, pickupCourse1);

        try {
            SharedMenu.of(listInvalid);
            fail();
        } catch (Exception e) {
            // ---
        }

        System.out.println("");
        System.out.println("Shared Menu Test Done");
    }

}
