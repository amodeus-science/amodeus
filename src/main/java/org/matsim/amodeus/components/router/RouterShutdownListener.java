package org.matsim.amodeus.components.router;

import java.io.IOException;

import org.matsim.amodeus.components.AmodeusRouter;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.listener.ShutdownListener;

public class RouterShutdownListener implements ShutdownListener {
    private final AmodeusRouter router;

    public RouterShutdownListener(AmodeusRouter router) {
        this.router = router;
    }

    @Override
    public void notifyShutdown(ShutdownEvent event) {
        try {
            router.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
