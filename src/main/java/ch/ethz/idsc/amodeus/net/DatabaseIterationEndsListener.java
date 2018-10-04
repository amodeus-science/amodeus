/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;

import com.google.inject.Inject;

/* package */ class DatabaseIterationEndsListener implements IterationEndsListener {
//    private final MatsimStaticDatabase db;
//
//    public DatabaseIterationEndsListener(MatsimStaticDatabase db) {
//        this.db = db;
//    }

    @Inject 
    private MatsimStaticDatabase db;
    
    @Override
    public void notifyIterationEnds(IterationEndsEvent event) {
        db.setIteration(null);
    }

}
