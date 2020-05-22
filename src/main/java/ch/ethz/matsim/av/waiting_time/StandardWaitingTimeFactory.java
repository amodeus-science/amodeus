package ch.ethz.matsim.av.waiting_time;

import org.matsim.api.core.v01.network.Network;

import com.google.inject.Singleton;

import ch.ethz.matsim.av.config.AmodeusModeConfig;
import ch.ethz.matsim.av.config.modal.AmodeusWaitingTimeEstimationConfig;
import ch.ethz.matsim.av.waiting_time.constant.ConstantWaitingTime;
import ch.ethz.matsim.av.waiting_time.dynamic.DynamicWaitingTime;
import ch.ethz.matsim.av.waiting_time.dynamic.LinkGroupDefinition;
import ch.ethz.matsim.av.waiting_time.link_attribute.LinkAttributeWaitingTime;
import ch.ethz.matsim.av.waiting_time.link_attribute.LinkWaitingTimeData;

@Singleton
public class StandardWaitingTimeFactory implements WaitingTimeFactory {
    @Override
    public WaitingTime createWaitingTime(AmodeusModeConfig modeConfig, Network network) {
        AmodeusWaitingTimeEstimationConfig waitingTimeConfig = modeConfig.getWaitingTimeEstimationConfig();

        if (waitingTimeConfig.getEstimationAlpha() > 0.0) {
            LinkWaitingTimeData linkWaitingTimeData = LinkWaitingTimeData.createEmpty();

            if (waitingTimeConfig.getConstantWaitingTimeLinkAttribute() != null) {
                linkWaitingTimeData = LinkWaitingTimeData.create(network, waitingTimeConfig.getConstantWaitingTimeLinkAttribute());
            }

            LinkGroupDefinition linkGroupDefinition = LinkGroupDefinition.create(network, waitingTimeConfig.getEstimationLinkAttribute());

            return new DynamicWaitingTime(linkGroupDefinition, linkWaitingTimeData, waitingTimeConfig.getDefaultWaitingTime(), waitingTimeConfig.getEstimationStartTime(),
                    waitingTimeConfig.getEstimationEndTime(), waitingTimeConfig.getEstimationInterval(), waitingTimeConfig.getEstimationAlpha());
        } else if (waitingTimeConfig.getConstantWaitingTimeLinkAttribute() != null) {
            LinkWaitingTimeData linkWaitingTimeData = LinkWaitingTimeData.create(network, waitingTimeConfig.getConstantWaitingTimeLinkAttribute());
            return new LinkAttributeWaitingTime(waitingTimeConfig.getDefaultWaitingTime(), linkWaitingTimeData);
        } else {
            return new ConstantWaitingTime(waitingTimeConfig.getDefaultWaitingTime());
        }
    }
}
