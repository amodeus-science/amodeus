/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.sca.Clip;
import ch.ethz.idsc.tensor.sca.Clips;

public enum PopulationTimeInterval {
    ;

    /** Removes all persons that have legs with departure time or the end
     * time of its predecessor activity outside the time interval [0,endTime) */
    public static void removeOutside(Population population, int endTime) {
        Clip timeClip = Clips.positive(endTime - 1); // TODO [0,endTime] -> [startTime,endTime]?
        System.out.println("All people in population  which have activities outside" //
                + " the time interval [0, " + endTime + ") are removed.");

        // Iterator<? extends Person> personIter = population.getPersons().values().iterator();
        // int counter = 0;
        // int nextMsg = 1;
        // while (personIter.hasNext()) {
        // counter++;
        // if (counter % nextMsg == 0) {
        // nextMsg *= 2;
        // System.out.println("we are at person # " + counter + ". ");
        // }
        // Person person = personIter.next();
        // boolean removePerson = false;
        // for (Plan plan : person.getPlans()) {
        // for (int i = 1; i < plan.getPlanElements().size() - 1; ++i) {
        // PlanElement planBefore = plan.getPlanElements().get(i - 1);
        // PlanElement planCurrent = plan.getPlanElements().get(i);
        // if (planCurrent instanceof Leg) {
        // Leg leg = (Leg) planCurrent;
        // Activity actBefore = (Activity) planBefore;
        // if (timeClip.isOutside(RealScalar.of(leg.getDepartureTime())))
        // removePerson = true;
        // if (timeClip.isOutside(RealScalar.of(actBefore.getEndTime())))
        // removePerson = true;
        // }
        // }
        // }
        // if (removePerson) {
        // personIter.remove();
        // }
        // }

        AtomicInteger aiCount = new AtomicInteger(1);
        AtomicInteger aiMsg = new AtomicInteger(1);
        population.getPersons().entrySet().removeIf(e -> {
            if (aiCount.getAndIncrement() % aiMsg.get() == 0) {
                System.out.println(String.format("we are at person # %d.", aiCount.get() - 1));
                aiMsg.set(2 * aiMsg.get());
            }
            return e.getValue().getPlans().stream().anyMatch(plan -> invalidPlan(timeClip, plan));
        });
    }

    private static boolean invalidPlan(Clip clip, Plan plan) {
        // TODO choose better performing option
        // List<PlanElement> planElements = plan.getPlanElements();
        // return IntStream.range(1, planElements.size()).filter(i -> planElements.get(i - 1) instanceof Leg).anyMatch(i -> {
        // Leg leg = (Leg) planElements.get(i - 1);
        // Activity prevAct = (Activity) planElements.get(i);
        // return clip.isOutside(RealScalar.of(leg.getDepartureTime())) || clip.isOutside(RealScalar.of(prevAct.getEndTime()));
        // });

        Iterator<PlanElement> iterator = plan.getPlanElements().iterator();
        PlanElement prev;
        PlanElement current = iterator.next();
        while (iterator.hasNext()) {
            prev = current;
            current = iterator.next();
            if (current instanceof Leg) {
                Leg leg = (Leg) current;
                Activity prevAct = (Activity) prev;
                if (clip.isOutside(RealScalar.of(leg.getDepartureTime().seconds())) || clip.isOutside(RealScalar.of(prevAct.getEndTime().seconds())))
                    return true;
            }
        }
        return false;
    }
}
