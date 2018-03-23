/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.traveldata;

import java.io.File;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.idsc.tensor.sca.Chop;

public class TravelDataTestHelper {
    
    public static TravelDataTestHelper prepare(VirtualNetwork<Link> vNCreated, VirtualNetwork<Link> vNSaved, File workingDirectory) throws Exception {
        return new TravelDataTestHelper(vNCreated, vNSaved, workingDirectory);
    }

    private TravelData tDCreated;
    private TravelData tDSaved;

    private TravelDataTestHelper(VirtualNetwork<Link> vNCreated, VirtualNetwork<Link> vNSaved, File workingDirectory) throws Exception {
        tDCreated = TravelDataGet.readDefault(vNCreated, workingDirectory);
        tDSaved = TravelDataIO.read(new File("resources/testComparisonFiles/travelData"), vNSaved);
    }

    public boolean tDCheck() {
        return (tDSaved.getdt() == tDCreated.getdt());
    }

    public boolean timeStepsCheck() {
        return (tDSaved.getNumbertimeSteps() == tDCreated.getNumbertimeSteps());
    }

    public boolean lambdaCheck() {
        return Chop._06.close(tDSaved.getLambdaforTime(2500), tDCreated.getLambdaforTime(2500));
    }

    public boolean lambdaijPSFCheck() {
        return Chop._06.close(tDSaved.getlambdaijPSFforTime(2500), tDCreated.getlambdaijPSFforTime(2500));
    }

    public boolean pijCheck() {
        return Chop._06.close(tDSaved.getpijforTime(2500, 4, 2), tDCreated.getpijforTime(2500, 4, 2));
    }

    public boolean pijPSFCheck() {
        return Chop._06.close(tDSaved.getpijPSFforTime(2500), tDCreated.getpijPSFforTime(2500));
    }

    public boolean alphaPSFCheck() {
        return Chop._06.close(tDSaved.getAlphaijPSFforTime(1580), tDCreated.getAlphaijPSFforTime(1580));
    }

}
