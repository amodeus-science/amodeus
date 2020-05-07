package ch.ethz.matsim.av.scenario;

import ch.ethz.matsim.av.framework.AVModule;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.QSimConfigGroup.StarttimeInterpretation;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class TestScenarioGenerator {
    final static int networkSize = 10;
    final static double networkScale = 1000.0;
    final static int populationSize = 100;
    final static long randomSeed = 0;
    final static double duration = 10 * 3600.0;
    final static double freespeed = 30.0 * 1000.0 / 3600.0;
    final static String defaultMode = TransportMode.walk;
    public final static String outputDir = "test_output";

    static public Scenario generate() {
        return generate(ConfigUtils.createConfig());
    }

    static public Scenario generate(Config config) {
        config.controler().setOutputDirectory(outputDir + "/output");

        config.controler().setLastIteration(0);
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
        config.controler().setWriteEventsInterval(-1);
        config.controler().setWritePlansInterval(-1);
        config.controler().setWriteSnapshotsInterval(-1);
        config.controler().setCreateGraphs(false);
        config.controler().setDumpDataAtEnd(false);
        config.qsim().setEndTime(duration * 2);
        config.planCalcScore().getOrCreateScoringParameters(null).getOrCreateActivityParams("activity").setScoringThisActivityAtAll(false);
        config.qsim().setSimStarttimeInterpretation(StarttimeInterpretation.onlyUseStarttime);

        Random random = new Random(randomSeed);
        Scenario scenario = ScenarioUtils.createScenario(config);

        generateNetwork(scenario.getNetwork());
        generatePopulation(scenario.getPopulation(), scenario.getNetwork(), random);

        return scenario;
    }

    static public Scenario generateWithAVLegs(Config config) {
        Scenario scenario = generate(config);

        scenario.getPopulation().getPersons().values().forEach(person -> {
            person.getSelectedPlan().getPlanElements().stream().filter(Leg.class::isInstance).forEach(leg -> ((Leg) leg).setMode("av")); // Refactor av
        });

        return scenario;
    }

    static private void generatePopulation(Population population, Network network, Random random) {
        PopulationFactory populationFactory = population.getFactory();

        List<Id<Link>> linkIds = network.getLinks().values().stream().map(link -> link.getId()).collect(Collectors.toList());

        for (int k = 0; k < populationSize; k++) {
            Person person = populationFactory.createPerson(Id.createPersonId(k));
            population.addPerson(person);

            Id<Link> originId = linkIds.get(random.nextInt(linkIds.size()));
            Id<Link> destinationId = linkIds.get(random.nextInt(linkIds.size()));
            double departureTime = random.nextDouble() * duration * 0.5;

            Plan plan = populationFactory.createPlan();
            person.addPlan(plan);

            Activity originActivity = populationFactory.createActivityFromLinkId("activity", originId);
            originActivity.setEndTime(departureTime);
            originActivity.setCoord(network.getLinks().get(originId).getCoord());
            plan.addActivity(originActivity);

            plan.addLeg(populationFactory.createLeg(defaultMode));

            Activity destinationActivity = populationFactory.createActivityFromLinkId("activity", destinationId);
            destinationActivity.setCoord(network.getLinks().get(destinationId).getCoord());
            plan.addActivity(destinationActivity);
        }
    }

    static private void generateNetwork(Network network) {
        NetworkFactory networkFactory = network.getFactory();

        for (int i = 0; i < networkSize; i++) {
            for (int j = 0; j < networkSize; j++) {
                network.addNode(networkFactory.createNode(Id.createNodeId(String.format("%d:%d", i, j)), new Coord(i * networkScale, j * networkScale)));
            }
        }

        Node fromNode;
        Node toNode;

        for (int i = 0; i < networkSize; i++) {
            for (int j = 1; j < networkSize; j++) {
                fromNode = network.getNodes().get(Id.createNodeId(String.format("%d:%d", i, j - 1)));
                toNode = network.getNodes().get(Id.createNodeId(String.format("%d:%d", i, j)));
                network.addLink(networkFactory.createLink(Id.createLinkId(String.format("%s_%s", fromNode.getId(), toNode.getId())), fromNode, toNode));

                fromNode = network.getNodes().get(Id.createNodeId(String.format("%d:%d", i, j)));
                toNode = network.getNodes().get(Id.createNodeId(String.format("%d:%d", i, j - 1)));
                network.addLink(networkFactory.createLink(Id.createLinkId(String.format("%s_%s", fromNode.getId(), toNode.getId())), fromNode, toNode));
            }
        }

        for (int j = 0; j < networkSize; j++) {
            for (int i = 1; i < networkSize; i++) {
                fromNode = network.getNodes().get(Id.createNodeId(String.format("%d:%d", i - 1, j)));
                toNode = network.getNodes().get(Id.createNodeId(String.format("%d:%d", i, j)));
                network.addLink(networkFactory.createLink(Id.createLinkId(String.format("%s_%s", fromNode.getId(), toNode.getId())), fromNode, toNode));

                fromNode = network.getNodes().get(Id.createNodeId(String.format("%d:%d", i, j)));
                toNode = network.getNodes().get(Id.createNodeId(String.format("%d:%d", i - 1, j)));
                network.addLink(networkFactory.createLink(Id.createLinkId(String.format("%s_%s", fromNode.getId(), toNode.getId())), fromNode, toNode));
            }
        }

        for (Link link : network.getLinks().values()) {
            link.setFreespeed(freespeed);
            link.setLength(networkScale);
        }
    }
}
