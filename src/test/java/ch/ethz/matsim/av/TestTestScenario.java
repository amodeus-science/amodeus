package ch.ethz.matsim.av;

import ch.ethz.matsim.av.scenario.TestScenarioGenerator;
import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.controler.Controler;

public class TestTestScenario {
    @Test
    public void testTestScenario() {
        Scenario scenario = TestScenarioGenerator.generate();
        Controler controler = new Controler(scenario);
        controler.run();
    }
}
