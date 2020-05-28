/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.highcap;

import java.util.List;
import java.util.Set;

import org.matsim.amodeus.dvrp.request.AVRequest;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;

/* package */ enum RunILP {
    ;
    public static List<Double> of(List<TripWithVehicle> grossListOfRTVEdges, List<AVRequest> openRequestList, //
            List<RoboTaxi> listOfRoboTaxiWithValidTrip, double costOfIgnoredReuqestNormal, //
            double costOfIgnoredReuqestHigh, Set<AVRequest> requestMatchedLastStep) {

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
