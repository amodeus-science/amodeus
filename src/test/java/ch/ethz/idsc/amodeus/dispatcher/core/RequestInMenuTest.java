/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Arrays;
import java.util.List;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseUtil;
import junit.framework.TestCase;

public class RequestInMenuTest extends TestCase {
    public void testSimple() {
        ArtificialScenarioCreator artificialScenarioCreator = new ArtificialScenarioCreator();

        SharedCourse pickupCourse1 = SharedCourse.pickupCourse(artificialScenarioCreator.avRequest1);
        SharedCourse dropoffCourse1 = SharedCourse.dropoffCourse(artificialScenarioCreator.avRequest1);
        List<SharedCourse> list1 = Arrays.asList(pickupCourse1, dropoffCourse1, pickupCourse1, dropoffCourse1);

        assertEquals(4, list1.size());
        assertEquals(1, SharedCourseUtil.getUniqueAVRequests(list1).size());
        assertEquals(1, SharedCourseUtil.getUniqueAVRequests(SharedCourseUtil.copy(list1)).size());
    }

}
