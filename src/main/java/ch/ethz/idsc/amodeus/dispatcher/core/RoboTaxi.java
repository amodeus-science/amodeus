/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.List;
import java.util.Objects;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.contrib.dvrp.util.LinkTimePair;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseListUtils;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMenu;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.schedule.AVDriveTask;
import ch.ethz.matsim.av.schedule.AVDropoffTask;
import ch.ethz.matsim.av.schedule.AVPickupTask;
import ch.ethz.matsim.av.schedule.AVStayTask;

/** RoboTaxi is central classs to be used in all dispatchers. Dispatchers control
 * a fleet of RoboTaxis, each is uniquely associated to an AVVehicle object in
 * MATSim. */
public class RoboTaxi {
    /** unit capacity fields */
    static private final Logger logger = Logger.getLogger(RoboTaxi.class);
    private final AVVehicle avVehicle;
    private RoboTaxiStatus status;
    private final RoboTaxiUsageType usageType; // final might be removed if dispatchers can modify usage

    /** last known location of the RoboTaxi */
    private Link lastKnownLocation;
    /** drive destination of the RoboTaxi, null for stay task */
    private Link driveDestination;
    /** location/time pair from where / when RoboTaxi path can be altered. */
    private LinkTimePair divertableLinkTime;
    private AbstractDirective directive;

    /** shared fields
     * The Shared menu contains a lot of information. These can be extracted with the Utils functions
     * in RoboTaxiUtils and SharedCourseLItsUtils */
    private SharedMenu menu = SharedMenu.empty();

    /** Standard constructor
     * 
     * @param avVehicle binding association to MATSim AVVehicle object
     * @param linkTimePair
     * @param driveDestination */
    // TODO make this package again
    public RoboTaxi(AVVehicle avVehicle, LinkTimePair divertableLinkTime, Link driveDestination, RoboTaxiUsageType usageType) {
        this.avVehicle = avVehicle;
        this.divertableLinkTime = divertableLinkTime;
        this.driveDestination = Objects.requireNonNull(driveDestination);
        this.directive = null;
        this.status = RoboTaxiStatus.STAY;
        this.usageType = usageType;
    }

    // **********************************************
    // Standard Robo Taxi Functionalities
    // **********************************************

    // ===================================================================================
    // methods to be used by dispatchers, public

    /** @return {@link} location at which robotaxi can be diverted, i.e. a Link
     *         with an endnode at which the robotaxi path can be altered */
    public Link getDivertableLocation() {
        return divertableLinkTime.link;
    }

    /** @return time when robotaxi can be diverted */
    /* package */ double getDivertableTime() {
        return divertableLinkTime.time;
    }

    /** @return null if vehicle is currently not driving, else the final
     *         {@link Link} of the path that the vehicles is currently driving
     *         on */
    public Link getCurrentDriveDestination() {
        return driveDestination;
    }

    /** @return last konwn {@link} location of the RoboTaxi, meant for data
     *         capturing, current location is not necessarily divertablelocation
     *         from where RoboTaxi could change its path, therefore use
     *         getDivertableLocation() for computations. */
    public Link getLastKnownLocation() {
        return lastKnownLocation;
    }

    /** @return true if vehicle is staying */
    public boolean isInStayTask() {
        return status.equals(RoboTaxiStatus.STAY);
    }

    /** @return {@Id<Link>} of the RoboTaxi, robotaxi ID is the same as AVVehicle ID */
    public Id<Vehicle> getId() {
        return avVehicle.getId();
    }

    /** @return RoboTaxiStatus of the vehicle */
    public RoboTaxiStatus getStatus() {
        return status;
    }

    /** Gets the capacity of the avVehicle. Now its an Integer and not a double as in Matsim
     * 
     * @return */
    public int getCapacity() {
        return (int) avVehicle.getCapacity();
    }

    // ===================================================================================
    // methods to be used by Core package

    /** function only used from VehicleMaintainer
     * 
     * @param divertableLinkTime update the divertableLinkTime of the RoboTaxi, */
    /* package */ void setDivertableLinkTime(LinkTimePair divertableLinkTime) {
        this.divertableLinkTime = Objects.requireNonNull(divertableLinkTime);
    }

    /** @return RoboTaxiPlan with RoboTaxiPlan.plans() Navigable Map containing all RoboTaxiPlanEntry
     *         elements sorted according to begin time */
    /* package */ RoboTaxiPlan getCurrentPlans(double time) {
        return RoboTaxiPlan.of(getSchedule(), time);
    }

    /** function only used from VehicleMaintainer in update steps
     * 
     * @param currentLocation last known link of RoboTaxi location */
    /* package */ void setLastKnownLocation(Link currentLocation) {
        this.lastKnownLocation = Objects.requireNonNull(currentLocation);
    }

    /** @param currentDriveDestination
     *            {@link} roboTaxi is driving to, to be used only by core
     *            package, use setVehiclePickup and setVehicleRebalance in
     *            dispatchers */
    /* package */ void setCurrentDriveDestination(Link currentDriveDestination) {
        this.driveDestination = Objects.requireNonNull(currentDriveDestination);
    }

    /** @param {@AVStatus}
     *            for robotaxi to be updated to, to be used only by core
     *            package, in dispatcher implementations, status will be adapted
     *            automatically. */
    /* package */ void setStatus(RoboTaxiStatus status) {
        GlobalAssert.that(!usageType.equals(RoboTaxiUsageType.SHARED));
        this.status = Objects.requireNonNull(status);
    }

    /** @return true if robotaxi is without a customer */
    /* package */ boolean isWithoutCustomer() {
        // For now this works with universal dispatcher i.e. single used robotaxis as number of customers is never changed
    	//!status.equals(RoboTaxiStatus.DRIVEWITHCUSTOMER) &&
        return  RoboTaxiUtils.getNumberOnBoardRequests(this) == 0;
    }

    /** @return {@Schedule} of the RoboTaxi, to be used only inside core package,
     *         the schedule will be used automatically for all updates
     *         associated to pickups, rebalance tasks */
    /* package */ Schedule getSchedule() {
        return avVehicle.getSchedule();
    }

    /** @param abstractDirective
     *            to be issued to RoboTaxi when commands change, to be used only
     *            in the core package, directives will be issued automatically
     *            when setVehiclePickup, setVehicleRebalance are called. */
    /* package */ void assignDirective(AbstractDirective abstractDirective) {
        GlobalAssert.that(isWithoutDirective());
        this.directive = abstractDirective;
    }

    /** @return true if RoboTaxi is without an unexecuted directive, to be used
     *         only inside core package */
    /* package */ boolean isWithoutDirective() {
        return directive == null;
    }

    /** @return true if robotaxi is not driving on the last link of its drive task,
     *         used for filtering purposes as currently the roboTaxis cannot be rerouted
     *         when driving on the last link of their route */
    /* package */ boolean notDrivingOnLastLink() {
        if (status.equals(RoboTaxiStatus.STAY))
            return true;

        Task avT = getSchedule().getCurrentTask();

        // TODO Who? check why this appears often
        if (avT instanceof AVStayTask) {
            // TODO MISC For now, this works, but probably needs fixing somewhere upfront /sh, apr 2018
            if (!usageType.equals(RoboTaxiUsageType.SHARED)) { // for shared this is allowed e.g. when a new course is added but the it has not been executed yet
                logger.warn("RoboTaxiStatus != STAY, but Schedule.getCurrentTask() == AVStayTask; probably needs fixing");
                System.out.println("status: " + status);
            }
            return true;
        }

        // Added cases when on pickup and dropoff task For shared taxis
        if (avT instanceof AVDriveTask) {
            AVDriveTask avDT = (AVDriveTask) avT;
            return avDT.getPath().getLinkCount() != 1;
        }
        if (avT instanceof AVPickupTask || avT instanceof AVDropoffTask)
            return false;
        throw new IllegalArgumentException("Found Unknown type of AVTASK !!");
    }

    /** execute the directive of a RoboTaxi, to be used only inside core package */
    /* package */ void executeDirective() {
        directive.execute();
        directive = null;
    }

    public RoboTaxiUsageType getUsageType() {
        return usageType;
    }

    // **********************************************
    // Definition Of Divertable depends on usage
    // **********************************************

    public boolean isDivertable() {
        if (usageType.equals(RoboTaxiUsageType.SINGLEUSED))
            return isWithoutDirective() && isWithoutCustomer() && notDrivingOnLastLink();
        if (usageType.equals(RoboTaxiUsageType.SHARED))
            return isWithoutDirective() && notDrivingOnLastLink();
        throw new IllegalArgumentException("Robo Taxi Usage Type is not defined");
    }

    // **********************************************
    // Shared Functionalities, needed here because of capacity
    // **********************************************

    /** Gives full information of the future menu (i.e. plans) of the {@link RoboTaxi}.
     * This Information contains for example the number of customers on Board or the possibility to pick up new customers.
     * To get all this Information the {@link SharedCourseListUtils} class offers some of the standard functionalities.
     * Similar Functionalities are Offered as well by the {@link RoboTaxiUtils} class. Take a look at these two clases when implementing Dispatchers
     * Further information can be pulled from this menu by using standard List functionalities.
     * 
     * @return An unmodifiable {@link List} of {@link SharedCourse}s which can only be read but not modified */
    public List<SharedCourse> getUnmodifiableViewOfCourses() {
        return menu.getRoboTaxiMenu();
    }

    /** Modifies the menu of the RoboTaxi. The given course is moved up in the menu by one position.
     * 
     * @param sharedCourse */
    public void moveAVCourseToPrev(SharedCourse sharedCourse) {
        setMenu(SharedMenuUtils.moveAVCourseToPrev(menu, sharedCourse));
    }

    /** Modifies the menu of the RoboTaxi. The given course is moved down in the menu by one position.
     * 
     * @param sharedCourse */
    public void moveAVCourseToNext(SharedCourse sharedCourse) {
        setMenu(SharedMenuUtils.moveAVCourseToNext(menu, sharedCourse));
    }

    /** This function allows to update the menu of the RoboTaxi with a new orderd menu.
     * Thereby the new menu has to fulfill the following conditions:
     * 1. The exact same Courses have to be in the Menu.
     * 2. The menu can not plan to pickup more persons than the capacity of the Robo Taxi at any Time
     * 
     * @param menu */
    private void updateMenu(SharedMenu menu) {
        GlobalAssert.that(SharedMenuUtils.containSameCourses(this.menu, menu));
        GlobalAssert.that(SharedCourseListUtils.checkMenuConsistency(getUnmodifiableViewOfCourses(), getCapacity()));
        setMenu(menu);
    }

    /** This function allows to update the menu of the RoboTaxi with a new List of Shared Courses.
     * Thereby the new menu has to fulfill the following conditions:
     * 1. The exact same Courses have to be in the Menu.
     * 2. The menu can not plan to pickup more persons than the capacity of the Robo Taxi at any Time
     * 3. The menu has to be consistent in itself (i.e. for each pickup a dropoff of the same request is present,
     * for each request the dropoff occurs after the pickup and no course apears exactely once)
     * 
     * @param List<SharedCourse> */
    public void updateMenu(List<SharedCourse> list) {
        updateMenu(SharedMenu.of(list));
    }

    /** This function is only for internal use. It should not be allowed that the menu can be changed from outside of the RoboTaxi directly.
     * 
     * @param menu */
    private final void setMenu(SharedMenu menu) {
        GlobalAssert.that(SharedMenuUtils.checkMenuConsistencyWithRoboTaxi(menu, getCapacity()));
        this.menu = menu;
        this.status = RoboTaxiUtils.calculateStatusFromMenu(this);
    }

    /* package */ void addAVRequestToMenu(AVRequest avRequest) {
        // TODO Lukas, with Claudio, Carl, what is the wanted behaviour? shouldnt the dispatcher take care of this
        // We could bring it into the rebalancing dispatcher, there we can add a function which is called: addAVrequestandRemoveFirstRebalancing(AVrequest)
        if (status.equals(RoboTaxiStatus.REBALANCEDRIVE)) {
            GlobalAssert.that(RoboTaxiUtils.getStarterCourse(this).get().getMealType().equals(SharedMealType.REDIRECT));
            if (getUnmodifiableViewOfCourses().size() == 1) {
                finishRedirection();
            }
        }
        SharedCourse pickupCourse = SharedCourse.pickupCourse(avRequest);
        SharedCourse dropoffCourse = SharedCourse.dropoffCourse(avRequest);
        setMenu(SharedMenuUtils.addAVCoursesAsDessert(menu, pickupCourse, dropoffCourse));
    }

    /* package */ void addRedirectCourseToMenu(SharedCourse redirectCourse) {
        GlobalAssert.that(redirectCourse.getMealType().equals(SharedMealType.REDIRECT));
        setMenu(SharedMenuUtils.addAVCoursesAsDessert(menu, redirectCourse));
    }

    /* package */ void addRedirectCourseToMenuAtBegining(SharedCourse redirectCourse) {
        GlobalAssert.that(redirectCourse.getMealType().equals(SharedMealType.REDIRECT));
        setMenu(SharedMenuUtils.addAVCoursesAsStarter(menu, redirectCourse));
    }

    /* package */ void pickupNewCustomerOnBoard() {
        GlobalAssert.that(RoboTaxiUtils.canPickupNewCustomer(this));
        GlobalAssert.that(RoboTaxiUtils.nextCourseIsOfType(this, SharedMealType.PICKUP));
        GlobalAssert.that(RoboTaxiUtils.getStarterLink(this).equals(getDivertableLocation()));
        setMenu(SharedMenuUtils.removeStarterCourse(menu));
    }

    /* package */ void dropOffCustomer() {
        GlobalAssert.that(RoboTaxiUtils.getNumberOnBoardRequests(this) > 0);
        GlobalAssert.that(RoboTaxiUtils.getNumberOnBoardRequests(this) <= getCapacity());
        GlobalAssert.that(RoboTaxiUtils.nextCourseIsOfType(this, SharedMealType.DROPOFF));
        GlobalAssert.that(RoboTaxiUtils.getStarterLink(this).equals(getDivertableLocation()));
        setMenu(SharedMenuUtils.removeStarterCourse(menu));
    }

    /* package */ void finishRedirection() {
        GlobalAssert.that(RoboTaxiUtils.hasNextCourse(this));
        GlobalAssert.that(RoboTaxiUtils.nextCourseIsOfType(this, SharedMealType.REDIRECT));
        setMenu(SharedMenuUtils.removeStarterCourse(menu));
    }

    /** Removes an AV Request from the Robo Taxi Menu. This function can only be called if the Request has not been picked up
     * 
     * @param avRequest */
    /* package */ void removeAVRequestFromMenu(AVRequest avRequest) {
        SharedCourse pickupCourse = SharedCourse.pickupCourse(avRequest);
        SharedCourse dropoffCourse = SharedCourse.dropoffCourse(avRequest);
        GlobalAssert.that(menu.getRoboTaxiMenu().contains(pickupCourse) && menu.getRoboTaxiMenu().contains(dropoffCourse));
        setMenu(SharedMenuUtils.removeAVCourses(menu, pickupCourse, dropoffCourse));
    }

    /** This function deletes all the current Courses from the menu.
     * 
     * @return all the courses which have been removed */
    /* package */ List<SharedCourse> cleanAndAbandonMenu() {
        GlobalAssert.that(RoboTaxiUtils.getNumberOnBoardRequests(this) == 0);
        GlobalAssert.that(isDivertable());
        List<SharedCourse> oldMenu = SharedCourseListUtils.copy(menu.getRoboTaxiMenu());
        setMenu(SharedMenu.empty());
        return oldMenu;
    }

}