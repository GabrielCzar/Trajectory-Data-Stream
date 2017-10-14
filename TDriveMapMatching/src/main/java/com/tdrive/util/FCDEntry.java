package com.tdrive.util;

import com.graphhopper.util.GPXEntry;

public class FCDEntry extends GPXEntry {
    private int speed;

    public FCDEntry(GPXEntry e, int speed) {
        this(e.lat, e.lon, e.ele, e.getTime(), speed);
    }

    public FCDEntry(double lat, double lon, long millis, int speed) {
        super(lat, lon, millis);
        this.speed = speed;
    }

    public FCDEntry(double lat, double lon, double ele, long millis, int speed) {
        super(lat, lon, ele, millis);
        this.speed = speed;
    }

    public GPXEntry toGPXEntry() {
        return new GPXEntry(this.lat, this.lon, this.ele, this.getTime());
    }

    /**
     * The speed in kilometers per hour.
     */
    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    @Override
    public int hashCode() {
        return 59 * super.hashCode() + (int) (speed ^ (speed >>> 32));
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
        return super.toString() + ", " + speed;
    }
}

