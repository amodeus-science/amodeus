/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.List;
import java.util.Objects;
import java.util.Set;

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
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMenuUtils;
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

    /** capacity > 1 fields */
    // TODO remove this field as it is redundant information to the RoboTaxiMenu
    private int onBoardCustomers = 0;
    private SharedMenu menu = SharedMenu.empty();;

    /** Standard constructor
     * 
     * @param avVehicle binding association to MATSim AVVehicle object
     * @param linkTimePair
     * @param driveDestination */
    RoboTaxi(AVVehicle avVehicle, LinkTimePair divertableLinkTime, Link driveDestination, RoboTaxiUsageType usageType) {
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
        this.status = Objects.requireNonNull(status);
    }

    /** @return true if robotaxi is without a customer */
    /* package */ boolean isWithoutCustomer() {
        // For now this works with universal dispatcher i.e. single used robotaxis as number of customers is never changed
        return !status.equals(RoboTaxiStatus.DRIVEWITHCUSTOMER) && getCurrentNumberOfCustomersOnBoard() == 0;
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
        if (!isWithoutDirective()) {
            System.out.println("here");
        }
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

        // TODO check why this appears often
        if (avT instanceof AVStayTask) {
            // TODO MISC For now, this works, but probably needs fixing somewhere upfront /sh, apr 2018
            logger.warn("RoboTaxiStatus != STAY, but Schedule.getCurrentTask() == AVStayTask; probably needs fixing");
            System.out.println("status: " + status);
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

    // TODO discuss with jan if this makes sense or if just a list should be given back
    // TODO Lukas CHECK WHERE THIS FUNCTION IS CALLED! THIS MIGHT GIVE A NICE INDICATION OF WHAT TO CHANGE NEXT
    public SharedMenu getCopyOfMenu() {
        return SharedMenu.of(menu.getRoboTaxiMenu());
    }

    public void moveAVCourseToPrev(SharedCourse sharedCourse) {
        menu = SharedMenuUtils.moveAVCourseToPrev(menu, sharedCourse);
    }
    
    public void moveAVCourseToNext(SharedCourse sharedCourse) {
        menu = SharedMenuUtils.moveAVCourseToNext(menu, sharedCourse);
    }
    
    public void updateMenu(SharedMenu menu) {
        // TODO Discuss with Claudio/ Jan if this should throw an error or just ignore the command
        GlobalAssert.that(checkMenuConsistency());
        GlobalAssert.that(SharedMenuUtils.containSameCourses(this.menu, menu));
        this.menu = menu;
    }

    // TODO MAKE THESE FUNCTIONS INTO A STATIC FUNCTIONS (e.g. RoboTaxiUtils)
    public boolean canPickupNewCustomer() {
        return getCurrentNumberOfCustomersOnBoard() >= 0 && getCurrentNumberOfCustomersOnBoard() < getCapacity();
    }

    public int getCurrentNumberOfCustomersOnBoard() {
        // TODO remove onboard customers in the future this
        GlobalAssert.that(onBoardCustomers == SharedCourseListUtils.getNumberCustomersOnBoard(menu.getRoboTaxiMenu()));
        return onBoardCustomers;
    }

    public boolean checkMenuConsistency() {
        return checkMenuDoesNotPlanToPickUpMoreCustomersThanCapacity();
    }

    public boolean checkMenuDoesNotPlanToPickUpMoreCustomersThanCapacity() {
        int futureNumberCustomers = getCurrentNumberOfCustomersOnBoard();
        for (SharedCourse sharedAVCourse : menu.getRoboTaxiMenu()) {
            if (sharedAVCourse.getMealType().equals(SharedMealType.PICKUP)) {
                futureNumberCustomers++;
            } else if (sharedAVCourse.getMealType().equals(SharedMealType.DROPOFF)) {
                futureNumberCustomers--;
            } else if (sharedAVCourse.getMealType().equals(SharedMealType.REDIRECT)) {
                // --
            } else {
                throw new IllegalArgumentException("Unknown SharedAVMealType -- please specify it !!!--");
            }
            if (futureNumberCustomers > getCapacity()) {
                return false;
            }
        }
        return true;
    }

    public Set<String> getAVRequestIdsOnBoard() {
        return SharedCourseListUtils.getOnBoardRequestIds(menu.getRoboTaxiMenu());
    }

    /* package */ void addAVRequestToMenu(AVRequest avRequest) {
        menu = SharedMenuUtils.addAVCourseAsDessert(menu, SharedCourse.pickupCourse(avRequest));
        menu = SharedMenuUtils.addAVCourseAsDessert(menu, SharedCourse.dropoffCourse(avRequest));
    }
    /* package */ void addRedirectCourseToMenu(SharedCourse redirectCourse) {
        GlobalAssert.that(menu.getStarterCourse().getMealType().equals(SharedMealType.REDIRECT));
        menu = SharedMenuUtils.addAVCourseAsDessert(menu, redirectCourse);
    }

    /* package */ void pickupNewCustomerOnBoard() {
        // TODO check these Global Asserts
        GlobalAssert.that(canPickupNewCustomer());
        GlobalAssert.that(menu.getStarterCourse().getMealType().equals(SharedMealType.PICKUP));
        // TODO This should be removed so that we are independant of this
        onBoardCustomers++;
        menu = SharedMenuUtils.removeStarterCourse(menu);
    }

    /* package */ void dropOffCustomer() {
        // TODO Check this Global Asserts
        GlobalAssert.that(getCurrentNumberOfCustomersOnBoard() > 0);
        GlobalAssert.that(getCurrentNumberOfCustomersOnBoard() <= getCapacity());
        GlobalAssert.that(menu.getStarterCourse().getMealType().equals(SharedMealType.DROPOFF));
        // TODO This should be removed so that we are independant of this
        onBoardCustomers--;
        menu = SharedMenuUtils.removeStarterCourse(menu);
    }

    /* package */ void finishRedirection() {
        GlobalAssert.that(menu.hasStarter());
        GlobalAssert.that(menu.getStarterCourse().getMealType().equals(SharedMealType.REDIRECT));
        GlobalAssert.that(menu.getStarterCourse().getLink().equals(getDivertableLocation()));
        menu = SharedMenuUtils.removeStarterCourse(menu);
    }

    /** Removes an AV Request from the Robo Taxi Menu. This function can only be called if the Request has not been picked up
     * 
     * @param avRequest */
    /* package */ void removeAVRequestFromMenu(AVRequest avRequest) {
        SharedCourse pickupCourse = SharedCourse.pickupCourse(avRequest);
        SharedCourse dropoffCourse = SharedCourse.dropoffCourse(avRequest);
        GlobalAssert.that(menu.getRoboTaxiMenu().contains(pickupCourse) && menu.getRoboTaxiMenu().contains(dropoffCourse));
        menu = SharedMenuUtils.removeAVCourse(menu, pickupCourse);
        menu = SharedMenuUtils.removeAVCourse(menu, dropoffCourse);
    }
    
    /**
     * This function deletes all the current Courses from the menu. 
     * @return all the courses which have been removed
     */
    /*package*/ List<SharedCourse> cleanAndAbandonMenu(){
        List<SharedCourse> oldMenu = menu.getModifiableCopyOfMenu();
        menu = SharedMenu.empty();
        return oldMenu;
    }

}