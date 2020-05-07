package ch.ethz.matsim.av.data;

import org.matsim.api.core.v01.Id;

import ch.ethz.matsim.av.config.operator.OperatorConfig;

public interface AVOperator {
    Id<AVOperator> getId();

    OperatorConfig getConfig();

    static public Id<AVOperator> createId(String id) {
        return Id.create(id, AVOperator.class);
    }
}
