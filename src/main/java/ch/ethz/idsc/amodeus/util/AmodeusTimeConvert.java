/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Objects;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public class AmodeusTimeConvert {

    private final ZoneId zoneId;

    /** @param zoneId
     * 
     *            Sample usage:
     *            AmodeusTimeConvert timeConvert = new AmodeusTimeConvert(ZoneId.of("America/Los_Angeles")); */
    public AmodeusTimeConvert(ZoneId zoneId) {
        this.zoneId = Objects.requireNonNull(zoneId);
    }

    /** @param epochSecond
     *            String in Unix Epoch Time [seconds]
     * @return seconds from midnight on @param localDate as integer for location San
     *         Francisco, US */
    // TODO one function of toAmodeus and ldtToAmodeus should call the other! otherwise redundant
    public int toAmodeus(long epochSecond, LocalDate localDate) {
        Instant instant = Instant.ofEpochSecond(epochSecond);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zoneId);
        int amodeusTime = (localDateTime.getDayOfYear() - localDate.getDayOfYear()) * 3600 * 24 //
                + localDateTime.getHour() * 3600 //
                + localDateTime.getMinute() * 60 //
                + localDateTime.getSecond();
        if (amodeusTime < 0) {
            System.err.println("unxTime: " + epochSecond);
            System.err.println("localDate: " + localDate);
            System.err.println("amodeusTime: " + amodeusTime);
        }
        GlobalAssert.that(amodeusTime >= 0);
        return amodeusTime;
    }

    /** @return the time in the AMoDeus framework as an integer for the {@link LocalDateTime}
     * @param localDateTime and the simulation date @param simDate */
    public int ldtToAmodeus(LocalDateTime localDateTime, LocalDate localDate) {
        int day = localDateTime.getDayOfYear() - localDate.getDayOfYear();
        int amodeusTime = day * 3600 * 24 //
                + localDateTime.getHour() * 3600//
                + localDateTime.getMinute() * 60 //
                + localDateTime.getSecond();
        if (amodeusTime < 0) {
            System.err.println("amodeus time is smaller than zero...");
            System.err.println("ldt:         " + localDateTime.toString());
            System.err.println("simDate:     " + localDate.toString());
            System.err.println("amodeustime: " + amodeusTime);
        }
        GlobalAssert.that(amodeusTime >= 0);
        return amodeusTime;
    }

    public long toEpochSec(LocalDateTime localDateTime) {
        return localDateTime.toEpochSecond(localDateTime.atZone(zoneId).getOffset());
    }

    public LocalDate toLocalDate(String unxTime) {
        long unxSeconds = Integer.parseInt(unxTime);
        Instant inst = Instant.ofEpochSecond(unxSeconds);
        LocalDateTime ldt = LocalDateTime.ofInstant(inst, zoneId);
        return LocalDate.of(ldt.getYear(), ldt.getMonth(), ldt.getDayOfMonth());
    }

    public LocalDateTime getLdt(int unxSeconds) {
        Instant inst = Instant.ofEpochSecond(unxSeconds);
        return LocalDateTime.ofInstant(inst, zoneId);
    }

    /** @return the {@link LocalDateTime} representing the first time instant on the {@link LocalDate}
     *         input @param localDate. One day is [00:00:00,23:59:59] */
    public LocalDateTime beginOf(LocalDate localDate) {
        LocalTime time = LocalTime.of(0, 0, 0, 0);
        return LocalDateTime.of(localDate, time);
    }

    /** @return the {@link LocalDateTime} representing the last time instant on the {@link LocalDate}
     *         input @param localDate. One day is [00:00:00,23:59:59] */
    public LocalDateTime endOf(LocalDate localDate) {
        LocalTime time = LocalTime.of(23, 59, 59, 999_999_999);
        return LocalDateTime.of(localDate, time);
    }

    public String toString(int unxSeconds) {
        Instant inst = Instant.ofEpochSecond(unxSeconds);
        LocalDateTime ldt = LocalDateTime.ofInstant(inst, zoneId);
        return (ldt.toString() + " (" + unxSeconds + ")\n");
    }

}