/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import ch.ethz.idsc.amodeus.dispatcher.core.RequestStatus;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;

/* package */ enum RequestStatusParser {
    ;

    // 2nd constructor that can be called when both AVStatus are known for logging reasons
    public static RequestStatus parseRequestStatus(RoboTaxiStatus nowState, RoboTaxiStatus lastState) {
        return parse(nowState, lastState);
    }

    private static RequestStatus parse(RoboTaxiStatus nowState, RoboTaxiStatus lastState) {
        // Check change of AVStatus from the vehicle and map corresponding requestStatus
        switch (nowState) {
        case DRIVETOCUSTOMER:
            switch (lastState) {
            case STAY:
            case REBALANCEDRIVE:
            case OFFSERVICE:
                // case DRIVEWITHCUSTOMER:
                return RequestStatus.REQUESTED;
            case DRIVETOCUSTOMER:
                return RequestStatus.PICKUPDRIVE;
            case DRIVEWITHCUSTOMER:
                return RequestStatus.CANCELLED;
            default:
                break;
            }
        case DRIVEWITHCUSTOMER:
            switch (lastState) {
            case STAY:
            case REBALANCEDRIVE:
            case DRIVETOCUSTOMER:
            case OFFSERVICE:
                return RequestStatus.PICKUP;
            case DRIVEWITHCUSTOMER:
                return RequestStatus.DRIVING;
            default:
                break;
            }
        case STAY:
        case REBALANCEDRIVE:
        case OFFSERVICE:
            switch (lastState) {
            case DRIVETOCUSTOMER:
                return RequestStatus.CANCELLED;
            case DRIVEWITHCUSTOMER:
                return RequestStatus.DROPOFF;
            default:
                break;
            }
        default:
            break;
        }
        return RequestStatus.INVALID;
    }

}
