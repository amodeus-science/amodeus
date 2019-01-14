/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import junit.framework.TestCase;

public class RoboTaxiStatusTest extends TestCase {
    public void testSimple() {
        assertTrue(RoboTaxiStatus.DRIVETOCUSTOMER.isDriving());
        assertTrue(RoboTaxiStatus.REBALANCEDRIVE.isDriving());
        assertTrue(RoboTaxiStatus.DRIVEWITHCUSTOMER.isDriving());
        assertFalse(RoboTaxiStatus.STAY.isDriving());
        assertFalse(RoboTaxiStatus.OFFSERVICE.isDriving());
    }

}
