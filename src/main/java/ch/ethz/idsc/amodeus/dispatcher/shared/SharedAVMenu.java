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

	public SharedAVMenu() {
	}

	private SharedAVMenu(SharedAVMenu sharedAVMenu) {
		sharedAVMenu.roboTaxiMenu.forEach(course -> roboTaxiMenu.add(course.copy()));
	}

	// **************************************************
	// ADDING COURSES
	// **************************************************

	public void addAVCourseAtIndex(SharedAVCourse avCourse, int courseIndex) {
		GlobalAssert.that(courseIndex >= 0 && courseIndex <= roboTaxiMenu.size());
		roboTaxiMenu.add(courseIndex, avCourse);
	}

	public void addAVCourseAsStarter(SharedAVCourse avCourse) {
		roboTaxiMenu.add(0, avCourse);
	}

	public void addAVCourseAsDessert(SharedAVCourse avCourse) {
		roboTaxiMenu.add(roboTaxiMenu.size(), avCourse);
	}

	// **************************************************
	// MOVING COURSES
	// **************************************************

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

	/**
	 * Replaces the all the courses of this menu with the course order of the new
	 * menu. It is required that the new menu contains exactly the same courses as
	 * the old one.
	 * 
	 * @param sharedAVMenu
	 */
	public void replaceWith(SharedAVMenu sharedAVMenu) {
		GlobalAssert.that(containsSameCourses(sharedAVMenu));
		roboTaxiMenu.clear();
		GlobalAssert.that(roboTaxiMenu.isEmpty());
		roboTaxiMenu.addAll(sharedAVMenu.getCourses());
	}

	// **************************************************
	// REMOVING COURSES
	// **************************************************

	public void removeStarterCourse() {
		GlobalAssert.that(hasStarter());
		removeAVCourse(0);
	}

	public void removeAVCourse(int courseIndex) {
		GlobalAssert.that(roboTaxiMenu.size() > courseIndex);
		roboTaxiMenu.remove(courseIndex);
	}

	/* package */ void removeCourse(SharedAVCourse sharedAVCourse) {
		GlobalAssert.that(containsCourse(sharedAVCourse));
		roboTaxiMenu.remove(sharedAVCourse);
	}

	// **************************************************
	// GET COURSES
	// **************************************************

	/**
	 * Gets the next course of the menu.
	 * 
	 * @return
	 */
	public SharedAVCourse getStarterCourse() {
		return roboTaxiMenu.isEmpty() ? null : roboTaxiMenu.get(0);
	}

	/**
	 * Gets the complete List of Courses in this Menu
	 * 
	 * @return
	 */
	public List<SharedAVCourse> getCourses() {
		// TODO which version is better??
		// return Collections.unmodifiableList(roboTaxiMenu);
		return roboTaxiMenu;
	}

	/**
	 * Get the position of the course in the menu. 0 is the next course (called
	 * Starter). see {@link getStarterCourse}.
	 * 
	 * @param course
	 * @return
	 */
	public int getIndexOf(SharedAVCourse course) {
		return roboTaxiMenu.indexOf(course);
	}

	/**
	 * Gets A deep Copy of this Menu
	 * 
	 * @return
	 */
	public SharedAVMenu copy() {
		return new SharedAVMenu(this);
	}

	/**
	 * Gets the indices of the give SharedAVMealType.
	 * 
	 * @param pickupOrDropoff
	 * @return
	 */
	public List<Integer> getPickupOrDropOffCoursesIndeces(SharedAVMealType pickupOrDropoff) {
		List<Integer> indeces = new ArrayList<>();
		for (int i = 0; i < roboTaxiMenu.size(); i++) {
			if (roboTaxiMenu.get(i).getPickupOrDropOff().equals(pickupOrDropoff)) {
				indeces.add(i);
			}
		}
		return indeces;
	}

	// **************************************************
	// CHECK FUNCTIONS
	// **************************************************

	/**
	 * Checks if the menu has entries.
	 * 
	 * @return
	 */
	public boolean hasStarter() {
		return !roboTaxiMenu.isEmpty();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SharedAVMenu) {
			SharedAVMenu sAvMenu = (SharedAVMenu) obj;
			List<SharedAVCourse> otherMenu = sAvMenu.getCourses();
			if (otherMenu.size() == roboTaxiMenu.size()) {
				for (int i = 0; i < roboTaxiMenu.size(); i++) {
					if (!roboTaxiMenu.get(i).equals(sAvMenu.getCourses().get(i))) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the given sharedAvCourse is contained in the menu
	 * 
	 * @param sharedAVCourse
	 * @return
	 */
	public boolean containsCourse(SharedAVCourse sharedAVCourse) {
		return roboTaxiMenu.contains(sharedAVCourse);
	}

	/**
	 * Checks if a Pickup course of the given request is in the current menu
	 * 
	 * @param requestId
	 * @return
	 */
	public boolean containsPickupCourse(Id<Request> requestId) {
		return containsCourse(SharedAVCourse.pickupCourse(requestId));
	}

	/**
	 * Checks if a Dropoff course of the given request is in the current menu
	 * 
	 * @param requestId
	 * @return
	 */
	public boolean containsDropoffCourse(Id<Request> requestId) {
		return containsCourse(SharedAVCourse.dropoffCourse(requestId));
	}

	/**
	 * checks that no Dropoff of a request is in the menu before its Pickup.
	 * 
	 * @return
	 */
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

	/**
	 * Checks if the menu contains exactly the same courses as the inputed menu.
	 * 
	 * @param sharedAVMenu
	 * @return true if the the two menus contain the same courses
	 */
	public boolean containsSameCourses(SharedAVMenu sharedAVMenu) {
		return (roboTaxiMenu.size() != sharedAVMenu.getCourses().size()) ? //
				false : //
				sharedAVMenu.getCourses().containsAll(roboTaxiMenu);
	}

}
