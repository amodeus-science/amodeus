/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.analysis.element;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import amodeus.amodeus.analysis.report.TotalValueAppender;
import amodeus.amodeus.analysis.report.TotalValueIdentifier;
import amodeus.amodeus.analysis.report.TtlValIdent;
import amodeus.amodeus.dispatcher.core.RStatusHelper;
import amodeus.amodeus.net.RequestContainer;
import amodeus.amodeus.net.SimulationObject;
import amodeus.amodeus.util.math.MeanOrZero;
import amodeus.amodeus.util.math.QuantileOrZero;
import amodeus.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Join;
import ch.ethz.idsc.tensor.io.TableBuilder;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.red.Max;
import ch.ethz.idsc.tensor.red.Mean;
import ch.ethz.idsc.tensor.red.Quantile;

public class TravelTimeAnalysis implements AnalysisElement, TotalValueAppender {

	private final Map<Integer, TravelHistory> travelHistories = new TreeMap<>();
	public final TableBuilder travelTimes = new TableBuilder();
	public final TableBuilder requstStmps = new TableBuilder();

	/** aggregate values {quantile1, quantile2, quantile3, min} */
	private Tensor waitTAgg;
	private Tensor drveAgg;
	private Tensor totJTAgg;
	private Scalar tLast = Quantity.of(0, SI.SECOND);

	/** max values */
	private Scalar maxWaitTime;
	private Scalar maxDrveTime;
	private Scalar maxTravelTime;

	/** time series during day */
	public final Tensor time = Tensors.empty();
	public final Tensor waitTimePlotValues = Tensors.empty();
	public final Tensor waitingCustomers = Tensors.empty();

	@Override
	public void register(SimulationObject simulationObject) {

		/** build a travel history for every request */
		for (RequestContainer requestContainer : simulationObject.requests) {
			Integer requestIndex = requestContainer.requestIndex;
			if (travelHistories.containsKey(requestIndex))
				travelHistories.get(requestIndex).register(requestContainer,
						Quantity.of(simulationObject.now, SI.SECOND));
			else
				travelHistories.put(requestIndex, new TravelHistory(requestContainer, simulationObject.now));
		}
		/**
		 * analyze the distribution of wait times at every time instant and the number
		 * of waiting customers
		 */
		time.append(RealScalar.of(simulationObject.now));
		Tensor submission = Tensor.of(simulationObject.requests.stream()//
				.filter(rc -> RStatusHelper.unserviced(rc.requestStatus))//
				.map(rc -> RealScalar.of(simulationObject.now - rc.submissionTime)));
		waitTimePlotValues.append(Join.of(Quantiles.SET.map(QuantileOrZero.of(submission)), //
				Tensors.vector(MeanOrZero.of(submission).number().doubleValue())));
		waitingCustomers.append(RationalScalar.of(simulationObject.requests.stream()//
				.filter(rc -> RStatusHelper.unserviced(rc.requestStatus)).count(), 1));//

		/** maximum time */
		tLast = Quantity.of(simulationObject.now, SI.SECOND);

	}

	@Override
	public void consolidate() {
		/** calculate standard dropoff time. */
		travelHistories.values().forEach(th -> th.fillNotFinishedData(tLast));

		/** finish filling of travel Histories */
		for (TravelHistory travelHistory : travelHistories.values()) {
			travelTimes.appendRow(Tensors.of( //
					RealScalar.of(travelHistory.reqIndx), travelHistory.getWaitTime(), //
					travelHistory.getDriveTime(), travelHistory.getTotalTravelTime()));
			requstStmps.appendRow(Tensors.of( //
					RealScalar.of(travelHistory.reqIndx), travelHistory.submsnTime, //
					travelHistory.getAssignmentTime(), travelHistory.getWaitEndTime(), //
					travelHistory.getDropOffTime()));
		}

		/** calculate maximum values */
		maxWaitTime = (Scalar) getWaitTimes().flatten(-1).reduce(Max::of).get();
		maxDrveTime = (Scalar) getDriveTimes().flatten(-1).reduce(Max::of).get();
		maxTravelTime = (Scalar) getTotalJourneyTimes().flatten(-1).reduce(Max::of).get();
		/** aggregate information {quantile1, quantile2, quantile3, mean, maximum} */
		waitTAgg = Tensors.of(Quantiles.SET.map(QuantileOrZero.of(getWaitTimes())), //
				Mean.of(getWaitTimes()), //
				maxWaitTime);
		drveAgg = Tensors.of(Quantiles.SET.map(QuantileOrZero.of(getDriveTimes())), //
				Mean.of(getDriveTimes()), //
				maxDrveTime);
		totJTAgg = Tensors.of(Quantiles.SET.map(QuantileOrZero.of(getTotalJourneyTimes())), //
				Mean.of(getTotalJourneyTimes()), //
				maxTravelTime);

	}

	/**
	 * @return {@link Tensor} containing all recorded wait times of the simulation
	 */
	public Tensor getWaitTimes() {
		return travelTimes.getTable().get(Tensor.ALL, 1);
	}

	/**
	 * @return {@link Tensor} containing all recorded drive times of the simulation
	 */
	public Tensor getDriveTimes() {
		return travelTimes.getTable().get(Tensor.ALL, 2);
	}

	/**
	 * @return {@link Tensor} containing all recorded total journey times of the
	 *         simulation
	 */
	public Tensor getTotalJourneyTimes() {
		return travelTimes.getTable().get(Tensor.ALL, 3);
	}

	/**
	 * @return {@link Tensor} containing {{wait time quantile 1, wait time quantile
	 *         2, wait time quantile 3},wait time mean,wait time maximum} chosen
	 *         quantiles defined in {@link Quantiles}
	 */
	public Tensor getWaitAggrgte() {
		return waitTAgg;
	}

	/**
	 * @return {@link Tensor} containing {{drive time quantile 1, drive time
	 *         quantile 2, drive time quantile 3},drive time mean,drive time
	 *         maximum} chosen quantiles defined in {@link Quantiles}
	 */
	public Tensor getDrveAggrgte() {
		return drveAgg;
	}

	/**
	 * @return {@link Tensor} containing {{total travel time quantile 1, total
	 *         travel time quantile 2, total travel time quantile 3},total travel
	 *         time mean, total travel time maximum} chosen quantiles defined in
	 *         {@link Quantiles}
	 */
	public Tensor getTotJAggrgte() {
		return totJTAgg;
	}

	/**
	 * @return and unmodifiable Map on the travel histories with the Request Index
	 *         as key and the {@link TravelHistory} as value
	 */
	public Map<Integer, TravelHistory> getTravelHistories() {
		return Collections.unmodifiableMap(travelHistories);
	}

	@Override
	public Map<TotalValueIdentifier, String> getTotalValues() {
		Map<TotalValueIdentifier, String> map = new HashMap<>();
		/** Wait Times */
		double meanWaitTime = ((Scalar) Mean.of(getWaitTimes())).number().doubleValue();
		map.put(TtlValIdent.WAITTMEA, String.valueOf(meanWaitTime));
		Tensor quantils = Quantiles.SET.map(Quantile.of(getWaitTimes()));
		map.put(TtlValIdent.WAITTQU1, String.valueOf(quantils.Get(0).number().doubleValue()));
		map.put(TtlValIdent.WAITTQU2, String.valueOf(quantils.Get(1).number().doubleValue()));
		map.put(TtlValIdent.WAITTQU3, String.valueOf(quantils.Get(2).number().doubleValue()));
		map.put(TtlValIdent.WAITTMAX, String.valueOf(maxWaitTime.number().doubleValue()));

		/** Drive Times */
		double meanDriveTime = ((Scalar) Mean.of(getDriveTimes())).number().doubleValue();
		map.put(TtlValIdent.DRIVETMEA, String.valueOf(meanDriveTime));
		Tensor quantilsDriveTime = Quantiles.SET.map(Quantile.of(getDriveTimes()));
		map.put(TtlValIdent.DRIVETQU1, String.valueOf(quantilsDriveTime.Get(0).number().doubleValue()));
		map.put(TtlValIdent.DRIVETQU2, String.valueOf(quantilsDriveTime.Get(1).number().doubleValue()));
		map.put(TtlValIdent.DRIVETQU3, String.valueOf(quantilsDriveTime.Get(2).number().doubleValue()));
		map.put(TtlValIdent.DRIVETMAX, String.valueOf(maxDrveTime.number().doubleValue()));

		/** Travel Times */
		double meanTravelTime = ((Scalar) Mean.of(getTotalJourneyTimes())).number().doubleValue();
		map.put(TtlValIdent.TRAVELTMEA, String.valueOf(meanTravelTime));
		Tensor quantilstravelTime = Quantiles.SET.map(Quantile.of(getTotalJourneyTimes()));
		map.put(TtlValIdent.TRAVELTQU1, String.valueOf(quantilstravelTime.Get(0).number().doubleValue()));
		map.put(TtlValIdent.TRAVELTQU2, String.valueOf(quantilstravelTime.Get(1).number().doubleValue()));
		map.put(TtlValIdent.TRAVELTQU3, String.valueOf(quantilstravelTime.Get(2).number().doubleValue()));
		map.put(TtlValIdent.TRAVELTMAX, String.valueOf(maxTravelTime.number().doubleValue()));

		return map;
	}
}
