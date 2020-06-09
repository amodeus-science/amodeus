/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import java.util.List;
import java.util.Objects;

import org.apache.log4j.Logger;
import org.matsim.amodeus.dvrp.schedule.AmodeusDriveTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusDropoffTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusPickupTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusStayTask;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.contrib.dvrp.util.LinkTimePair;

import amodeus.amodeus.dispatcher.shared.Compatibility;
import amodeus.amodeus.dispatcher.shared.SharedCourse;
import amodeus.amodeus.dispatcher.shared.SharedCourseAccess;
import amodeus.amodeus.dispatcher.shared.SharedCourseAdd;
import amodeus.amodeus.dispatcher.shared.SharedCourseMove;
import amodeus.amodeus.dispatcher.shared.SharedCourseRemove;
import amodeus.amodeus.dispatcher.shared.SharedCourseUtil;
import amodeus.amodeus.dispatcher.shared.SharedMealType;
import amodeus.amodeus.dispatcher.shared.SharedMenu;
import amodeus.amodeus.dispatcher.shared.SharedMenuCheck;
import amodeus.amodeus.util.math.GlobalAssert;

/** RoboTaxi is central class to be used in all dispatchers. Dispatchers control
 * a fleet of RoboTaxis, each is uniquely associated to an AVVehicle object in
 * MATSim. */
public final class RoboTaxi {
    /** unit capacity fields */
    private static final Logger LOGGER = Logger.getLogger(RoboTaxi.class);

    private final DvrpVehicle avVehicle;
    private RoboTaxiStatus status;
    private final RoboTaxiUsageType usageType; // final might be removed if dispatchers can modify usage

    /** last known location of the RoboTaxi */
    private Link lastKnownLocation;

    /** drive destination of the RoboTaxi, null for stay task */
    private Link driveDestination;
    /** location/time pair from where / when RoboTaxi path can be altered. */
    private LinkTimePair divertableLinkTime;
    private DirectiveInterface directive;

    /** shared fields The Shared menu contains a lot of information. These can be
     * extracted with the Utils functions in RoboTaxiUtils and SharedCourseLItsUtils */
    private SharedMenu menu = SharedMenu.empty();
    private boolean dropoffInProgress = false;

    /** Standard constructor
     * 
     * @param avVehicle binding association to MATSim AVVehicle object
     * @param divertableLinkTime
     * @param driveDestination
     * @param usageType */
    /* package */ RoboTaxi(DvrpVehicle avVehicle, LinkTimePair divertableLinkTime, Link driveDestination, RoboTaxiUsageType usageType) {
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

    /** @return {@link} location at which robotaxi can be diverted, i.e. a Link with
     *         an endnode at which the robotaxi path can be altered */
    public Link getDivertableLocation() {
        return divertableLinkTime.link;
    }

    /** @return time when robotaxi can be diverted */
    /* package */ double getDivertableTime() {
        return divertableLinkTime.time;
    }

    /** @return null if vehicle is currently not driving, else the final {@link Link}
     *         of the path that the vehicles is currently driving on */
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
    public Id<DvrpVehicle> getId() {
        return avVehicle.getId();
    }

    /** @return RoboTaxiStatus of the vehicle */
    public RoboTaxiStatus getStatus() {
        return status;
    }

    /** Gets the capacity of the avVehicle. Now its an Integer and not a double as in
     * MATSim, the current number of people on board can be accessed with
     * {@link RoboTaxiUtils.getNumberOnBoardRequests(roboTaxi)}
     * 
     * @return */
    public int getCapacity() {
        return avVehicle.getCapacity();
    }

    // ===================================================================================
    // methods to be used by Core package

    /** function only used from VehicleMaintainer
     * 
     * @param divertableLinkTime update the divertableLinkTime of the RoboTaxi, */
    /* package */ void setDivertableLinkTime(LinkTimePair divertableLinkTime) {
        this.divertableLinkTime = Objects.requireNonNull(divertableLinkTime);
    }

    /** @return RoboTaxiPlan with RoboTaxiPlan.plans() Navigable Map containing all
     *         RoboTaxiPlanEntry elements sorted according to begin time */
    /* package */ RoboTaxiPlan getCurrentPlans(double time) {
        return RoboTaxiPlan.of(getSchedule(), time);
    }

    /** function only used from VehicleMaintainer in update steps
     * 
     * @param currentLocation last known link of RoboTaxi location */
    /* package */ void setLastKnownLocation(Link currentLocation) {
        this.lastKnownLocation = Objects.requireNonNull(currentLocation);
    }

    /** @param currentDriveDestination {@link} roboTaxi is driving to, to be used
     *            only by core package, use setVehiclePickup and
     *            setVehicleRebalance in dispatchers */
    /* package */ void setCurrentDriveDestination(Link currentDriveDestination) {
        this.driveDestination = Objects.requireNonNull(currentDriveDestination);
    }

    /** @param {@AVStatus} for robotaxi to be updated to, to be used only by core
     *            package, in dispatcher implementations, status will be adapted
     *            automatically. */
    /* package */ void setStatus(RoboTaxiStatus status) {
        // TODO @ChengQi which code handles shared taxi status?????
        GlobalAssert.that(!usageType.equals(RoboTaxiUsageType.SHARED));
        this.status = Objects.requireNonNull(status);
    }

    /** @return true if robotaxi is without a customer */
    /* package */ boolean isWithoutCustomer() {
        // For now this works with universal dispatcher i.e. single used robotaxis as
        // number of customers is never changed
        return !status.equals(RoboTaxiStatus.DRIVEWITHCUSTOMER) && //
                menu.getMenuOnBoardCustomers() == 0;
    }

    /** @return {@Schedule} of the RoboTaxi, to be used only inside core package, the
     *         schedule will be used automatically for all updates associated to
     *         pickups, rebalance tasks */
    public Schedule getSchedule() {
        return avVehicle.getSchedule();
    }

    /** @param abstractDirective to be issued to RoboTaxi when commands change, to be
     *            used only in the core package, directives will be
     *            issued automatically when setVehiclePickup,
     *            setVehicleRebalance are called. */
    /* package */ void assignDirective(DirectiveInterface abstractDirective) {
        GlobalAssert.that(isWithoutDirective());
        this.directive = abstractDirective;
    }

    /** @return true if RoboTaxi is without an unexecuted directive, to be used only
     *         inside core package */
    /* package */ boolean isWithoutDirective() {
        return directive == null;
    }

    /** @return true if robotaxi is not driving on the last link of its drive task,
     *         used for filtering purposes as currently the roboTaxis cannot be
     *         rerouted when driving on the last link of their route */
    /* package */ boolean notDrivingOnLastLink() {
        if (status.equals(RoboTaxiStatus.STAY))
            return true;

        Task avT = getSchedule().getCurrentTask();

        // TODO @ChengQi check why this appears often
        if (avT instanceof AmodeusStayTask) {
            // TODO @ChengQi For now, this works, but probably needs fixing somewhere upfront
            // /sh, apr 2018
            if (!usageType.equals(RoboTaxiUsageType.SHARED)) { // for shared this is allowed e.g. when a new course is
                                                               // added but the it has not been executed
                                                               // yet
                LOGGER.warn("RoboTaxiStatus != STAY, but Schedule.getCurrentTask() == AVStayTask; probably needs fixing");
                System.out.println("status: " + status);
            }
            return true;
        }

        // Added cases when on pickup and dropoff task For shared taxis
        if (avT instanceof AmodeusDriveTask) {
            AmodeusDriveTask avDT = (AmodeusDriveTask) avT;
            // TODO @clruch seems it is different to the same function in AmodeusDriveTaskTracker
            return avDT.getPath().getLinkCount() != 1;
        }
        if (avT instanceof AmodeusPickupTask || avT instanceof AmodeusDropoffTask)
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
        return canReroute() && (usageType.equals(RoboTaxiUsageType.SHARED) || isWithoutCustomer());
    }

    // added by luc for rerouting purpose
    public boolean canReroute() {
        return isWithoutDirective() && notDrivingOnLastLink();
    }

    // **********************************************
    // Shared Functionalities, needed here because of capacity
    // **********************************************

    /** Gives full information of the future menu (i.e. plans) of the
     * {@link RoboTaxi}. This Information contains for example the number of
     * customers on Board or the possibility to pick up new customers. To get all
     * this Information the {@link SharedCourseAccess} class offers some of the
     * standard functionalities. Similar Functionalities are Offered as well by the
     * {@link SharedRoboTaxiUtils} class. Take a look at these two clases when
     * implementing Dispatchers Further information can be pulled from this menu by
     * using standard List functionalities.
     * 
     * @return An unmodifiable {@link List} of {@link SharedCourse}s which can only
     *         be read but not modified */
    public List<SharedCourse> getUnmodifiableViewOfCourses() {
        return menu.getCourseList();
    }

    public long getOnBoardPassengers() {
        return menu.getMenuOnBoardCustomers();
    }

    /** Modifies the menu of the RoboTaxi. The given course is moved up in the menu
     * by one position.
     * 
     * @param sharedCourse */
    public void moveAVCourseToPrev(SharedCourse sharedCourse) {
        setMenu(SharedCourseMove.moveAVCourseToPrev(menu, sharedCourse));
    }

    /** Modifies the menu of the RoboTaxi. The given course is moved down in the menu
     * by one position.
     * 
     * @param sharedCourse */
    public void moveAVCourseToNext(SharedCourse sharedCourse) {
        setMenu(SharedCourseMove.moveAVCourseToNext(menu, sharedCourse));
    }

    /** This function allows to update the menu of the RoboTaxi with a new ordered
     * menu. Thereby the new menu has to fulfill the following conditions: 1. The
     * exact same Courses have to be in the Menu. 2. The menu can not plan to pickup
     * more persons than the capacity of the RoboTaxi at any time
     * 
     * @param menu */
    private void updateMenu(SharedMenu menu) {
        GlobalAssert.that(SharedMenuCheck.containSameCourses(this.menu, menu));
        GlobalAssert.that(Compatibility.of(getUnmodifiableViewOfCourses()).forCapacity(getCapacity()));
        setMenu(menu);
    }

    /** This function allows to update the menu of the RoboTaxi with a new List of
     * Shared Courses. Thereby the new menu has to fulfill the following conditions:
     * 1. The exact same Courses have to be in the Menu. 2. The menu can not plan to
     * pickup more persons than the capacity of the Robo Taxi at any Time 3. The
     * menu has to be consistent in itself (i.e. for each pickup a dropoff of the
     * same request is present, for each request the dropoff occurs after the pickup
     * and no course apears exactely once)
     * 
     * If a Dropoff is currently in progress then this course can not be moved away
     * from the first position. All other changes are still possible. If a dropoff
     * is in progress if the divertable link of the robotaxi equals the link of the
     * Dropoff Course.
     * 
     * @param list {@link List<SharedCourse>} */
    public void updateMenu(List<SharedCourse> list) {
        updateMenu(SharedMenu.of(list));
    }

    /** This function is only for internal use. It should not be allowed that the
     * menu can be changed from outside of the RoboTaxi directly.
     * 
     * @param menu */
    private final void setMenu(SharedMenu menu) {
        GlobalAssert.that(Compatibility.of(menu.getCourseList()).forCapacity(getCapacity()));
        if (dropoffInProgress) {
            GlobalAssert.that(this.menu.getCourseList().get(0).equals(menu.getCourseList().get(0)));
        }
        this.menu = menu;
        this.status = SharedRoboTaxiUtils.calculateStatusFromMenu(this);
    }

    /* package */ void addPassengerRequestToMenu(PassengerRequest avRequest) {
        // TODO @ChengQi what is the wanted behaviour? shouldn't the
        // dispatcher take care of this? We could bring it into the rebalancing dispatcher,
        // there we can add a function which is called:
        // addAVrequestandRemoveFirstRebalancing(AVrequest)
        if (status.equals(RoboTaxiStatus.REBALANCEDRIVE)) {
            GlobalAssert.that(SharedCourseAccess.getStarter(this).get().getMealType().equals(SharedMealType.REDIRECT));
            if (getUnmodifiableViewOfCourses().size() == 1)
                finishRedirection();
        }
        SharedCourse pickupCourse = SharedCourse.pickupCourse(avRequest);
        SharedCourse dropoffCourse = SharedCourse.dropoffCourse(avRequest);
        setMenu(SharedCourseAdd.asDessert(menu, pickupCourse, dropoffCourse));
    }

    // we should only allow one redirect course at the menu. --Luc, on 9 May 2019, after one
    // afternoon's debugging.
    /* package */ void addRedirectCourseToMenu(SharedCourse redirectCourse) {
        GlobalAssert.that(redirectCourse.getMealType().equals(SharedMealType.REDIRECT));
        // this if statment is added by Luc and that fix the problem
        if (status.equals(RoboTaxiStatus.REBALANCEDRIVE))
            finishRedirection();
        setMenu(SharedCourseAdd.asDessert(menu, redirectCourse));
    }

    /* package */ void addRedirectCourseToMenuAtBegining(SharedCourse redirectCourse) {
        GlobalAssert.that(redirectCourse.getMealType().equals(SharedMealType.REDIRECT));
        setMenu(SharedCourseAdd.asStarter(menu, redirectCourse));
    }

    /* package */ void pickupNewCustomerOnBoard() {
        GlobalAssert.that(SharedRoboTaxiUtils.isNextCourseOfType(this, SharedMealType.PICKUP));
        GlobalAssert.that(SharedRoboTaxiUtils.getStarterLink(this).equals(getDivertableLocation()));
        setMenu(SharedCourseRemove.starter(menu));
    }

    /* package */ void pickupOf(List<PassengerRequest> avrs) {
        for (PassengerRequest avr : avrs)
            setMenu(SharedCourseRemove.several(menu, SharedCourse.pickupCourse(avr)));
    }

    /* package */ void dropOffCustomer() {
        checkAbilityToDropOff();
        dropoffInProgress = false;
        setMenu(SharedCourseRemove.starter(menu));
    }

    private void checkAbilityToDropOff() {
        GlobalAssert.that(menu.getMenuOnBoardCustomers() > 0);
        GlobalAssert.that(menu.getMenuOnBoardCustomers() <= getCapacity());
        GlobalAssert.that(SharedRoboTaxiUtils.isNextCourseOfType(this, SharedMealType.DROPOFF));
        GlobalAssert.that(SharedRoboTaxiUtils.getStarterLink(this).equals(getDivertableLocation()));
    }

    /* package */ void startDropoff() {
        checkAbilityToDropOff();
        dropoffInProgress = true;
    }

    /* package */ void finishRedirection() {
        GlobalAssert.that(SharedCourseAccess.hasStarter(this));
        GlobalAssert.that(SharedRoboTaxiUtils.isNextCourseOfType(this, SharedMealType.REDIRECT));
        setMenu(SharedCourseRemove.starter(menu));
    }

    /** Removes an AV Request from the Robo Taxi Menu. This function can only be
     * called if the Request has not been picked up
     * 
     * @param avRequest */
    /* package */ void removePassengerRequestFromMenu(PassengerRequest avRequest) {
        SharedCourse pickupCourse = SharedCourse.pickupCourse(avRequest);
        SharedCourse dropoffCourse = SharedCourse.dropoffCourse(avRequest);
        GlobalAssert.that(menu.getCourseList().contains(pickupCourse) && menu.getCourseList().contains(dropoffCourse));
        setMenu(SharedCourseRemove.several(menu, pickupCourse, dropoffCourse));
    }

    /** This function deletes all the current Courses from the menu.
     * 
     * @return all the courses which have been removed */
    /* package */ List<SharedCourse> cleanAndAbandonMenu() {
        GlobalAssert.that(menu.getMenuOnBoardCustomers() == 0);
        GlobalAssert.that(isDivertable());
        List<SharedCourse> oldMenu = SharedCourseUtil.copy(menu.getCourseList());
        setMenu(SharedMenu.empty());
        return oldMenu;
    }

}