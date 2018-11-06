package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Optional;

import org.junit.Test;
import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import junit.framework.TestCase;

public class RoboTaxiDiversionTest extends TestCase {
    @Test
    public void testStayTask() {
        ArtificialScenarioCreator s = new ArtificialScenarioCreator();

        Optional<Link> entry = SharedRoboTaxiDiversionHelper.getToLink(s.roboTaxi1, 0.0);
        assertFalse(entry.isPresent());

        s.roboTaxi1.addAVRequestToMenu(s.avRequest1);
        Optional<Link> entry2 = SharedRoboTaxiDiversionHelper.getToLink(s.roboTaxi1, 0.0);
        assertTrue(entry2.isPresent());
        assertTrue(entry2.get().equals(s.avRequest1.getFromLink()));
        assertEquals(s.roboTaxi1.getStatus(), RoboTaxiStatus.DRIVETOCUSTOMER);
        s.roboTaxi1.addRedirectCourseToMenu(SharedCourse.redirectCourse(s.linkDepotOut, "redirect0"));

        s.roboTaxi1.addRedirectCourseToMenuAtBegining(SharedCourse.redirectCourse(s.linkDepotIn, "redirect1"));
        Optional<Link> entry3 = SharedRoboTaxiDiversionHelper.getToLink(s.roboTaxi1, 0.0);
        assertTrue(entry3.isPresent());
        assertTrue(entry3.get().equals(s.linkDepotIn));
        assertEquals(s.roboTaxi1.getStatus(), RoboTaxiStatus.DRIVETOCUSTOMER);
        s.roboTaxi1.removeAVRequestFromMenu(s.avRequest1);
        assertEquals(s.roboTaxi1.getStatus(), RoboTaxiStatus.REBALANCEDRIVE);

        
        s.roboTaxi1.addAVRequestToMenu(s.avRequest2);
        s.roboTaxi1.moveAVCourseToPrev(SharedCourse.pickupCourse(s.avRequest2));
        s.roboTaxi1.moveAVCourseToPrev(SharedCourse.pickupCourse(s.avRequest2));
        s.roboTaxi1.moveAVCourseToPrev(SharedCourse.pickupCourse(s.avRequest2));
        s.roboTaxi1.moveAVCourseToPrev(SharedCourse.pickupCourse(s.avRequest2));

        Optional<Link> entry4 = SharedRoboTaxiDiversionHelper.getToLink(s.roboTaxi1, 0.0);
        assertTrue(entry4.isPresent());
        assertTrue(entry4.get().equals(s.avRequest2.getFromLink()));
        assertEquals(s.roboTaxi1.getStatus(), RoboTaxiStatus.DRIVETOCUSTOMER);

        SharedCourse tocurrLoCourse = SharedCourse.redirectCourse(s.roboTaxi1.getDivertableLocation(), "redirecttoCurrentLoc");
        s.roboTaxi1.addRedirectCourseToMenuAtBegining(tocurrLoCourse);
        assertTrue(RoboTaxiUtils.getStarterCourse(s.roboTaxi1).get().equals(tocurrLoCourse));
        Optional<Link> entrycurr = SharedRoboTaxiDiversionHelper.getToLink(s.roboTaxi1, 0.0);
        assertFalse(RoboTaxiUtils.getStarterCourse(s.roboTaxi1).get().equals(tocurrLoCourse));
        assertTrue(entrycurr.isPresent());
        assertTrue(entrycurr.get().equals(entry4.get()));

        // TODO here we need many more Tests to make sure we cover all cases which are possible!
       
        System.out.println("Robo Taxi diversion Test done");

    }
}
