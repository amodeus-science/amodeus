package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.data.Request;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/**
 * @author onicoloLsieber Object containing list of pickup and dropoff planned
 *         for an AV.
 */
public class SharedAVMenu {

	private final List<SharedAVCourse> roboTaxiMenu = new ArrayList<>();

	public SharedAVMenu(SharedAVMenu sharedAVMenu) {
		sharedAVMenu.roboTaxiMenu.forEach(course -> roboTaxiMenu.add(course.copy()));
	}

	public SharedAVMenu() {
	}

	public void addAVCourseWithIndex(SharedAVCourse avCourse, int courseIndex) {
		GlobalAssert.that(courseIndex >= 0 && courseIndex <= roboTaxiMenu.size());
		roboTaxiMenu.add(courseIndex, avCourse);
	}

	public void addAVCourseAsaStarter(SharedAVCourse avCourse) {
		roboTaxiMenu.add(0, avCourse);
	}

	public boolean moveAVCourseToPrev(SharedAVCourse sharedAVCourse) {
		GlobalAssert.that(containsCourse(sharedAVCourse));
		int i = getIndexOf(sharedAVCourse);
		if (i > 0 && i < roboTaxiMenu.size()) {
			Collections.swap(roboTaxiMenu, i, i - 1);
			return true;
		}
		return false;
	}

	public boolean moveAVCourseToNext(SharedAVCourse sharedAVCourse) {
		GlobalAssert.that(containsCourse(sharedAVCourse));
		int i = getIndexOf(sharedAVCourse);
		if (i >= 0 && i < roboTaxiMenu.size() - 1) {
			Collections.swap(roboTaxiMenu, i, i + 1);
			return true;
		}
		return false;
	}

	public void addAVCourseAsaDessert(SharedAVCourse avCourse) {
		roboTaxiMenu.add(roboTaxiMenu.size(), avCourse);
	}

	public void removeAVCourse(int courseIndex) {
		roboTaxiMenu.remove(courseIndex);
	}

	/* package */ void removeCourse(SharedAVCourse sharedAVCourse) {
		GlobalAssert.that(containsCourse(sharedAVCourse));
		roboTaxiMenu.remove(sharedAVCourse);
	}

	public int getIndexOf(SharedAVCourse course) {
		return roboTaxiMenu.indexOf(course);
	}

	public List<SharedAVCourse> getCurrentSharedAVMenu() {
//		return (List<SharedAVCourse>) Collections.unmodifiableCollection(roboTaxiMenu);
		return roboTaxiMenu;
	}
	
	public SharedAVMenu copy() {
		return new SharedAVMenu(this);
	}

	public SharedAVCourse getSharedAVStarter() {
		return roboTaxiMenu.isEmpty() ? null : roboTaxiMenu.get(0);
	}

	public boolean hasStarter() {
		return !roboTaxiMenu.isEmpty();
	}

	public void clearSharedAVMenu() {
		roboTaxiMenu.clear();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SharedAVMenu) {
			SharedAVMenu sAvMenu = (SharedAVMenu) obj;
			List<SharedAVCourse> otherMenu =  sAvMenu.getCurrentSharedAVMenu();
			if (otherMenu.size() == roboTaxiMenu.size()) {
				for (int i = 0; i < roboTaxiMenu.size(); i++) {
					if (!roboTaxiMenu.get(i).equals(sAvMenu.getCurrentSharedAVMenu().get(i))) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}



	public boolean containsCourse(SharedAVCourse sharedAVCourse) {
		return roboTaxiMenu.contains(sharedAVCourse);
	}

	public boolean containsPickupCourse(Id<Request> requestId) {
		return containsCourse(SharedAVCourse.pickupCourse(requestId));
	}

	public boolean containsDropoffCourse(Id<Request> requestId) {
		return containsCourse(SharedAVCourse.dropoffCourse(requestId));
	}

	public boolean checkNoPickupAfterDropoffOfSameRequest() {
		for (SharedAVCourse sharedAVCourse : roboTaxiMenu) {
			if (sharedAVCourse.getPickupOrDropOff().equals(SharedAVMealType.DROPOFF)) {
				int dropofIndex = getIndexOf(sharedAVCourse);
				SharedAVCourse sharedAVCoursePickup = new SharedAVCourse(sharedAVCourse.getRequestId(),
						SharedAVMealType.PICKUP);
				if (containsCourse(sharedAVCoursePickup)) {
					int pickupIndex = getIndexOf(sharedAVCoursePickup);
					if (pickupIndex > dropofIndex) {
						return false;
					}
				}
			}
		}
		return true;
	}
}
