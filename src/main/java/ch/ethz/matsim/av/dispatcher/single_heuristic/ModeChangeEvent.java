package ch.ethz.matsim.av.dispatcher.single_heuristic;

import ch.ethz.matsim.av.data.AVOperator;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;

import java.util.Map;

public class ModeChangeEvent extends Event {
    final private SingleHeuristicDispatcher.HeuristicMode mode;
    final private Id<AVOperator> operatorId;

    public ModeChangeEvent(SingleHeuristicDispatcher.HeuristicMode mode, Id<AVOperator> operatorId, double time) {
        super(time);

        this.mode = mode;
        this.operatorId = operatorId;
    }

    @Override
    public String getEventType() {
        return "AVHeuristicModeChange";
    }

    @Override
    public Map<String, String> getAttributes() {
        Map<String, String> attr = super.getAttributes();
        attr.put("mode", mode.toString());
        attr.put("operator", operatorId.toString());
        return attr;
    }
}
