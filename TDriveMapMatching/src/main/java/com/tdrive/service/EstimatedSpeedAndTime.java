package com.tdrive.service;

import com.graphhopper.matching.EdgeMatch;
import com.graphhopper.matching.GPXExtension;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint3D;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import org.geotools.geometry.jts.JTSFactoryFinder;

import java.util.*;

public class EstimatedSpeedAndTime {
    private static GeometryFactory geoFactory = JTSFactoryFinder.getGeometryFactory();

    public static class SpeedMatch {
        public EdgeMatch edgeMatch;
        public double speed;
        public double timestamp;
        public double travelTime;
    }

    public static Map<Integer, SpeedMatch> estimateSpeed(List<EdgeMatch> matches) {
        List<EdgeMatch> linksToDistributeSpeed = new ArrayList<>();
        Map<Integer, SpeedMatch> mapLinkToSpeed = new HashMap<>();
        double totalLenght = 0, endDelta = 0;
        boolean firstFound = false, lastFound = false;
        GHPoint3D gpsLast = null, gpsFirst = null;
        Point last = null, first = null;
        long timeFirst = 0, timeLast = 0;
        for (int i = 0; i < matches.size(); i++) {
            EdgeMatch edgeMatch = matches.get(i);

            // Get gps points in the actual edge
            List<GPXExtension> gpsCorrected = edgeMatch.getGpxExtensions();
            // Get edge geometry
            PointList geometry = edgeMatch.getEdgeState().fetchWayGeometry(3);
            Coordinate[] coords = getCoordinates(geometry);
            linksToDistributeSpeed.add(edgeMatch);

            if (gpsCorrected.size() <= 1) {
                LineString lineString = geoFactory.createLineString(coords);
                totalLenght += getDistanceInMeters(lineString.getLength());
                continue;
            }

            if (!firstFound) {
                gpsFirst = gpsCorrected.get(0).getQueryResult().getSnappedPoint();
                timeFirst = gpsCorrected.get(0).getEntry().getTime();
                first = geoFactory.createPoint(new Coordinate(gpsFirst.lon, gpsFirst.lat));
            }

            if (!lastFound) {
                gpsLast = gpsCorrected.get(gpsCorrected.size() - 1).getQueryResult().getSnappedPoint();
                last = geoFactory.createPoint(new Coordinate(gpsLast.lon, gpsLast.lat));
                timeLast = gpsCorrected.get(gpsCorrected.size() - 1).getEntry().getTime();
            }

            for (int j = 0; j < coords.length - 1; j++) {
                LineString lineString = geoFactory.createLineString(Arrays.copyOfRange(coords, j, j + 2));
                if (!firstFound) {
                    if (isPointInLine(first, lineString)) {
                        firstFound = true;
                        totalLenght += getDistanceInMeters(first.distance(lineString.getEndPoint()));
                    }
                }

                if (firstFound && !lastFound && gpsLast != null) {
                    if (isPointInLine(last, lineString)) {
                        lastFound = true;
                        // end delta do ponto de gps até o final da linha
                        endDelta += getDistanceInMeters(last.distance(lineString.getEndPoint()));

                        // total do início da linha até o ponto de gps
                        totalLenght += getDistanceInMeters(lineString.getStartPoint().distance(last));

                    } else {
                        totalLenght += getDistanceInMeters(lineString.getLength());
                    }
                } else {
                    endDelta += getDistanceInMeters(lineString.getLength());
                }
            }

            if (firstFound && lastFound) {
                double speed = msTokmh(totalLenght / (timeLast - timeFirst) * 1000);
                linksToDistributeSpeed.add(edgeMatch);
                double nextTimestamp = -1;

                for (EdgeMatch edge : linksToDistributeSpeed) {
                    SpeedMatch speedMatch = new SpeedMatch();
                    speedMatch.edgeMatch = edgeMatch;
                    speedMatch.speed = speed;

                    if (nextTimestamp == -1) {
                        speedMatch.timestamp = edgeMatch.getGpxExtensions().get(0).getEntry().getTime();
                        GHPoint3D point = edgeMatch.getGpxExtensions().get(0).getQueryResult().getSnappedPoint();
                        Point p = geoFactory.createPoint(new Coordinate(point.lat, point.lon));
                        PointList pointList = edgeMatch.getEdgeState().fetchWayGeometry(3);
                        double distanceInMeters = getDistanceInMeters(p.distance(
                                geoFactory.createPoint(new Coordinate(pointList.getLongitude(pointList.getSize() - 1),
                                        pointList.getLatitude(pointList.getSize() - 1)))));
                        speedMatch.travelTime = distanceInMeters / speed;
                        nextTimestamp = speedMatch.timestamp + speedMatch.travelTime;
                    } else {
                        speedMatch.timestamp = nextTimestamp;
                        PointList pointList = edgeMatch.getEdgeState().fetchWayGeometry(3);
                        Point p1 = geoFactory
                                .createPoint(new Coordinate(pointList.getLongitude(0), pointList.getLatitude(0)));
                        Point p2 = geoFactory
                                .createPoint(new Coordinate(pointList.getLongitude(pointList.getSize() - 1),
                                        pointList.getLatitude(pointList.getSize() - 1)));
                        double distanceInMeters = getDistanceInMeters(p1.distance(p2));

                        speedMatch.travelTime = distanceInMeters / speed;
                        nextTimestamp = speedMatch.timestamp + speedMatch.travelTime;
                    }

                    mapLinkToSpeed.put(edge.getEdgeState().getEdge(), speedMatch);
                }
                linksToDistributeSpeed.clear();
                totalLenght = endDelta;
                endDelta = 0;
                firstFound = true;
                lastFound = false;
                timeFirst = timeLast;
            }

        }

        return mapLinkToSpeed;
    }

    private static double msTokmh(double speed) {
        return speed * 3.6;
    }

    private static Coordinate[] getCoordinates(PointList g) {
        Coordinate[] coords = new Coordinate[g.size()];

        for (int i = 0; i < coords.length; i++) {
            coords[i] = new Coordinate(g.getLon(i), g.getLat(i));
        }

        return coords;
    }

    private static double getDistanceInMeters(double angularDistance) {
        return angularDistance * (Math.PI / 180) * 6378137;
    }

    private static boolean isPointInLine(Point point, LineString line) {
        if (getDistanceInMeters(line.distance(point)) < 0.1)
            return true;
        return false;
    }

}
