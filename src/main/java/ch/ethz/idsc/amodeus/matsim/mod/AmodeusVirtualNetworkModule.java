/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.mod;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.zip.DataFormatException;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.dvrp.run.DvrpModes;
import org.matsim.contrib.dvrp.run.ModalProviders;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.controler.AbstractModule;

import com.google.inject.Inject;
import com.google.inject.TypeLiteral;

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

/** provides the {@link VirtualNetwork} and {@link TravelData} and therefore {@link VirtualNetworkPreparer} has to be run in the Preparer */
public class AmodeusVirtualNetworkModule extends AbstractModule {
    private final static Logger logger = Logger.getLogger(AmodeusVirtualNetworkModule.class);
    private final ScenarioOptions scenarioOptions;

    public AmodeusVirtualNetworkModule(ScenarioOptions scenarioOptions) {
        this.scenarioOptions = scenarioOptions;
    }

    @Override
    public void install() {
        installVirtualNetworks();
        installTravelDatas();
    }

    static private class VirtualNetworkProviderFromScenarioOptions extends ModalProviders.AbstractProvider<VirtualNetwork<Link>> {
        private final ScenarioOptions scenarioOptions;

        VirtualNetworkProviderFromScenarioOptions(String mode, ScenarioOptions scenarioOptions) {
            super(mode);
            this.scenarioOptions = scenarioOptions;
        }

        @Override
        public VirtualNetwork<Link> get() {
            try {
                OperatorConfig operatorConfig = getModalInstance(OperatorConfig.class);

                logger.info(String.format("Loading VirtualNetwork for operator '%s' from ScenarioOptions:", operatorConfig.getId()));
                logger.info(String.format("  - creator: ", scenarioOptions.getVirtualNetworkCreator().getClass().getSimpleName()));
                logger.info(String.format("  - name: ", scenarioOptions.getVirtualNetworkName()));

                Network network = getModalInstance(Network.class);
                return VirtualNetworkGet.readDefault(network, scenarioOptions);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static private class VirtualNeworkProviderFromConfig extends ModalProviders.AbstractProvider<VirtualNetwork<Link>> {
        private final ScenarioOptions scenarioOptions;

        @Inject
        Config config;

        @Inject
        Population population;

        VirtualNeworkProviderFromConfig(String mode, ScenarioOptions scenarioOptions) {
            super(mode);
            this.scenarioOptions = scenarioOptions;
        }

        @Override
        public VirtualNetwork<Link> get() {
            try {
                OperatorConfig operatorConfig = getModalInstance(OperatorConfig.class);
                Network network = getModalInstance(Network.class);

                String virtualNetworkPath = operatorConfig.getParams().get("virtualNetworkPath");
                URL virtualNetworkUrl = ConfigGroup.getInputFileURL(config.getContext(), virtualNetworkPath);
                File virtualNetworkFile = new File(virtualNetworkUrl.getPath());

                // TODO: Make this a proper config option!
                String regenerateVirtualNetworkParameter = operatorConfig.getParams().getOrDefault("regenerateVirtualNetwork", "true");
                boolean regenerateVirtualNetwork = Boolean.parseBoolean(regenerateVirtualNetworkParameter);

                if (!virtualNetworkFile.exists() || regenerateVirtualNetwork) {
                    logger.info(String.format("Regenerating VirtualNetwork for operator '%s' at '%s'", operatorConfig.getId(), virtualNetworkFile));
                    logger.info("Currently we use information from ScenarioOptions for that. Later on this should be moved to a specific config module.");
                    logger.info(String.format("Using VirtualNetworkCreator: %s", scenarioOptions.getVirtualNetworkCreator().getClass().getSimpleName()));

                    int numberOfVehicles = operatorConfig.getGeneratorConfig().getNumberOfVehicles();
                    VirtualNetwork<Link> virtualNetwork = scenarioOptions.getVirtualNetworkCreator().create(network, population, scenarioOptions, numberOfVehicles, //
                            (int) config.qsim().getEndTime().seconds());

                    VirtualNetworkIO.toByte(virtualNetworkFile, virtualNetwork);
                }

                logger.info(String.format("Loading VirtualNetwork for operator '%s' from '%s'", operatorConfig.getId(), virtualNetworkFile));
                return VirtualNetworkGet.readFile(network, virtualNetworkFile);
            } catch (IOException | ClassNotFoundException | DataFormatException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void installVirtualNetworks() {
        for (OperatorConfig operatorConfig : AVConfigGroup.getOrCreate(getConfig()).getOperatorConfigs().values()) {
            // TODO: Make this a proper option!
            String virtualNetworkPath = operatorConfig.getParams().get("virtualNetworkPath");

            if (virtualNetworkPath == null) {
                // If nothing is set in the configuration file, we fall back to ScenarioOptions.

                // TODO: Replace this one we have proper modes in config!
                bind(DvrpModes.key(new TypeLiteral<VirtualNetwork<Link>>() {
                }, "av")).toProvider( //
                        new VirtualNetworkProviderFromScenarioOptions("av", scenarioOptions));
            } else {
                bind(DvrpModes.key(new TypeLiteral<VirtualNetwork<Link>>() {
                }, "av")).toProvider( //
                        new VirtualNeworkProviderFromConfig("av", scenarioOptions));
            }
        }
    }

    static private class TravelDataProviderFromScenarioOptions extends ModalProviders.AbstractProvider<TravelData> {
        private final ScenarioOptions scenarioOptions;

        TravelDataProviderFromScenarioOptions(String mode, ScenarioOptions scenarioOptions) {
            super(mode);
            this.scenarioOptions = scenarioOptions;
        }

        @Override
        public TravelData get() {
            OperatorConfig operatorConfig = getModalInstance(OperatorConfig.class);

            logger.info(String.format("Loading TravelData for operator '%s' from ScenarioOptions:", operatorConfig.getId()));
            logger.info(String.format("  - name: ", scenarioOptions.getTravelDataName()));

            VirtualNetwork<Link> virtualNetwork = getModalInstance(new TypeLiteral<VirtualNetwork<Link>>() {
            });

            return TravelDataGet.readStatic(virtualNetwork, scenarioOptions);
        }
    }

    static private class TravelDataProviderFromConfig extends ModalProviders.AbstractProvider<TravelData> {
        private final ScenarioOptions scenarioOptions;

        @Inject
        Config config;

        @Inject
        Population population;

        TravelDataProviderFromConfig(String mode, ScenarioOptions scenarioOptions) {
            super(mode);
            this.scenarioOptions = scenarioOptions;
        }

        @Override
        public TravelData get() {
            try {
                OperatorConfig operatorConfig = getModalInstance(OperatorConfig.class);
                VirtualNetwork<Link> virtualNetwork = getModalInstance(new TypeLiteral<VirtualNetwork<Link>>() {
                });
                Network network = getModalInstance(Network.class);

                String travelDataPath = operatorConfig.getParams().get("travelDataPath");

                URL travelDataUrl = ConfigGroup.getInputFileURL(config.getContext(), travelDataPath);
                File travelDataFile = new File(travelDataUrl.getPath());

                // TODO: Make this a proper config option.
                String regenerateTravelDataParameter = operatorConfig.getParams().getOrDefault("regenerateTravelData", "true");
                boolean regenerateTravelData = Boolean.parseBoolean(regenerateTravelDataParameter);

                if (!travelDataFile.exists() || regenerateTravelData) {
                    logger.info(String.format("Regenerating TravelData for operator '%s' at '%s'", operatorConfig.getId(), travelDataFile));
                    logger.info("Currently we use information from ScenarioOptions for that. Later on this should be moved to a specific config module.");
                    logger.info("Using StaticTravelDataCreator");

                    File workingDirectory = new File(config.getContext().getPath());
                    int numberOfVehicles = operatorConfig.getGeneratorConfig().getNumberOfVehicles();
                    int interval = scenarioOptions.getdtTravelData();

                    StaticTravelData travelData = StaticTravelDataCreator.create(workingDirectory, virtualNetwork, network, population, interval, numberOfVehicles, //
                            (int) config.qsim().getEndTime().seconds());
                    TravelDataIO.writeStatic(travelDataFile, travelData);
                }

                logger.info(String.format("Loading TravelData for operator '%s' from '%s'", operatorConfig.getId(), travelDataFile));
                return TravelDataGet.readFile(virtualNetwork, travelDataFile);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void installTravelDatas() {
        for (OperatorConfig operatorConfig : AVConfigGroup.getOrCreate(getConfig()).getOperatorConfigs().values()) {
            String travelDataPath = operatorConfig.getParams().get("travelDataPath");

            if (travelDataPath == null) {
                // If nothing is set in the configuration file, we fall back to ScenarioOptions.

                // TODO: Update mode, once we use them properly!
                bind(DvrpModes.key(TravelData.class, "av")).toProvider( //
                        new TravelDataProviderFromScenarioOptions("av", scenarioOptions));
            } else {
                // TODO: Update mode, once we use them properly!
                bind(DvrpModes.key(TravelData.class, "av")).toProvider( //
                        new TravelDataProviderFromConfig("av", scenarioOptions));
            }
        }
    }
}
