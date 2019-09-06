package ch.ethz.idsc.amodeus.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public class AmodeusTimeConvert {

    private final ZoneId zoneId;

    /** @param zoneId
     * 
     *            Sample usage:
     *            AmodeusTimeConvert timeConvert = new AmodeusTimeConvert(ZoneId.of("America/Los_Angeles")); */
    public AmodeusTimeConvert(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    /** @param unxTime
     *            String in Unix Epoch Time [seconds]
     * @return seconds from midnight on @param localDate as integer for location San
     *         Francisco, US */
    public int toAmodeus(Integer unxTime, LocalDate localDate) {
        Instant inst = Instant.ofEpochSecond(unxTime);
        // LocalDateTime ldt = LocalDateTime.ofInstant(inst, ZoneId.of("America/Los_Angeles"));
        LocalDateTime ldt = LocalDateTime.ofInstant(inst, zoneId);
        int amodeusTime = (ldt.getDayOfYear() - localDate.getDayOfYear()) * 3600 * 24 + ldt.getHour() * 3600//
                + ldt.getMinute() * 60 //
                + ldt.getSecond();
        if (amodeusTime < 0) {
            System.err.println("unxTime: " + unxTime);
            System.err.println("localDate: " + localDate);
            System.err.println("amodeusTime: " + amodeusTime);
        }
        GlobalAssert.that(amodeusTime >= 0);
        return amodeusTime;
    }

    /** @return the time in the AMoDeus framework as an integer for the {@link LocalDateTime}
     * @param ldt and the simulation date @param simDate */
    public int ldtToAmodeus(LocalDateTime ldt, LocalDate simDate) {
        // Instant inst = Instant.ofEpochSecond(unxTime);
        // // LocalDateTime ldt = LocalDateTime.ofInstant(inst, ZoneId.of("America/Los_Angeles"));
        // LocalDateTime ldt = LocalDateTime.ofInstant(inst, zoneId);

        int day = ldt.getDayOfYear() - simDate.getDayOfYear();
        int amodeusTime = day * 3600 * 24 //
                + ldt.getHour() * 3600//
                + ldt.getMinute() * 60 //
                + ldt.getSecond();
        if (amodeusTime < 0) {
            System.err.println("amodeus time is smaller than zero...");
            System.err.println("ldt:         " + ldt.toString());
            System.err.println("simDate:     " + simDate.toString());
            System.err.println("amodeustime: " + amodeusTime);
        }
        GlobalAssert.that(amodeusTime >= 0);
        return amodeusTime;
    }

    public long toEpochSec(LocalDateTime ldt) {
        return ldt.toEpochSecond(ldt.atZone(zoneId).getOffset());
    }

    public LocalDate toLocalDate(String unxTime) {
        long unxSeconds = Integer.parseInt(unxTime);
        Instant inst = Instant.ofEpochSecond(unxSeconds);
        // LocalDateTime ldt = LocalDateTime.ofInstant(inst, ZoneId.of("America/Los_Angeles"));
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
        LocalTime time = LocalTime.of(23, 59, 59, 999999999);
        return LocalDateTime.of(localDate, time);
    }

    public String toString(int unxSeconds) {
        Instant inst = Instant.ofEpochSecond(unxSeconds);
        LocalDateTime ldt = LocalDateTime.ofInstant(inst, zoneId);
        return (ldt.toString() + " (" + unxSeconds + ")\n");
    }

}