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

	@Test
	public void testNoPickupTime() {
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

	@Test
	public void testPickupTimePerStop() {
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

	@Test
	public void testPickupTimePerPerson() {
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

	@Test
	public void testWaitEmptyForPerson() {
		AVConfigGroup config = TestScenario.createConfig();
		Scenario scenario = TestScenario.createScenario(config, Arrays.asList(new TestRequest(0.0, 100.0)));

		TestScenario.ArrivalListener listener = new TestScenario.ArrivalListener();
		Controler controller = TestScenario.createController(scenario, listener, 1);
		controller.run();

		Assert.assertEquals(1, listener.times.size());
		Assert.assertEquals(1013.0 + 100.0, listener.times.get(0), 1e-3);
	}

	@Test
	public void testDropoffTime() {
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
