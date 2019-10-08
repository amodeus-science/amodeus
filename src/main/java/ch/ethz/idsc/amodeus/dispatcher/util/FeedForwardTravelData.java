/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.io.File;
import java.io.FileNotFoundException;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import ch.ethz.idsc.amodeus.lp.LPCreator;
import ch.ethz.idsc.amodeus.lp.LPSolver;
import ch.ethz.idsc.amodeus.options.LPOptions;
import ch.ethz.idsc.amodeus.options.LPOptionsBase;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.traveldata.LambdaAbsolute;
import ch.ethz.idsc.amodeus.traveldata.StaticTravelData;
import ch.ethz.idsc.amodeus.traveldata.TravelDataIO;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.matsim.av.config.AVConfigGroup;
import ch.ethz.matsim.av.config.operator.GeneratorConfig;

/* package */ enum FeedForwardTravelData {
    ;

    public static void overwriteIfRequired(LPCreator lpCreator, StaticTravelData travelData, VirtualNetwork<Link> virtualNetwork, ScenarioOptions scenarioOptions) {
        if (!travelData.getLPName().equals(lpCreator.name())) {
            try {
                /** MATSim config */
                AVConfigGroup avConfigGroup = new AVConfigGroup();
                Config config = ConfigUtils.loadConfig(scenarioOptions.getSimulationConfigName(), avConfigGroup);
                GeneratorConfig avGeneratorConfig = avConfigGroup.getOperatorConfigs().values().iterator().next().getGeneratorConfig();
                int numRt = (int) avGeneratorConfig.getNumberOfVehicles();
                int endTime = (int) config.qsim().getEndTime();
                Scenario scenario = ScenarioUtils.loadScenario(config);

                System.out.println("We could load the simulation Scenario");
                System.out.println("Start The LP again");
                /** reading the whole travel data */

                Tensor lambdaAbsolute = LambdaAbsolute.get( //
                        scenario.getNetwork(), virtualNetwork, //
                        scenario.getPopulation(), scenarioOptions.getdtTravelData(), endTime);
                LPOptions lpOptions = new LPOptions(scenarioOptions.getWorkingDirectory(), LPOptionsBase.getDefault());
                System.out.println("Loaded the Lp Options");

                LPSolver lpSolver = lpCreator.create(virtualNetwork, scenario.getNetwork(), lpOptions, lambdaAbsolute, numRt, endTime);

                System.out.println("Start The LP Solver");

                lpSolver.initiateLP();
                lpSolver.solveLP(false);

                System.out.println("Prepare For Travel Data");

                String lpName = lpSolver.getClass().getSimpleName();
                Tensor alphaAbsolute = lpSolver.getAlphaAbsolute_ij();
                Tensor v0_i = lpSolver.getV0_i();
                Tensor fAbsolute = lpSolver.getFAbsolute_ij();

                System.out.println("New Travel Data");

                travelData = new StaticTravelData(virtualNetwork.getvNetworkID(), lambdaAbsolute, alphaAbsolute, fAbsolute, v0_i, lpName, endTime);

                System.out.println("Write New Travel Data");
                File travelDataFile = new File(scenarioOptions.getVirtualNetworkDirectoryName(), scenarioOptions.getTravelDataName());
                TravelDataIO.writeStatic(travelDataFile, travelData);
            } catch (FileNotFoundException fileNotFoundException) {
                System.err.println("could not find the file");
                fileNotFoundException.printStackTrace();
            } catch (Exception exception) {
                System.err.println("I am not able to create proper Travel Data based on the given input. And at the same time the Travel Data from the Preparer has the wron type");
                exception.printStackTrace();
            }
        } else {
            System.out.println("The Travel Data created in the ");
        }

    }

}
