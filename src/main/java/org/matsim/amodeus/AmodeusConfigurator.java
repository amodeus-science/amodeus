package org.matsim.amodeus;

import org.apache.log4j.Logger;
import org.matsim.amodeus.config.AmodeusConfigGroup;
import org.matsim.amodeus.framework.AmodeusModule;
import org.matsim.amodeus.framework.AmodeusQSimModule;
import org.matsim.amodeus.routing.AmodeusRoute;
import org.matsim.amodeus.routing.AmodeusRouteFactory;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.dvrp.run.DvrpModule;
import org.matsim.core.controler.Controler;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;

public class AmodeusConfigurator {
    private final static Logger logger = Logger.getLogger(AmodeusConfigurator.class);

    static public final String configurationChangedMessage = "" + //
            "Attention! The configuration of Amodeus has changed after a major refactoring and simplification " + //
            "of the project structure. In the MATSim configuration file there is no more 'av' module, but now a " + //
            "'amodeus' module. This module is not anymore divided into 'operators', but rather 'modes'. This is " + //
            "relevant for full MATSim simulation in which multiple AMoD modes should be simulated with different " + //
            "control policies, pricing schemes, etc. Have a look at resources/testScenario/config_full.xml for " + //
            "an example.";

    /** Configures a MATSim scenario for use with AMoDeus */
    static public void configureScenario(Scenario scenario) {
        scenario.getPopulation().getFactory().getRouteFactories().setRouteFactory(AmodeusRoute.class, new AmodeusRouteFactory());
    }

    /** Configures a MATSim controller for a standard use case of AMoDeus. */
    static public void configureController(Controler controller, MatsimAmodeusDatabase db, ScenarioOptions scenarioOptions) {
        if (controller.getConfig().getModules().containsKey("av")) {
            logger.warn(configurationChangedMessage);
            throw new RuntimeException();
        }

        controller.addOverridingModule(new DvrpModule());
        controller.addOverridingModule(new AmodeusModule(db, scenarioOptions));

        controller.addOverridingQSimModule(new AmodeusQSimModule());
        controller.configureQSimComponents(AmodeusQSimModule.activateModes(AmodeusConfigGroup.get(controller.getConfig())));
    }
}
