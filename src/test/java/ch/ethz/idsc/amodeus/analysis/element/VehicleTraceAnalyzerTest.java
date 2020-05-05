///* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
//package ch.ethz.idsc.amodeus.analysis.element;
//
//import ch.ethz.idsc.amodeus.ArtificialScenarioCreator;
//import ch.ethz.idsc.amodeus.data.ReferenceFrame;
//import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
//import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
//import ch.ethz.idsc.amodeus.net.VehicleContainer;
//import ch.ethz.idsc.amodeus.test.ScalarAssert;
//import ch.ethz.idsc.tensor.RationalScalar;
//import ch.ethz.idsc.tensor.Scalar;
//import ch.ethz.idsc.tensor.qty.Quantity;
//import ch.ethz.idsc.tensor.red.Total;
//import ch.ethz.idsc.tensor.sca.Round;
//import junit.framework.TestCase;
//import org.matsim.core.utils.geometry.CoordinateTransformation;
//
//public class VehicleTraceAnalyzerTest extends TestCase {
//    public void test1() {
//        VehicleTraceAnalyzerSetup setup = new VehicleTraceAnalyzerSetup();
//
//        VehicleContainer vc1 = new VehicleContainer();
//        vc1.vehicleIndex = 1;
//        vc1.roboTaxiStatus = RoboTaxiStatus.DRIVETOCUSTOMER;
//        vc1.destinationLinkIndex = setup.idLeft;
//        vc1.linkTrace = new int[] { setup.idUp };
//
//        VehicleContainer vc2 = new VehicleContainer();
//        vc2.vehicleIndex = 2;
//        vc2.roboTaxiStatus = RoboTaxiStatus.DRIVEWITHCUSTOMER;
//        vc2.destinationLinkIndex = setup.idLeft;
//        vc2.linkTrace = new int[] { setup.idRight, setup.idDown };
//
//        VehicleContainer vc3 = new VehicleContainer();
//        vc3.vehicleIndex = 3;
//        vc3.roboTaxiStatus = RoboTaxiStatus.REBALANCEDRIVE;
//        vc3.destinationLinkIndex = setup.idLeft;
//        vc3.linkTrace = new int[] { setup.idDown, setup.idLeft };
//
//        VehicleTraceAnalyzer traceAnalyzer = new VehicleTraceAnalyzer(4, setup.db);
//        traceAnalyzer.register(0, vc1);
//        traceAnalyzer.register(1, vc2);
//        traceAnalyzer.register(2, vc3);
//        traceAnalyzer.consolidate();
//
////        ScalarAssert scalarAssert = new ScalarAssert();
////        scalarAssert.add(setup.lengthUp.add(setup.lengthRight.add(setup.lengthDown).add(setup.lengthLeft)), (Scalar) Total.of(traceAnalyzer.stepDistanceTotal));
////        scalarAssert.add(setup.lengthLeft, (Scalar) Total.of(traceAnalyzer.stepDistancePickup));
////        scalarAssert.add(setup.lengthRight.add(setup.lengthDown.multiply(RationalScalar.HALF)), (Scalar) Total.of(traceAnalyzer.stepDistanceWithCustomer));
////        scalarAssert.add(setup.lengthDown.multiply(RationalScalar.HALF).add(setup.lengthLeft), (Scalar) Total.of(traceAnalyzer.stepDistanceRebalance));
////        scalarAssert.consolidate();
//    }
//
//    public void test2() {
//        VehicleTraceAnalyzerSetup setup = new VehicleTraceAnalyzerSetup();
//
//        VehicleContainer vc1 = new VehicleContainer();
//        vc1.vehicleIndex = 1;
//        vc1.roboTaxiStatus = RoboTaxiStatus.STAY;
//        vc1.destinationLinkIndex = setup.idLeft;
//        vc1.linkTrace = new int[] { setup.idUp };
//
//        VehicleContainer vc2 = new VehicleContainer();
//        vc2.vehicleIndex = 2;
//        vc2.roboTaxiStatus = RoboTaxiStatus.DRIVETOCUSTOMER;
//        vc2.destinationLinkIndex = setup.idLeft;
//        vc2.linkTrace = new int[] { setup.idUp };
//
//        VehicleContainer vc3 = new VehicleContainer();
//        vc3.vehicleIndex = 3;
//        vc3.roboTaxiStatus = RoboTaxiStatus.DRIVEWITHCUSTOMER;
//        vc3.destinationLinkIndex = setup.idLeft;
//        vc3.linkTrace = new int[] { setup.idRight, setup.idDown };
//
//        VehicleContainer vc4 = new VehicleContainer();
//        vc4.vehicleIndex = 4;
//        vc4.roboTaxiStatus = RoboTaxiStatus.DRIVEWITHCUSTOMER;
//        vc4.destinationLinkIndex = setup.idLeft;
//        vc4.linkTrace = new int[] { setup.idDown };
//
//        VehicleContainer vc5 = new VehicleContainer();
//        vc5.vehicleIndex = 5;
//        vc5.roboTaxiStatus = RoboTaxiStatus.REBALANCEDRIVE;
//        vc5.destinationLinkIndex = setup.idLeft;
//        vc5.linkTrace = new int[] { setup.idDown, setup.idLeft };
//
//        VehicleTraceAnalyzer traceAnalyzer = new VehicleTraceAnalyzer(5, setup.db);
//        traceAnalyzer.register(0, vc1);
//        traceAnalyzer.register(1, vc2);
//        traceAnalyzer.register(2, vc3);
//        traceAnalyzer.register(3, vc4);
//        traceAnalyzer.register(4, vc5);
//        traceAnalyzer.consolidate();
//
////        ScalarAssert scalarAssert = new ScalarAssert();
////        scalarAssert.add(setup.lengthUp.add(setup.lengthRight.add(setup.lengthDown).add(setup.lengthLeft)), (Scalar) Total.of(traceAnalyzer.stepDistanceTotal));
////        scalarAssert.add(setup.lengthLeft, (Scalar) Total.of(traceAnalyzer.stepDistancePickup));
////        scalarAssert.add(setup.lengthRight.add(setup.lengthDown.multiply(RationalScalar.of(2, 3))), (Scalar) Total.of(traceAnalyzer.stepDistanceWithCustomer));
////        scalarAssert.add(Round._9.apply(setup.lengthDown.multiply(RationalScalar.of(1, 3)).add(setup.lengthLeft)), //
////                Round._9.apply((Scalar) Total.of(traceAnalyzer.stepDistanceRebalance)));
////        scalarAssert.consolidate();
//    }
//
//    class VehicleTraceAnalyzerSetup {
//        final MatsimAmodeusDatabase db;
//
//        final int idUp;
//        final int idLeft;
//        final int idRight;
//        final int idDown;
//
//        final Scalar lengthUp;
//        final Scalar lengthRight;
//        final Scalar lengthDown;
//        final Scalar lengthLeft;
//
//        VehicleTraceAnalyzerSetup() {
//            ArtificialScenarioCreator creator = new ArtificialScenarioCreator();
//            ReferenceFrame referenceFrame = new ReferenceFrame() {
//                @Override
//                public CoordinateTransformation coords_fromWGS84() {
//                    return c -> c;
//                }
//
//                @Override
//                public CoordinateTransformation coords_toWGS84() {
//                    return c -> c;
//                }
//            };
//            db = MatsimAmodeusDatabase.initialize(creator.network, referenceFrame);
//
//            idUp = db.getLinkIndex(creator.linkUp);
//            idLeft = db.getLinkIndex(creator.linkLeft);
//            idRight = db.getLinkIndex(creator.linkRight);
//            idDown = db.getLinkIndex(creator.linkDown);
//
//            lengthUp = Quantity.of(db.getOsmLink(idUp).getLength(), referenceFrame.unit());
//            lengthRight = Quantity.of(db.getOsmLink(idDown).getLength(), referenceFrame.unit());
//            lengthDown = Quantity.of(db.getOsmLink(idDown).getLength(), referenceFrame.unit());
//            lengthLeft = Quantity.of(db.getOsmLink(idLeft).getLength(), referenceFrame.unit());
//        }
//    }
//}
