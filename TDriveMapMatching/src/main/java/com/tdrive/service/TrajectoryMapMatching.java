package com.tdrive.service;

import com.graphhopper.GraphHopper;
import com.graphhopper.matching.EdgeMatch;
import com.graphhopper.matching.GPXFile;
import com.graphhopper.matching.MapMatching;
import com.graphhopper.matching.MatchResult;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.*;
import com.graphhopper.util.GPXEntry;
import com.graphhopper.util.Parameters;

import java.util.List;
import java.util.Map;

public class TrajectoryMapMatching {
    private final String algorithm = Parameters.Algorithms.DIJKSTRA_BI;
    private AlgorithmOptions algoOptions;
    private CarFlagEncoder encoder;
    private Weighting weighting;
    private GraphHopper hopper;

    public TrajectoryMapMatching(String osmFilePath, String graphHopperLocation) {
        hopper = new GraphHopperOSM();
        hopper.setDataReaderFile(osmFilePath);
        hopper.setGraphHopperLocation(graphHopperLocation);
        encoder = new CarFlagEncoder();
        hopper.setEncodingManager(new EncodingManager(encoder));
        hopper.getCHFactoryDecorator().setEnabled(false);
        hopper.importOrLoad();

        weighting = new FastestWeighting(encoder);
        algoOptions = new AlgorithmOptions(algorithm, weighting);
    }

    public List<EdgeMatch> edgeMatches(List<GPXEntry> entries) {
        MapMatching mapMatching = new MapMatching(hopper, algoOptions);
        MatchResult mr = mapMatching.doWork(entries);
        return mr.getEdgeMatches();
    }

}
