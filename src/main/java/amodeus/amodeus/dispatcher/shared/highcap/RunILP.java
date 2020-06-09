/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.highcap;

import java.util.List;
import java.util.Set;

import org.matsim.contrib.dvrp.passenger.PassengerRequest;

import amodeus.amodeus.dispatcher.core.RoboTaxi;

/* package */ enum RunILP {
    ;

    public static List<Double> of(List<TripWithVehicle> grossListOfRTVEdges, List<PassengerRequest> openRequestList, //
            List<RoboTaxi> listOfRoboTaxiWithValidTrip, double costOfIgnoredReuqestNormal, //
            double costOfIgnoredReuqestHigh, Set<PassengerRequest> requestMatchedLastStep) {

        ILPConstruction iLPCode = new ILPConstruction();

        iLPCode.defineLP(grossListOfRTVEdges, openRequestList, listOfRoboTaxiWithValidTrip, //
                costOfIgnoredReuqestNormal, costOfIgnoredReuqestHigh, requestMatchedLastStep);
        // iLPCode.writeLPEquations();
        iLPCode.solveLP(true);
        List<Double> outputList = iLPCode.writeLPSolution();
        iLPCode.closeLP();

        // System.out.println(outputList);
        return outputList;
    }
}
