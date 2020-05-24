package ch.ethz.matsim.av.config;

import java.util.Collection;
import java.util.stream.Collectors;

import org.matsim.contrib.dvrp.run.Modal;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ReflectiveConfigGroup;

import ch.ethz.matsim.av.config.modal.PricingConfig;
import ch.ethz.matsim.av.config.modal.WaitingTimeConfig;
import ch.ethz.matsim.av.config.modal.AmodeusScoringConfig;
import ch.ethz.matsim.av.config.modal.DispatcherConfig;
import ch.ethz.matsim.av.config.modal.GeneratorConfig;
import ch.ethz.matsim.av.config.modal.InteractionFinderConfig;
import ch.ethz.matsim.av.config.modal.RouterConfig;
import ch.ethz.matsim.av.config.modal.TimingConfig;

public class AmodeusModeConfig extends ReflectiveConfigGroup implements Modal {
    static public final String GROUP_NAME = "mode";

    static public final String MODE = "mode";

    static public final String ROUTER_TYPE = "routerType";
    static public final String PREDICT_ROUTE_TRAVEL_TIME = "predictRouteTravelTime";
    static public final String PREDICT_ROUTE_PRICE = "predictRoutePrice";
    static final public String USE_MODE_FILTERED_SUBNETWORK = "useModeFilteredSubnetwork";
    static final public String USE_ACCESS_EGRESS = "useAccessEgress";

    private String mode = "av";

    private boolean predictRouteTravelTime = false;
    private boolean predictRoutePrice = false;
    private boolean useAccessEgress = false;
    private boolean useModeFilteredSubnetwork = false;

    private PricingConfig pricingConfig = new PricingConfig();
    private WaitingTimeConfig waitingTimeEstimationConfig = new WaitingTimeConfig();

    private GeneratorConfig generatorConfig = new GeneratorConfig();
    private DispatcherConfig dispatcherConfig = new DispatcherConfig();
    private RouterConfig routerConfig = new RouterConfig();
    private InteractionFinderConfig interactionFinderConfig = new InteractionFinderConfig();
    private TimingConfig timingConfig = new TimingConfig();

    private boolean hasDefaultScoringConfiguration = true;

    AmodeusModeConfig() {
        super(GROUP_NAME);

        super.addParameterSet(pricingConfig);
        super.addParameterSet(waitingTimeEstimationConfig);

        super.addParameterSet(generatorConfig);
        super.addParameterSet(dispatcherConfig);
        super.addParameterSet(routerConfig);
        super.addParameterSet(interactionFinderConfig);
        super.addParameterSet(timingConfig);

        super.addParameterSet(new AmodeusScoringConfig());
    }

    public AmodeusModeConfig(String mode) {
        this();
        setMode(mode);
    }

    // Config bootstrap code

    @Override
    public ConfigGroup createParameterSet(String type) {
        switch (type) {
        case PricingConfig.GROUP_NAME:
            return pricingConfig;
        case WaitingTimeConfig.GROUP_NAME:
            return waitingTimeEstimationConfig;
        case GeneratorConfig.GROUP_NAME:
            return generatorConfig;
        case DispatcherConfig.GROUP_NAME:
            return dispatcherConfig;
        case RouterConfig.GROUP_NAME:
            return routerConfig;
        case InteractionFinderConfig.GROUP_NAME:
            return interactionFinderConfig;
        case TimingConfig.GROUP_NAME:
            return timingConfig;
        case AmodeusScoringConfig.GROUP_NAME:
            return new AmodeusScoringConfig();
        default:
            throw new IllegalStateException("AmodeusModeConfig does not support parameter set type: " + type);
        }
    }

    @Override
    public void addParameterSet(ConfigGroup parameterSet) {
        if (parameterSet instanceof PricingConfig) {
            if (!(parameterSet == pricingConfig)) {
                throw new IllegalStateException("Use getPricingConfig() to change the pricing configuration.");
            }

            return;
        }

        if (parameterSet instanceof WaitingTimeConfig) {
            if (!(parameterSet == waitingTimeEstimationConfig)) {
                throw new IllegalStateException("Use getWaitingTimeEstimationConfig() to change the pricing configuration.");
            }

            return;
        }

        if (parameterSet instanceof GeneratorConfig) {
            if (!(parameterSet == generatorConfig)) {
                throw new IllegalStateException("Use getGeneratorConfig() to change the generator configuration.");
            }

            return;
        }

        if (parameterSet instanceof DispatcherConfig) {
            if (!(parameterSet == dispatcherConfig)) {
                throw new IllegalStateException("Use getDispatcherConfig() to change the dispatcher configuration.");
            }

            return;
        }

        if (parameterSet instanceof RouterConfig) {
            if (!(parameterSet == routerConfig)) {
                throw new IllegalStateException("Use getRouterConfig() to change the router configuration.");
            }

            return;
        }

        if (parameterSet instanceof InteractionFinderConfig) {
            if (!(parameterSet == interactionFinderConfig)) {
                throw new IllegalStateException("Use getInteractionFinderConfig() to change the interaction finder configuration.");
            }

            return;
        }

        if (parameterSet instanceof TimingConfig) {
            if (!(parameterSet == timingConfig)) {
                throw new IllegalStateException("Use getTimingConfig() to change the timing configuration.");
            }

            return;
        }

        if (parameterSet instanceof AmodeusScoringConfig) {
            if (hasDefaultScoringConfiguration) {
                clearScoringParameters();
                hasDefaultScoringConfiguration = false;
            }

            super.addParameterSet(parameterSet);
            return;
        }

        throw new IllegalStateException("Invalid parameter set for AmodeusModeConfig: " + parameterSet.getName());
    }

    public PricingConfig getPricingConfig() {
        return pricingConfig;
    }

    public WaitingTimeConfig getWaitingTimeEstimationConfig() {
        return waitingTimeEstimationConfig;
    }

    public GeneratorConfig getGeneratorConfig() {
        return generatorConfig;
    }

    public DispatcherConfig getDispatcherConfig() {
        return dispatcherConfig;
    }

    public RouterConfig getRouterConfig() {
        return routerConfig;
    }

    public InteractionFinderConfig getInteractionFinderConfig() {
        return interactionFinderConfig;
    }

    public TimingConfig getTimingConfig() {
        return timingConfig;
    }

    public void addScoringParameters(AmodeusScoringConfig scoringParameters) {
        addParameterSet(scoringParameters);
    }

    public Collection<AmodeusScoringConfig> getScoringParameters() {
        return getParameterSets(AmodeusScoringConfig.GROUP_NAME).stream().map(AmodeusScoringConfig.class::cast).collect(Collectors.toSet());
    }

    public AmodeusScoringConfig getScoringParameters(String subpopulation) {
        for (AmodeusScoringConfig set : getScoringParameters()) {
            if (set.getSubpopulation() == null) {
                if (subpopulation == null) {
                    return set;
                }
            } else if (set.getSubpopulation().equals(subpopulation)) {
                return set;
            }
        }

        throw new IllegalStateException(String.format("No AMoDeus scoring parameters found for mode '%s' and subpopulation '%s'.", mode, subpopulation));
    }

    public void clearScoringParameters() {
        clearParameterSetsForType(AmodeusScoringConfig.GROUP_NAME);
    }

    // Getters and setters

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

    @StringGetter(USE_MODE_FILTERED_SUBNETWORK)
    public boolean getUseModeFilteredSubnetwork() {
        return useModeFilteredSubnetwork;
    }

    @StringSetter(USE_MODE_FILTERED_SUBNETWORK)
    public void setUseModeFilteredSubnetwork(boolean useModeFilteredSubnetwork) {
        this.useModeFilteredSubnetwork = useModeFilteredSubnetwork;
    }

    @StringGetter(USE_ACCESS_EGRESS)
    public boolean getUseAccessEgress() {
        return useAccessEgress;
    }

    @StringSetter(USE_ACCESS_EGRESS)
    public void setUseAccessAgress(boolean useAccessEgress) {
        this.useAccessEgress = useAccessEgress;
    }

    @StringGetter(MODE)
    @Override
    public String getMode() {
        return mode;
    }

    @StringSetter(MODE)
    public void setMode(String mode) {
        this.mode = mode;
    }
}
