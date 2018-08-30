package ch.ethz.idsc.amodeus.prep;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;

/* package */ class SplitUp {

    public static Person of(Population population, Person oldPerson, Scalar numLegs, String mode) {

        PopulationFactory factory = population.getFactory();
        IDGenerator generator = new IDGenerator(population);
        Id<Person> newID = generator.generateUnusedID();
        Person newPerson = factory.createPerson(newID);

        Scalar numReq = RealScalar.ZERO;

        for (Plan plan : oldPerson.getPlans()) {
            Plan planShifted = factory.createPlan();
            planShifted.setPerson(newPerson);
            planShifted.setScore(plan.getScore());
            planShifted.setType(plan.getType());

            for (PlanElement pE : plan.getPlanElements()) {
                if (pE instanceof Activity) {
                    Activity actOld = (Activity) pE;
                    Activity actNew = factory.createActivityFromCoord(actOld.getType(), actOld.getCoord());
                    actNew.setStartTime(actOld.getStartTime());
                    actNew.setEndTime(actOld.getEndTime());
                    actNew.setLinkId(actOld.getLinkId());
                    actNew.setFacilityId(actOld.getFacilityId());
                    planShifted.addActivity(actNew);
                    if(numLegs.equals(numReq))
                        break;                    
                }
                if (pE instanceof Leg) {
                    Leg leg = (Leg) pE;
                    if(leg.getMode().equals(mode))
                        numReq = numLegs.add(RealScalar.ONE);                    
                    Leg legNew = factory.createLeg(leg.getMode());
                    legNew.setDepartureTime(leg.getDepartureTime());
                    planShifted.addLeg(legNew);
                }
            }
            newPerson.addPlan(planShifted);
        }
        return newPerson;
    }
}
