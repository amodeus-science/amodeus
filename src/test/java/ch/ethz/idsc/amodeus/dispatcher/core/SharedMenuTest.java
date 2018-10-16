package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.contrib.dvrp.data.Request;
import org.matsim.core.network.NetworkUtils;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseListUtils;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMenu;
import ch.ethz.matsim.av.passenger.AVRequest;
import junit.framework.TestCase;

public class SharedMenuTest extends TestCase {
    public void testSimple() {

        Id<Node> nodeid1 = Id.createNodeId("node1");
        Coord coord1 = new Coord(100, 100);
        Node node1 = NetworkUtils.createNode(nodeid1, coord1);

        Id<Node> nodeid2 = Id.createNodeId("node2");
        Coord coord2 = new Coord(100, 200);
        Node node2 = NetworkUtils.createNode(nodeid2, coord2);

        Id<Node> nodeid3 = Id.createNodeId("node3");
        Coord coord3 = new Coord(200, 200);
        Node node3 = NetworkUtils.createNode(nodeid3, coord3);

        Id<Node> nodeid4 = Id.createNodeId("node4");
        Coord coord4 = new Coord(200, 100);
        Node node4 = NetworkUtils.createNode(nodeid4, coord4);

        Network network = null;
        double length = 100.0;
        double freespeed = 20.0;
        double capacity = 500.0;
        double lanes = 1.0;

        Id<Link> idUp = Id.createLinkId("linkUp");
        Link linkUp = NetworkUtils.createLink(idUp, node1, node2, network, length, freespeed, capacity, lanes);
        Id<Link> idRight = Id.createLinkId("linkUp");
        Link linkRight = NetworkUtils.createLink(idRight, node2, node3, network, length, freespeed, capacity, lanes);
        Id<Link> idDown = Id.createLinkId("linkUp");
        Link linkDown = NetworkUtils.createLink(idDown, node3, node4, network, length, freespeed, capacity, lanes);
        Id<Link> idLeft = Id.createLinkId("linkUp");
        Link linkLeft = NetworkUtils.createLink(idLeft, node4, node1, network, length, freespeed, capacity, lanes);

        AVRequest avRequest1 = new AVRequest(Id.create("p1", Request.class), null, linkUp, linkDown, 0.0, 0.0, null, null, null);
        AVRequest avRequest2 = new AVRequest(Id.create("p1", Request.class), null, linkRight, linkLeft, 0.0, 0.0, null, null, null);

        SharedCourse pickupCourse1 = SharedCourse.pickupCourse(avRequest1);
        SharedCourse dropoffCourse1 = SharedCourse.dropoffCourse(avRequest1);
        SharedCourse pickupCourse2 = SharedCourse.pickupCourse(avRequest2);
        SharedCourse dropoffCourse2 = SharedCourse.dropoffCourse(avRequest2);

        List<SharedCourse> list1 = Arrays.asList(pickupCourse1, dropoffCourse1, pickupCourse2, dropoffCourse2);
        SharedMenu menu1 = SharedMenu.of(list1);
        
        assertTrue(SharedMenuUtils.checkMenuDoesNotPlanToPickUpMoreCustomersThanCapacity(menu1, 1));
        assertTrue(SharedMenuUtils.checkNoPickupAfterDropoffOfSameRequest(menu1));
        assertTrue(SharedMenuUtils.checkAllCoursesAppearOnlyOnce(menu1));
        
        SharedMenu menu2 = SharedMenuUtils.moveAVCourseToNext(menu1, dropoffCourse1);
        assertTrue(SharedMenuUtils.containSameCourses(menu1, menu2));
        SharedMenu menu2Check = SharedMenu.of(Arrays.asList(pickupCourse1, pickupCourse2, dropoffCourse1, dropoffCourse2));
        assertFalse(menu1.getRoboTaxiMenu().equals(menu2.getRoboTaxiMenu()));
        assertTrue(menu2.getRoboTaxiMenu().equals(menu2Check.getRoboTaxiMenu()));
        
        List<SharedCourse> listInvalid = Arrays.asList(dropoffCourse1,pickupCourse1);

        try {
            SharedMenu.of(listInvalid);
            assertTrue(false);
        } catch (Exception e) {
        }
        

        System.out.println("Shared Menu Test Done");
    }

}
