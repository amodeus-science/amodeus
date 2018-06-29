/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Objects;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.contrib.dvrp.util.LinkTimePair;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedAVCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedAVMealType;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedAVMenu;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.schedule.AVDriveTask;
import ch.ethz.matsim.av.schedule.AVStayTask;

/** RoboTaxi is central classs to be used in all dispatchers. Dispatchers control
 * a fleet of RoboTaxis, each is uniquely associated to an AVVehicle object in
 * MATSim.
 * 
 * @author Claudio Ruch */
public class RoboTaxi {
    // Standard Taxi Fields
    static private final Logger logger = Logger.getLogger(RoboTaxi.class);

    protected final AVVehicle avVehicle;
    private RoboTaxiStatus status;

    /** last known location of the RoboTaxi */
    private Link lastKnownLocation;
    /** drive destination of the RoboTaxi, null for stay task */
    private Link driveDestination;
    /** location/time pair from where / when RoboTaxi path can be altered. */
    private LinkTimePair divertableLinkTime;
    private AbstractDirective directive;

    // Shared Taxi Fields
    private int onBoardCustomers = 0;
    private final SharedAVMenu menu = new SharedAVMenu();

    /** Standard constructor
     * 
     * @param avVehicle binding association to MATSim AVVehicle object
     * @param linkTimePair
     * @param driveDestination */
    RoboTaxi(AVVehicle avVehicle, LinkTimePair divertableLinkTime, Link driveDestination) {
        this.avVehicle = avVehicle;
        this.divertableLinkTime = divertableLinkTime;
        this.driveDestination = Objects.requireNonNull(driveDestination);
        this.directive = null;
        this.status = RoboTaxiStatus.STAY;
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

    /** @return RoboTaxiPlan with RoboTaxiPlan.plans() Navigable Map containing all RoboTaxiPlanEntry
     *         elements sorted according to begin time */
    public RoboTaxiPlan getCurrentPlans(double time) {
        return RoboTaxiPlan.of(getSchedule(), time);
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

    /** @return true if customer is without a customer */
    /* package */ boolean isWithoutCustomer() {
        return !status.equals(RoboTaxiStatus.DRIVEWITHCUSTOMER);
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
        if (directive == null)
            return true;
        return false;
    }

    /** @return true if robotaxi is not driving on the last link of its drive task,
     *         used for filtering purposes as currently the roboTaxis cannot be rerouted
     *         when driving on the last link of their route */
    /* package */ boolean notDrivingOnLastLink() {
        if (status.equals(RoboTaxiStatus.STAY)) {
            return true;
        }
        Task avT = getSchedule().getCurrentTask();

        if (avT instanceof AVStayTask) {
            // TODO: For now, this works, but probably needs fixing somewhere upfront /sh, apr 2018
            logger.warn("RoboTaxiStatus != STAY, but Schedule.getCurrentTask() == AVStayTask; probably needs fixing");
            return true;
        }

        GlobalAssert.that(avT instanceof AVDriveTask);
        AVDriveTask avDT = (AVDriveTask) avT;
        if (avDT.getPath().getLinkCount() == 1) {
            return false;
        }
        return true;
    }

    /** execute the directive of a RoboTaxi, to be used only inside core package */
    /* package */ void executeDirective() {
        directive.execute();
        directive = null;
    }

    // **********************************************
    // Definition Of Divertable
    // **********************************************

    public boolean isDivertable() {
        if (getCapacity() == 1) {
            return isWithoutDirective() && isWithoutCustomer() && notDrivingOnLastLink();
        } else if (getCapacity() > 1) {
            return isWithoutDirective() && notDrivingOnLastLink();
        } else {
            throw new IllegalArgumentException("Capacity smaller 1 is not allowed");
        }

    }

    // **********************************************
    // Shared Functionalities
    // **********************************************

    /* package */ void pickupNewCustomerOnBoard() {
        GlobalAssert.that(canPickupNewCustomer());
        GlobalAssert.that(menu.getStarterCourse().getPickupOrDropOff().equals(SharedAVMealType.PICKUP));
        onBoardCustomers++;
        menu.removeAVCourse(0);
    }

    public boolean canPickupNewCustomer() {
        return onBoardCustomers >= 0 && onBoardCustomers < getCapacity();
    }

    /* package */ void dropOffCustomer() {
        GlobalAssert.that(onBoardCustomers > 0);
        GlobalAssert.that(onBoardCustomers <= getCapacity());
        GlobalAssert.that(menu.getStarterCourse().getPickupOrDropOff().equals(SharedAVMealType.DROPOFF));
        onBoardCustomers--;
        menu.removeAVCourse(0);
    }

    public int getCurrentNumberOfCustomersOnBoard() {
        return onBoardCustomers;
    }

    /* package */ boolean hasAtLeastXSeatsFree(int x) {
        return getCapacity() - onBoardCustomers >= x;
    }

    public SharedAVMenu getMenu() {
        return menu;
    }

    public boolean checkMenuDoesNotPlanToPickUpMoreCustomersThanCapacity() {
        int futureNumberCustomers = getCurrentNumberOfCustomersOnBoard();
        for (SharedAVCourse sharedAVCourse : menu.getCourses()) {
            if (sharedAVCourse.getPickupOrDropOff().equals(SharedAVMealType.PICKUP)) {
                futureNumberCustomers++;
            } else if (sharedAVCourse.getPickupOrDropOff().equals(SharedAVMealType.DROPOFF)) {
                futureNumberCustomers--;
            } else {
                throw new IllegalArgumentException("Unknown SharedAVMealType -- please specify it !!!--");
            }
            if (futureNumberCustomers > getCapacity()) {
                return false;
            }
        }
        return true;
    }

}