package org.matsim.amodeus.config.modal;

import org.matsim.core.config.ReflectiveConfigGroup;
import org.matsim.core.utils.misc.Time;

public class WaitingTimeConfig extends ReflectiveConfigGroup {
    static public final String GROUP_NAME = "waitingTime";

    public final static String ESTIMATION_START_TIME = "estimationStartTime";
    public final static String ESTIMATION_END_TIME = "estimationEndTime";
    public final static String ESTIMATION_INTERVAL = "estimationInterval";
    public final static String ESTIMATION_ALPHA = "estimationAlpha";
    public final static String ESTIMATION_LINK_ATTRIBITE = "estimationLinkAttribute";

    public final static String CONSTANT_WAITING_TIME_LINK_ATTRIBUTE = "constantWaitingTimeAttribute";
    public final static String DEFAULT_WAITING_TIME = "defaultWaitingTime";

    private double estimationStartTime = 5.0 * 3600.0;
    private double estimationEndTime = 22.0 * 3600.0;
    private double estimationInterval = 300.0;
    private double estimationAlpha = 0.0;
    private String estimationLinkAttribute = "avWaitingTimeGroup";

    private String constantWaitingTimeLinkAttribute;
    private double defaultWaitingTime = 300.0;

    public WaitingTimeConfig() {
        super(GROUP_NAME, true);
    }

    public double getEstimationStartTime() {
        return estimationStartTime;
    }

    public void setEstimationStartTime(double estimationStartTime) {
        this.estimationStartTime = estimationStartTime;
    }

    @StringGetter(ESTIMATION_START_TIME)
    public String getEstimationStartTimeAsString() {
        return Time.writeTime(estimationStartTime);
    }

    @StringSetter(ESTIMATION_START_TIME)
    public void setEstimationStartTimeAsString(String estimationStartTime) {
        this.estimationStartTime = Time.parseTime(estimationStartTime);
    }

    public double getEstimationEndTime() {
        return estimationEndTime;
    }

    public void setEstimationEndTime(double estimationEndTime) {
        this.estimationEndTime = estimationEndTime;
    }

    @StringGetter(ESTIMATION_END_TIME)
    public String getEstimationEndTimeAsString() {
        return Time.writeTime(estimationEndTime);
    }

    @StringSetter(ESTIMATION_END_TIME)
    public void setEstimationEndTimeAsString(String estimationEndTime) {
        this.estimationEndTime = Time.parseTime(estimationEndTime);
    }

    public double getEstimationInterval() {
        return estimationInterval;
    }

    public void setEstimationInterval(double estimationInterval) {
        this.estimationInterval = estimationInterval;
    }

    @StringGetter(ESTIMATION_INTERVAL)
    public String getEstimationIntervalAsString() {
        return Time.writeTime(estimationInterval);
    }

    @StringSetter(ESTIMATION_INTERVAL)
    public void setEstimationIntervalAsString(String estimationInterval) {
        this.estimationInterval = Time.parseTime(estimationInterval);
    }

    @StringGetter(ESTIMATION_ALPHA)
    public double getEstimationAlpha() {
        return estimationAlpha;
    }

    @StringSetter(ESTIMATION_ALPHA)
    public void setEstimationAlpha(double estimationAlpha) {
        this.estimationAlpha = estimationAlpha;
    }

    @StringGetter(ESTIMATION_LINK_ATTRIBITE)
    public String getEstimationLinkAttribute() {
        return estimationLinkAttribute;
    }

    @StringSetter(ESTIMATION_LINK_ATTRIBITE)
    public void setEstimationLinkAttribute(String estimationLinkAttribute) {
        this.estimationLinkAttribute = estimationLinkAttribute;
    }

    @StringGetter(CONSTANT_WAITING_TIME_LINK_ATTRIBUTE)
    public String getConstantWaitingTimeLinkAttribute() {
        return constantWaitingTimeLinkAttribute;
    }

    @StringSetter(CONSTANT_WAITING_TIME_LINK_ATTRIBUTE)
    public void setConstantWaitingTimeLinkAttribute(String constantWaitingTimeLinkAttribute) {
        this.constantWaitingTimeLinkAttribute = constantWaitingTimeLinkAttribute;
    }

    @StringGetter(DEFAULT_WAITING_TIME)
    public double getDefaultWaitingTime() {
        return defaultWaitingTime;
    }

    @StringSetter(DEFAULT_WAITING_TIME)
    public void setDefaultWaitingTime(double defaultWaitingTime) {
        this.defaultWaitingTime = defaultWaitingTime;
    }
}
