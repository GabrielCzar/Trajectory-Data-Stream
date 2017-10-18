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
import com.graphhopper.util.shapes.GHPoint3D;
import com.tdrive.util.FCDEntry;
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

    public List<GPXEntry> doMatchingAndGetGPXEntries(List<GPXEntry> entries) {
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

        //System.out.println("Speed - EdgeMatches -> " + weighting.getFlagEncoder().getSpeed(mr.getEdgeMatches().get(0).getEdgeState().getFlags()));
        // Get points of matched track
        Path path = mapMatching.calcPath(mr);
        PointList points = path.calcPoints();

        if (points != null && !points.isEmpty()) {
            for (GHPoint pt : points) {
                //System.out.println(pt);
                gpxMatched.add(new GPXEntry(pt.getLat(), pt.getLon(), 0.0, 0));
            }
        }
        return gpxMatched;
    }

    public MatchResult doMatching(List<GPXEntry> entries) {
        MapMatching mapMatching = new MapMatching(hopper, algoOptions);
        mapMatching.setMeasurementErrorSigma(50);
        MatchResult mr = null;
        try {
            mr = mapMatching.doWork(entries);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return mr;
    }


    public List<FCDEntry> doMatchingAndGetFCDEntries(List<GPXEntry> entries) {
        MapMatching mapMatching = new MapMatching(hopper, algoOptions);
        mapMatching.setMeasurementErrorSigma(50);
        MatchResult mr = null;
        try {
            mr = mapMatching.doWork(entries);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        List<FCDEntry> gpxMatched = new ArrayList<>();

//        for (int i = 0; i < mr.getEdgeMatches().size(); i++) {
  //          System.out.println("Speed - EdgeMatches -> " + i  + " - " +
    //                weighting.getFlagEncoder().getSpeed(mr.getEdgeMatches().get(i).getEdgeState().getFlags()));
      //  }

        System.out.println("SIZE EDGE MATCHES --> " + mr.getEdgeMatches().size());


        mr.getEdgeMatches().get(0).getGpxExtensions().get(0).getEntry();

        // Get points of matched track
        Path path = mapMatching.calcPath(mr);
        PointList points = path.calcPoints();

        if (points != null && !points.isEmpty()) {
            for (GHPoint pt : points) {
                //System.out.println(pt);
                gpxMatched.add(new FCDEntry(pt.getLat(), pt.getLon(), 0, 0));
            }
        }
        return gpxMatched;
    }

    public double getSpeed (long flags) {
        return weighting.getFlagEncoder().getSpeed(flags);
    }
}