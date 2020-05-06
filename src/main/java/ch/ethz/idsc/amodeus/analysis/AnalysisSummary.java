/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

import ch.ethz.idsc.amodeus.analysis.element.DistanceElement;
import ch.ethz.idsc.amodeus.analysis.element.NumberPassengersAnalysis;
import ch.ethz.idsc.amodeus.analysis.element.RequestRobotaxiInformationElement;
import ch.ethz.idsc.amodeus.analysis.element.StatusDistributionElement;
import ch.ethz.idsc.amodeus.analysis.element.TravelTimeAnalysis;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;

/**
 * Analysis summary, builds and contains various default analysis elements
 */
public class AnalysisSummary implements Serializable {

    private final ScenarioParameters scenarioParameters;
    private final RequestRobotaxiInformationElement reqInfoElement;
    private final StatusDistributionElement statusDistribution = new StatusDistributionElement();
    private final DistanceElement distanceElement;
    private final TravelTimeAnalysis travelTimeAnalysis = new TravelTimeAnalysis();
    private final NumberPassengersAnalysis numberPassengersAnalysis = new NumberPassengersAnalysis();

    /**
     *
     * @param numVehicles - number of vehicles
     * @param size - not used as of now
     * @param db - amodeus database, default analysis elements will be computed based on the snapshot information therein
     * @param scenarioOptions - scenario options
     * @throws IOException
     */
    public AnalysisSummary(int numVehicles, int size, MatsimAmodeusDatabase db, //
            ScenarioOptions scenarioOptions) throws IOException {
        Objects.requireNonNull(db);
        reqInfoElement= new RequestRobotaxiInformationElement();
        distanceElement = new DistanceElement(numVehicles, size, db,reqInfoElement);
        scenarioParameters = new ScenarioParameters(scenarioOptions);

    }

    public ScenarioParameters getScenarioParameters() {
        return scenarioParameters;
    }

    public RequestRobotaxiInformationElement getSimulationInformationElement() {
        return reqInfoElement;
    }

    public StatusDistributionElement getStatusDistribution() {
        return statusDistribution;
    }

    public DistanceElement getDistanceElement() {
        return distanceElement;
    }

    public TravelTimeAnalysis getTravelTimeAnalysis() {
        return travelTimeAnalysis;
    }

    public NumberPassengersAnalysis getNumberPassengersAnalysis() {
        return numberPassengersAnalysis;
    }
}
