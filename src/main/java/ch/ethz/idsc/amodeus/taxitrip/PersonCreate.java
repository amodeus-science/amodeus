/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.taxitrip;

import java.time.LocalDate;

import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PopulationFactory;

import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public enum PersonCreate {
    ;

    public static Person fromTrip(TaxiTrip taxiTrip, String globalId, PopulationFactory populationFactory, //
            FastLinkLookup fastLinkLookup, LocalDate simulationDate, AmodeusTimeConvert timeConvert) {

        Id<Person> personID = Id.create(globalId, Person.class);
        Person person = populationFactory.createPerson(personID);
        Plan plan = populationFactory.createPlan();

        /** pickup and dropoff link */
        Link pickupLocation = fastLinkLookup.linkFromWGS84(taxiTrip.pickupLoc);
        Link drpoffLocation = fastLinkLookup.linkFromWGS84(taxiTrip.dropoffLoc);

        /** start activity */
        Activity startActivity = populationFactory.createActivityFromLinkId("activity", pickupLocation.getId());
        startActivity.setEndTime(timeConvert.ldtToAmodeus(taxiTrip.pickupTimeDate, simulationDate));

        /** end activity */
        Activity endActivity = populationFactory.createActivityFromLinkId("activity", drpoffLocation.getId());
        endActivity.setStartTime(timeConvert.ldtToAmodeus(taxiTrip.dropoffTimeDate, simulationDate));

        /** leg between activity */
        Leg leg = populationFactory.createLeg("av");
        leg.setDepartureTime(startActivity.getEndTime().seconds());

        /** assemble */
        plan.addActivity(startActivity);
        plan.addLeg(leg);
        plan.addActivity(endActivity);
        person.addPlan(plan);

        /** checks */
        GlobalAssert.that(startActivity.getEndTime().seconds() >= 0);
        GlobalAssert.that(leg.getDepartureTime().seconds() >= 0);
        return person;
    }
}
