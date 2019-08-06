/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed;

import java.util.Objects;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.groups.TravelTimeCalculatorConfigGroup;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;
import org.matsim.core.mobsim.qsim.qnetsimengine.ConfigurableQNetworkFactory;
import org.matsim.core.mobsim.qsim.qnetsimengine.QNetworkFactory;
import org.matsim.core.mobsim.qsim.qnetsimengine.linkspeedcalculator.LinkSpeedCalculator;

import com.google.inject.Provides;
import com.google.inject.Singleton;

public class TrafficDataModule extends AbstractQSimModule {
    private final LinkSpeedDataContainer lsData;

    public TrafficDataModule(LinkSpeedDataContainer lsData) {
        this.lsData = Objects.isNull(lsData) ? new LinkSpeedDataContainer() : lsData;
    }

    // TODO Claudio / Sebastian. After the change to Matsim 11, it was nesscesary to comment this lines out as this caused an error saying that the QNetworkFactory
    // should only be loaded via AbstractQSimModule. Thus I commented this function here out. Please check if this has further implications on other parts of the
    // code. Lukas, 3.8.19
    
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
        Objects.requireNonNull(config);
        return new DefaultTaxiTrafficData(lsData, config.getTraveltimeBinSize(), network);
    }

    // @Override
    // public void install() {
    // bind(TaxiTrafficData.class).to(DefaultTaxiTrafficData.class).asEagerSingleton();
    // }

    @Override
    protected void configureQSim() {
        bind(TaxiTrafficData.class).to(DefaultTaxiTrafficData.class).asEagerSingleton();

    }
}