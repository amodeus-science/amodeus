package ch.ethz.matsim.av.routing;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.population.routes.AbstractRoute;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import ch.ethz.matsim.av.data.AVOperator;

public class AVRoute extends AbstractRoute {
    final static String AV_ROUTE = "av";

    private Id<AVOperator> operatorId;
    private double waitingTime;
    private double inVehicleTime;
    private double price;

    public AVRoute(Id<Link> startLinkId, Id<Link> endLinkId) {
        super(startLinkId, endLinkId);
    }

    public Id<AVOperator> getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Id<AVOperator> operatorId) {
        this.operatorId = operatorId;
    }

    public double getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(double waitingTime) {
        this.waitingTime = waitingTime;
    }

    public double getInVehicleTime() {
        return inVehicleTime;
    }

    public void setInVehicleTime(double inVehicleTime) {
        this.inVehicleTime = inVehicleTime;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    private void interpretAttributes(Map<String, Object> attributes) {
        String operatorId = (String) attributes.get("operatorId");
        Double waitingTime = (Double) attributes.get("waitingTime");
        Double inVehicleTime = (Double) attributes.get("inVehicleTime");
        Double price = (Double) attributes.get("price");

        this.operatorId = Id.create(operatorId, AVOperator.class);
        this.waitingTime = waitingTime;
        this.inVehicleTime = inVehicleTime;
        this.price = price;
    }

    private Map<String, Object> buildAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("operatorId", operatorId.toString());
        attributes.put("waitingTime", waitingTime);
        attributes.put("inVehicleTime", inVehicleTime);
        attributes.put("price", price);
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
