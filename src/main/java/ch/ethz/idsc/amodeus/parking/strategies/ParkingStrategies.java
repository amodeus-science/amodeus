/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.strategies;

import java.util.Random;

public enum ParkingStrategies {
    NONE {
        @Override
        public ParkingStrategy generateParkingStrategy(Random random) {
            return new ParkingNullStrategy();
        }
    }, //
    RANDOMDIFUSION {
        @Override
        public ParkingStrategy generateParkingStrategy(Random random) {
            return new ParkingRandomDiffusion(random);
        }
    }, //
    DIRECTEDDIFUSION {
        @Override
        public ParkingStrategy generateParkingStrategy(Random random) {
            return new ParkingDirectedDiffusion(random);
        }
    }, //
    LP {
        @Override
        public ParkingStrategy generateParkingStrategy(Random random) {
            return new ParkingFlow();
        }
    };

    public abstract ParkingStrategy generateParkingStrategy(Random random);

}
