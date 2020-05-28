package org.matsim.amodeus.components.router;

import java.io.IOException;

import org.matsim.amodeus.components.AVRouter;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.listener.ShutdownListener;

public class AVRouterShutdownListener implements ShutdownListener {
    private final AVRouter router;

    public AVRouterShutdownListener(AVRouter router) {
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
