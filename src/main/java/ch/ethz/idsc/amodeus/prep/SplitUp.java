/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;

/* package */ enum SplitUp {
    ;

    /** @return {@link Person} identical to @param oldPerson from @param population with
     *         the number of legs in mode @param mode reduced to @param numLegs
     * 
     *         usage example: Person splitPerson = SplitUp.of(population, personX, 1, "av") */
    public static Person of(Population population, Person oldPerson, int numLegs, String mode) {
        System.out.println("split person, num Legs: " + numLegs);
        PopulationFactory factory = population.getFactory();
        IDGenerator generator = new IDGenerator(population);
        Id<Person> newID = generator.generateUnusedID();
        Person newPerson = factory.createPerson(newID);
        int numReq = 0;
        for (Plan plan : oldPerson.getPlans()) {
            Plan planShifted = factory.createPlan();
            planShifted.setPerson(newPerson);
            planShifted.setScore(plan.getScore());
            planShifted.setType(plan.getType());
            for (PlanElement pE : plan.getPlanElements()) {
                if (pE instanceof Activity) {
                    Activity actOld = (Activity) pE;

                    // copy activity
                    Activity actNew = factory.createActivityFromCoord(actOld.getType(), actOld.getCoord());
                    actNew.setStartTime(actOld.getStartTime().seconds());
                    actNew.setEndTime(actOld.getEndTime().seconds());
                    actNew.setLinkId(actOld.getLinkId());
                    actNew.setFacilityId(actOld.getFacilityId());

                    planShifted.addActivity(actNew);
                    if (numLegs == numReq)
                        break;
                }
                if (pE instanceof Leg) {
                    Leg leg = (Leg) pE;
                    if (leg.getMode().equals(mode))
                        numReq++;

                    Leg legNew = factory.createLeg(leg.getMode());
                    legNew.setDepartureTime(leg.getDepartureTime().seconds());
                    planShifted.addLeg(legNew);
                }
            }
            newPerson.addPlan(planShifted);
        }
        if (LegCount.of(newPerson, mode) != numLegs) {
            System.err.println("LegCount.of(newPerson, mode): " + LegCount.of(newPerson, mode));
            throw new RuntimeException("numLegs:                      " + numLegs);
        }
        return newPerson;
    }
}
