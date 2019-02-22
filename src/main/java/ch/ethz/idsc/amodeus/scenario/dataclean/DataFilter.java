package ch.ethz.idsc.amodeus.scenario.dataclean;

import java.util.stream.Stream;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;

public interface DataFilter<T> {

    Stream<T> filter(Stream<T> stream, ScenarioOptions simOptions, Network network);

}
