package com.tdrive;

import com.graphhopper.matching.EdgeMatch;
import com.graphhopper.util.GPXEntry;
import com.tdrive.dao.Repository;
import com.tdrive.service.TrajectoryMapMatching;

import java.util.List;
import java.util.Map;

public class App {
    private static final String TABLE = "taxi_graph_hopper_teste",
                                OSM_FILE_PATH = "Beijing.osm.pbf",
                                GRAPH_HOPPER_LOCATION = "graphopper-beijing";

    public static void main(String[] args) {
        Repository repository = new Repository();
        try {
            Map<Integer, List<GPXEntry>> gpxEntries = repository.getAllEntriesAsGPX(TABLE, 140);

            TrajectoryMapMatching mapMatching = new TrajectoryMapMatching(OSM_FILE_PATH, GRAPH_HOPPER_LOCATION);

            List<EdgeMatch> edgeMatches = mapMatching.getEdgesMatches(gpxEntries.get(1368));

            System.out.println(edgeMatches.size());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
