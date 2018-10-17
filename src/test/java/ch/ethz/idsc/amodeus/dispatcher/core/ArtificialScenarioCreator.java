package ch.ethz.idsc.amodeus.dispatcher.core;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.contrib.dvrp.data.Request;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.util.LinkTimePair;
import org.matsim.core.config.Config;
import org.matsim.core.network.NetworkUtils;

import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.passenger.AVRequest;

/*package*/ class ArtificialScenarioCreator {
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

    public final AVRequest avRequest1;
    public final AVRequest avRequest2;
    public final AVRequest avRequest3;
    public final AVRequest avRequest4;
    public final AVRequest avRequest5;
    public final AVRequest avRequest6;
    public final AVRequest avRequest7;

    public final AVVehicle vehicle1;
    public final AVVehicle vehicle2;
    public final RoboTaxi roboTaxi1;
    public final RoboTaxi roboTaxi2;

    static final double length = 100.0;
    static final double freespeed = 20.0;
    static final double capacity = 500.0;
    static final double lanes = 1.0;
    static final int seats = 3;

    /* package */ ArtificialScenarioCreator() {
        this(null);
    }

    /* package */ ArtificialScenarioCreator(Config config) {
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

        avRequest1 = new AVRequest(Id.create("p1", Request.class), null, linkUp, linkDown, 0.0, 0.0, null, null, null);
        avRequest2 = new AVRequest(Id.create("p2", Request.class), null, linkRight, linkLeft, 0.0, 0.0, null, null, null);
        avRequest3 = new AVRequest(Id.create("p3", Request.class), null, linkRight, linkUp, 0.0, 0.0, null, null, null);
        avRequest4 = new AVRequest(Id.create("p4", Request.class), null, linkRight, linkDown, 0.0, 0.0, null, null, null);
        avRequest5 = new AVRequest(Id.create("p5", Request.class), null, linkUp, linkRight, 0.0, 0.0, null, null, null);
        avRequest6 = new AVRequest(Id.create("p6", Request.class), null, linkUp, linkLeft, 0.0, 0.0, null, null, null);
        avRequest7 = new AVRequest(Id.create("p7", Request.class), null, linkRight, linkLeft, 0.0, 0.0, null, null, null);

        LinkTimePair divertableLinkTime = new LinkTimePair(linkDepotOut, 0.0);

        Id<Vehicle> idAv1 = Id.create("av1", Vehicle.class);
        vehicle1 = new AVVehicle(idAv1, linkDepotOut, seats, 0.0, Double.POSITIVE_INFINITY);
        roboTaxi1 = new RoboTaxi(vehicle1, divertableLinkTime, linkDepotOut, RoboTaxiUsageType.SHARED);

        Id<Vehicle> idAv2 = Id.create("av2", Vehicle.class);
        vehicle2 = new AVVehicle(idAv2, linkDepotOut, seats, 0.0, Double.POSITIVE_INFINITY);
        roboTaxi2 = new RoboTaxi(vehicle2, divertableLinkTime, linkDepotOut, RoboTaxiUsageType.SHARED);

    }

}
