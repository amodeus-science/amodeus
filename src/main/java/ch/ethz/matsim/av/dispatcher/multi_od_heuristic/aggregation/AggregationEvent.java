package ch.ethz.matsim.av.dispatcher.multi_od_heuristic.aggregation;

import java.util.Map;

import org.matsim.api.core.v01.events.Event;

import ch.ethz.matsim.av.passenger.AVRequest;

public class AggregationEvent extends Event {
    final private AVRequest master;
    final private AVRequest slave;

    public AggregationEvent(AVRequest master, AVRequest slave, double time) {
        super(time);

        this.master = master;
        this.slave = slave;
    }

    @Override
    public String getEventType() {
        return "ODRSAggregation";
    }

    @Override
    public Map<String, String> getAttributes() {
        Map<String, String> attr = super.getAttributes();
        attr.put("master", master.getPassengerId().toString());
        attr.put("slave", slave.getPassengerId().toString());
        return attr;
    }
}
