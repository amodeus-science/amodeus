package ch.ethz.matsim.av.config.operator;

import org.matsim.core.config.ReflectiveConfigGroup;

import ch.ethz.matsim.av.generator.PopulationDensityGenerator;

public class GeneratorConfig extends ReflectiveConfigGroup {
    static public final String GROUP_NAME = "generator";

    static public final String TYPE = "type";
    static public final String NUMBER_OF_VEHICLES = "numberOfVehicles";
    static public final String VEHICLE_TYPE = "vehicleType";

    static public final String DEFAULT_GENERATOR = PopulationDensityGenerator.TYPE;
    private String type = DEFAULT_GENERATOR;

    private int numberOfVehicles;
    private String vehicleType;

    public GeneratorConfig() {
        super(GROUP_NAME, true);
    }

    @StringGetter(TYPE)
    public String getType() {
        return type;
    }

    @StringSetter(TYPE)
    public void setType(String type) {
        this.type = type;
    }

    @StringGetter(NUMBER_OF_VEHICLES)
    public int getNumberOfVehicles() {
        return numberOfVehicles;
    }

    @StringSetter(NUMBER_OF_VEHICLES)
    public void setNumberOfVehicles(int numberOfVehicles) {
        this.numberOfVehicles = numberOfVehicles;
    }

    @StringGetter(VEHICLE_TYPE)
    public String getVehicleType() {
        return vehicleType;
    }

    @StringSetter(VEHICLE_TYPE)
    public void getVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }
}
