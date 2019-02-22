package ch.ethz.idsc.amodeus.scenario.chicago;

import java.util.Objects;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;

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
                    Link newLink = modesFilteredNetwork.getFactory().createLink(link.getId(), filteredFromNode, filteredToNode);

                    newLink.setAllowedModes(link.getAllowedModes());
                    newLink.setLength(link.getLength());
                    newLink.setCapacity(link.getCapacity());
                    newLink.setFreespeed(link.getFreespeed());
                    // newLink.setNumberOfLanes(link.getNumberOfLanes());

                    modesFilteredNetwork.addLink(newLink);
                }
            }

        }

        String output = modes.getModesSet().toString();
        System.out.println("The following modes are kept in the network: " + output);

        return modesFilteredNetwork;
    }
//
//    public static void printNettworkCuttingInfo(Network originalNetwork, Network modifiedNetwork) {
//        long numberOfLinksOriginal = originalNetwork.getLinks().size();
//        long numberOfNodesOriginal = originalNetwork.getNodes().size();
//        long numberOfLinksFiltered = modifiedNetwork.getLinks().size();
//        long numberOfNodesFiltered = modifiedNetwork.getNodes().size();
//
//        String cutInfo = "------------NETWORK CUTTING INFOS----------------\n";
//        cutInfo += "  Number of nodes in original network: " + numberOfNodesOriginal + "\n";
//        cutInfo += "  Number of Links in original network: " + numberOfLinksOriginal + "\n";
//
//        cutInfo += String.format("  Number of nodes in filtered network: %d (%.2f%%)", numberOfNodesFiltered, 100.0 * numberOfNodesFiltered / numberOfNodesOriginal) + "\n";
//        cutInfo += String.format("  Number of links in filtered network: %d (%.2f%%)", numberOfLinksFiltered, 100.0 * numberOfLinksFiltered / numberOfLinksOriginal) + "\n";
//        System.out.println(cutInfo);
//    }
//
//    public static void setAllLinkSpeedsTo(Network network, double d) {
//        for (Link link : network.getLinks().values()) {
//            link.setFreespeed(d);
//        }
//    }
//
//    public static void setLinkSpeedsToFraction(Network network, double fraction) {
//        int i = 0;
//        for (Link link : network.getLinks().values()) {
//            link.setFreespeed(link.getFreespeed() * fraction);
//            i++;
//        }
//        GlobalAssert.that(i == network.getLinks().size());
//    }
//
//    public static void setLinkSpeedsToDatabaseValues(Network network, DatabaseSetting databaseSetting) {
//        DatabaseGoogle linkSpeedDatabase = DatabaseGoogle.loadDatabaseFromCSV(databaseSetting);
//
//        Map<String, Double> linkSpeedsGoogle = new HashMap<>();
//
//        List<DatabaseElement> databaseElements = linkSpeedDatabase.getAllDatabaseElements();
//        for (DatabaseElement dE : databaseElements) {
//            // LS_TODO create member function in DatabaseElement to evaluate the following condition
//            if (!Double.isNaN(dE.getDistance()) && !Double.isNaN(dE.getDuration()))
//                if (dE.getDuration() != 0 && dE.getDistance() >= databaseSetting.getMinimalLinkLength()) {
//                    linkSpeedsGoogle.put(dE.getAttribute1(), (double) (dE.getDistance() / dE.getDuration()));
//                }
//        }
//
//        // Tensor networkSpeeds = Tensors.empty();
//        // Tensor googleSpeeds = Tensors.empty();
//        for (Link link : network.getLinks().values()) {
//            if (linkSpeedsGoogle.containsKey(link.getId().toString())) {
//                // networkSpeeds.append(RealScalar.of(link.getFreespeed()));
//                // googleSpeeds.append(RealScalar.of(linkSpeedsGoogle.get(link.getId().toString())));
//                link.setFreespeed(linkSpeedsGoogle.get(link.getId().toString()));
//            }
//        }
//
//        // List<Tensor> dataVectors = new ArrayList<>();
//        // System.out.println(Pretty.of(networkSpeeds));
//        //
//        // System.out.println(Pretty.of(googleSpeeds));
//        // dataVectors.add(networkSpeeds);
//        // dataVectors.add(googleSpeeds);
//        // Scalar binSize = RealScalar.of(1);
//        // Scalar scaling = RealScalar.of(1);
//        // int length = 40;
//        // String filename = "linkComparisonGoogle";
//        // String diagramTitle = "Percent of Links per Free Speed, NumberOfLinks: " + networkSpeeds.length();
//        // String axisLabelY = "percent Of Links";
//        // String axisLabelX = "freeSpeed";
//        // int imageWidth = DiagramSettings.WIDTH;
//        // int imageHeight = DiagramSettings.HEIGHT;
//        // ColorScheme colorScheme = ColorScheme.STANDARD;
//        // String[] labels = { "Network OSm", "ooogle Speeds" };
//        // try {
//        // File directory = MultiFileTools.getWorkingDirectory();
//        //
//        // HistogramPlotMultipleSeries.of(dataVectors, binSize, scaling, length, directory, filename, diagramTitle, axisLabelY, axisLabelX, imageWidth,
//        // imageHeight,
//        // colorScheme,
//        // labels);
//        // } catch (Exception e) {
//        // e.printStackTrace();
//        // }
//
//    }
//
//    public static void main(String[] args) throws IOException {
//        // For Testing
//        File workingDirectory = MultiFileTools.getWorkingDirectory();
//        ScenarioOptionsIDSC scenarioOptions = ScenarioOptionsIDSC.load(workingDirectory);
//        File configFile = new File(workingDirectory, scenarioOptions.getSimulationConfigName());
//        Config config = ConfigUtils.loadConfig(configFile.getPath());
//        Scenario scenario = ScenarioUtils.loadScenario(config);
//        Network network = scenario.getNetwork();
//        DatabaseSetting databaseSetting = new DatabaseSettingLinkSpeed();
//
//        setLinkSpeedsToDatabaseValues(network, databaseSetting);
//    }

}
