/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.strategies;

import java.util.Random;

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
            return new ParkingRandomDiffusion(new Random(RANDOMSEED));
        }
    }, //
    DIRECTEDDIFUSION {
        @Override
        public ParkingStrategy generateParkingStrategy() {
            return new ParkingDirectedDiffusion(new Random(RANDOMSEED));
        }
    }, //
    LP {
        @Override
        public ParkingStrategy generateParkingStrategy() {
            return new ParkingFlow();
        }
    };

    private static final long RANDOMSEED = 1234;

    public abstract ParkingStrategy generateParkingStrategy();

}
