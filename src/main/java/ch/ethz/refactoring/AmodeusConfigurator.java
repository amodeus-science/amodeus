package ch.ethz.refactoring;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.run.DvrpModule;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;

import com.google.inject.Key;
import com.google.inject.name.Names;

import ch.ethz.idsc.amodeus.matsim.mod.AmodeusDatabaseModule;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusDispatcherModule;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusModule;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusQSimModule;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusRouterModule;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusVehicleGeneratorModule;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusVirtualNetworkModule;
import ch.ethz.idsc.amodeus.net.DatabaseModule;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.matsim.av.config.AmodeusConfigGroup;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.framework.AVQSimModule;

public class AmodeusConfigurator {
    private final static Logger logger = Logger.getLogger(AmodeusConfigurator.class);

    static public final String configurationChangedMessage = "" + //
            "Attention! The configuration of Amodeus has changed after a major refactoring and simplification " + //
            "of the project structure. In the MATSim configuration file there is no more 'av' module, but now a " + //
            "'amodeus' module. This module is not anymore divided into 'operators', but rather 'modes'. This is " + //
            "relevant for full MATSim simulation in which multiple AMoD modes should be simulated with different " + //
            "control policies, pricing schemes, etc. Have a look at resources/testScenario/config_full.xml for " + //
            "an example.";

    /** Configures a MATSim controller for a standard use case of AMoDeus. */
    static public void configureController(Controler controller, MatsimAmodeusDatabase db, ScenarioOptions scenarioOptions) {
        if (controller.getConfig().getModules().containsKey("av")) {
            logger.warn(configurationChangedMessage);
            throw new RuntimeException();
        }

        controller.addOverridingModule(new DvrpModule());
        controller.addOverridingModule(new DvrpTravelTimeModule());
        controller.addOverridingModule(new AVModule());
        controller.addOverridingModule(new DatabaseModule());
        controller.addOverridingModule(new AmodeusVehicleGeneratorModule());
        controller.addOverridingModule(new AmodeusDispatcherModule());
        controller.addOverridingModule(new AmodeusDatabaseModule(db));
        controller.addOverridingModule(new AmodeusVirtualNetworkModule(scenarioOptions));
        controller.addOverridingModule(new AmodeusModule());
        controller.addOverridingModule(new AmodeusRouterModule());

        controller.addOverridingQSimModule(new AmodeusQSimModule());
        controller.configureQSimComponents(AVQSimModule.activateModes(AmodeusConfigGroup.get(controller.getConfig())));

        controller.addOverridingModule(new AbstractModule() { // REFACTOR ? Not sure if this si still needed
            @Override
            public void install() {
                bind(Key.get(Network.class, Names.named("dvrp_routing"))).to(Network.class);
            }
        });
    }
}
