/* amod - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.simulation;

import java.io.File;
import java.util.Objects;

import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;

import com.google.inject.Key;
import com.google.inject.name.Names;

import ch.ethz.idsc.amodeus.analysis.Analysis;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusDatabaseModule;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusDispatcherModule;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusModule;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusVehicleGeneratorModule;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusVirtualNetworkModule;
import ch.ethz.idsc.amodeus.net.DatabaseModule;
import ch.ethz.idsc.amodeus.net.SimulationServer;
import ch.ethz.matsim.av.framework.AVModule;

public enum ScenarioServerHelper {

    ;
    /** Sets up a standard configuration of the MATSim environment.
     * The Folowing Modules are added:
     * {@link AVModule}
     * {@link DvrpTravelTimeModule}
     * {@link DatabaseModule}
     * {@link AmodeusModule}
     * {@link AmodeusVehicleGeneratorModule}
     * {@link AmodeusDispatcherModule}
     * {@link AmodeusVirtualNetworkModule}
     * {@link AmodeusDatabaseModule}
     * {@link IDSCGeneratorModule}
     * {@link IDSCDispatcherModule}
     * dvrp_routing
     * 
     * @param simulationProperties
     * @return */
    public static Controler setUpStandardControlerAmodeus(SimulationProperties simulationProperties) {
        Controler controler = new Controler(simulationProperties.scenario);

        controler.addOverridingModule(new DvrpTravelTimeModule());
        controler.addOverridingModule(new AVModule());
        controler.addOverridingModule(new DatabaseModule());
        controler.addOverridingModule(new AmodeusModule());
        controler.addOverridingModule(new AmodeusVehicleGeneratorModule());
        controler.addOverridingModule(new AmodeusDispatcherModule());
        controler.addOverridingModule(new AmodeusDatabaseModule(simulationProperties.db));
        controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                bind(Key.get(Network.class, Names.named("dvrp_routing"))).to(Network.class);
            }
        });

        return controler;
    }

    /** open server port for clients to connect to
     * 
     * @param simulationProperties */
    public static void startServerPort(SimulationProperties simulationProperties) {
        SimulationServer.INSTANCE.startAcceptingNonBlocking();
        boolean waitForClients = simulationProperties.getScenarioOptions().getBoolean("waitForClients");
        SimulationServer.INSTANCE.setWaitForClients(waitForClients);
    }

    /** stop the Server Port.
     * call this function after running the controller
     * 
     * @param simulationProperties */
    public static void stopPort(SimulationProperties simulationProperties) {
        /** close port for visualizaiton */
        SimulationServer.INSTANCE.stopAccepting();
    }

    /** Sets a typical Duration for each Activity Param if not set yet.
     * 
     * @param simulationProperties */
    public static void setActivityDurations(SimulationProperties simulationProperties, double typicalDuration) {
        simulationProperties.config.planCalcScore().addActivityParams(new ActivityParams("activity"));
        for (ActivityParams activityParams : simulationProperties.config.planCalcScore().getActivityParams()) {
            activityParams.setTypicalDuration(typicalDuration);
        }
    }

    public static Analysis setUpAnalysis(SimulationProperties simulationProperties) {
        String outputDirectory = simulationProperties.config.controler().getOutputDirectory();
        Analysis analysis;
        try {
            analysis = Analysis.setup(simulationProperties.workingDirectory, simulationProperties.configFile, new File(outputDirectory), simulationProperties.db);
        } catch (Exception e) {
            System.err.println("The Analysis Is not born yet. Are you by coincidence a doctor? No? But you are the only one around to help.");
            e.printStackTrace();
            throw new RuntimeException();
        }
        return Objects.requireNonNull(analysis);
    }

    public static void runAnalysis(Analysis analysis) {
        try {
            analysis.run();
        } catch (Exception e) {
            System.err.println(
                    "The Analysis needs your profesional help. It is not as durable and robust as you are it. So check out what went wrong to help this well developing child in its way to adolescence");
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

}
