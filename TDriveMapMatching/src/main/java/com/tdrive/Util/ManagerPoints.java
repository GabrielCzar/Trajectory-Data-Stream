package com.tdrive.Util;

import com.graphhopper.util.PointList;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class ManagerPoints {

    public static boolean isPointInLine(Point point, LineString line) {
        if (getDistanceInMeters(line.distance(point)) < 0.1)
            return true;
        return false;
    }

    public static double getDistanceInMeters(double angularDistance) {
        return angularDistance * (Math.PI / 180) * 6378137;
    }


    public static Coordinate [] getCoordinates(PointList g) {
        int tam = g.size();
        Coordinate [] coords = new Coordinate[tam];
        for (int i = 0; i < tam; i++)
            coords[i] = new Coordinate(g.getLon(i), g.getLat(i));
        return coords;
    }
}
