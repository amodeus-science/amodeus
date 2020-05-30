/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package org.matsim.amodeus.framework;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.zip.DataFormatException;

import org.apache.log4j.Logger;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.amodeus.config.modal.DispatcherConfig;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.dvrp.run.AbstractDvrpModeModule;
import org.matsim.contrib.dvrp.run.ModalProviders.InstanceGetter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;

import com.google.inject.Singleton;
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

/** provides the {@link VirtualNetwork} and {@link TravelData} and therefore {@link VirtualNetworkPreparer} has to be run in the Preparer */
public class VirtualNetworkModeModule extends AbstractDvrpModeModule {
    private final static Logger logger = Logger.getLogger(VirtualNetworkModeModule.class);

    private final AmodeusModeConfig modeConfig;

    public VirtualNetworkModeModule(AmodeusModeConfig modeConfig) {
        super(modeConfig.getMode());
        this.modeConfig = modeConfig;
    }

    @Override
    public void install() {
        installVirtualNetwork();
        installTravelData();
    }

    static private VirtualNetwork<Link> provideVirtualNetworkFromScenarioOptions(InstanceGetter getter) {
        try {
            AmodeusModeConfig modeConfig = getter.getModal(AmodeusModeConfig.class);
            ScenarioOptions scenarioOptions = getter.get(ScenarioOptions.class);

            logger.info(String.format("Loading VirtualNetwork for mode '%s' from ScenarioOptions:", modeConfig.getMode()));
            logger.info(String.format("  - creator: ", scenarioOptions.getVirtualNetworkCreator().getClass().getSimpleName()));
            logger.info(String.format("  - name: ", scenarioOptions.getVirtualNetworkName()));

            Network network = getter.getModal(Network.class);
            return VirtualNetworkGet.readDefault(network, scenarioOptions);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static private VirtualNetwork<Link> provideVirtualNetworkFromConfig(InstanceGetter getter) {
        try {
            ScenarioOptions scenarioOptions = getter.get(ScenarioOptions.class);
            Config config = getter.get(Config.class);
            Population population = getter.get(Population.class);

            AmodeusModeConfig modeConfig = getter.getModal(AmodeusModeConfig.class);
            DispatcherConfig dispatcherConfig = modeConfig.getDispatcherConfig();
            Network network = getter.getModal(Network.class);

            URL virtualNetworkUrl = ConfigGroup.getInputFileURL(config.getContext(), dispatcherConfig.getVirtualNetworkPath());
            File virtualNetworkFile = new File(virtualNetworkUrl.getPath());

            if (!virtualNetworkFile.exists() || dispatcherConfig.getRegenerateVirtualNetwork()) {
                logger.info(String.format("Regenerating VirtualNetwork for mode '%s' at '%s'", modeConfig.getMode(), virtualNetworkFile));
                logger.info("Currently we use information from ScenarioOptions for that. Later on this should be moved to a specific config module.");
                logger.info(String.format("Using VirtualNetworkCreator: %s", scenarioOptions.getVirtualNetworkCreator().getClass().getSimpleName()));

                int numberOfVehicles = modeConfig.getGeneratorConfig().getNumberOfVehicles();
                VirtualNetwork<Link> virtualNetwork = scenarioOptions.getVirtualNetworkCreator().create(network, population, scenarioOptions, numberOfVehicles, //
                        (int) config.qsim().getEndTime().seconds());

                VirtualNetworkIO.toByte(virtualNetworkFile, virtualNetwork);
            }

            logger.info(String.format("Loading VirtualNetwork for operator '%s' from '%s'", modeConfig.getMode(), virtualNetworkFile));
            return VirtualNetworkGet.readFile(network, virtualNetworkFile);
        } catch (IOException | ClassNotFoundException | DataFormatException e) {
            throw new RuntimeException(e);
        }
    }

    public void installVirtualNetwork() {
        String virtualNetworkPath = modeConfig.getDispatcherConfig().getVirtualNetworkPath();

        if (virtualNetworkPath == null) {
            // If nothing is set in the configuration file, we fall back to ScenarioOptions.

            bindModal(new TypeLiteral<VirtualNetwork<Link>>() {
            }).toProvider(modalProvider(VirtualNetworkModeModule::provideVirtualNetworkFromScenarioOptions)).in(Singleton.class);
        } else {
            bindModal(new TypeLiteral<VirtualNetwork<Link>>() {
            }).toProvider(modalProvider(VirtualNetworkModeModule::provideVirtualNetworkFromConfig)).in(Singleton.class);
        }
    }

    static private TravelData provideTravelDataFromScenarioOptions(InstanceGetter getter) {
        AmodeusModeConfig modeConfig = getter.getModal(AmodeusModeConfig.class);
        ScenarioOptions scenarioOptions = getter.get(ScenarioOptions.class);

        logger.info(String.format("Loading TravelData for mode '%s' from ScenarioOptions:", modeConfig.getMode()));
        logger.info(String.format("  - name: ", scenarioOptions.getTravelDataName()));

        VirtualNetwork<Link> virtualNetwork = getter.getModal(new TypeLiteral<VirtualNetwork<Link>>() {
        });

        return TravelDataGet.readStatic(virtualNetwork, scenarioOptions);
    }

    static private TravelData provideTravelDataFromConfig(InstanceGetter getter) {
        try {
            AmodeusModeConfig modeConfig = getter.getModal(AmodeusModeConfig.class);
            DispatcherConfig dispatcherConfig = modeConfig.getDispatcherConfig();

            VirtualNetwork<Link> virtualNetwork = getter.getModal(new TypeLiteral<VirtualNetwork<Link>>() {
            });
            Network network = getter.getModal(Network.class);
            Config config = getter.get(Config.class);
            ScenarioOptions scenarioOptions = getter.get(ScenarioOptions.class);
            Population population = getter.get(Population.class);

            URL travelDataUrl = ConfigGroup.getInputFileURL(config.getContext(), dispatcherConfig.getTravelDataPath());
            File travelDataFile = new File(travelDataUrl.getPath());

            if (!travelDataFile.exists() || dispatcherConfig.getRegenerateTravelData()) {
                logger.info(String.format("Regenerating TravelData for mode '%s' at '%s'", modeConfig.getMode(), travelDataFile));
                logger.info("Currently we use information from ScenarioOptions for that. Later on this should be moved to a specific config module.");
                logger.info("Using StaticTravelDataCreator");

                File workingDirectory = new File(config.getContext().getPath()).getParentFile();
                int numberOfVehicles = modeConfig.getGeneratorConfig().getNumberOfVehicles();
                int interval = scenarioOptions.getdtTravelData();

                StaticTravelData travelData = StaticTravelDataCreator.create(workingDirectory, virtualNetwork, network, population, interval, numberOfVehicles, //
                        (int) config.qsim().getEndTime().seconds());
                TravelDataIO.writeStatic(travelDataFile, travelData);
            }

            logger.info(String.format("Loading TravelData for mode '%s' from '%s'", modeConfig.getMode(), travelDataFile));
            return TravelDataGet.readFile(virtualNetwork, travelDataFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void installTravelData() {
        String travelDataPath = modeConfig.getDispatcherConfig().getTravelDataPath();

        if (travelDataPath == null) {
            // If nothing is set in the configuration file, we fall back to ScenarioOptions.

            bindModal(TravelData.class).toProvider(modalProvider(VirtualNetworkModeModule::provideTravelDataFromScenarioOptions)).in(Singleton.class);
        } else {
            bindModal(TravelData.class).toProvider(modalProvider(VirtualNetworkModeModule::provideTravelDataFromConfig)).in(Singleton.class);
        }
    }
}
