package ch.ethz.idsc.amodeus.dispatcher.core;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.util.LinkTimePair;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.data.AVVehicle;

public class SharedRoboTaxi extends AbstractRoboTaxi {

    private int onBoardCustomers = 0;

    SharedRoboTaxi(AVVehicle avVehicle, LinkTimePair divertableLinkTime, Link driveDestination) {
        super(avVehicle, divertableLinkTime, driveDestination);
    }

    public void pickupNewCustomerOnBoard() {
        GlobalAssert.that(canPickupNewCustomer());
        onBoardCustomers++;
    }

    public boolean canPickupNewCustomer() {
        return onBoardCustomers >= 0 && onBoardCustomers < (int) avVehicle.getCapacity();
    }

    public void dropOffCustomer() {
        GlobalAssert.that(onBoardCustomers > 0);
        GlobalAssert.that(onBoardCustomers <= (int) avVehicle.getCapacity());
        onBoardCustomers--;
    }

    public int getCurrentNumberOfCustomersOnBoard() {
        return onBoardCustomers;
    }

}
