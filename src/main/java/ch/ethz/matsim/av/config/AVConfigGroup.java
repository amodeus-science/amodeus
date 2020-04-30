package ch.ethz.matsim.av.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ReflectiveConfigGroup;

import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.data.AVOperator;

public class AVConfigGroup extends ReflectiveConfigGroup {
	static final public String GROUP_NAME = "av";

	static final public String NUMBER_OF_PARALLEL_ROUTERS = "numberOfParallelRouters";
	static final public String ALLOWED_LINK_MODE = "allowedLinkMode";
	static final public String USE_ACCESS_EGRESS = "useAccessEgress";

	static final public String PASSENGER_ANALYSIS_INTERVAL = "passengerAnalysisInterval";
	static final public String VEHICLE_ANALYSIS_INTERVAL = "vehicleAnalysisInterval";
	static final public String ENABLE_DISTANCE_ANALYSIS = "enableDistanceAnalysis";

	private long parallelRouters = 4;

	private boolean useAccessEgress = false;
	private String allowedLinkMode = null;
	private int passengerAnalysisInterval = 0;
	private int vehicleAnalysisInterval = 0;
	private boolean enableDistanceAnalysis = false;

	public AVConfigGroup() {
		super(GROUP_NAME);
		addScoringParameters(new AVScoringParameterSet());
	}

	@Override
	public ConfigGroup createParameterSet(final String type) {
		switch (type) {
		case AVScoringParameterSet.GROUP_NAME:
			return new AVScoringParameterSet();
		case OperatorConfig.GROUP_NAME:
			return new OperatorConfig();
		}

		throw new IllegalStateException("Unknown parameter set in AV config: " + type);
	}

	public Map<Id<AVOperator>, OperatorConfig> getOperatorConfigs() {
		Map<Id<AVOperator>, OperatorConfig> map = new HashMap<>();

		for (ConfigGroup _operator : getParameterSets(OperatorConfig.GROUP_NAME)) {
			OperatorConfig operator = (OperatorConfig) _operator;

			if (map.containsKey(operator.getId())) {
				throw new IllegalStateException("Error duplicate operator in config: " + operator.getId());
			}

			map.put(operator.getId(), operator);
		}

		return Collections.unmodifiableMap(map);
	}

	public void addOperator(OperatorConfig operator) {
		if (getOperatorConfigs().containsKey(operator.getId())) {
			throw new IllegalStateException("Another operator with this ID exists already: " + operator.getId());
		}

		addParameterSet(operator);
	}

	public void removeOperator(Id<AVOperator> id) {
		OperatorConfig operator = getOperatorConfig(id);
		removeParameterSet(operator);
	}

	public OperatorConfig getOperatorConfig(Id<AVOperator> id) {
		OperatorConfig operator = getOperatorConfigs().get(id);

		if (operator == null) {
			throw new IllegalStateException("Operator does not exist: " + id);
		}

		return operator;
	}

	public void clearOperators() {
		clearParameterSetsForType(OperatorConfig.GROUP_NAME);
	}

	public Map<String, AVScoringParameterSet> getScoringParameters() {
		Map<String, AVScoringParameterSet> map = new HashMap<>();

		for (ConfigGroup _parameters : getParameterSets(AVScoringParameterSet.GROUP_NAME)) {
			AVScoringParameterSet parameters = (AVScoringParameterSet) _parameters;

			if (map.containsKey(parameters.getSubpopulation())) {
				throw new IllegalStateException(
						"Error duplicate subpopulation in config: " + parameters.getSubpopulation());
			}

			map.put(parameters.getSubpopulation(), parameters);
		}

		return Collections.unmodifiableMap(map);
	}

	@Override
	public void addParameterSet(ConfigGroup set) {
		// Makes sure that we don't have duplicate subpopulations
		if (set instanceof AVScoringParameterSet) {
			AVScoringParameterSet scoringParameters = (AVScoringParameterSet) set;

			if (getScoringParameters().containsKey(scoringParameters.getSubpopulation())) {
				removeScoringParameters(scoringParameters.getSubpopulation());
			}
		}

		super.addParameterSet(set);
	}

	public void addScoringParameters(AVScoringParameterSet parameters) {
		if (getScoringParameters().containsKey(parameters.getSubpopulation())) {
			throw new IllegalStateException("Subpopulation exists already: " + parameters.getSubpopulation());
		}

		addParameterSet(parameters);
	}

	public void removeScoringParameters(String subpopulation) {
		AVScoringParameterSet parameters = getScoringParameters(subpopulation);
		removeParameterSet(parameters);
	}

	public AVScoringParameterSet getScoringParameters(String subpopulation) {
		AVScoringParameterSet parameters = getScoringParameters().get(subpopulation);

		if (parameters == null) {
			throw new IllegalStateException("Missing AV scoring parameters for subpopulation: " + subpopulation);
		}

		return parameters;
	}

	public void clearScoringParameters() {
		clearParameterSetsForType(AVScoringParameterSet.GROUP_NAME);
	}

	@StringGetter(NUMBER_OF_PARALLEL_ROUTERS)
	public long getNumberOfParallelRouters() {
		return parallelRouters;
	}

	@StringSetter(NUMBER_OF_PARALLEL_ROUTERS)
	public void setNumberOfParallelRouters(long parallelRouters) {
		this.parallelRouters = parallelRouters;
	}

	@StringGetter(USE_ACCESS_EGRESS)
	public boolean getUseAccessEgress() {
		return useAccessEgress;
	}

	@StringSetter(USE_ACCESS_EGRESS)
	public void setUseAccessAgress(boolean useAccessEgress) {
		this.useAccessEgress = useAccessEgress;
	}

	@StringGetter(ALLOWED_LINK_MODE)
	public String getAllowedLinkMode() {
		return allowedLinkMode;
	}

	@StringSetter(ALLOWED_LINK_MODE)
	public void setAllowedLinkMode(String allowedLinkMode) {
		this.allowedLinkMode = allowedLinkMode;
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

	@StringGetter(ENABLE_DISTANCE_ANALYSIS)
	public boolean getEnableDistanceAnalysis() {
		return enableDistanceAnalysis;
	}

	@StringSetter(ENABLE_DISTANCE_ANALYSIS)
	public void setEnableDistanceAnalysis(boolean enableDistanceAnalysis) {
		this.enableDistanceAnalysis = enableDistanceAnalysis;
	}

	@Override
	protected void checkConsistency(Config config) {
		super.checkConsistency(config);
		new AVConfigConsistencyChecker().checkConsistency(config);
	}

	static public AVConfigGroup getOrCreate(Config config) {
		AVConfigGroup configGroup = (AVConfigGroup) config.getModules().get(GROUP_NAME);

		if (configGroup == null) {
			configGroup = new AVConfigGroup();
			config.addModule(configGroup);
		}

		return configGroup;
	}
}
