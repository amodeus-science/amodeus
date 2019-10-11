/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;

/** class maintains a {@link FuturePathContainer} while the path is being
 * computer. the resulting path is available upon the function call execute(...) */
/* package */ abstract class FuturePathDirective implements DirectiveInterface {
    protected final FuturePathContainer futurePathContainer;

    FuturePathDirective(FuturePathContainer futurePathContainer) {
        this.futurePathContainer = futurePathContainer;
    }

    @Override
    public final void execute() {
        VrpPathWithTravelData vrpPathWithTravelData = futurePathContainer.getVrpPathWithTravelData();
        executeWithPath(vrpPathWithTravelData);
    }

    abstract void executeWithPath(VrpPathWithTravelData vrpPathWithTravelData);

    protected void reportExecutionBypass(double excess) {
        // System.out.println(" \\- bypass " + getClass().getSimpleName() + ",
        // exceeds EndTime by " + excess + " sec");
    }

}
