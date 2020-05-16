package ch.ethz.matsim.av.dynamics;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.controler.Controler;

import ch.ethz.matsim.av.config.AVConfigGroup;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.config.operator.TimingConfig;

/**
 * Execute av.dynamics pick-up and drop-off tests
 */
public class AVPickupDropoffTest {

    /**
     * Test pick-up duration time = 0.
     */
    @Test public void testNoPickupTime() {
        { // One agent, no pickup time
            AVConfigGroup config = TestScenario.createConfig();
            Scenario scenario = TestScenario.createScenario(config, Arrays.asList(new TestRequest(0.0, 0.0)));

            TestScenario.ArrivalListener listener = new TestScenario.ArrivalListener();
            Controler controller = TestScenario.createController(scenario, listener, 1);
            controller.run();

            Assert.assertEquals(1, listener.times.size());
            Assert.assertEquals(1013.0, listener.times.get(0), 1e-3);
        }

        { // Three agents, no pickup time
            AVConfigGroup config = TestScenario.createConfig();
            Scenario scenario = TestScenario.createScenario(config, Arrays.asList( //
                    new TestRequest(0.0, 0.0), //
                    new TestRequest(0.0, 0.0), //
                    new TestRequest(0.0, 0.0) //
            ));

            TestScenario.ArrivalListener listener = new TestScenario.ArrivalListener();
            Controler controller = TestScenario.createController(scenario, listener, 4);
            controller.run();

            Assert.assertEquals(3, listener.times.size());
            Assert.assertEquals(1013.0, listener.times.get(0), 1e-3);
            Assert.assertEquals(1013.0, listener.times.get(1), 1e-3);
            Assert.assertEquals(1013.0, listener.times.get(2), 1e-3);
        }
    }

    /**
     * Test pick-up duration time = 15 (pickupDurationPerStop setting), test for single stop and multiple stops
     */
    @Test public void testPickupTimePerStop() {
        { // One agent, 15s pickup time per stop
            AVConfigGroup config = TestScenario.createConfig();
            OperatorConfig operatorConfig = config.getOperatorConfigs().values().iterator().next();
            TimingConfig timingConfig = operatorConfig.getTimingConfig();
            timingConfig.setPickupDurationPerStop(15.0);

            Scenario scenario = TestScenario.createScenario(config, Arrays.asList(new TestRequest(0.0, 0.0)));

            TestScenario.ArrivalListener listener = new TestScenario.ArrivalListener();
            Controler controller = TestScenario.createController(scenario, listener, 1);
            controller.run();

            Assert.assertEquals(1, listener.times.size());
            Assert.assertEquals(1013.0 + 15.0, listener.times.get(0), 1e-3);
        }

        { // Three agents, 15s pickup time per stop
            AVConfigGroup config = TestScenario.createConfig();
            OperatorConfig operatorConfig = config.getOperatorConfigs().values().iterator().next();
            TimingConfig timingConfig = operatorConfig.getTimingConfig();
            timingConfig.setPickupDurationPerStop(15.0);

            Scenario scenario = TestScenario.createScenario(config, Arrays.asList( //
                    new TestRequest(0.0, 0.0), //
                    new TestRequest(0.0, 0.0), //
                    new TestRequest(0.0, 0.0) //
            ));

            TestScenario.ArrivalListener listener = new TestScenario.ArrivalListener();
            Controler controller = TestScenario.createController(scenario, listener, 4);
            controller.run();

            Assert.assertEquals(3, listener.times.size());
            Assert.assertEquals(1013.0 + 15.0, listener.times.get(0), 1e-3);
            Assert.assertEquals(1013.0 + 15.0, listener.times.get(1), 1e-3);
            Assert.assertEquals(1013.0 + 15.0, listener.times.get(2), 1e-3);
        }
    }

    /**
     * Test pick-up duration per passenger setting, single agent and multiple agents
     */
    @Test public void testPickupTimePerPerson() {
        { // One agent, 15s pickup time per person
            AVConfigGroup config = TestScenario.createConfig();
            OperatorConfig operatorConfig = config.getOperatorConfigs().values().iterator().next();
            TimingConfig timingConfig = operatorConfig.getTimingConfig();
            timingConfig.setPickupDurationPerPassenger(15.0);

            Scenario scenario = TestScenario.createScenario(config, Arrays.asList(new TestRequest(0.0, 0.0)));

            TestScenario.ArrivalListener listener = new TestScenario.ArrivalListener();
            Controler controller = TestScenario.createController(scenario, listener, 1);
            controller.run();

            Assert.assertEquals(1, listener.times.size());
            Assert.assertEquals(1013.0 + 15.0, listener.times.get(0), 1e-3);
        }

        { // Three agents, 15s pickup time per person
            AVConfigGroup config = TestScenario.createConfig();
            OperatorConfig operatorConfig = config.getOperatorConfigs().values().iterator().next();
            TimingConfig timingConfig = operatorConfig.getTimingConfig();
            timingConfig.setPickupDurationPerPassenger(15.0);

            Scenario scenario = TestScenario.createScenario(config, Arrays.asList( //
                    new TestRequest(0.0, 0.0), //
                    new TestRequest(0.0, 0.0), //
                    new TestRequest(0.0, 0.0) //
            ));

            TestScenario.ArrivalListener listener = new TestScenario.ArrivalListener();
            Controler controller = TestScenario.createController(scenario, listener, 4);
            controller.run();

            Assert.assertEquals(3, listener.times.size());
            Assert.assertEquals(1013.0 + 15.0 * 3.0, listener.times.get(0), 1e-3);
            Assert.assertEquals(1013.0 + 15.0 * 3.0, listener.times.get(1), 1e-3);
            Assert.assertEquals(1013.0 + 15.0 * 3.0, listener.times.get(2), 1e-3);
        }
    }

    /**
     * Test request delay time
     */
    @Test public void testWaitEmptyForPerson() {
        AVConfigGroup config = TestScenario.createConfig();
        Scenario scenario = TestScenario.createScenario(config, Arrays.asList(new TestRequest(0.0, 100.0)));

        TestScenario.ArrivalListener listener = new TestScenario.ArrivalListener();
        Controler controller = TestScenario.createController(scenario, listener, 1);
        controller.run();

        Assert.assertEquals(1, listener.times.size());
        Assert.assertEquals(1013.0 + 100.0, listener.times.get(0), 1e-3);
    }

    /**
     * Test various drop-off parameters (scenario where there is no drop-off time, then where there is
     * a drop-off time of 15s per stop and a last scenario where there is a drop-off time per passenger
     */
    @Test public void testDropoffTime() {
        { // Three agents, no dropoff time
            AVConfigGroup config = TestScenario.createConfig();
            Scenario scenario = TestScenario.createScenario(config, Arrays.asList( //
                    new TestRequest(0.0, 0.0), //
                    new TestRequest(0.0, 0.0), //
                    new TestRequest(0.0, 0.0) //
            ));

            TestScenario.ArrivalListener listener = new TestScenario.ArrivalListener();
            Controler controller = TestScenario.createController(scenario, listener, 4);
            controller.run();

            Assert.assertEquals(3, listener.times.size());
            Assert.assertEquals(1013.0, listener.times.get(0), 1e-3);
            Assert.assertEquals(1013.0, listener.times.get(1), 1e-3);
            Assert.assertEquals(1013.0, listener.times.get(2), 1e-3);
        }

        { // Three agents, 15s dropoff time per stop
            AVConfigGroup config = TestScenario.createConfig();
            OperatorConfig operatorConfig = config.getOperatorConfigs().values().iterator().next();
            TimingConfig timingConfig = operatorConfig.getTimingConfig();
            timingConfig.setDropoffDurationPerStop(15.0);

            Scenario scenario = TestScenario.createScenario(config, Arrays.asList( //
                    new TestRequest(0.0, 0.0), //
                    new TestRequest(0.0, 0.0), //
                    new TestRequest(0.0, 0.0) //
            ));

            TestScenario.ArrivalListener listener = new TestScenario.ArrivalListener();
            Controler controller = TestScenario.createController(scenario, listener, 4);
            controller.run();

            Assert.assertEquals(3, listener.times.size());
            Assert.assertEquals(1013.0 + 15.0, listener.times.get(0), 1e-3);
            Assert.assertEquals(1013.0 + 15.0, listener.times.get(1), 1e-3);
            Assert.assertEquals(1013.0 + 15.0, listener.times.get(2), 1e-3);
        }

        { // Three agents, 15s dropoff time per passenger
            AVConfigGroup config = TestScenario.createConfig();
            OperatorConfig operatorConfig = config.getOperatorConfigs().values().iterator().next();
            TimingConfig timingConfig = operatorConfig.getTimingConfig();
            timingConfig.setDropoffDurationPerPassenger(15.0);

            Scenario scenario = TestScenario.createScenario(config, Arrays.asList( //
                    new TestRequest(0.0, 0.0), //
                    new TestRequest(0.0, 0.0), //
                    new TestRequest(0.0, 0.0) //
            ));

            TestScenario.ArrivalListener listener = new TestScenario.ArrivalListener();
            Controler controller = TestScenario.createController(scenario, listener, 4);
            controller.run();

            Assert.assertEquals(3, listener.times.size());
            Assert.assertEquals(1013.0 + 15.0 * 3.0, listener.times.get(0), 1e-3);
            Assert.assertEquals(1013.0 + 15.0 * 3.0, listener.times.get(1), 1e-3);
            Assert.assertEquals(1013.0 + 15.0 * 3.0, listener.times.get(2), 1e-3);
        }
    }
}
