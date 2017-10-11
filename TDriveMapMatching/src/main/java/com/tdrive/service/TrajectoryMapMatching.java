package com.tdrive.service;

import com.graphhopper.GraphHopper;
import com.graphhopper.matching.*;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.*;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.util.GPXEntry;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import sun.security.krb5.internal.PAData;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class TrajectoryMapMatching {
    private final String algorithm = Parameters.Algorithms.DIJKSTRA_BI;
    private AlgorithmOptions algoOptions;
    private CarFlagEncoder encoder;
    private Weighting weighting;
    private GraphHopper hopper;

    public TrajectoryMapMatching(String osmFilePath, String graphHopperLocation) {
        //hopper = new GraphHopperOSM();
        hopper = new GraphHopper();
        hopper.setDataReaderFile(osmFilePath);
        hopper.setGraphHopperLocation(graphHopperLocation);
        encoder = new CarFlagEncoder();
        hopper.setEncodingManager(new EncodingManager(encoder));
        hopper.getCHFactoryDecorator().setEnabled(false);
        hopper.importOrLoad();

        weighting = new FastestWeighting(encoder);
        algoOptions = new AlgorithmOptions(algorithm, weighting);
    }

    public List<GPXEntry> doMatching(List<GPXEntry> entries) {
        MapMatching mapMatching = new MapMatching(hopper, algoOptions);
        mapMatching.setMeasurementErrorSigma(50);
        MatchResult mr = null;
        try {
            mr = mapMatching.doWork(entries);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        List<GPXEntry> gpxMatched = new ArrayList<>();

        // Get points of matched track
        Path path = mapMatching.calcPath(mr);
        PointList points = path.calcPoints();
        if (points != null && !points.isEmpty()) {
            for (GHPoint pt : points) {
                gpxMatched.add(new GPXEntry(pt.getLat(), pt.getLon(), 0.0, 0));
            }
        }
        return gpxMatched;
    }

    public void saveMapMatching(List<GPXEntry> gpxEntries, Integer trajectoryID, String fileName) {
        try {
            PrintStream pt = new PrintStream(new FileOutputStream(fileName, true));

            pt.println("taxi_id, latitude, longitude, ele, date_time");
            for (GPXEntry gpx : gpxEntries) {
                pt.println(formatGpxEntry(gpx, trajectoryID));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatGpxEntry (GPXEntry gpxEntry, int trajectoryID) {
        return trajectoryID + ", " + gpxEntry.toString();
    }

}
