/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Arrays;
import java.util.List;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMenu;
import junit.framework.TestCase;

public class SharedMenuTest extends TestCase {
    public void testSimple() {
        ArtificialScenarioCreator s = new ArtificialScenarioCreator();
        SharedCourse pickupCourse1 = SharedCourse.pickupCourse(s.avRequest1);
        assertEquals(pickupCourse1.getLink(), s.avRequest1.getFromLink());
        assertEquals(pickupCourse1.getMealType(), SharedMealType.PICKUP);
        assertEquals(pickupCourse1.getAvRequest(), s.avRequest1);
        assertEquals(pickupCourse1.getCourseId(), s.avRequest1.getId().toString());

        SharedCourse dropoffCourse1 = SharedCourse.dropoffCourse(s.avRequest1);
        assertEquals(dropoffCourse1.getLink(), s.avRequest1.getToLink());
        assertEquals(dropoffCourse1.getMealType(), SharedMealType.DROPOFF);
        assertEquals(dropoffCourse1.getAvRequest(), s.avRequest1);
        assertEquals(dropoffCourse1.getCourseId(), s.avRequest1.getId().toString());

        SharedCourse pickupCourse2 = SharedCourse.pickupCourse(s.avRequest2);
        SharedCourse dropoffCourse2 = SharedCourse.dropoffCourse(s.avRequest2);

        List<SharedCourse> list1 = Arrays.asList(pickupCourse1, dropoffCourse1, pickupCourse2, dropoffCourse2);
        SharedMenu menu1 = SharedMenu.of(list1);

        assertEquals(SharedMenuUtils.getStarterCourse(menu1).get(), pickupCourse1);
        assertTrue(SharedMenuUtils.checkMenuDoesNotPlanToPickUpMoreCustomersThanCapacity(menu1, 1));
        assertTrue(SharedMenuUtils.checkNoPickupAfterDropoffOfSameRequest(menu1));
        assertTrue(SharedMenuUtils.checkAllCoursesAppearOnlyOnce(menu1));

        SharedMenu menu2 = SharedMenuUtils.moveAVCourseToNext(menu1, dropoffCourse1);
        assertTrue(SharedMenuUtils.containSameCourses(menu1, menu2));
        SharedMenu menu2Check = SharedMenu.of(Arrays.asList(pickupCourse1, pickupCourse2, dropoffCourse1, dropoffCourse2));
        assertFalse(menu1.getRoboTaxiMenu().equals(menu2.getRoboTaxiMenu()));
        assertTrue(menu2.getRoboTaxiMenu().equals(menu2Check.getRoboTaxiMenu()));

        List<SharedCourse> listInvalid = Arrays.asList(dropoffCourse1, pickupCourse1);

        try {
            SharedMenu.of(listInvalid);
            assertTrue(false);
        } catch (Exception e) {
            // ---
        }

        System.out.println("");
        System.out.println("Shared Menu Test Done");
    }

}
