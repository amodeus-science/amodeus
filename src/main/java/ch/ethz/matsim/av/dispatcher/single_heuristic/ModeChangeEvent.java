package ch.ethz.matsim.av.dispatcher.single_heuristic;

import java.util.Map;

import org.matsim.api.core.v01.events.Event;

public class ModeChangeEvent extends Event {
    final private SingleHeuristicDispatcher.HeuristicMode dispatcherMode;
    final private String mode;

    public ModeChangeEvent(SingleHeuristicDispatcher.HeuristicMode dispatcherMode, String mode, double time) {
        super(time);

        this.dispatcherMode = dispatcherMode;
        this.mode = mode;
    }

    @Override
    public String getEventType() {
        return "AVHeuristicModeChange";
    }

    @Override
    public Map<String, String> getAttributes() {
        Map<String, String> attr = super.getAttributes();
        attr.put("mode", mode.toString());
        attr.put("dispatcherMode", dispatcherMode.toString());
        return attr;
    }
}
