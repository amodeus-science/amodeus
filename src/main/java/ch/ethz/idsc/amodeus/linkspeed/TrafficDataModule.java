/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed;

import java.util.Objects;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.groups.TravelTimeCalculatorConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.mobsim.qsim.qnetsimengine.ConfigurableQNetworkFactory;
import org.matsim.core.mobsim.qsim.qnetsimengine.QNetworkFactory;
import org.matsim.core.mobsim.qsim.qnetsimengine.linkspeedcalculator.LinkSpeedCalculator;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import ch.ethz.idsc.owl.data.GlobalAssert;

public class TrafficDataModule extends AbstractModule {
    private final LinkSpeedDataContainer lsData;
    private final double alpha;

    public TrafficDataModule(LinkSpeedDataContainer lsData, double alpha) {
        GlobalAssert.that(alpha >= 0.0); // alpha = 0.0 --> freeflow speeds.
        GlobalAssert.that(alpha <= 1.0); // alpha = 1.0 --> maximally reduced speeds.
        this.lsData = Objects.isNull(lsData) ? new LinkSpeedDataContainer() : lsData;
        this.alpha = alpha;
    }

    @Provides
    @Singleton
    public QNetworkFactory provideCustomConfigurableQNetworkFactory(EventsManager events, Scenario scenario, TaxiTrafficData trafficData) {
        ConfigurableQNetworkFactory factory = new ConfigurableQNetworkFactory(events, scenario);
        LinkSpeedCalculator AVLinkSpeedCalculator = new AmodeusLinkSpeedCalculator(trafficData);
        factory.setLinkSpeedCalculator(AVLinkSpeedCalculator);
        return factory;
    }

    @Provides
    @Singleton
    public DefaultTaxiTrafficData provideTaxiTrafficData(Network network, TravelTimeCalculatorConfigGroup config) {
        return new DefaultTaxiTrafficData(lsData, config.getTraveltimeBinSize(), network, alpha);
    }

    @Override
    public void install() {
        bind(TaxiTrafficData.class).to(DefaultTaxiTrafficData.class).asEagerSingleton();
    }
}