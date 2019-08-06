/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario;

import java.util.Objects;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.matsim.utils.objectattributes.attributable.AttributesUtils;

public enum NetworkCutterUtils {
    ;

    public static Network modeFilter(Network originalNetwork, LinkModes modes) {
        if (modes.allModesAllowed) {
            System.out.println("No modes filtered. Network was not modified");
            return originalNetwork;
        }
        // Filter out modes
        Network modesFilteredNetwork = NetworkUtils.createNetwork();
        for (Node node : originalNetwork.getNodes().values()) {
            modesFilteredNetwork.addNode(modesFilteredNetwork.getFactory().createNode(node.getId(), node.getCoord()));
        }
        for (Link link : originalNetwork.getLinks().values()) {
            Node filteredFromNode = modesFilteredNetwork.getNodes().get(link.getFromNode().getId());
            Node filteredToNode = modesFilteredNetwork.getNodes().get(link.getToNode().getId());
            if (Objects.nonNull(filteredFromNode) && Objects.nonNull(filteredToNode)) {
                boolean allowedMode = modes.getModesSet().stream().anyMatch(link.getAllowedModes()::contains);
                if (allowedMode) {
                    // TODO put to generic "preserving link copy function"
                    Link newLink = modesFilteredNetwork.getFactory().createLink(link.getId(), filteredFromNode, filteredToNode);
                    newLink.setAllowedModes(link.getAllowedModes());
                    newLink.setLength(link.getLength());
                    newLink.setCapacity(link.getCapacity());
                    newLink.setFreespeed(link.getFreespeed());
                    AttributesUtils.copyTo(link.getAttributes(), newLink.getAttributes());
                    modesFilteredNetwork.addLink(newLink);
                }
            }

        }

        String output = modes.getModesSet().toString();
        System.out.println("The following modes are kept in the network: " + output);

        return modesFilteredNetwork;
    }
}
