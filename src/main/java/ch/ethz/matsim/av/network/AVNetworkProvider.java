package ch.ethz.matsim.av.network;

import org.jboss.logging.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.filter.NetworkFilterManager;

import ch.ethz.matsim.av.data.AVOperator;

public class AVNetworkProvider {
    private final static Logger logger = Logger.getLogger(AVNetworkProvider.class);

    private final boolean cleanNetwork;
    private final String allowedLinkMode;
    private final String allowedLinkAttribute;

    public AVNetworkProvider(String allowedLinkMode, String allowedLinkAttribute, boolean cleanNetwork) {
        this.allowedLinkAttribute = allowedLinkAttribute;
        this.allowedLinkMode = allowedLinkMode;
        this.cleanNetwork = cleanNetwork;
    }

    public Network apply(Id<AVOperator> operatorId, Network fullNetwork, AVNetworkFilter customFilter) {
        NetworkFilterManager manager = new NetworkFilterManager(fullNetwork);

        if (allowedLinkMode != null) {
            manager.addLinkFilter(l -> {
                return l.getAllowedModes().contains(allowedLinkMode);
            });
        }

        if (allowedLinkAttribute != null) {
            manager.addLinkFilter(l -> {
                Boolean attribute = (Boolean) l.getAttributes().getAttribute(allowedLinkAttribute);
                return attribute != null && attribute;
            });
        }

        manager.addLinkFilter(l -> {
            return customFilter.isAllowed(operatorId, l);
        });

        Network filteredNetwork = manager.applyFilters();

        int numberOfLinks = filteredNetwork.getLinks().size();
        int numberOfNodes = filteredNetwork.getNodes().size();

        new NetworkCleaner().run(filteredNetwork);

        int cleanedNumberOfLinks = filteredNetwork.getLinks().size();
        int cleanedNumberOfNodes = filteredNetwork.getNodes().size();

        if (cleanNetwork) {
            logger.info(String.format("Links before/after cleaning: %d/%d", numberOfLinks, cleanedNumberOfLinks));
            logger.info(String.format("Nodes before/after cleaning: %d/%d", numberOfNodes, cleanedNumberOfNodes));
        } else if (numberOfLinks != cleanedNumberOfLinks || numberOfNodes != cleanedNumberOfNodes) {
            logger.error(String.format("Links before/after cleaning: %d/%d", numberOfLinks, cleanedNumberOfLinks));
            logger.error(String.format("Nodes before/after cleaning: %d/%d", numberOfNodes, cleanedNumberOfNodes));
            throw new IllegalStateException(String.format("The current network definition (mode and attribute) for operator %s is not valid!", operatorId));
        }

        if (numberOfLinks == 0) {
            throw new IllegalStateException(String.format("The current network definition (mode and attribute) for operator %s is empty!", operatorId));
        }

        return filteredNetwork;
    }
}
