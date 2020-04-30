package ch.ethz.matsim.av.router;

import java.io.IOException;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.listener.ShutdownListener;

import com.google.inject.Inject;

import ch.ethz.matsim.av.data.AVOperator;

public class AVRouterShutdownListener implements ShutdownListener {
	@Inject
	Map<Id<AVOperator>, AVRouter> routers;

	@Override
	public void notifyShutdown(ShutdownEvent event) {
		for (AVRouter router : routers.values()) {
			try {
				router.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
