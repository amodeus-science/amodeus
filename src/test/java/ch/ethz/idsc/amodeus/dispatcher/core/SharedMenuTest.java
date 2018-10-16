//package ch.ethz.idsc.amodeus.dispatcher.core;
//
//import java.util.Arrays;
//import java.util.List;
//
//import org.matsim.api.core.v01.Id;
//import org.matsim.api.core.v01.network.Link;
//import org.matsim.contrib.dvrp.data.Request;
//
//import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
//import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMenu;
//import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
//import ch.ethz.matsim.av.passenger.AVRequest;
//import junit.framework.TestCase;
//
//public class SharedMenuTest extends TestCase {
//    public void testSimple() {
//
//        Link pickupLink = null;
//        AVRequest avRequest1 = new AVRequest(Id.create("p1", Request.class), null, pickupLink, null, 0.0, 0.0, null, null, null);
//        AVRequest avRequest2 = new AVRequest(Id.create("p1", Request.class), null, pickupLink, null, 0.0, 0.0, null, null, null);
//
//        SharedCourse pickupCourse1 = SharedCourse.pickupCourse(avRequest1);
//        SharedCourse dropoffCourse1 = SharedCourse.dropoffCourse(avRequest1);
//        SharedCourse pickupCourse2 = SharedCourse.pickupCourse(avRequest2);
//        SharedCourse dropoffCourse2 = SharedCourse.dropoffCourse(avRequest2);
//
//        List<SharedCourse> list = Arrays.asList(pickupCourse1, dropoffCourse1, pickupCourse2, dropoffCourse2);
//        SharedMenu menu = SharedMenu.of(list);
//
//        GlobalAssert.that(false);
//        System.out.println("Done");
//    }
//}
