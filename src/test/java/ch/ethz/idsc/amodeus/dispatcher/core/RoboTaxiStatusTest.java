/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import org.matsim.contrib.dvrp.util.LinkTimePair;

import junit.framework.TestCase;

public class RoboTaxiStatusTest extends TestCase {
    public void testSimple() {
        assertTrue(RoboTaxiStatus.DRIVETOCUSTOMER.isDriving());
        assertTrue(RoboTaxiStatus.REBALANCEDRIVE.isDriving());
        assertTrue(RoboTaxiStatus.DRIVEWITHCUSTOMER.isDriving());
        assertFalse(RoboTaxiStatus.STAY.isDriving());
        assertFalse(RoboTaxiStatus.OFFSERVICE.isDriving());
    }

    public void testGetIsDroppingOf() {

//        ArtificialScenarioCreator s = new ArtificialScenarioCreator();
//        s.roboTaxi1.addAVRequestToMenu(s.avRequest1);
//        s.roboTaxi1.setDivertableLinkTime(new LinkTimePair(s.linkUp, 1.0));
//        s.roboTaxi1.pickupNewCustomerOnBoard();
//        s.roboTaxi1.setDivertableLinkTime(new LinkTimePair(s.linkDown, 20.0));
//
//        assertFalse(s.roboTaxi1.getDropoffInProgress());
//
//        s.roboTaxi1.startDropoff();
//        // Assign Directive To roboTaxi
//        // TravelTime travelTime = new FreeSpeedTravelTime();
//        // Future<Path> futurePath;
//        // FuturePathContainer futurePathContainer = new FuturePathContainer(s.avRequest1.getFromLink(), s.avRequest1.getToLink(), 20.0, futurePath, travelTime);
//        // s.roboTaxi1.assignDirective(new SharedGeneralDropoffDirective(s.roboTaxi1, s.avRequest1, futurePathContainer, 20.0, 10.0));
//
//        assertTrue(s.roboTaxi1.getDropoffInProgress());
//        // assertFalse(s.roboTaxi1.isDivertable());
//
//        // s.roboTaxi1.executeDirective();
//        // assertTrue(s.roboTaxi1.getDropoffInProgress());
//        // assertFalse(s.roboTaxi1.isDivertable());
//
//        s.roboTaxi1.dropOffCustomer();
//
//        assertFalse(s.roboTaxi1.getDropoffInProgress());

    }
}
