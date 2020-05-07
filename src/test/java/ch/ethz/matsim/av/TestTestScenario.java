package ch.ethz.matsim.av;

import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.controler.Controler;

import ch.ethz.matsim.av.scenario.TestScenarioGenerator;

public class TestTestScenario {
    @Test
    public void testTestScenario() {
        Scenario scenario = TestScenarioGenerator.generate();
        Controler controler = new Controler(scenario);
        controler.run();
    }
}
