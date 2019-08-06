/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;

public class PopulationCutterNetworkBased implements PopulationCutterFunction {

    private final Network network;
    private final int endTime;

    public PopulationCutterNetworkBased(Network network, int endTime) {
        this.network = network;
        this.endTime = endTime;
    }

    @Override
    public void process(Population population) {
        PopulationReduce.outsideNetwork(population, network);
        System.out.println("population size after removing all people outside the network: " + population.getPersons().size());
        // TODO why is this here? check and remove TODO big TODO massive TODO
        PopulationTimeInterval.removeOutside(population, endTime);
        System.out.println("population size after removing all people outside the time interval: " + population.getPersons().size());
    }

    @Override
    public void printCutSummary() {
        System.out.println("Population cutter: network based.");
        System.out.println("Population outside network removed.");
    }

}
