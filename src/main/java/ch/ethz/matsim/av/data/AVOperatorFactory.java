package ch.ethz.matsim.av.data;

import org.matsim.api.core.v01.Id;

import com.google.inject.Singleton;

import ch.ethz.matsim.av.config.operator.OperatorConfig;

@Singleton
public class AVOperatorFactory {
    public AVOperator createOperator(Id<AVOperator> id, OperatorConfig config) {
        return new AVOperatorImpl(id, config);
    }
}
