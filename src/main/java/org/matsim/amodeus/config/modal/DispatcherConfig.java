package org.matsim.amodeus.config.modal;

import org.matsim.amodeus.components.dispatcher.single_heuristic.SingleHeuristicDispatcher;
import org.matsim.core.config.ReflectiveConfigGroup;

public class DispatcherConfig extends ReflectiveConfigGroup {
    static public final String GROUP_NAME = "dispatcher";

    static public final String VIRTUAL_NETWORK_PATH = "virtualNetworkPath";
    static public final String REGENERATE_VIRTUAL_NETWORK = "regenerateVirtualNetwork";

    static public final String TRAVEL_DATA_PATH = "travelDataPath";
    static public final String REGENERATE_TRAVEL_DATA = "regenerateTravelData";

    static public final String PUBLISH_PERIOD = "publishPeriod";

    static public final String TYPE = "type";

    static public final String DEFAULT_DISPATCHER = SingleHeuristicDispatcher.TYPE;
    private String type = DEFAULT_DISPATCHER;

    private String virtualNetworkPath;
    private boolean regenerateVirtualNetwork = false;

    private String travelDataPath;
    private boolean regenerateTravelData = false;

    private int publishPeriod = 10;

    public DispatcherConfig() {
        super(GROUP_NAME, true);
    }

    @StringGetter(TYPE)
    public String getType() {
        return type;
    }

    @StringSetter(TYPE)
    public void setType(String type) {
        this.type = type;
    }

    @StringGetter(VIRTUAL_NETWORK_PATH)
    public String getVirtualNetworkPath() {
        return virtualNetworkPath;
    }

    @StringSetter(VIRTUAL_NETWORK_PATH)
    public void setVirtualNetworkPath(String virtualNetworkPath) {
        this.virtualNetworkPath = virtualNetworkPath;
    }

    @StringGetter(TRAVEL_DATA_PATH)
    public String getTravelDataPath() {
        return travelDataPath;
    }

    @StringSetter(TRAVEL_DATA_PATH)
    public void setTravelDataPath(String travelDataPath) {
        this.travelDataPath = travelDataPath;
    }

    @StringGetter(REGENERATE_VIRTUAL_NETWORK)
    public boolean getRegenerateVirtualNetwork() {
        return regenerateVirtualNetwork;
    }

    @StringSetter(REGENERATE_VIRTUAL_NETWORK)
    public void setRegenerateVirtualNetwork(boolean regenerateVirtualNetwork) {
        this.regenerateVirtualNetwork = regenerateVirtualNetwork;
    }

    @StringGetter(REGENERATE_TRAVEL_DATA)
    public boolean getRegenerateTravelData() {
        return regenerateTravelData;
    }

    @StringSetter(REGENERATE_TRAVEL_DATA)
    public void setRegenerateTravelData(boolean regenerateTravelData) {
        this.regenerateTravelData = regenerateTravelData;
    }

    @StringGetter(PUBLISH_PERIOD)
    public int getPublishPeriod() {
        return publishPeriod;
    }

    @StringSetter(PUBLISH_PERIOD)
    public void setPublishPeriod(int publishPeriod) {
        this.publishPeriod = publishPeriod;
    }
}
