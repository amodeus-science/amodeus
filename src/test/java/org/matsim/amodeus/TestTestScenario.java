package org.matsim.amodeus;

import org.junit.Test;
import org.matsim.amodeus.scenario.TestScenarioGenerator;
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
