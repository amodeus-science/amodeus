package ch.ethz.idsc.amodeus.prep;

import java.io.IOException;
import java.net.MalformedURLException;

import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;

public interface PopulationCutter {

    abstract void cut(Population population, Network network, Config config) throws MalformedURLException, IOException, Exception;
}
