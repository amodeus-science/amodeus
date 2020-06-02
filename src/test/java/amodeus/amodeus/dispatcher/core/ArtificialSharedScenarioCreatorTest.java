/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import java.util.Arrays;
import java.util.List;

import amodeus.amodeus.dispatcher.shared.SharedCourse;
import amodeus.amodeus.dispatcher.shared.SharedCourseUtil;
import junit.framework.TestCase;

public class ArtificialSharedScenarioCreatorTest extends TestCase {
    public void testSimple() {
        ArtificialSharedScenarioCreator artificialScenarioCreator = new ArtificialSharedScenarioCreator();

        SharedCourse pickupCourse1 = SharedCourse.pickupCourse(artificialScenarioCreator.avRequest1);
        SharedCourse dropoffCourse1 = SharedCourse.dropoffCourse(artificialScenarioCreator.avRequest1);
        List<SharedCourse> list1 = Arrays.asList(pickupCourse1, dropoffCourse1, pickupCourse1, dropoffCourse1);

        assertEquals(4, list1.size());
        assertEquals(1, SharedCourseUtil.getUniquePassengerRequests(list1).size());
        assertEquals(1, SharedCourseUtil.getUniquePassengerRequests(SharedCourseUtil.copy(list1)).size());
    }

}
