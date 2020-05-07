package ch.ethz.matsim.av.replanning;

import java.util.LinkedList;
import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.HasPlansAndId;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.replanning.PlanStrategy;
import org.matsim.core.replanning.ReplanningContext;

import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.routing.AVRoute;

public class AVOperatorChoiceStrategy implements PlanStrategy {
    // TODO: Remove this after the switch to modes!

    List<String> modes = new LinkedList<>();
    List<Id<AVOperator>> operatorIds = new LinkedList<>();

    public AVOperatorChoiceStrategy(List<Id<AVOperator>> operatorIds, List<String> modes) {
        this.operatorIds.addAll(operatorIds);
        this.modes.addAll(modes);
    }

    @Override
    public void run(HasPlansAndId<Plan, Person> person) {
        for (PlanElement element : person.getSelectedPlan().getPlanElements()) {
            if (element instanceof Leg) {
                Leg leg = (Leg) element;

                if (modes.contains(leg.getMode())) {
                    AVRoute route = (AVRoute) leg.getRoute();
                    route.setOperatorId(chooseRandomOperator());
                }
            }
        }
    }

    @Override
    public void init(ReplanningContext replanningContext) {
    }

    @Override
    public void finish() {
    }

    public Id<AVOperator> chooseRandomOperator() {
        if (operatorIds.size() == 0) {
            throw new IllegalStateException("No AV operators have been defined.");
        }

        int draw = MatsimRandom.getRandom().nextInt(operatorIds.size());
        return operatorIds.get(draw);
    }
}
