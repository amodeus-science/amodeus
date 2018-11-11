package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.DoubleStream;

import org.junit.Test;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelDataImpl;
import org.matsim.contrib.dvrp.util.LinkTimePair;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import junit.framework.TestCase;

public class RoboTaxiDiversionTest extends TestCase {
    @Test
    public void testStayTask() {
        ArtificialScenarioCreator s = new ArtificialScenarioCreator();

        Link divertableLink = s.linkDepotOut;
        RoboTaxi roboTaxi = StaticRoboTaxiCreator.createStayingRoboTaxi(divertableLink, null);

        // *****************************
        // Case 1 Staying vehicle where stayTask Link equals Divertable Link;

        // Casse 1a) No Course present
        assertFalse(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0).isPresent()); // case empty menu

        // Case 1b) Next course is on same link
        roboTaxi.addAVRequestToMenu(s.avRequestDepotOut);
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0).isPresent()); // case pickup course
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0).get().equals(s.avRequestDepotOut.getFromLink())); // case pickup course

        roboTaxi.pickupNewCustomerOnBoard();
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0).isPresent()); // case dropoff course
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0).get().equals(s.avRequestDepotOut.getToLink())); // case pickup course

        StaticRoboTaxiCreator.updateRoboTaxiMenuTo(roboTaxi, Arrays.asList(SharedCourse.redirectCourse(s.linkDepotOut, "depotRed")));
        try {
            Optional<Link> link = SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0);
            assertTrue(false);
        } catch (Exception e) {
        }
        StaticRoboTaxiCreator.cleanRTMenu(roboTaxi);

        // Case 1c) Next course is on different link
        assertFalse(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0).isPresent()); // case empty menu
        roboTaxi.addAVRequestToMenu(s.avRequest1);
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0).isPresent()); // case pickup course
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0).get().equals(s.avRequest1.getFromLink())); // case pickup course

        StaticRoboTaxiCreator.updateRoboTaxiMenuTo(roboTaxi, Arrays.asList(SharedCourse.dropoffCourse(s.avRequest1)));
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0).isPresent()); // case dropoff course
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0).get().equals(s.avRequest1.getToLink())); // case pickup course

        StaticRoboTaxiCreator.updateRoboTaxiMenuTo(roboTaxi, Arrays.asList(SharedCourse.redirectCourse(s.linkUp, "linkUpRedirection")));
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0).isPresent()); // case redirect course
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0).get().equals(s.linkUp)); // case pickup course
        StaticRoboTaxiCreator.cleanRTMenu(roboTaxi);

        // *****************************
        // Case 2 Staying vehicle where stayTask Link NOT (!!!) equals Divertable Link;
        roboTaxi = StaticRoboTaxiCreator.createStayingRoboTaxi(divertableLink, s.linkDepotIn);
        // Case 2a) No Course present
        assertFalse(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0).isPresent()); // case empty menu

        // Case 2b) Next course is on same link as Divertable Location
        roboTaxi.addAVRequestToMenu(s.avRequestDepotOut);
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0).isPresent()); // case pickup course
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0).get().equals(s.linkDepotOut)); // case pickup course

        roboTaxi.pickupNewCustomerOnBoard();
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0).isPresent()); // case dropoff course
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0).get().equals(s.linkDepotOut)); // case pickup course

        StaticRoboTaxiCreator.updateRoboTaxiMenuTo(roboTaxi, Arrays.asList(SharedCourse.redirectCourse(s.linkDepotOut, "depotRed")));
        // FIXME the next test should be possible This is a diversion from the stay task to the divertable location
        // assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0).get().equals(s.linkDepotOut)); // case pickup course
        try {
            SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0);
            assertTrue(false);
        } catch (Exception e) {
        }
        StaticRoboTaxiCreator.cleanRTMenu(roboTaxi);

        // Case 2c) Next course is on same link as Stay Task Location
        roboTaxi.addAVRequestToMenu(s.avRequestDepotIn);
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0).isPresent()); // case pickup course
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0).get().equals(s.linkDepotIn)); // case pickup course

        roboTaxi.setDivertableLinkTime(new LinkTimePair(s.linkDepotIn, 0.0));
        roboTaxi.pickupNewCustomerOnBoard();
        roboTaxi.setDivertableLinkTime(new LinkTimePair(s.linkDepotOut, 0.0));

        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0).isPresent()); // case dropoff course
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0).get().equals(s.linkDepotIn)); // case pickup course

        StaticRoboTaxiCreator.updateRoboTaxiMenuTo(roboTaxi, Arrays.asList(SharedCourse.redirectCourse(s.linkDepotIn, "depotRed")));
        // FIXME This should be empoty as it is a redirection to the currewnt location so no diversion is required.
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0).isPresent()); // case dropoff course
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0).get().equals(s.linkDepotIn)); // case pickup course
        StaticRoboTaxiCreator.cleanRTMenu(roboTaxi);

        // Case 2d) Next course is on different link
        assertFalse(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0).isPresent()); // case empty menu
        roboTaxi.addAVRequestToMenu(s.avRequest1);
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0).isPresent()); // case pickup course
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0).get().equals(s.avRequest1.getFromLink())); // case pickup course

        StaticRoboTaxiCreator.updateRoboTaxiMenuTo(roboTaxi, Arrays.asList(SharedCourse.dropoffCourse(s.avRequest1)));
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0).isPresent()); // case dropoff course
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0).get().equals(s.avRequest1.getToLink())); // case pickup course

        StaticRoboTaxiCreator.updateRoboTaxiMenuTo(roboTaxi, Arrays.asList(SharedCourse.redirectCourse(s.linkUp, "linkUpRedirection")));
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0).isPresent()); // case redirect course
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, 0.0).get().equals(s.linkUp)); // case pickup course
        StaticRoboTaxiCreator.cleanRTMenu(roboTaxi);

    }

    @Test
    public void testPickupTask() {
        ArtificialScenarioCreator s = new ArtificialScenarioCreator();

        Link divertableLink = s.linkDepotOut;
        RoboTaxi roboTaxi = StaticRoboTaxiCreator.createPickUpRoboTaxi(divertableLink);

        // ***************************************************
        // Case 1 Task ends in the future. Now is 5.0
        double now = 5.0;

        // Case 1a) No course present
        try { // Impossible because if a pickup task is going on there has always to be a dropoff course
            SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now);
            assertTrue(false);
        } catch (Exception e) {
        }

        // Case 1b) Next course is on same link
        roboTaxi.addAVRequestToMenu(s.avRequestDepotOut);
        assertFalse(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent()); // case pickup course

        roboTaxi.pickupNewCustomerOnBoard();
        assertFalse(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent()); // case dropoff course

        StaticRoboTaxiCreator.updateRoboTaxiMenuTo(roboTaxi, Arrays.asList(SharedCourse.redirectCourse(s.linkDepotOut, "depotRed")));
        try {
            SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now);
            assertTrue(false);
        } catch (Exception e) {
        }
        StaticRoboTaxiCreator.cleanRTMenu(roboTaxi);

        // Case 1c) Next course is on other link
        roboTaxi.addAVRequestToMenu(s.avRequest1);
        assertFalse(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent()); // case pickup course

        StaticRoboTaxiCreator.updateRoboTaxiMenuTo(roboTaxi, Arrays.asList(SharedCourse.dropoffCourse(s.avRequest1)));
        assertFalse(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent()); // case dropoff course

        StaticRoboTaxiCreator.updateRoboTaxiMenuTo(roboTaxi, Arrays.asList(SharedCourse.redirectCourse(s.linkUp, "RedirectionUp")));
        assertFalse(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent()); // case dropoff course
        StaticRoboTaxiCreator.cleanRTMenu(roboTaxi);

        // ***************************************************
        // Case 2 Task ends now. Now is equal to task end
        now = StaticRoboTaxiCreator.TASK_END;

        // Case 2a) No course present
        try { // Impossible because if a pickup task is going on there has always to be a dropoff course
            SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now);
            assertTrue(false);
        } catch (Exception e) {
        }

        // Case 2b) Next course is on same link
        roboTaxi.addAVRequestToMenu(s.avRequestDepotOut);
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent()); // case pickup course
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).get().equals(s.linkDepotOut)); // case pickup course

        roboTaxi.pickupNewCustomerOnBoard();
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent()); // case dropoff course
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).get().equals(s.linkDepotOut)); // case dropoff course

        StaticRoboTaxiCreator.updateRoboTaxiMenuTo(roboTaxi, Arrays.asList(SharedCourse.redirectCourse(s.linkDepotOut, "depotRed")));
        try {
            SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now);
            assertTrue(false);
        } catch (Exception e) {
        }
        StaticRoboTaxiCreator.cleanRTMenu(roboTaxi);

        // Case 2c) Next course is on other link
        roboTaxi.addAVRequestToMenu(s.avRequest1);
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent()); // case pickup course
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).get().equals(s.linkUp)); // case pickup course

        StaticRoboTaxiCreator.updateRoboTaxiMenuTo(roboTaxi, Arrays.asList(SharedCourse.dropoffCourse(s.avRequest1)));
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent()); // case dropoff course
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).get().equals(s.linkDown)); // case pickup course

        StaticRoboTaxiCreator.updateRoboTaxiMenuTo(roboTaxi, Arrays.asList(SharedCourse.redirectCourse(s.linkUp, "RedirectionUp")));
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent()); // case dropoff course
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).get().equals(s.linkUp)); // case pickup course
        StaticRoboTaxiCreator.cleanRTMenu(roboTaxi);
    }

    @Test
    public void testDropoffTask() {
        ArtificialScenarioCreator s = new ArtificialScenarioCreator();

        Link divertableLink = s.linkDepotOut;
        RoboTaxi roboTaxi = StaticRoboTaxiCreator.createDropoffRoboTaxi(divertableLink);

        // ***************************************************
        // Case 1 Task ends in the future. Now is 5.0
        double now = 5.0;

        // Case 1a) No course present
        try { // Impossible because if a pickup task is going on there has always to be a dropoff course
            SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now);
            assertTrue(false);
        } catch (Exception e) {
        }

        // Case 1b) Next course is on same link
        roboTaxi.addAVRequestToMenu(s.avRequestDepotOut);
        assertFalse(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent()); // case pickup course

        roboTaxi.pickupNewCustomerOnBoard();
        assertFalse(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent()); // case dropoff course

        StaticRoboTaxiCreator.updateRoboTaxiMenuTo(roboTaxi, Arrays.asList(SharedCourse.redirectCourse(s.linkDepotOut, "depotRed")));
        try {
            SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now);
            assertTrue(false);
        } catch (Exception e) {
        }
        StaticRoboTaxiCreator.cleanRTMenu(roboTaxi);

        // Case 1c) Next course is on other link
        roboTaxi.addAVRequestToMenu(s.avRequest1);
        assertFalse(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent()); // case pickup course

        StaticRoboTaxiCreator.updateRoboTaxiMenuTo(roboTaxi, Arrays.asList(SharedCourse.dropoffCourse(s.avRequest1)));
        assertFalse(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent()); // case dropoff course

        StaticRoboTaxiCreator.updateRoboTaxiMenuTo(roboTaxi, Arrays.asList(SharedCourse.redirectCourse(s.linkUp, "RedirectionUp")));
        assertFalse(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent()); // case dropoff course
        StaticRoboTaxiCreator.cleanRTMenu(roboTaxi);

        // ***************************************************
        // Case 2 Task ends now. Now is equal to task end
        now = StaticRoboTaxiCreator.TASK_END;

        // Case 2a) No course present
        assertFalse(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent());

        // Case 2b) Next course is on same link
        roboTaxi.addAVRequestToMenu(s.avRequestDepotOut);
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent()); // case pickup course
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).get().equals(s.linkDepotOut)); // case pickup course

        roboTaxi.pickupNewCustomerOnBoard();
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent()); // case dropoff course
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).get().equals(s.linkDepotOut)); // case dropoff course

        StaticRoboTaxiCreator.updateRoboTaxiMenuTo(roboTaxi, Arrays.asList(SharedCourse.redirectCourse(s.linkDepotOut, "depotRed")));
        try {
            SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now);
            assertTrue(false);
        } catch (Exception e) {
        }
        StaticRoboTaxiCreator.cleanRTMenu(roboTaxi);

        // Case 2c) Next course is on other link
        roboTaxi.addAVRequestToMenu(s.avRequest1);
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent()); // case pickup course
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).get().equals(s.linkUp)); // case pickup course

        StaticRoboTaxiCreator.updateRoboTaxiMenuTo(roboTaxi, Arrays.asList(SharedCourse.dropoffCourse(s.avRequest1)));
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent()); // case dropoff course
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).get().equals(s.linkDown)); // case pickup course

        StaticRoboTaxiCreator.updateRoboTaxiMenuTo(roboTaxi, Arrays.asList(SharedCourse.redirectCourse(s.linkUp, "RedirectionUp")));
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent()); // case dropoff course
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).get().equals(s.linkUp)); // case pickup course
        StaticRoboTaxiCreator.cleanRTMenu(roboTaxi);
    }

    
    
    @Test
    public void testDriveTask() {
        ArtificialScenarioCreator s = new ArtificialScenarioCreator();

        Link[] links = { s.linkDepotOut, s.linkLeft, s.linkUp };
        double[] linkTTs = { 5.0, 5.0, 5.0 };
        double travelTIme = DoubleStream.of(linkTTs).sum();

        VrpPathWithTravelData vrpPathWithTravelData = new VrpPathWithTravelDataImpl(0.0, travelTIme, links, linkTTs);
        RoboTaxi roboTaxi = StaticRoboTaxiCreator.createDriveRoboTaxi(vrpPathWithTravelData);

        // ***************************************************
        // Case 1 Task ends in the future. Now is 5.0
        double now = 5.0;

        // Case 1a) No course present
        {
            Optional<Link> link = SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now);
            assertTrue(link.isPresent());
            assertTrue(link.get().equals(s.linkDepotOut));
        }

        // Case 1b) Next course is on same link as current position
        roboTaxi.addAVRequestToMenu(s.avRequestDepotOut);
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent());
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).get().equals(s.linkDepotOut));

        roboTaxi.pickupNewCustomerOnBoard();
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent());
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).get().equals(s.linkDepotOut));

        StaticRoboTaxiCreator.updateRoboTaxiMenuTo(roboTaxi, Arrays.asList(SharedCourse.redirectCourse(s.linkDepotOut, "depotRed")));
        try {
            SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now);
            assertTrue(false);
        } catch (Exception e) {
        }
        StaticRoboTaxiCreator.cleanRTMenu(roboTaxi);

        // Case 1c) Next course is on same course as destination link
        roboTaxi.addAVRequestToMenu(s.avRequest1);
        assertFalse(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent()); // case pickup course

        StaticRoboTaxiCreator.updateRoboTaxiMenuTo(roboTaxi, Arrays.asList(SharedCourse.dropoffCourse(s.avRequest3)));
        assertFalse(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent()); // case dropoff course

        StaticRoboTaxiCreator.updateRoboTaxiMenuTo(roboTaxi, Arrays.asList(SharedCourse.redirectCourse(s.linkUp, "RedirectionUp")));
        assertFalse(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent()); // case dropoff course
        StaticRoboTaxiCreator.cleanRTMenu(roboTaxi);

        // Case 1d) Next course is on different link than divertable and destination link
        roboTaxi.addAVRequestToMenu(s.avRequest4);
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent());
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).get().equals(s.linkRight));

        StaticRoboTaxiCreator.updateRoboTaxiMenuTo(roboTaxi, Arrays.asList(SharedCourse.dropoffCourse(s.avRequest4)));
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent());
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).get().equals(s.linkDown));

        StaticRoboTaxiCreator.updateRoboTaxiMenuTo(roboTaxi, Arrays.asList(SharedCourse.redirectCourse(s.linkDepotIn, "depotRedIn")));
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent());
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).get().equals(s.linkDepotIn));
        StaticRoboTaxiCreator.cleanRTMenu(roboTaxi);

        // ***************************************************
        // Case 2 Task ends now. Now is equal to task end but we are not yet on the last link of the path
        now = travelTIme;

        // Case 2a) No course present
        { // Divertable Location is not on last link of path
            Optional<Link> link = SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now);
            assertTrue(link.isPresent());
            assertTrue(link.get().equals(s.linkDepotOut));
        }

        { // Divertable Location is on last link of path
            roboTaxi.setDivertableLinkTime(new LinkTimePair(s.linkUp, now));
            Optional<Link> link = SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now);
            assertTrue(link.isPresent());
            assertTrue(link.get().equals(s.linkDepotOut));
            roboTaxi.setDivertableLinkTime(new LinkTimePair(s.linkDepotOut, now));
        }

        // Case 2b) Next course is on same link as current position
        roboTaxi.addAVRequestToMenu(s.avRequestDepotOut);
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent());
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).get().equals(s.linkDepotOut));

        roboTaxi.pickupNewCustomerOnBoard();
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent());
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).get().equals(s.linkDepotOut));

        StaticRoboTaxiCreator.updateRoboTaxiMenuTo(roboTaxi, Arrays.asList(SharedCourse.redirectCourse(s.linkDepotOut, "depotRed")));
        try {
            SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now);
            assertTrue(false);
        } catch (Exception e) {
        }
        StaticRoboTaxiCreator.cleanRTMenu(roboTaxi);

        // Case 2c) Next course is on same course as destination link
        roboTaxi.addAVRequestToMenu(s.avRequest1);
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent()); // case pickup course
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).get().equals(s.linkUp));

        StaticRoboTaxiCreator.updateRoboTaxiMenuTo(roboTaxi, Arrays.asList(SharedCourse.dropoffCourse(s.avRequest3)));
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent()); // case dropoff course
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).get().equals(s.linkUp));

        StaticRoboTaxiCreator.updateRoboTaxiMenuTo(roboTaxi, Arrays.asList(SharedCourse.redirectCourse(s.linkUp, "RedirectionUp")));
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent()); // case dropoff course
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).get().equals(s.linkUp));
        StaticRoboTaxiCreator.cleanRTMenu(roboTaxi);

        // Case 2c) Next course is on different link than divertable and end link
        roboTaxi.addAVRequestToMenu(s.avRequest4);
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent());
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).get().equals(s.linkRight));

        StaticRoboTaxiCreator.updateRoboTaxiMenuTo(roboTaxi, Arrays.asList(SharedCourse.dropoffCourse(s.avRequest4)));
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent());
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).get().equals(s.linkDown));

        StaticRoboTaxiCreator.updateRoboTaxiMenuTo(roboTaxi, Arrays.asList(SharedCourse.redirectCourse(s.linkDepotIn, "depotRedIn")));
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).isPresent());
        assertTrue(SharedRoboTaxiDiversionHelper.getToLink(roboTaxi, now).get().equals(s.linkDepotIn));
        StaticRoboTaxiCreator.cleanRTMenu(roboTaxi);
    }

}
