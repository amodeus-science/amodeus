/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.mod;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.zip.DataFormatException;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.controler.AbstractModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.prep.VirtualNetworkPreparer;
import ch.ethz.idsc.amodeus.traveldata.StaticTravelData;
import ch.ethz.idsc.amodeus.traveldata.StaticTravelDataCreator;
import ch.ethz.idsc.amodeus.traveldata.TravelData;
import ch.ethz.idsc.amodeus.traveldata.TravelDataGet;
import ch.ethz.idsc.amodeus.traveldata.TravelDataIO;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetworkGet;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetworkIO;
import ch.ethz.matsim.av.config.AVConfigGroup;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.data.AVOperator;

/** provides the {@link VirtualNetwork} and {@link TravelData} and therefore {@link VirtualNetworkPreparer} has to be run in the Preparer */
public class AmodeusVirtualNetworkModule extends AbstractModule {
    private final Logger logger = Logger.getLogger(AmodeusVirtualNetworkModule.class);
    private final ScenarioOptions scenarioOptions;

    public AmodeusVirtualNetworkModule(ScenarioOptions scenarioOptions) {
        this.scenarioOptions = scenarioOptions;
    }

    @Override
    public void install() {
        // ---
    }

    @Provides
    @Singleton
    public Map<Id<AVOperator>, VirtualNetwork<Link>> provideVirtualNetworks(Config mainConfig, AVConfigGroup config, Map<Id<AVOperator>, Network> networks, Population population)
            throws ClassNotFoundException, DataFormatException, IOException {
        Map<Id<AVOperator>, VirtualNetwork<Link>> virtualNetworks = new HashMap<>();

        for (OperatorConfig operatorConfig : config.getOperatorConfigs().values()) {
            Network network = networks.get(operatorConfig.getId());
            VirtualNetwork<Link> virtualNetwork = null;

            String virtualNetworkPath = operatorConfig.getParams().get("virtualNetworkPath");

            if (Objects.isNull(virtualNetworkPath)) {
                // If nothing is set in the configuration file, we fall back to sceanrioOptions. This *may* return null.

                if (scenarioOptions.getVirtualNetworkName().trim().length() > 0) {
                    logger.info(String.format("Loading VirtualNetwork for operator '%s' from ScenarioOptions", operatorConfig.getId()));
                    virtualNetwork = VirtualNetworkGet.readDefault(network, scenarioOptions);
                } else
                    logger.info(String.format("Not loading any VirtualNetwork for operator '%s'", operatorConfig.getId()));
            } else {
                URL virtualNetworkUrl = ConfigGroup.getInputFileURL(mainConfig.getContext(), virtualNetworkPath);
                File virtualNetworkFile = new File(virtualNetworkUrl.getPath());

                String regenerateVirtualNetworkParameter = operatorConfig.getParams().getOrDefault("regenerateVirtualNetwork", "true");
                boolean regenerateVirtualNetwork = Boolean.parseBoolean(regenerateVirtualNetworkParameter);

                if (!virtualNetworkFile.exists() || regenerateVirtualNetwork) {
                    logger.info(String.format("Regenerating VirtualNetwork for operator '%s' at '%s'", operatorConfig.getId(), virtualNetworkFile));
                    logger.info("Currently we use information from ScenarioOptions for that. Later on this should be moved to a specific config module.");
                    logger.info(String.format("Using VirtualNetworkCreator: %s", scenarioOptions.getVirtualNetworkCreator().getClass().getSimpleName()));

                    int numberOfVehicles = operatorConfig.getGeneratorConfig().getNumberOfVehicles();
                    virtualNetwork = scenarioOptions.getVirtualNetworkCreator().create(network, population, scenarioOptions, numberOfVehicles, //
                            (int) mainConfig.qsim().getEndTime().seconds());

                    VirtualNetworkIO.toByte(virtualNetworkFile, virtualNetwork);
                } else
                    logger.info(String.format("Loading VirtualNetwork for operator '%s' from '%s'", operatorConfig.getId(), virtualNetworkFile));
                virtualNetwork = VirtualNetworkGet.readFile(network, virtualNetworkFile);
            }
            virtualNetworks.put(operatorConfig.getId(), virtualNetwork);
        }
        return virtualNetworks;
    }

    @Provides
    @Singleton
    public Map<Id<AVOperator>, TravelData> provideTravelDatas(Config mainConfig, AVConfigGroup config, Map<Id<AVOperator>, VirtualNetwork<Link>> virtualNetworks, //
            Map<Id<AVOperator>, Network> networks, Population population) throws Exception {
        Map<Id<AVOperator>, TravelData> travelDatas = new HashMap<>();

        for (OperatorConfig operatorConfig : config.getOperatorConfigs().values()) {
            VirtualNetwork<Link> virtualNetwork = virtualNetworks.get(operatorConfig.getId());
            Network network = networks.get(operatorConfig.getId());
            StaticTravelData travelData = null;

            String travelDataPath = operatorConfig.getParams().get("travelDataPath");

            if (Objects.nonNull(virtualNetwork)) {
                if (Objects.isNull(travelDataPath)) {
                    // If nothing is set in the configuration file, we fall back to sceanrioOptions. This *may* return null.
                    if (scenarioOptions.getVirtualNetworkName().trim().length() > 0) {
                        logger.info(String.format("Loading TravelData for operator '%s' from ScenarioOptions", operatorConfig.getId()));
                        travelData = TravelDataGet.readStatic(virtualNetwork, scenarioOptions);
                    } else
                        logger.info(String.format("Not loading any TravelData for operator '%s'", operatorConfig.getId()));
                } else {
                    URL travelDataUrl = ConfigGroup.getInputFileURL(mainConfig.getContext(), travelDataPath);
                    File travelDataFile = new File(travelDataUrl.getPath());

                    String regenerateTravelDataParameter = operatorConfig.getParams().getOrDefault("regenerateTravelData", "true");
                    boolean regenerateTravelData = Boolean.parseBoolean(regenerateTravelDataParameter);

                    if (!travelDataFile.exists() || regenerateTravelData) {
                        logger.info(String.format("Regenerating TravelData for operator '%s' at '%s'", operatorConfig.getId(), travelDataFile));
                        logger.info("Currently we use information from ScenarioOptions for that. Later on this should be moved to a specific config module.");
                        logger.info("Using StaticTravelDataCreator");

                        File workingDirectory = new File(mainConfig.getContext().getPath());
                        int numberOfVehicles = operatorConfig.getGeneratorConfig().getNumberOfVehicles();
                        int interval = scenarioOptions.getdtTravelData();

                        travelData = StaticTravelDataCreator.create(workingDirectory, virtualNetwork, network, population, interval, numberOfVehicles, //
                                (int) mainConfig.qsim().getEndTime().seconds());
                        TravelDataIO.writeStatic(travelDataFile, travelData);
                    } else {
                        logger.info(String.format("Loading TravelData for operator '%s' from '%s'", operatorConfig.getId(), travelDataFile));
                        travelData = TravelDataGet.readFile(virtualNetwork, travelDataFile);
                    }
                }
                travelDatas.put(operatorConfig.getId(), travelData);
            } else
                logger.info(String.format("Not loading any TravelData for operator '%s' because not VirtualNetwork is available", operatorConfig.getId()));
        }
        return travelDatas;
    }
}
