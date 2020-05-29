package org.matsim.amodeus.routing;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.population.routes.AbstractRoute;
import org.matsim.core.utils.misc.OptionalTime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class AmodeusRoute extends AbstractRoute {
    public final static String AV_ROUTE = "av";

    private OptionalTime waitingTime = OptionalTime.undefined();
    private Optional<Double> price = Optional.empty();

    public AmodeusRoute(Id<Link> startLinkId, Id<Link> endLinkId) {
        super(startLinkId, endLinkId);
    }

    public OptionalTime getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(double waitingTime) {
        this.waitingTime = OptionalTime.defined(waitingTime);
    }

    public Optional<Double> getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = Optional.of(price);
    }

    public Optional<Double> getExpectedDistance() {
        return Optional.ofNullable(getDistance());
    }

    public OptionalTime getInVehicleTime() {
        return getTravelTime().isDefined() && getWaitingTime().isDefined() ? OptionalTime.defined(getTravelTime().seconds() - getWaitingTime().seconds())
                : OptionalTime.undefined();
    }

    private void interpretAttributes(Map<String, Object> attributes) {
        Double waitingTime = (Double) attributes.get("waitingTime");
        Double price = (Double) attributes.get("price");

        this.waitingTime = Optional.ofNullable(waitingTime).map(OptionalTime::defined).orElse(OptionalTime.undefined());
        this.price = Optional.ofNullable(price);
    }

    private Map<String, Object> buildAttributes() {
        Map<String, Object> attributes = new HashMap<>();

        if (waitingTime.isDefined()) {
            attributes.put("waitingTime", waitingTime.seconds());
        }

        if (price.isPresent()) {
            attributes.put("price", price.get());
        }

        return attributes;
    }

    private final ObjectMapper mapper = new ObjectMapper();
    private final MapType mapType = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class);

    @Override
    public String getRouteDescription() {
        try {
            return new ObjectMapper().writeValueAsString(buildAttributes());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setRouteDescription(String routeDescription) {
        try {
            Map<String, Object> attributes = mapper.readValue(routeDescription, mapType);
            interpretAttributes(attributes);
        } catch (IOException e) {
            new RuntimeException(e);
        }
    }

    @Override
    public String getRouteType() {
        return AV_ROUTE;
    }
}
