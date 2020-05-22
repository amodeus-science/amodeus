package ch.ethz.matsim.av.dynamics;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpModule;
import org.matsim.contrib.dvrp.run.ModalProviders.InstanceGetter;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ModeParams;
import org.matsim.core.config.groups.QSimConfigGroup.StarttimeInterpretation;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.events.handler.EventHandler;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;

import ch.ethz.matsim.av.config.AVScoringParameterSet;
import ch.ethz.matsim.av.config.AmodeusConfigGroup;
import ch.ethz.matsim.av.config.AmodeusModeConfig;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.dispatcher.multi_od_heuristic.MultiODHeuristic;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.framework.AVQSimModule;
import ch.ethz.matsim.av.framework.AVUtils;
import ch.ethz.matsim.av.generator.AVGenerator;

/** TestScenario is used to create a various elements of a test scenario. This is used in various av.dynamics tests. */
public class TestScenario {

    static public Scenario createScenario(AmodeusConfigGroup avConfig, Collection<TestRequest> requests) {
        DvrpConfigGroup dvrpConfigGroup = new DvrpConfigGroup();
        dvrpConfigGroup.setMobsimMode("car");
        dvrpConfigGroup.setNetworkModes(ImmutableSet.of("av"));

        Config config = ConfigUtils.createConfig(avConfig, dvrpConfigGroup);

        config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
        config.controler().setLastIteration(0);
        config.qsim().setSimStarttimeInterpretation(StarttimeInterpretation.onlyUseStarttime);

        config.qsim().setFlowCapFactor(1000.0);
        config.qsim().setEndTime(10 * 3600.0);

        ModeParams modeParams = new ModeParams("av");
        config.planCalcScore().addModeParams(modeParams);

        ActivityParams activityParams = new ActivityParams("av interaction");
        activityParams.setTypicalDuration(1.0);
        activityParams.setScoringThisActivityAtAll(false);
        config.planCalcScore().addActivityParams(activityParams);

        activityParams = new ActivityParams("generic");
        activityParams.setTypicalDuration(1.0);
        activityParams.setScoringThisActivityAtAll(false);
        config.planCalcScore().addActivityParams(activityParams);

        config.plansCalcRoute().getModeRoutingParams().get("walk").setTeleportedModeSpeed(1.0);
        config.plansCalcRoute().getModeRoutingParams().get("walk").setBeelineDistanceFactor(1.0);

        Scenario scenario = ScenarioUtils.createScenario(config);

        Network network = scenario.getNetwork();
        NetworkFactory networkFactory = network.getFactory();

        Node node1 = networkFactory.createNode(Id.createNodeId("node1"), new Coord(0.0, 0.0));
        Node node2 = networkFactory.createNode(Id.createNodeId("node2"), new Coord(10000.0, 0.0));
        Node node3 = networkFactory.createNode(Id.createNodeId("node3"), new Coord(20000.0, 0.0));
        Node node4 = networkFactory.createNode(Id.createNodeId("node4"), new Coord(30000.0, 0.0));

        Link link1 = networkFactory.createLink(Id.createLinkId("link1"), node1, node2);
        Link link2 = networkFactory.createLink(Id.createLinkId("link2"), node2, node3);
        Link link3 = networkFactory.createLink(Id.createLinkId("link3"), node3, node4);
        Link link1r = networkFactory.createLink(Id.createLinkId("link1r"), node2, node1);
        Link link2r = networkFactory.createLink(Id.createLinkId("link2r"), node3, node2);
        Link link3r = networkFactory.createLink(Id.createLinkId("link3r"), node4, node3);

        link1.setAllowedModes(Collections.singleton("av"));
        link2.setAllowedModes(Collections.singleton("av"));
        link3.setAllowedModes(Collections.singleton("av"));
        link1r.setAllowedModes(Collections.singleton("av"));
        link2r.setAllowedModes(Collections.singleton("av"));
        link3r.setAllowedModes(Collections.singleton("av"));

        link1.setFreespeed(10.0);
        link2.setFreespeed(10.0);
        link3.setFreespeed(10.0);
        link1r.setFreespeed(10.0);
        link2r.setFreespeed(10.0);
        link3r.setFreespeed(10.0);

        network.addNode(node1);
        network.addNode(node2);
        network.addNode(node3);
        network.addNode(node4);

        network.addLink(link1);
        network.addLink(link2);
        network.addLink(link3);
        network.addLink(link1r);
        network.addLink(link2r);
        network.addLink(link3r);

        Population population = scenario.getPopulation();
        PopulationFactory populationFactory = population.getFactory();
        int personIndex = 0;

        for (TestRequest request : requests) {
            Person person = populationFactory.createPerson(Id.createPersonId(personIndex++));
            population.addPerson(person);

            Plan plan = populationFactory.createPlan();
            person.addPlan(plan);

            Coord startCoord = link1.getToNode().getCoord();
            startCoord = CoordUtils.plus(startCoord, new Coord(-request.delayTime, 0.0));

            Activity firstActivity = populationFactory.createActivityFromLinkId("generic", link1.getId());
            firstActivity.setCoord(startCoord);
            firstActivity.setEndTime(request.departureTime);
            plan.addActivity(firstActivity);

            Leg leg = populationFactory.createLeg("av");
            plan.addLeg(leg);

            Activity secondActivity = populationFactory.createActivityFromLinkId("generic", link3.getId());
            secondActivity.setCoord(link2.getCoord());
            plan.addActivity(secondActivity);
        }

        return scenario;
    }

    static public class SingleVehicleGenerator implements AVGenerator {
        private final int capacity;
        private final Link link;

        public SingleVehicleGenerator(Link link, int capacity) {
            this.link = link;
            this.capacity = capacity;
        }

        @Override
        public List<AVVehicle> generateVehicles() {
            VehicleType vehicleType = VehicleUtils.getDefaultVehicleType();
            vehicleType.getCapacity().setSeats(capacity);

            return Collections.singletonList(new AVVehicle(Id.create("vehicle", DvrpVehicle.class), link, 0.0, Double.POSITIVE_INFINITY, vehicleType));
        }
    }

    static public class SingleVehicleGeneratorFactory implements AVGenerator.AVGeneratorFactory {
        private final int capacity;
        private final Id<Link> linkId;

        public SingleVehicleGeneratorFactory(int capacity, Id<Link> linkId) {
            this.capacity = capacity;
            this.linkId = linkId;
        }

        @Override
        public AVGenerator createGenerator(InstanceGetter inject) {
            Link link = inject.getModal(Network.class).getLinks().get(linkId);
            return new SingleVehicleGenerator(link, capacity);
        }
    }

    static public AmodeusConfigGroup createConfig() {
        AmodeusConfigGroup config = new AmodeusConfigGroup();

        AmodeusModeConfig operatorConfig = new AmodeusModeConfig("av");
        operatorConfig.getDispatcherConfig().setType(MultiODHeuristic.TYPE);
        operatorConfig.getGeneratorConfig().setType("Single");
        config.addMode(operatorConfig);

        AVScoringParameterSet scoringParams = operatorConfig.getScoringParameters(null);
        scoringParams.setMarginalUtilityOfWaitingTime(-0.84);

        operatorConfig.getTimingConfig().setPickupDurationPerPassenger(0.0);
        operatorConfig.getTimingConfig().setPickupDurationPerStop(0.0);
        operatorConfig.getTimingConfig().setDropoffDurationPerPassenger(0.0);
        operatorConfig.getTimingConfig().setDropoffDurationPerStop(0.0);

        operatorConfig.setUseAccessAgress(true);

        return config;
    }

    static public Controler createController(Scenario scenario, EventHandler handler, int vehicleCapacity) {
        Controler controller = new Controler(scenario);

        controller.addOverridingModule(new DvrpModule());
        controller.addOverridingModule(new DvrpTravelTimeModule());
        controller.addOverridingModule(new AVModule());

        controller.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                AVUtils.registerGeneratorFactory(binder(), "Single", SingleVehicleGeneratorFactory.class);
                addEventHandlerBinding().toInstance(handler);
            }

            @Provides
            public SingleVehicleGeneratorFactory provideFactory() {
                return new SingleVehicleGeneratorFactory(vehicleCapacity, Id.createLinkId("link1"));
            }
        });

        controller.addOverridingQSimModule(new AVQSimModule());

        controller.configureQSimComponents(AVQSimModule.activateModes("av"));

        return controller;
    }

    public static class ArrivalListener implements PersonArrivalEventHandler {
        public List<Double> times = new LinkedList<>();

        @Override
        public void handleEvent(PersonArrivalEvent event) {
            if (!event.getPersonId().toString().equals("vehicle") && event.getLegMode().equals("av"))
                times.add(event.getTime());
        }
    }

}
