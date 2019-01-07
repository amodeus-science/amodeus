package ch.ethz.idsc.amodeus.dispatcher.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.geotools.util.WeakCollectionCleaner;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import ch.ethz.idsc.amodeus.lp.LPCreator;
import ch.ethz.idsc.amodeus.lp.LPSolver;
import ch.ethz.idsc.amodeus.lp.LPTimeInvariant;
import ch.ethz.idsc.amodeus.lp.LPUtils;
import ch.ethz.idsc.amodeus.options.LPOptions;
import ch.ethz.idsc.amodeus.options.LPOptionsBase;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.traveldata.TravelData;
import ch.ethz.idsc.amodeus.traveldata.TravelDataCreator;
import ch.ethz.idsc.amodeus.traveldata.TravelDataIO;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodeus.util.io.ProvideAVConfig;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.matsim.av.config.AVConfig;
import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.framework.AVConfigGroup;

public enum FeedForwardTravelData {
    ;

    public static void overwriteIfRequired(LPCreator lpCreator, TravelData travelData, VirtualNetwork<Link> virtualNetwork) {
        if (!travelData.getLPName().equals(lpCreator.name())) {
            try {
                /** amodeus options */
                File workingDirectory = MultiFileTools.getWorkingDirectory();
                ScenarioOptions scenarioOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());

                /** MATSim config */
                AVConfigGroup avCg = new AVConfigGroup();
                Config config = ConfigUtils.loadConfig(scenarioOptions.getSimulationConfigName(), avCg);
                AVConfig avC = ProvideAVConfig.with(config, avCg);
                AVGeneratorConfig genConfig = avC.getOperatorConfigs().iterator().next().getGeneratorConfig();
                int numRt = (int) genConfig.getNumberOfVehicles();
                int endTime = (int) config.qsim().getEndTime();
                Scenario scenario = ScenarioUtils.loadScenario(config);

                System.out.println("We could load the simulation Scenario");
                System.out.println("Start The LP again");
                /** reading the whole travel data */

                Tensor lambdaAbsolute = TravelDataCreator.getLambdaAbsolute(scenario.getNetwork(), virtualNetwork, scenario.getPopulation(),  scenarioOptions.getdtTravelData(), endTime);
                LPOptions lpOptions = new LPOptions(workingDirectory, LPOptionsBase.getDefault());
                System.out.println("Loaded teh Lp Options");

                LPSolver solver = lpCreator.create(virtualNetwork, scenario.getNetwork(), lpOptions, lambdaAbsolute, numRt, endTime);
                
                System.out.println("Start The LP Solver");

                solver.initiateLP();
                solver.solveLP(false);
                
                System.out.println("Prepare For Travel Data");

                String lpName = solver.getClass().getSimpleName();
                Tensor alphaAbsolute = solver.getAlphaAbsolute_ij();
                Tensor v0_i = solver.getV0_i();
                Tensor fAbsolute = solver.getFAbsolute_ij();

                System.out.println("New Travel Data");

                travelData =  new TravelData(virtualNetwork.getvNetworkID(), lambdaAbsolute, alphaAbsolute, fAbsolute, v0_i, lpName, endTime);

                System.out.println("Write New Travel Data");
                File travelDataFile = new File(scenarioOptions.getVirtualNetworkName(), scenarioOptions.getTravelDataName());
                TravelDataIO.write(travelDataFile, travelData);
            }catch (FileNotFoundException e) {
                System.err.println("could not find the file");
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("I am not able to create proper Travel Data based on the given input. And at the same time the Travel Data from the Preparer has the wron type");
                e.printStackTrace();
            }
        } else {
            System.out.println("The Travel Data created in the ");
        }

    }

}
