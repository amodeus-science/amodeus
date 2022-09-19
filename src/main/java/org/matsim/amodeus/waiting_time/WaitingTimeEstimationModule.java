package org.matsim.amodeus.waiting_time;

import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.amodeus.config.modal.WaitingTimeConfig;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.run.AbstractDvrpModeModule;
import org.matsim.core.modal.ModalProviders;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

public class WaitingTimeEstimationModule extends AbstractDvrpModeModule {
    private final AmodeusModeConfig modeConfig;

    public WaitingTimeEstimationModule(AmodeusModeConfig modeConfig) {
        super(modeConfig.getMode());
        this.modeConfig = modeConfig;
    }

    @Override
    public void install() {
        WaitingTimeConfig waitingTimeConfig = modeConfig.getWaitingTimeEstimationConfig();

        if (waitingTimeConfig.getEstimationAlpha() > 0.0) {
            bindModal(WaitingTimeCollector.class)
                    .toProvider((Provider<? extends WaitingTimeCollector>) new WaitingTimeCollectorProvider(getMode()))
                    .in(Singleton.class);
            bindModal(WaitingTimeListener.class)
                    .toProvider((Provider<? extends WaitingTimeListener>) new WaitingTimeListenerProvider(getMode()))
                    .in(Singleton.class);

            addEventHandlerBinding().to(modalKey(WaitingTimeListener.class));
            addControlerListenerBinding().to(modalKey(WaitingTimeListener.class));
        }

        bindModal(WaitingTime.class)
                .toProvider((Provider<? extends WaitingTime>) new WaitingTimeProvider(getMode()))
                .in(Singleton.class);
    }

    private static class WaitingTimeProvider extends ModalProviders.AbstractProvider<WaitingTime> {
        @Inject
        WaitingTimeFactory factory; // TODO: Why not named factories?

        WaitingTimeProvider(String mode) {
            super(mode);
        }

        @Override
        public WaitingTime get() {
            Network network = getModalInstance(Network.class);
            AmodeusModeConfig modeConfig = getModalInstance(AmodeusModeConfig.class);
            return factory.createWaitingTime(modeConfig, network);
        }
    };

    private static class WaitingTimeCollectorProvider extends ModalProviders.AbstractProvider<WaitingTimeCollector> {
        WaitingTimeCollectorProvider(String mode) {
            super(mode);
        }

        @Override
        public WaitingTimeCollector get() {
            return (WaitingTimeCollector) getModalInstance(WaitingTime.class);
        }
    };

    private static class WaitingTimeListenerProvider extends ModalProviders.AbstractProvider<WaitingTimeListener> {
        WaitingTimeListenerProvider(String mode) {
            super(mode);
        }

        @Override
        public WaitingTimeListener get() {
            WaitingTimeCollector collector = getModalInstance(WaitingTimeCollector.class);
            return new WaitingTimeListener(collector, getMode());
        }
    }
}
