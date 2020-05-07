package ch.ethz.matsim.av.waiting_time;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.run.AbstractDvrpModeModule;
import org.matsim.contrib.dvrp.run.ModalProviders;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import ch.ethz.matsim.av.config.AVConfigGroup;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.data.AVOperator;

public class WaitingTimeModeModule extends AbstractDvrpModeModule {
    private final Id<AVOperator> operatorId;

    public WaitingTimeModeModule(Id<AVOperator> operatorId, String mode) {
        super(mode);
        this.operatorId = operatorId;
    }

    @Override
    public void install() {
        OperatorConfig operatorConfig = AVConfigGroup.getOrCreate(getConfig()).getOperatorConfig(operatorId);

        if (operatorConfig.getWaitingTimeConfig().getEstimationAlpha() > 0.0) {
            bindModal(WaitingTimeCollector.class).toProvider(new WaitingTimeCollectorProvider(getMode())).in(Singleton.class);
            bindModal(WaitingTimeListener.class).toProvider(new WaitingTimeListenerProvider(getMode())).in(Singleton.class);

            addEventHandlerBinding().to(modalKey(WaitingTimeListener.class));
            addControlerListenerBinding().to(modalKey(WaitingTimeListener.class));
        }

        bindModal(WaitingTime.class).toProvider(new WaitingTimeProvider(getMode())).in(Singleton.class);
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
            OperatorConfig operatorConfig = getModalInstance(OperatorConfig.class);
            return factory.createWaitingTime(operatorConfig, network);
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
