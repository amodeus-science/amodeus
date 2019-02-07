/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Coord;
import org.matsim.core.network.NetworkUtils;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public enum StaticMenuUtils {
	;

	/**
	 * Changes Order of the Menue such that first all Pickups and then All Dropoffs
	 * occur. The order is kept. The Redirect Courses are put at the end
	 */
	public static List<SharedCourse> firstAllPickupsThenDropoffs(List<SharedCourse> roboTaxiMenu) {
		// TODO Computationally improve
		List<SharedCourse> list = new ArrayList<>();
		// add PickupCourses
		for (SharedCourse sharedCourse : roboTaxiMenu) {
			if (sharedCourse.getMealType().equals(SharedMealType.PICKUP)) {
				list.add(sharedCourse);
			}
		}
		for (SharedCourse sharedCourse : roboTaxiMenu) {
			if (sharedCourse.getMealType().equals(SharedMealType.DROPOFF)) {
				list.add(sharedCourse);
			}
		}
		for (SharedCourse sharedCourse : roboTaxiMenu) {
			if (sharedCourse.getMealType().equals(SharedMealType.REDIRECT)) {
				list.add(sharedCourse);
			}
		}
		for (SharedCourse sharedCourse : roboTaxiMenu) {
			if (sharedCourse.getMealType().equals(SharedMealType.WAIT)) {
				list.add(sharedCourse);
			}
		}
		for (SharedCourse sharedCourse : roboTaxiMenu) {
			if (sharedCourse.getMealType().equals(SharedMealType.PARK)) {
				list.add(sharedCourse);
			}
		}
		return list;
	}

	public static List<SharedCourse> fastDropoffTour(List<SharedCourse> unmodifiableSharedMenu) {
		List<SharedCourse> sharedMenu = new ArrayList<>(unmodifiableSharedMenu);
		GlobalAssert.that(checkAllPickupsFirst(sharedMenu));
		GlobalAssert.that(!sharedMenu.stream().anyMatch(sc -> sc.getMealType().equals(SharedMealType.REDIRECT)
				&& sc.getMealType().equals(SharedMealType.WAIT) && sc.getMealType().equals(SharedMealType.PARK)));

		Coord lastPickupCoord = getLastPickup(sharedMenu).getLink().getCoord();
		Set<SharedCourse> set = new HashSet<>();
		for (SharedCourse sharedCourse : sharedMenu) {
			if (sharedCourse.getMealType().equals(SharedMealType.DROPOFF)) {
				set.add(sharedCourse);
			}
		}

		for (SharedCourse sharedCourse : set) {
			SharedCourseListUtils.removeAVCourse(sharedMenu, sharedCourse);
		}

		int numIter = set.size();
		for (int i = 0; i < numIter; i++) {
			SharedCourse sharedCourse = getClosestCourse(set, lastPickupCoord);
			SharedCourseListUtils.addAVCourseAsDessert(sharedMenu, sharedCourse);
			set.remove(sharedCourse);
		}
		GlobalAssert.that(set.isEmpty());
		return sharedMenu;
	}

	private static SharedCourse getLastPickup(List<SharedCourse> sharedMenu) {
		SharedCourse lastCourse = null;
		for (SharedCourse sharedCourse : sharedMenu) {
			if (sharedCourse.getMealType().equals(SharedMealType.PICKUP)) {
				lastCourse = sharedCourse;
			}
		}
		return lastCourse;
	}

	public static List<SharedCourse> fastPickupTour(List<SharedCourse> unmodifiableSharedMenu, Coord startCoord) {
		List<SharedCourse> sharedMenu = new ArrayList<>(unmodifiableSharedMenu);

		GlobalAssert.that(checkAllPickupsFirst(sharedMenu));
		int originalSize = sharedMenu.size();
		Collection<SharedCourse> sharedCourses = sharedMenu.stream()
				.filter(sc -> sc.getMealType().equals(SharedMealType.PICKUP)).collect(Collectors.toList());

		for (SharedCourse sharedCourse : sharedCourses) {
			SharedCourseListUtils.removeAVCourse(sharedMenu, sharedCourse);
		}

		int currentIndex = 0;
		Coord nextCoord = startCoord;
		while (!sharedCourses.isEmpty()) {
			SharedCourse closestCourse = getClosestCourse(sharedCourses, nextCoord);
			SharedCourseListUtils.addAVCourseAtIndex(sharedMenu, closestCourse, currentIndex);
			currentIndex++;
			nextCoord = closestCourse.getLink().getCoord();
			sharedCourses.remove(closestCourse);
		}

		GlobalAssert.that(originalSize == sharedMenu.size());
		return sharedMenu;
	}

	private static SharedCourse getClosestCourse(Collection<SharedCourse> sharedCourses, Coord coord) {
		GlobalAssert.that(!sharedCourses.isEmpty());
		SharedCourse closestCourse = null;
		Double distance = null;
		for (SharedCourse sharedCourse : sharedCourses) {
			double d = NetworkUtils.getEuclideanDistance(sharedCourse.getLink().getCoord(), coord);

			if (closestCourse == null) {
				closestCourse = sharedCourse;
				distance = d;
			} else {
				if (d < distance) {
					distance = d;
					closestCourse = sharedCourse;
				}
			}
		}
		return closestCourse;
	}

	private static boolean checkAllPickupsFirst(List<SharedCourse> sharedMenu) {
		boolean justPickupsSoFar = true;
		for (SharedCourse sharedCourse : sharedMenu) {
			if (sharedCourse.getMealType().equals(SharedMealType.PICKUP)) {
				if (!justPickupsSoFar) {
					return false;
				}
			} else {
				justPickupsSoFar = false;
			}
		}
		return true;
	}

}
