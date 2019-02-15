// TODO
/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
// package ch.ethz.idsc.amodeus.dispatcher.core;
//
// import org.matsim.api.core.v01.events.Event;
// import org.matsim.core.api.experimental.events.EventsManager;
// import org.matsim.core.config.Config;
// import org.matsim.core.events.handler.EventHandler;
// import org.matsim.core.router.util.TravelTime;
// import org.matsim.core.utils.geometry.CoordinateTransformation;
// import org.matsim.core.utils.geometry.transformations.IdentityTransformation;
//
// import ch.ethz.idsc.amodeus.data.ReferenceFrame;
// import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
// import ch.ethz.matsim.av.config.AVConfig;
// import ch.ethz.matsim.av.config.AVDispatcherConfig;
// import ch.ethz.matsim.av.config.AVOperatorConfig;
// import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;
// import junit.framework.TestCase;
//
// public class SharedUniversalDispatcherTest extends TestCase {
// public void testSimple() {
//
// Config config = new Config();
// config.addCoreModules();
// AVConfig avConfig = new AVConfig();
//
// AVOperatorConfig aVOperatorConfig = avConfig.createOperatorConfig("oper");
// AVDispatcherConfig avDispatcherConfig = aVOperatorConfig.createDispatcherConfig("disp"); //
// TravelTime travelTime = null;//
// ParallelLeastCostPathCalculator parallelLeastCostPathCalculator = null; //
// EventsManager eventsManager = new EventsManager() {
//
// @Override
// public void resetHandlers(int iteration) {
// }
//
// @Override
// public void removeHandler(EventHandler handler) {
// }
//
// @Override
// public void processEvent(Event event) {
// }
//
// @Override
// public void initProcessing() {
// }
//
// @Override
// public void finishProcessing() {
// }
//
// @Override
// public void afterSimStep(double time) {
// }
//
// @Override
// public void addHandler(EventHandler handler) {
// }
// }; //
//
// ReferenceFrame ref = new ReferenceFrame() {
//
// @Override
// public CoordinateTransformation coords_toWGS84() {
// return new IdentityTransformation();
// }
//
// @Override
// public CoordinateTransformation coords_fromWGS84() {
// return new IdentityTransformation();
// }
// };
//
// ArtificialScenarioCreator s = new ArtificialScenarioCreator(config);
// MatsimAmodeusDatabase db = MatsimAmodeusDatabase.initialize(s.network, ref);
// SharedUniversalDispatcher dispatcher = new SharedUniversalDispatcher(config, avDispatcherConfig, travelTime, parallelLeastCostPathCalculator, eventsManager,
// db) {
//
// @Override
// protected void redispatch(double now) {
//
// }
//
// };
//
// dispatcher.addVehicle(s.vehicle1);
// dispatcher.addVehicle(s.vehicle2);
// dispatcher.onRequestSubmitted(s.avRequest1);
// dispatcher.onRequestSubmitted(s.avRequest2);
// dispatcher.onRequestSubmitted(s.avRequest3);
//
// dispatcher.onNextTimestep(0.0);
//
// assertEquals(dispatcher.getAVRequests().size(), 3);
// assertEquals(dispatcher.getUnassignedAVRequests().size(), 3);
// assertEquals(dispatcher.getDivertableRoboTaxisWithoutCustomerOnBoard().size(), 2);
// assertEquals(dispatcher.getDivertableRoboTaxis(), 2);
// assertEquals(dispatcher.getRoboTaxis().size(), 2);
//
// // TestDispatcherShared disp = new TestDispatcherShared(config, avDispatcherConfig, travelTime, parallelLeastCostPathCalculator, eventsManager, db)
//
// }
// }
