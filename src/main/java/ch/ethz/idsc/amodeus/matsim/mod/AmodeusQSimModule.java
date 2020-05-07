/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.mod;

import org.matsim.core.mobsim.qsim.AbstractQSimModule;

import ch.ethz.matsim.av.framework.AVQSimModule;

public class AmodeusQSimModule extends AbstractQSimModule {
    @Override
    protected void configureQSim() {
        /* Basically, we install the QSimModule that is provided by the AV package, but
         * we override one little detail: Instead of the standard LegCreator a custom
         * one is used which adds IDSC tracking functionality to the dynamic vehicle
         * legs. */

        install(new AVQSimModule());

        // TODO: Can be removed!'
        /* install(Modules.override(new AVQSimModule()).with(new AbstractModule() {
         * 
         * @Override
         * protected void configure() {
         * for (OperatorConfig operatorConfig : AVConfigGroup.getOrCreate(getConfig()).getOperatorConfigs().values()) {
         * // TODO: Fix this one we have no modes!
         * 
         * bind(DvrpModes.key(VrpLegFactory.class, "av")).toProvider(ModalProviders.createProvider("av", getter -> {
         * QSim qsim = getter.get(QSim.class);
         * AVOptimizer optimizer = getter.getModal(AVOptimizer.class);
         * 
         * return TrackingHelper.createLegCreatorWithIDSCTracking(optimizer, qsim.getSimTimer());
         * }));
         * }
         * }
         * })); */
    }
}
