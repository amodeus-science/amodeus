/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.parking.strategies;

public enum ParkingStrategies {
    NONE() {
        @Override
        public ParkingStrategy generateParkingStrategy() {
            return new ParkingNullStrategy();
        }
    }, //
    RANDOMDIFUSION {
        @Override
        public ParkingStrategy generateParkingStrategy() {
            return new ParkingRandomDiffusion();
        }
    }, //
    DIRECTEDDIFUSION {
        @Override
        public ParkingStrategy generateParkingStrategy() {
            return new ParkingDirectedDiffusion();
        }
    }, //
    LP {
        @Override
        public ParkingStrategy generateParkingStrategy() {
            return new ParkingLP();
        }
    };

    public abstract ParkingStrategy generateParkingStrategy();

}
