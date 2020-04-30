package ch.ethz.matsim.av.config.operator;

import org.matsim.core.config.ReflectiveConfigGroup;

public class PricingConfig extends ReflectiveConfigGroup {
	static public final String GROUP_NAME = "pricing";

	static public final String PRICE_PER_KM = "pricePerKm";
	static public final String PRICE_PER_MIN = "pricePerMin";
	static public final String PRICE_PER_TRIP = "pricePerTrip";

	static public final String TEMPORAL_BILLING_INTERVAL = "temporalBillingInterval";
	static public final String SPATIAL_BILLING_INTERVAL = "spatialBillingInterval";

	private double pricePerKm = 0.0;
	private double pricePerMin = 0.0;
	private double pricePerTrip = 0.0;

	private double temporalBillingInterval = 1.0;
	private double spatialBillingInterval = 1.0;

	public PricingConfig() {
		super(GROUP_NAME);
	}

	public static PricingConfig createDefault() {
		return new PricingConfig();
	}

	@StringGetter(PRICE_PER_KM)
	public double getPricePerKm() {
		return pricePerKm;
	}

	@StringSetter(PRICE_PER_KM)
	public void setPricePerKm(double pricePerKm) {
		this.pricePerKm = pricePerKm;
	}

	@StringGetter(PRICE_PER_MIN)
	public double getPricePerMin() {
		return pricePerMin;
	}

	@StringSetter(PRICE_PER_MIN)
	public void setPricePerMin(double pricePerMin) {
		this.pricePerMin = pricePerMin;
	}

	@StringGetter(PRICE_PER_TRIP)
	public double getPricePerTrip() {
		return pricePerTrip;
	}

	@StringSetter(PRICE_PER_TRIP)
	public void setPricePerTrip(double pricePerTrip) {
		this.pricePerTrip = pricePerTrip;
	}

	@StringGetter(TEMPORAL_BILLING_INTERVAL)
	public double getTemporalBillingInterval() {
		return temporalBillingInterval;
	}

	@StringSetter(TEMPORAL_BILLING_INTERVAL)
	public void setTemporalBillingInterval(double temporalBillingInterval) {
		this.temporalBillingInterval = temporalBillingInterval;
	}

	@StringGetter(SPATIAL_BILLING_INTERVAL)
	public double getSpatialBillingInterval() {
		return spatialBillingInterval;
	}

	@StringSetter(SPATIAL_BILLING_INTERVAL)
	public void setSpatialBillingInterval(double spatialBillingInterval) {
		this.spatialBillingInterval = spatialBillingInterval;
	}
}
