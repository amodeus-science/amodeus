/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.matsim.contrib.dvrp.util.LinkTimePair;

import ch.ethz.idsc.amodeus.dispatcher.shared.OnboardRequests;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseAccess;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseUtil;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import junit.framework.TestCase;

public class RoboTaxiUtilsTest extends TestCase {
    public void testSimple() {
        ArtificialScenarioCreator s = new ArtificialScenarioCreator();

        assertTrue(SharedRoboTaxiUtils.calculateStatusFromMenu(s.roboTaxi1).equals(RoboTaxiStatus.STAY));
        s.roboTaxi1.addRedirectCourseToMenu(SharedCourse.redirectCourse(s.linkUp, "redirect0"));
        s.roboTaxi1.setDivertableLinkTime(new LinkTimePair(s.linkUp, 1.0));
        assertTrue(SharedRoboTaxiUtils.calculateStatusFromMenu(s.roboTaxi1).equals(RoboTaxiStatus.REBALANCEDRIVE));
        s.roboTaxi1.finishRedirection();

        s.roboTaxi1.addAVRequestToMenu(s.avRequest1);
        try { // A request can only be added once to a robo Taxi
            s.roboTaxi1.addAVRequestToMenu(s.avRequest1);
            assertTrue(false);
        } catch (Exception e) {
            // ---
        }
        try { // The PickupCourse can not be after the dropoff
            s.roboTaxi1.moveAVCourseToNext(SharedCourse.pickupCourse(s.avRequest1));
            assertTrue(false);
        } catch (Exception e) {
            // ---
        }
        try { // it can not be possible to dropoff a customer as there is none on board
            s.roboTaxi1.dropOffCustomer();
            assertTrue(false);
        } catch (Exception e) {
            // ---
        }
        try { // can not move as long as the request is not in the menu
            s.roboTaxi1.moveAVCourseToPrev(SharedCourse.pickupCourse(s.avRequest2));
            assertTrue(false);
        } catch (Exception e) {
            // ---
        }
        Optional<SharedCourse> secondcourse1 = SharedCourseAccess.getSecond(s.roboTaxi1.getUnmodifiableViewOfCourses());
        assertTrue(secondcourse1.isPresent());
        assertTrue(secondcourse1.get().equals(SharedCourse.dropoffCourse(s.avRequest1)));
        assertTrue(SharedRoboTaxiUtils.calculateStatusFromMenu(s.roboTaxi1).equals(RoboTaxiStatus.DRIVETOCUSTOMER));
        s.roboTaxi1.cleanAndAbandonMenu();
        assertEquals(s.roboTaxi1.getUnmodifiableViewOfCourses(), new ArrayList<>());

        s.roboTaxi1.addAVRequestToMenu(s.avRequest1);
        s.roboTaxi1.addAVRequestToMenu(s.avRequest2);
        s.roboTaxi1.moveAVCourseToNext(SharedCourse.dropoffCourse(s.avRequest1));
        List<SharedCourse> courses = s.roboTaxi1.getUnmodifiableViewOfCourses();
        try {// You should only be able to view this list
            courses.add(0, SharedCourse.pickupCourse(s.avRequest3));
            assertTrue(false);
        } catch (Exception e) {
            // ---
        }
        List<SharedCourse> modifiableList = SharedCourseUtil.copy(courses);
        modifiableList.add(0, SharedCourse.pickupCourse(s.avRequest3));
        try { // an update of the menu with a list which does not contain the same courses is invalid
            s.roboTaxi1.updateMenu(modifiableList);
            assertTrue(false);
        } catch (Exception e) {
            // ---
        }
        modifiableList.add(SharedCourse.dropoffCourse(s.avRequest3));
        s.roboTaxi1.addAVRequestToMenu(s.avRequest3);
        s.roboTaxi1.updateMenu(modifiableList);
        s.roboTaxi1.addAVRequestToMenu(s.avRequest4);
        s.roboTaxi1.moveAVCourseToPrev(SharedCourse.pickupCourse(s.avRequest4));
        s.roboTaxi1.moveAVCourseToPrev(SharedCourse.pickupCourse(s.avRequest4));
        try { // It should not be Possible to have a menu which plans to pick up more customers than capacity
            s.roboTaxi1.moveAVCourseToPrev(SharedCourse.pickupCourse(s.avRequest4));
            assertTrue(false);
        } catch (Exception e) {
            // ---
        }

        int numcourses = s.roboTaxi1.getUnmodifiableViewOfCourses().size();
        try { // IT is not possible to pick up if the robotaxi is not on the right link
            s.roboTaxi1.pickupNewCustomerOnBoard();
            assertTrue(false);
        } catch (Exception e) {
            // ---
        }
        s.roboTaxi1.setDivertableLinkTime(new LinkTimePair(s.linkRight, 1.0));
        s.roboTaxi1.pickupNewCustomerOnBoard();
        assertTrue(SharedRoboTaxiUtils.calculateStatusFromMenu(s.roboTaxi1).equals(RoboTaxiStatus.DRIVEWITHCUSTOMER));

        assertEquals(OnboardRequests.getNumberOnBoardRequests(s.roboTaxi1), 1);
        assertEquals(s.roboTaxi1.getUnmodifiableViewOfCourses().size(), numcourses - 1);
        try { // It should not be Possible to have a menu which plans to pick up more customers than capacity
            s.roboTaxi1.moveAVCourseToPrev(SharedCourse.pickupCourse(s.avRequest4));
            assertTrue(false);
        } catch (Exception e) {
            // ---
        }
        try { // The dropoff should only be allowed if the robotaxi has the customer on board and its the next course
            s.roboTaxi1.dropOffCustomer();
            assertTrue(false);
        } catch (Exception e) {
            // ---
        }
        try { // because the next customer was picked up it is not allowed anymore to abort this request
            s.roboTaxi1.removeAVRequestFromMenu(s.avRequest3);
            assertTrue(false);
        } catch (Exception e) {
            // ---
        }

        s.roboTaxi1.addRedirectCourseToMenu(SharedCourse.redirectCourse(s.linkUp, "redirect1"));
        List<SharedCourse> newMenuCourses = SharedCourseUtil.copy(s.roboTaxi1.getUnmodifiableViewOfCourses());
        newMenuCourses.add(0, SharedCourse.redirectCourse(s.linkUp, "redirect1"));
        newMenuCourses.remove(newMenuCourses.size() - 1);
        s.roboTaxi1.updateMenu(newMenuCourses);

        try { // dropoff not possible because next course id redirect
            s.roboTaxi1.dropOffCustomer();
            assertTrue(false);
        } catch (Exception e) {
            // ---
        }
        try { // pickup not possible because next course is redirect
            s.roboTaxi1.pickupNewCustomerOnBoard();
            assertTrue(false);
        } catch (Exception e) {
            // ---
        }
        assertEquals(OnboardRequests.getNumberOnBoardRequests(s.roboTaxi1), 1);
        assertTrue(OnboardRequests.canPickupNewCustomer(s.roboTaxi1));

        s.roboTaxi1.setDivertableLinkTime(new LinkTimePair(s.linkUp, 1.0));
        assertTrue(SharedRoboTaxiUtils.isNextCourseOfType(s.roboTaxi1, SharedMealType.REDIRECT));
        assertTrue(SharedRoboTaxiUtils.calculateStatusFromMenu(s.roboTaxi1).equals(RoboTaxiStatus.DRIVEWITHCUSTOMER));

        s.roboTaxi1.finishRedirection();
        s.roboTaxi1.setDivertableLinkTime(new LinkTimePair(s.avRequest1.getFromLink(), 1.0));
        s.roboTaxi1.pickupNewCustomerOnBoard();
        assertEquals(OnboardRequests.getNumberOnBoardRequests(s.roboTaxi1), 2);

        assertEquals(OnboardRequests.getOnBoardRequests(s.roboTaxi1.getUnmodifiableViewOfCourses()), //
                new HashSet<>(Arrays.asList(s.avRequest1, s.avRequest3)));
        assertEquals(SharedCourseAccess.getStarter(s.roboTaxi1).get(), SharedCourse.pickupCourse(s.avRequest2));
        assertTrue(SharedRoboTaxiUtils.isNextCourseOfType(s.roboTaxi1, SharedMealType.PICKUP));
        assertEquals(SharedCourseUtil.getUniqueAVRequests(s.roboTaxi1.getUnmodifiableViewOfCourses()),
                new HashSet<>(Arrays.asList(s.avRequest1, s.avRequest2, s.avRequest3, s.avRequest4)));

        s.roboTaxi1.setDivertableLinkTime(new LinkTimePair(s.avRequest2.getFromLink(), 1.0));
        s.roboTaxi1.pickupNewCustomerOnBoard();
        assertFalse(OnboardRequests.canPickupNewCustomer(s.roboTaxi1));

        s.roboTaxi1.setDivertableLinkTime(new LinkTimePair(s.avRequest1.getToLink(), 1.0));
        s.roboTaxi1.dropOffCustomer();

        s.roboTaxi1.removeAVRequestFromMenu(s.avRequest4);
        try {
            s.roboTaxi1.cleanAndAbandonMenu();
            assertTrue(false);
        } catch (Exception e) {
            // ---
        }
        s.roboTaxi1.addRedirectCourseToMenuAtBegining(SharedCourse.redirectCourse(s.linkDepotIn, "backTodepot"));
        assertEquals(SharedRoboTaxiUtils.getStarterLink(s.roboTaxi1), s.linkDepotIn);
        s.roboTaxi1.setDivertableLinkTime(new LinkTimePair(s.linkDepotIn, 1.0));
        s.roboTaxi1.finishRedirection();

        s.roboTaxi1.setDivertableLinkTime(new LinkTimePair(s.linkLeft, 1.0));
        s.roboTaxi1.startDropoff();
        s.roboTaxi1.addAVRequestToMenu(s.avRequest5);
        try {
            s.roboTaxi1.moveAVCourseToNext(SharedCourseAccess.getStarter(s.roboTaxi1).get());
            assertTrue(false);
        } catch (Exception e) {
            // ---
        }
        System.out.println("Robo Taxi Test done");

    }

}
