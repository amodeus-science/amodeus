package org.matsim.amodeus.dynamics;

/** Creates a new TestRequest */
public class TestRequest {
    public final double departureTime;
    public final double delayTime;

    /** Constructor
     * 
     * @param departureTime
     * @param delayTime */
    public TestRequest(double departureTime, double delayTime) {
        this.departureTime = departureTime;
        this.delayTime = delayTime;
    }
}
