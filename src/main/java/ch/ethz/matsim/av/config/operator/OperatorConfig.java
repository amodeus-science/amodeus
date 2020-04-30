package ch.ethz.matsim.av.config.operator;

import org.matsim.api.core.v01.Id;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ReflectiveConfigGroup;

import ch.ethz.matsim.av.data.AVOperator;

public class OperatorConfig extends ReflectiveConfigGroup {
	static public final String GROUP_NAME = "operator";

	static public final String ID = "id";
	static public final String ROUTER_TYPE = "routerType";
	static public final String ALLOWED_LINK_ATTRIBUTE = "allowedLinkAttribute";
	static public final String CLEAN_NETWORK = "cleanNetwork";
	static public final String PREDICT_ROUTE_TRAVEL_TIME = "predictRouteTravelTime";
	static public final String PREDICT_ROUTE_PRICE = "predictRoutePrice";

	static public final Id<AVOperator> DEFAULT_OPERATOR_ID = AVOperator.createId("default");
	private Id<AVOperator> id = DEFAULT_OPERATOR_ID;

	private String allowedLinkAttribute = null;
	private boolean cleanNetwork = false;
	private boolean predictRouteTravelTime = false;
	private boolean predictRoutePrice = false;

	private final DispatcherConfig dispatcherConfig = new DispatcherConfig();
	private final GeneratorConfig generatorConfig = new GeneratorConfig();
	private final TimingConfig timingConfig = new TimingConfig();
	private final PricingConfig pricingConfig = new PricingConfig();
	private final RouterConfig routerConfig = new RouterConfig();
	private final InteractionFinderConfig interactionFinderConfig = new InteractionFinderConfig();
	private final WaitingTimeConfig waitingTimeConfig = new WaitingTimeConfig();

	public OperatorConfig() {
		super(GROUP_NAME);

		super.addParameterSet(dispatcherConfig);
		super.addParameterSet(generatorConfig);
		super.addParameterSet(timingConfig);
		super.addParameterSet(pricingConfig);
		super.addParameterSet(routerConfig);
		super.addParameterSet(interactionFinderConfig);
		super.addParameterSet(waitingTimeConfig);
	}

	@StringGetter(ID)
	public String getIdAsString() {
		return id.toString();
	}

	@StringSetter(ID)
	public void setIdAsString(String id) {
		this.id = AVOperator.createId(id);
	}

	public Id<AVOperator> getId() {
		return id;
	}

	public void setId(Id<AVOperator> id) {
		this.id = id;
	}

	@StringGetter(ALLOWED_LINK_ATTRIBUTE)
	public String getAllowedLinkAttribute() {
		return allowedLinkAttribute;
	}

	@StringSetter(ALLOWED_LINK_ATTRIBUTE)
	public void setAllowedLinkAttribute(String allowedLinkAttribute) {
		this.allowedLinkAttribute = allowedLinkAttribute;
	}

	@StringGetter(PREDICT_ROUTE_TRAVEL_TIME)
	public boolean getPredictRouteTravelTime() {
		return predictRouteTravelTime;
	}

	@StringSetter(PREDICT_ROUTE_TRAVEL_TIME)
	public void setPredictRouteTravelTime(boolean predictRouteTravelTime) {
		this.predictRouteTravelTime = predictRouteTravelTime;
	}

	@StringGetter(PREDICT_ROUTE_PRICE)
	public boolean getPredictRoutePrice() {
		return predictRoutePrice;
	}

	@StringSetter(PREDICT_ROUTE_PRICE)
	public void setPredictRoutePrice(boolean predictRoutePrice) {
		this.predictRoutePrice = predictRoutePrice;
	}

	@StringGetter(CLEAN_NETWORK)
	public boolean getCleanNetwork() {
		return cleanNetwork;
	}

	@StringSetter(CLEAN_NETWORK)
	public void setCleanNetwork(boolean cleanNetwork) {
		this.cleanNetwork = cleanNetwork;
	}

	public DispatcherConfig getDispatcherConfig() {
		return dispatcherConfig;
	}

	public GeneratorConfig getGeneratorConfig() {
		return generatorConfig;
	}

	public TimingConfig getTimingConfig() {
		return timingConfig;
	}

	public PricingConfig getPricingConfig() {
		return pricingConfig;
	}

	public RouterConfig getRouterConfig() {
		return routerConfig;
	}

	public InteractionFinderConfig getInteractionFinderConfig() {
		return interactionFinderConfig;
	}

	public WaitingTimeConfig getWaitingTimeConfig() {
		return waitingTimeConfig;
	}

	@Override
	public ConfigGroup createParameterSet(final String type) {
		switch (type) {
		case DispatcherConfig.GROUP_NAME:
			return dispatcherConfig;
		case GeneratorConfig.GROUP_NAME:
			return generatorConfig;
		case TimingConfig.GROUP_NAME:
			return timingConfig;
		case PricingConfig.GROUP_NAME:
			return pricingConfig;
		case RouterConfig.GROUP_NAME:
			return routerConfig;
		case InteractionFinderConfig.GROUP_NAME:
			return interactionFinderConfig;
		case WaitingTimeConfig.GROUP_NAME:
			return waitingTimeConfig;
		}

		throw new IllegalStateException("Unknown parameter set for operator: " + type);
	}

	@Override
	public void addParameterSet(ConfigGroup set) {
		boolean isValid = false;

		isValid |= set == dispatcherConfig;
		isValid |= set == generatorConfig;
		isValid |= set == timingConfig;
		isValid |= set == pricingConfig;
		isValid |= set == routerConfig;
		isValid |= set == interactionFinderConfig;
		isValid |= set == waitingTimeConfig;

		if (!isValid) {
			throw new IllegalStateException("Attempt to add unknown parameter set to OperatorConfig");
		}
	}
}
