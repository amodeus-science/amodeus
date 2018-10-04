/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.listener.IterationStartsListener;

/* package */ class DatabaseIterationStartsListener implements IterationStartsListener {
    private final MatsimStaticDatabase db;

    public DatabaseIterationStartsListener(MatsimStaticDatabase db) {
        this.db = db;
    }

    @Override
    public void notifyIterationStarts(IterationStartsEvent event) {
        db.setIteration(event.getIteration());
    }
}
