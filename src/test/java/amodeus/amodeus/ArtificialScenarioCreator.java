/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus;

import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.amodeus.dvrp.request.AmodeusRequest;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.contrib.dvrp.optimizer.Request;
import org.matsim.core.config.Config;
import org.matsim.core.network.NetworkUtils;

public class ArtificialScenarioCreator {
    public final Node node1;
    public final Node node2;
    public final Node node3;
    public final Node node4;
    public final Node depot;

    public final Link linkUp;
    public final Link linkRight;
    public final Link linkDown;
    public final Link linkLeft;
    public final Link linkDepotIn;
    public final Link linkDepotOut;
    public final Network network;

    public final AmodeusRequest avRequest1;
    public final AmodeusRequest avRequest2;
    public final AmodeusRequest avRequest3;
    public final AmodeusRequest avRequest4;
    public final AmodeusRequest avRequest5;
    public final AmodeusRequest avRequest6;
    public final AmodeusRequest avRequest7;
    public final AmodeusRequest avRequestDepotOut;
    public final AmodeusRequest avRequestDepotIn;

    static final double length = 100.0;
    static final double freespeed = 20.0;
    static final double capacity = 500.0;
    static final double lanes = 1.0;
    static final int seats = 3;

    public ArtificialScenarioCreator() {
        this(null);
    }

    public ArtificialScenarioCreator(Config config) {
        network = (config == null) ? NetworkUtils.createNetwork() : NetworkUtils.createNetwork(config);

        Id<Node> nodeid1 = Id.createNodeId("node1");
        Coord coord1 = new Coord(100, 100);
        node1 = NetworkUtils.createNode(nodeid1, coord1);
        network.addNode(node1);

        Id<Node> nodeid2 = Id.createNodeId("node2");
        Coord coord2 = new Coord(100, 200);
        node2 = NetworkUtils.createNode(nodeid2, coord2);
        network.addNode(node2);

        Id<Node> nodeid3 = Id.createNodeId("node3");
        Coord coord3 = new Coord(200, 200);
        node3 = NetworkUtils.createNode(nodeid3, coord3);
        network.addNode(node3);

        Id<Node> nodeid4 = Id.createNodeId("node4");
        Coord coord4 = new Coord(200, 100);
        node4 = NetworkUtils.createNode(nodeid4, coord4);
        network.addNode(node4);

        Id<Node> depotId = Id.createNodeId("depot");
        Coord depotCoord = new Coord(100, 0);
        depot = NetworkUtils.createNode(depotId, depotCoord);
        network.addNode(depot);

        Id<Link> idUp = Id.createLinkId("linkUp");
        linkUp = NetworkUtils.createLink(idUp, node1, node2, network, length, freespeed, capacity, lanes);
        Id<Link> idRight = Id.createLinkId("linkRight");
        linkRight = NetworkUtils.createLink(idRight, node2, node3, network, length, freespeed, capacity, lanes);
        Id<Link> idDown = Id.createLinkId("linkDown");
        linkDown = NetworkUtils.createLink(idDown, node3, node4, network, length, freespeed, capacity, lanes);
        Id<Link> idLeft = Id.createLinkId("linkLeft");
        linkLeft = NetworkUtils.createLink(idLeft, node4, node1, network, length, freespeed, capacity, lanes);

        Id<Link> iddepotIn = Id.createLinkId("linkDepotIn");
        linkDepotIn = NetworkUtils.createLink(iddepotIn, node1, depot, network, length, freespeed, capacity, lanes);
        Id<Link> iddepotOut = Id.createLinkId("linkDepotOut");
        linkDepotOut = NetworkUtils.createLink(iddepotOut, depot, node1, network, length, freespeed, capacity, lanes);

        network.addLink(linkUp);
        network.addLink(linkRight);
        network.addLink(linkDown);
        network.addLink(linkLeft);
        network.addLink(linkDepotIn);
        network.addLink(linkDepotOut);

        avRequest1 = new AmodeusRequest(Id.create("p1", Request.class), null, linkUp, linkDown, 0.0, AmodeusModeConfig.DEFAULT_MODE, null, Double.MAX_VALUE, Double.MAX_VALUE);
        avRequest2 = new AmodeusRequest(Id.create("p2", Request.class), null, linkRight, linkLeft, 0.0, AmodeusModeConfig.DEFAULT_MODE, null, Double.MAX_VALUE, Double.MAX_VALUE);
        avRequest3 = new AmodeusRequest(Id.create("p3", Request.class), null, linkRight, linkUp, 0.0, AmodeusModeConfig.DEFAULT_MODE, null, Double.MAX_VALUE, Double.MAX_VALUE);
        avRequest4 = new AmodeusRequest(Id.create("p4", Request.class), null, linkRight, linkDown, 0.0, AmodeusModeConfig.DEFAULT_MODE, null, Double.MAX_VALUE, Double.MAX_VALUE);
        avRequest5 = new AmodeusRequest(Id.create("p5", Request.class), null, linkUp, linkRight, 0.0, AmodeusModeConfig.DEFAULT_MODE, null, Double.MAX_VALUE, Double.MAX_VALUE);
        avRequest6 = new AmodeusRequest(Id.create("p6", Request.class), null, linkUp, linkLeft, 0.0, AmodeusModeConfig.DEFAULT_MODE, null, Double.MAX_VALUE, Double.MAX_VALUE);
        avRequest7 = new AmodeusRequest(Id.create("p7", Request.class), null, linkRight, linkLeft, 0.0, AmodeusModeConfig.DEFAULT_MODE, null, Double.MAX_VALUE, Double.MAX_VALUE);
        avRequestDepotOut = new AmodeusRequest(Id.create("depotRequestOut", Request.class), null, linkDepotOut, linkDepotOut, 0.0, AmodeusModeConfig.DEFAULT_MODE, null,
                Double.MAX_VALUE, Double.MAX_VALUE);
        avRequestDepotIn = new AmodeusRequest(Id.create("depotRequestIn", Request.class), null, linkDepotIn, linkDepotIn, 0.0, AmodeusModeConfig.DEFAULT_MODE, null,
                Double.MAX_VALUE, Double.MAX_VALUE);

    }
}
