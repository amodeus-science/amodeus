package ch.ethz.matsim.av.dispatcher;

import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.core.mobsim.framework.events.MobsimBeforeSimStepEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimBeforeSimStepListener;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import ch.ethz.matsim.av.data.AVOperator;

@Singleton
public class AVDispatchmentListener implements MobsimBeforeSimStepListener {
    @Inject
    Map<Id<AVOperator>, AVDispatcher> dispatchers;

    @Override
    public void notifyMobsimBeforeSimStep(@SuppressWarnings("rawtypes") MobsimBeforeSimStepEvent e) {
        for (AVDispatcher dispatcher : dispatchers.values()) {
            dispatcher.onNextTimestep(e.getSimulationTime());
        }
    }
}
