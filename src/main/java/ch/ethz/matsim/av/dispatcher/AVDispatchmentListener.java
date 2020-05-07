package ch.ethz.matsim.av.dispatcher;

import org.matsim.core.mobsim.framework.events.MobsimBeforeSimStepEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimBeforeSimStepListener;

// TODO: I think this class is pretty useless... pass this on directly to the dispatcher, it is QSim scope!
public class AVDispatchmentListener implements MobsimBeforeSimStepListener {
    private final AVDispatcher dispatcher;

    public AVDispatchmentListener(AVDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void notifyMobsimBeforeSimStep(@SuppressWarnings("rawtypes") MobsimBeforeSimStepEvent e) {
        dispatcher.onNextTimestep(e.getSimulationTime());
    }
}
