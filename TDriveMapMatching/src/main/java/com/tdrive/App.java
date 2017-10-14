package com.tdrive;

import com.graphhopper.matching.MatchResult;
import com.graphhopper.util.GPXEntry;
import com.tdrive.dao.Repository;
import com.tdrive.service.EstimatedSpeedAndTime;
import com.tdrive.service.FCDMatcher;
import com.tdrive.service.TrajectoryMapMatching;
import com.tdrive.util.CSVWriter;
import com.tdrive.util.FCDEntry;

import com.tdrive.service.EstimatedSpeedAndTime.SpeedMatch;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class App {
    private static final String TABLE = "taxi_graph_hopper_teste",
                                OSM_FILE_PATH = "Beijing.osm.pbf",
                                GHLOCATION = "graphopper-beijing";

    public static void main(String[] args) {
        Repository repository = new Repository();
        try {
            Map<Integer, List<GPXEntry>> gpxEntries = repository.getAllEntriesAsGPX(TABLE, 140);

            TrajectoryMapMatching mapMatching = new TrajectoryMapMatching(OSM_FILE_PATH, GHLOCATION);

            // Match in GPX entries
            List<GPXEntry> gpxUnmatched = gpxEntries.get(1368);
            

            //MatchResult mr = mapMatching.doMatching(gpxUnmatched);
            ///Map<Integer, SpeedMatch> estimateSpeed = EstimatedSpeedAndTime.estimateSpeed(mr.getEdgeMatches());

            //CSVWriter.writeSpeedMatch("estimated-speed.csv", estimateSpeed, 1368);

            /** CSV Files **/
                                                        // Personalize Matcher
            // useFCDEntries(gpxEntries.get(1368), mapMatching); // FCDMatcher
            // defaultGPXEntries(gpxUnmatched, mapMatching); // GraphHopper Matcher

            /** GPX Files **/

            //List<GPXEntry> gpxMatched = mapMatching.doMatching(gpxUnmatched);
            //GPXWriter.writer("gpx-matched.gpx", gpxMatched);

            //GPXWriter.writer("gpx-unmatched.gpx", gpxUnmatched);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void personalizeMatched(List<GPXEntry> gpxUnmatched, TrajectoryMapMatching mapMatching) {

    }

    private static void useFCDEntries (List<GPXEntry> gpxUnmatched, TrajectoryMapMatching mapMatching) {

        // Match in GPX entries
        List<GPXEntry> gpxMatched = mapMatching.doMatchingAndGetGPXEntries(gpxUnmatched);
        // Convert GPX entries in FCD entries
        List<FCDEntry> fcdUnmatched = FCDMatcher.convertGPXEntryInFCDEntry(gpxUnmatched);
        List<FCDEntry> fcdMatched = FCDMatcher.convertGPXEntryInFCDEntry(gpxMatched);
        // Rematch FCD entries
        List<FCDEntry> fcdMatch = FCDMatcher.doFCDMatching(fcdUnmatched, fcdMatched);
        // Remove gaps in FCD entries
        List<FCDEntry> fcdEntriesNoGaps = FCDMatcher.fillGaps(fcdMatch);
        // Export to CSV
        CSVWriter.writerFCDEntries("fcd-match-with-fill-gaps-date-format.csv", fcdEntriesNoGaps, 1368);
    }

    private static void defaultGPXEntries (List<GPXEntry> gpxEntries, TrajectoryMapMatching mapMatching) {
        List<GPXEntry> gpxMatched = mapMatching.doMatchingAndGetGPXEntries(gpxEntries);
        CSVWriter.writerGPXEntries("map-matching-gpx-entries.csv", gpxMatched,1368);
    }
}
