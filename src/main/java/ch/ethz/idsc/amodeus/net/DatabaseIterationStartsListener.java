/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.listener.IterationStartsListener;

/* package */ class DatabaseIterationStartsListener implements IterationStartsListener {
    @Override
    public void notifyIterationStarts(IterationStartsEvent event) {
        MatsimStaticDatabase.INSTANCE.setIteration(event.getIteration());
    }
}
