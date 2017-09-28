package com.tdrive.service;

import com.graphhopper.GraphHopper;
import com.graphhopper.matching.EdgeMatch;
import com.graphhopper.matching.GPXFile;
import com.graphhopper.matching.MapMatching;
import com.graphhopper.matching.MatchResult;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.GPXEntry;
import com.graphhopper.util.Parameters;

import java.util.List;

public class TrajectoryMapMatching {
    private final String algorithm = Parameters.Algorithms.DIJKSTRA_BI;
    private AlgorithmOptions algoOptions;
    private CarFlagEncoder encoder;
    private Weighting weighting;
    private GraphHopper hopper;

    public TrajectoryMapMatching(String osmFilePath, String graphHopperLocation) {
        encoder = new CarFlagEncoder();
        weighting = new FastestWeighting(encoder);
        algoOptions = new AlgorithmOptions(algorithm, weighting);

        hopper = new GraphHopperOSM();
        configGraphHopper(osmFilePath, graphHopperLocation);
    }

    public void configGraphHopper(String osmPath, String location) {
        hopper.setDataReaderFile(osmPath);
        hopper.setGraphHopperLocation(location);
        hopper.setEncodingManager(new EncodingManager(encoder));
        hopper.getCHFactoryDecorator().setEnabled(false);
        hopper.importOrLoad();
    }

    public List<EdgeMatch> edgeMatches() {
        MapMatching mapMatching = new MapMatching(hopper, algoOptions);

        List<GPXEntry> inputGPXEntries = new GPXFile().doImport("nice.gpx").getEntries(); // CHANGE THIS

        MatchResult mr = mapMatching.doWork(inputGPXEntries);
        return mr.getEdgeMatches();
    }
}
