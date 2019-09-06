/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.util.io.LocateUtils;

public class RequestTest {
    private static Network network;
    private static Link link0;
    private static Link link1;
    private static Link link2;

    @BeforeClass
    public static void setup() throws IOException {

        /* input data */
        File scenarioDirectory = new File(LocateUtils.getSuperFolder(RequestTest.class, "amodeus"), "resources/testScenario");
        ScenarioOptions scenarioOptions = new ScenarioOptions(scenarioDirectory, ScenarioOptionsBase.getDefault());
        File configFile = new File(scenarioOptions.getPreparerConfigName());
        Config config = ConfigUtils.loadConfig(configFile.getAbsolutePath());
        Scenario scenario = ScenarioUtils.loadScenario(config);
        network = scenario.getNetwork();

        link0 = (Link) network.getLinks().values().toArray()[0];
        link1 = (Link) network.getLinks().values().toArray()[1];
        link2 = (Link) network.getLinks().values().toArray()[2];
    }

    @Test
    public void testFilterLinks() {
        Request request0 = new Request(1, link0, link1);
        Request request1 = new Request(1, link2, link1);
        Set<Request> requests = new HashSet<>();
        requests.add(request0);
        requests.add(request1);

        System.out.println(link0);
        System.out.println(link1);
        System.out.println(link2);

        Set<Link> links = new HashSet<>();
        assertEquals(Request.filterLinks(requests, links), Collections.emptySet());

        links.add(link0);
        assertEquals(Request.filterLinks(requests, links), Stream.of(request0).collect(Collectors.toSet()));

        links.add(link1);
        assertEquals(Request.filterLinks(requests, links), Stream.of(request0).collect(Collectors.toSet()));

        links.add(link2);
        assertEquals(Request.filterLinks(requests, links), Stream.of(request0, request1).collect(Collectors.toSet()));

    }
}
