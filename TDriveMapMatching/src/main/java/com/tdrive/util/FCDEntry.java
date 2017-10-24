package com.tdrive.util;

import com.graphhopper.util.GPXEntry;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class FCDEntry extends GPXEntry {
    private double speed;

    public FCDEntry(GPXEntry e) {
        this(e.lat, e.lon, e.ele, e.getTime(), 0);
    }

    public FCDEntry(GPXEntry e, int speed) {
        this(e.lat, e.lon, e.ele, e.getTime(), speed);
    }

    public FCDEntry(double lat, double lon, long millis, double speed) {
        super(lat, lon, millis);
        this.speed = speed;
    }

    public FCDEntry(double lat, double lon, double ele, long millis, double speed) {
        super(lat, lon, ele, millis);
        this.speed = speed;
    }


    public GPXEntry toGPXEntry() {
        return new GPXEntry(this.lat, this.lon, this.ele, this.getTime());
    }

    /**
     * The speed in kilometers per hour.
     */
    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    @Override
    public int hashCode() {
        return 59 * super.hashCode() + ((int)speed ^ ((int)speed >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        final FCDEntry other = (FCDEntry) obj;
        return speed == other.speed && super.equals(obj);
    }

    @Override
    public String toString() {
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(this.getTime()), ZoneId.of("GMT+8"));
        return this.lat + ", " + this.lon + ", " + this.ele + ", " + ldt + ", " + this.getSpeed();
    }
}

