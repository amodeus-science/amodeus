/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.report;

import java.util.HashMap;
import java.util.Map;

import org.matsim.core.utils.misc.Time;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.ScenarioParameters;

public class ScenarioParametersHtml implements HtmlReportElement {

    @Override
    public Map<String, HtmlBodyElement> process(AnalysisSummary analysisSummary) {
        ScenarioParameters scenarioParameters = analysisSummary.getScenarioParameters();
        HtmlBodyElement bodyElement = new HtmlBodyElement();
        HtmlGenerator htmlUtils = bodyElement.getHTMLGenerator();
        htmlUtils.insertTextLeft("User:" + //
                "\nTimestamp:");
        htmlUtils.insertTextLeft(scenarioParameters.user + //
                "\n" + scenarioParameters.date);
        htmlUtils.newLine();
        htmlUtils.insertTextLeft("Iterations:");
        htmlUtils.insertTextLeft(String.valueOf(scenarioParameters.iterations + 1) + " ,i.e., " + //
                String.valueOf(scenarioParameters.iterations) + " in matsim config.");
        htmlUtils.newLine();
        htmlUtils.insertLink("av.xml", "AV File");
        htmlUtils.insertLink("av_config.xml", "AV_Config File");
        htmlUtils.newLine();
        htmlUtils.insertTextLeft("Dispatcher:" + //
                "\nRebalancing Period:" + //
                "\nRedispatching Period:");
        htmlUtils.insertTextLeft(scenarioParameters.dispatcher + //
                "\n" + Time.writeTime(scenarioParameters.rebalancingPeriod) + //
                "\n" + Time.writeTime(scenarioParameters.redispatchPeriod));
        htmlUtils.newLine();
        htmlUtils.insertTextLeft("Network:" + //
                "\nVirtual Nodes:" + //
                "\nPopulation:"// + //
        );
        htmlUtils.insertTextLeft(scenarioParameters.networkName + //
                "\n" + scenarioParameters.virtualNodes + //
                "\n" + scenarioParameters.populationSize // + //
        );
        Map<String, HtmlBodyElement> bodyElements = new HashMap<>();
        bodyElements.put(BodyElementKeys.SIMULATIONINFORMATION, bodyElement);
        return bodyElements;

    }

}
