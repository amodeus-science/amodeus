package org.matsim.amodeus.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ReflectiveConfigGroup;

public class AmodeusConfigGroup extends ReflectiveConfigGroup {
    static public final String GROUP_NAME = "amodeus";

    static final public String NUMBER_OF_PARALLEL_ROUTERS = "numberOfParallelRouters";
    static final public String PASSENGER_ANALYSIS_INTERVAL = "passengerAnalysisInterval";
    static final public String VEHICLE_ANALYSIS_INTERVAL = "vehicleAnalysisInterval";
    static final public String USE_SCORING = "useScoring";

    private long parallelRouters = 4;
    private int passengerAnalysisInterval = 0;
    private int vehicleAnalysisInterval = 0;
    private boolean useScoring = true;

    public AmodeusConfigGroup() {
        super(GROUP_NAME);
    }

    // General Config bootstrapping code

    @Override
    public ConfigGroup createParameterSet(final String type) {
        if (type.equals(AmodeusModeConfig.GROUP_NAME)) {
            return new AmodeusModeConfig();
        }

        throw new IllegalStateException("AMoDeus configuration module only accepts parameter sets of type: " + AmodeusModeConfig.GROUP_NAME);
    }

    @Override
    public void addParameterSet(ConfigGroup parameterSet) {
        if (parameterSet instanceof AmodeusModeConfig) {
            AmodeusModeConfig modeConfig = (AmodeusModeConfig) parameterSet;

            if (!getModes().containsKey(modeConfig.getMode())) {
                super.addParameterSet(parameterSet);
                return;
            }

            throw new IllegalStateException("Attempt to add duplicate mode to AMoDeus configuration: " + modeConfig.getMode());
        }

        throw new IllegalStateException("AMoDeus configuration module only accepts parameter sets of type AmodeusModeConfig");
    }

    // Mode management

    public Map<String, AmodeusModeConfig> getModes() {
        Map<String, AmodeusModeConfig> map = new HashMap<>();

        for (ConfigGroup parameterSet : getParameterSets(AmodeusModeConfig.GROUP_NAME)) {
            AmodeusModeConfig modeConfig = (AmodeusModeConfig) parameterSet;

            if (map.containsKey(modeConfig.getMode())) {
                throw new IllegalStateException("Error duplicate mode in AMoDeus config module: " + modeConfig.getName());
            }

            map.put(modeConfig.getMode(), modeConfig);
        }

        return Collections.unmodifiableMap(map);
    }

    public void addMode(AmodeusModeConfig modeConfig) {
        addParameterSet(modeConfig);
    }

    public AmodeusModeConfig getMode(String mode) {
        AmodeusModeConfig modeConfig = getModes().get(mode);

        if (modeConfig != null) {
            return modeConfig;
        }

        throw new IllegalStateException("Cannot find AMoDeus mode: " + mode);
    }

    public void removeMode(String mode) {
        removeParameterSet(getMode(mode));
    }

    public void clearModes() {
        clearParameterSetsForType(AmodeusModeConfig.GROUP_NAME);
    }

    static public AmodeusConfigGroup get(Config config) {
        AmodeusConfigGroup amodeusConfig = (AmodeusConfigGroup) config.getModules().get(AmodeusConfigGroup.GROUP_NAME);

        if (amodeusConfig == null) {
            amodeusConfig = new AmodeusConfigGroup();
            config.addModule(amodeusConfig);
        }

        return amodeusConfig;
    }

    // Getters and setters

    @StringGetter(NUMBER_OF_PARALLEL_ROUTERS)
    public long getNumberOfParallelRouters() {
        return parallelRouters;
    }

    @StringSetter(NUMBER_OF_PARALLEL_ROUTERS)
    public void setNumberOfParallelRouters(long parallelRouters) {
        this.parallelRouters = parallelRouters;
    }

    @StringGetter(PASSENGER_ANALYSIS_INTERVAL)
    public int getPassengerAnalysisInterval() {
        return passengerAnalysisInterval;
    }

    @StringSetter(PASSENGER_ANALYSIS_INTERVAL)
    public void setPassengerAnalysisInterval(int passengerAnalysisInterval) {
        this.passengerAnalysisInterval = passengerAnalysisInterval;
    }

    @StringGetter(VEHICLE_ANALYSIS_INTERVAL)
    public int getVehicleAnalysisInterval() {
        return vehicleAnalysisInterval;
    }

    @StringSetter(VEHICLE_ANALYSIS_INTERVAL)
    public void setVehicleAnalysisInterval(int vehicleAnalysisInterval) {
        this.vehicleAnalysisInterval = vehicleAnalysisInterval;
    }

    @StringGetter(USE_SCORING)
    public boolean getUseScoring() {
        return useScoring;
    }

    @StringSetter(USE_SCORING)
    public void setUseScoring(boolean useScoring) {
        this.useScoring = useScoring;
    }
}
