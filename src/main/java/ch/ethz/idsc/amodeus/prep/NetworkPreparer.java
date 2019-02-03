/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.api.internal.NetworkRunnable;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.io.NetworkWriter;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.util.io.GZHandler;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNode;

public enum NetworkPreparer {
    ;

    public static Network run(Network network, ScenarioOptions scenOptions) {
        GlobalAssert.that(Objects.nonNull(network));
        System.out.println("++++++++++++++++++++++++ NETWORK PREPARER +++++++++++++++++++++++++++++++");
        System.out.println("Network (Link) size original: " + network.getLinks().values().size());
        Network modifiedNetwork = network;

        /** put any network modifications, e.g., cutting, here */
        
        System.out.println("Reducing network: " + !scenOptions.isCompleteGraph());
        
        if(!scenOptions.isCompleteGraph()) {
            List<Coord> coordList = new ArrayList<>();
            for(Node node: modifiedNetwork.getNodes().values()) {
                coordList.add(node.getCoord());
            }
            
            double xsum = 0;
            double ysum = 0;
            
            for(Coord coord: coordList) {
                xsum = coord.getX() + xsum;
                ysum = coord.getY() + ysum;
            }
            
            double xCoord = xsum / coordList.size();
            double yCoord = ysum / coordList.size();

            Coord coord = new Coord(xCoord, yCoord);
            double distance = scenOptions.getNetworkRadiusCut();

            Collection<Node> reducedNodes = NetworkUtils.getNearestNodes(modifiedNetwork, coord, distance);
            
            List<Node> deleteNodes = new ArrayList<>();
            for(Node node: modifiedNetwork.getNodes().values()) {
                if(!reducedNodes.contains(node)) {
                     deleteNodes.add(node);      
                }
            }
            
            for(Node node: deleteNodes) {
                modifiedNetwork.removeNode(node.getId());
            }
            
            Map<Id<Node>, Node> mapNode = new NetworkCleaner().searchBiggestCluster(modifiedNetwork);
            
            NetworkCleaner.reduceToBiggestCluster(modifiedNetwork,mapNode);
            
            System.out.println("Reduced network to: " + modifiedNetwork.getLinks().values().size());
        }

        modifiedNetwork.setName(network.getName() + "_prepared");
        modifiedNetwork.setCapacityPeriod(network.getCapacityPeriod());
        modifiedNetwork.setEffectiveCellSize(network.getEffectiveCellSize());
        modifiedNetwork.setEffectiveLaneWidth(network.getEffectiveLaneWidth());
        
        System.out.println("Modify link free speed (times): " + scenOptions.getModifierLinkFreeSpeed());
        
        modifiedNetwork.getLinks().values().forEach(link -> link.setFreespeed(link.getFreespeed()*scenOptions.getModifierLinkFreeSpeed()));
                        
        final File fileExportGz = new File(scenOptions.getPreparedNetworkName() + ".xml.gz");
        final File fileExport = new File(scenOptions.getPreparedNetworkName() + ".xml");
        {
            // write the modified population to file
            NetworkWriter nw = new NetworkWriter(modifiedNetwork);
            nw.write(fileExportGz.toString());
        }
        // extract the created .gz file
        try {
            GZHandler.extract(fileExportGz, fileExport);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("saved converted network to: " + scenOptions.getPreparedNetworkName() + ".xml");
        return modifiedNetwork;
    }

}
