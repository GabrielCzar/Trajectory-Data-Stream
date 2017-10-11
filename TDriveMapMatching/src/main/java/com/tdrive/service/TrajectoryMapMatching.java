package com.tdrive.service;

import com.graphhopper.GraphHopper;
import com.graphhopper.matching.*;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.*;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.util.GPXEntry;
import com.graphhopper.util.Parameters;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public List<EdgeMatch> getEdgesMatches(List<GPXEntry> entries) {
        MapMatching mapMatching = new MapMatching(hopper, algoOptions);
        MatchResult mr = mapMatching.doWork(entries);
        return mr.getEdgeMatches();
    }

    public List<GPXEntry> convertEdgeMatchesInListGpxEntries(List<EdgeMatch> edgeMatches) {
        List<GPXEntry> gpxMatches = new ArrayList<>();

        for (EdgeMatch edgeMatch : edgeMatches) {
            List<GPXExtension> gpxExtensions = edgeMatch.getGpxExtensions();

            for (GPXExtension gpxExtension : gpxExtensions) {
                // This just gives me an entry from my input which is closest
                // to the tower node of the matching edge
                gpxMatches.add(gpxExtension.getEntry());
            }
        }

        return gpxMatches;
    }

    public void saveMapMatching(List<EdgeMatch> edgeMatches, Integer trajectorieID) {
        try {
            PrintStream pt = new PrintStream(new FileOutputStream("map-matching-edges-matches", true));


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
