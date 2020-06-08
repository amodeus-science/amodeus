/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.gfx;

/* package */ enum VirtualNodeShader {
    None, //
    VehicleCount, //
    RequestCount, //
    MeanRequestDistance, //
    MeanRequestWaiting, //
    MedianRequestWaiting, //
    MaxRequestWaiting;

    public boolean renderBoundary() {
        return !equals(None);
    }
}
