/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.report;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.element.DistanceDistributionOverDayImage;
import ch.ethz.idsc.amodeus.analysis.element.EmptyRoboTaxiStatusDistribution;
import ch.ethz.idsc.amodeus.analysis.element.OccupancyDistanceRatiosImage;
import ch.ethz.idsc.amodeus.analysis.element.StatusDistributionElement;
import ch.ethz.idsc.amodeus.analysis.plot.CompositionStack;
import ch.ethz.idsc.amodeus.analysis.shared.NumberPassengerStatusDistribution;
import ch.ethz.idsc.amodeus.analysis.shared.RideSharingDistributionCompositionStack;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.red.Max;

public enum FleetEfficiencyHtml implements HtmlReportElement {
    INSTANCE;

    private static final String IMAGE_FOLDER = "../data"; // relative to report folder

    @Override
    public Map<String, HtmlBodyElement> process(AnalysisSummary analysisSummary) {
        Map<String, HtmlBodyElement> bodyElements = new HashMap<>();
        StatusDistributionElement sDE = analysisSummary.getStatusDistribution();
        Tensor statusesTensor = Transpose.of(sDE.statusTensor);
        Tensor notWithCustomer = Tensors.empty();
        notWithCustomer.append(statusesTensor.get(RoboTaxiStatus.DRIVETOCUSTOMER.ordinal()));
        notWithCustomer.append(statusesTensor.get(RoboTaxiStatus.REBALANCEDRIVE.ordinal()));
        notWithCustomer.append(statusesTensor.get(RoboTaxiStatus.PARKING.ordinal()));
        
        Tensor totalOverDay = notWithCustomer.stream().reduce(Tensor::add).get();
        double maxEmptyRoboTaxi = totalOverDay.stream().reduce(Max::of).get().Get().number().doubleValue();
        
        Tensor waitingVehicles = Tensors.empty();
        waitingVehicles.append(statusesTensor.get(RoboTaxiStatus.WAITING.ordinal()));
                
        double maxWaitingRoboTaxi = 0;
        
        if(waitingVehicles.equals(Tensors.empty())) {
        	maxWaitingRoboTaxi = waitingVehicles.stream().reduce(Max::of).get().Get().number().doubleValue();
        }
                
        // Fleet Efficency
        HtmlBodyElement fEElement = new HtmlBodyElement();
        fEElement.getHTMLGenerator().insertTextLeft("Maximum Empty Driving Vehicles:" + //
        		"\n" + "Maximum Waiting Vehicles");
        fEElement.getHTMLGenerator().insertTextLeft(String.valueOf(maxEmptyRoboTaxi) + //
        		"\n" + String.valueOf(maxWaitingRoboTaxi));
        fEElement.getHTMLGenerator().newLine();
        fEElement.getHTMLGenerator().insertImg(IMAGE_FOLDER + "/" + DistanceDistributionOverDayImage.FILENAME + ".png", 800, 600);
        fEElement.getHTMLGenerator().insertImg(IMAGE_FOLDER + "/" + OccupancyDistanceRatiosImage.FILENAME + ".png", 800, 600);
        fEElement.getHTMLGenerator().insertImg(IMAGE_FOLDER + "/" + NumberPassengerStatusDistribution.FILENAME + ".png", 800, 600);
        fEElement.getHTMLGenerator().insertImg(IMAGE_FOLDER + "/" + EmptyRoboTaxiStatusDistribution.FILENAME + ".png", 800, 600);
        fEElement.getHTMLGenerator().insertImg(IMAGE_FOLDER + "/" + RideSharingDistributionCompositionStack.FILENAME + ".png", CompositionStack.WIDTH, CompositionStack.HEIGHT);
        bodyElements.put(BodyElementKeys.FLEETEFFICIENCY, fEElement);
        return bodyElements;
    }

}
