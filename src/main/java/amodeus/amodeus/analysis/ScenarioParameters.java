/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.analysis;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.matsim.amodeus.config.AmodeusConfigGroup;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.amodeus.config.modal.DispatcherConfig;
import org.matsim.amodeus.config.modal.GeneratorConfig;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import amodeus.amodeus.analysis.report.TotalValueAppender;
import amodeus.amodeus.analysis.report.TotalValueIdentifier;
import amodeus.amodeus.analysis.report.TtlValIdent;
import amodeus.amodeus.options.ScenarioOptions;
import amodeus.amodeus.options.ScenarioOptionsBase;
import amodeus.amodeus.util.matsim.SafeConfig;
import amodeus.amodeus.virtualnetwork.core.VirtualNetwork;
import amodeus.amodeus.virtualnetwork.core.VirtualNetworkGet;
import ch.ethz.idsc.tensor.ext.UserName;

public class ScenarioParameters implements TotalValueAppender, Serializable {
    public static final int UNDEFINED_INT = -1;
    public static final String UNDEFINED_STRING = "";
    public static final DateFormat DATEFORMAT = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss");
    // ---
    public static final String DISPATCHPERIODSTRING = "dispatchPeriod";
    public static final String REBALANCINGPERIODSTRING = "rebalancingPeriod";
    public static final String DISTANCEHEURISTICSTRING = "distanceHeuristics";

    public final int populationSize;
    public final int iterations;
    public final int redispatchPeriod;
    public final int rebalancingPeriod;
    public final int virtualNodesCount;

    public final String dispatcher;
    public final String distanceHeuristic;
    public final String virtualNetworkCreator;
    public final String vehicleGenerator;
    public final String networkName;
    public final String user = UserName.get();
    public final String date = DATEFORMAT.format(new Date());

    public ScenarioParameters(ScenarioOptions scenOptions) {
        System.out.println("scenOptions.getSimulationConfigName: " + scenOptions.getSimulationConfigName());

        AmodeusConfigGroup avConfigGroup = new AmodeusConfigGroup();
        Config config = ConfigUtils.loadConfig(scenOptions.getSimulationConfigName(), avConfigGroup);

        // TODO: This assumes that there is only one mode registered for Amodeus. There are two ways to go
        // forward. Either we need to put in a check somewhere that when Amodeus is used with ScenarioOptions
        // that there is really only one mode registered. Alternatives are to make he ScenarioOptions multimodal
        // or to put all ScenarioOptions into the AmodeusModeConfig. This will happen anyways (for the MATSim
        // users, but I would keep a fallback to the ScenarioOptions file anyways).
        AmodeusModeConfig operatorConfig = avConfigGroup.getModes().values().iterator().next();
        DispatcherConfig dispatcherConfig = operatorConfig.getDispatcherConfig();
        SafeConfig safeConfig = SafeConfig.wrap(dispatcherConfig);
        GeneratorConfig generatorConfig = operatorConfig.getGeneratorConfig();

        redispatchPeriod = safeConfig.getInteger(DISPATCHPERIODSTRING, UNDEFINED_INT);
        rebalancingPeriod = safeConfig.getInteger(REBALANCINGPERIODSTRING, UNDEFINED_INT);
        dispatcher = dispatcherConfig.getType();
        vehicleGenerator = generatorConfig.getType();
        Scenario scenario = ScenarioUtils.loadScenario(config);

        distanceHeuristic = safeConfig.getString(DISTANCEHEURISTICSTRING, UNDEFINED_STRING);
        populationSize = scenario.getPopulation().getPersons().values().size();
        virtualNetworkCreator = scenOptions.getString(ScenarioOptionsBase.VIRTUALNETWORKCREATORIDENTIFIER);

        Network network = scenario.getNetwork();
        if (Objects.isNull(network.getName()))
            networkName = "no network name found in network.xml";
        else
            networkName = network.getName();

        VirtualNetwork<Link> virtualNetwork = null;
        try {
            virtualNetwork = VirtualNetworkGet.readDefault(network, scenOptions);
        } catch (IOException e) {
            System.err.println("INFO not able to load virtual network for report");
            e.printStackTrace();
        }

        virtualNodesCount = Objects.isNull(virtualNetwork) //
                ? UNDEFINED_INT
                : virtualNetwork.getvNodesCount();

        iterations = config.controler().getLastIteration();

    }

    public String getVirtualNetworkDescription() {
        return virtualNodesCount == UNDEFINED_INT //
                ? "no virtual network found"
                : virtualNodesCount + " virtual nodes.";
    }

    @Override
    public Map<TotalValueIdentifier, String> getTotalValues() {
        Map<TotalValueIdentifier, String> map = new HashMap<>();
        map.put(TtlValIdent.DISPATCHER, dispatcher);
        map.put(TtlValIdent.DISPATCHINGPERIOD, String.valueOf(redispatchPeriod));
        map.put(TtlValIdent.REBALANCEPERIOD, String.valueOf(rebalancingPeriod));
        map.put(TtlValIdent.DISTANCEHEURISTIC, String.valueOf(distanceHeuristic));
        map.put(TtlValIdent.POPULATIONSIZE, String.valueOf(populationSize));
        map.put(TtlValIdent.VIRTUALNODES, String.valueOf(virtualNodesCount));
        map.put(TtlValIdent.VEHICLEGENERATOR, vehicleGenerator);
        map.put(TtlValIdent.TIMESTAMP, date);
        return map;
    }

}
