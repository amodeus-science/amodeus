package ch.ethz.matsim.av.data;

import org.matsim.api.core.v01.Id;

import ch.ethz.matsim.av.config.operator.OperatorConfig;

public class AVOperatorImpl implements AVOperator {
    final private Id<AVOperator> id;
    private final OperatorConfig config;

    public AVOperatorImpl(Id<AVOperator> id, OperatorConfig config) {
        this.id = id;
        this.config = config;
    }

    @Override
    public Id<AVOperator> getId() {
        return id;
    }

    @Override
    public OperatorConfig getConfig() {
        return config;
    }
}
