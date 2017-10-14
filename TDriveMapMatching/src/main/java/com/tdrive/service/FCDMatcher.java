package com.tdrive.service;

import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistancePlaneProjection;
import com.graphhopper.util.GPXEntry;
import com.tdrive.util.FCDEntry;
import com.tdrive.util.GPXWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

public class FCDMatcher {

    public static List<FCDEntry> doFCDMatching(List<FCDEntry> fcdUnmatched, List<FCDEntry> fcdMatched) {
        // init list for query nodes (outer loop)
        ConcurrentSkipListSet<Integer> queryNodes = new ConcurrentSkipListSet<>();
        // init and fill hash map for matching candidates per inner node (inner loop)
        ConcurrentHashMap<Integer, ConcurrentSkipListMap<Double, Integer>> candidateNodes = new ConcurrentHashMap<>();

        DistanceCalc distanceCalc = new DistancePlaneProjection();
        List<FCDEntry> outerLoop = null;
        List<FCDEntry> innerLoop = null;

        // choose smaller point list as outer loop
        if (fcdUnmatched.size() < fcdMatched.size()) {
            outerLoop = fcdUnmatched;
            innerLoop = fcdMatched;
        }
        else {
            outerLoop = fcdMatched;
            innerLoop = fcdUnmatched;
        }

        for (int i=0;i<innerLoop.size();i++)
            candidateNodes.put(i, new ConcurrentSkipListMap<>());

        // loop over smaller point list
        for (int i=0;i<outerLoop.size();i++) {
            FCDEntry outerNode = outerLoop.get(i);

            // parameters to approach closest node
            double checkDistance = 50.0;
            boolean found = false;
            int tolerance = 5;

            // loop over greater point list
            for (int j=0;j<innerLoop.size();j++) {
                FCDEntry innerNode = innerLoop.get(j);

                // calculate the distance
                double distance = distanceCalc.calcDist(
                        outerNode.lat, outerNode.lon, innerNode.lat, innerNode.lon
                );

                // are we getting closer than the checkDistance?
                if (distance <= checkDistance) {
                    found = true; // yes we are
                    tolerance = 5; // keep tolerance
                    //checkDistance = distance; // update checkDistance
                    candidateNodes.get(j).put(distance, i); // remember node as possible candidate
                }
                else {
                    // if found is true we are moving further away again
                    // but we might get closer again, so we use a tolerance counter
                    tolerance += found ? -1 : 0;
                    if (tolerance < 0)
                        break;
                }
            }
            // add query node if we got a candidate
            if (found)
                queryNodes.add(i);
        }

        // create new map to control if matches are not in mixed order
        ConcurrentSkipListMap<Integer, Integer> matches = new ConcurrentSkipListMap<>();

        // search for the best matches as long as queryNodes are not matched completely
        while (!queryNodes.isEmpty()) {
            for (int i : queryNodes) {
                // at first every query node is a potential candidate
                // but this might change later in the code
                boolean hasCandidates = false;

                // search where the query node is the best candidate for one or multiple inner nodes
                TreeMap<Double,Integer> winnerNodes = new TreeMap<>();
                for (Map.Entry<Integer,ConcurrentSkipListMap<Double,Integer>> entry : candidateNodes.entrySet())
                    if (!entry.getValue().isEmpty()) {
                        // is the query node among the candidates of the inner node?
                        if (entry.getValue().containsValue(i))
                            hasCandidates = true;

                        // pick a "winner" node
                        if (entry.getValue().firstEntry().getValue() == i)
                            winnerNodes.put(entry.getValue().firstKey(), entry.getKey());
                    }

                // if query node is not a candidate for any of the inner nodes (anymore) it will be removed
                if (!hasCandidates) {
                    queryNodes.remove(i);
                    continue;
                }

                // process possible candidates
                if (!winnerNodes.isEmpty()) {
                    for (int innerNodeNo : winnerNodes.values()) {
                        // check if new match is violating the order of inner nodes
                        if (!matches.isEmpty()) {
                            // get the matching inner nodes for lower and upper query nodes
                            int lowerBound = matches.floorEntry(i) == null ? 0 : matches.floorEntry(i).getValue();
                            int upperBound = matches.ceilingEntry(i) == null ? innerNodeNo : matches.ceilingEntry(i).getValue();
                            if (lowerBound > innerNodeNo || upperBound < innerNodeNo) {
                                // match is not possible, so remove relation from candidate nodes
                                candidateNodes.get(innerNodeNo).values().remove(i);
                                continue;
                            }
                        }

                        // it's a match!
                        // copy time and speed between matching points
                        if (outerLoop.get(i).getTime() == 0) {
                            outerLoop.get(i).setTime(innerLoop.get(innerNodeNo).getTime());
                            outerLoop.get(i).setSpeed(innerLoop.get(innerNodeNo).getSpeed());
                        }
                        else {
                            innerLoop.get(innerNodeNo).setTime(outerLoop.get(i).getTime());
                            innerLoop.get(innerNodeNo).setSpeed(outerLoop.get(i).getSpeed());
                        }

                        // list the new match and remove the combo from other elements
                        matches.put(i, innerNodeNo);
                        queryNodes.remove(i);
                        candidateNodes.remove(innerNodeNo);
                        for (Entry<Integer,ConcurrentSkipListMap<Double,Integer>> entry : candidateNodes.entrySet())
                            if (entry.getValue().containsValue(i)) {
                                entry.getValue().values().remove(i);
                            }
                        // stop processing winnerNodes as we already have a match
                        break;
                    }
                }
            }
            // move on if there are still query nodes left
            // some "winner" position might be free now to take over
        }

        return fcdMatched;
    }

    public static List<FCDEntry> fillGaps(List<FCDEntry> fcdWithGaps) {

        List<FCDEntry> fcdWithoutGaps = new ArrayList<>();
        List<FCDEntry> gap = new ArrayList<>();

        for (int i = 0; i < fcdWithGaps.size(); i++) {
            FCDEntry fcdEntry = fcdWithGaps.get(i);
            // extend the gap
            gap.add(fcdEntry);

            // if entry has no time set continue with loop
            if (fcdEntry.getTime() == 0)
                continue;
            else {
                // fill the gap if it contains more than one value (must contain zeros then)
                if (gap.size() > 1 && !(gap.size() == 2 && gap.get(0).getTime() > 0))
                    fillGap(gap, fcdWithoutGaps);
                // start new gap
                gap.clear();
                gap.add(fcdEntry);
                // add entry to result list
                fcdWithoutGaps.add(fcdEntry);
            }
        }

        // there might be a gap left
        if (gap.size() > 1 && !(gap.size() == 2 && gap.get(0).getTime() > 0))
            fillGap(gap, fcdWithoutGaps);

        return fcdWithoutGaps;
    }

    private static void fillGap(List<FCDEntry> gap, List<FCDEntry> fcdWithoutGaps) {

        DistanceCalc distanceCalc = new DistancePlaneProjection();
        double distance = 0.0;
        List<Double> accumDist = new ArrayList<>();

        // start accumulative sum with 0.0
        accumDist.add(distance);

        // get the length of the the gap
        for (int i = 1; i < gap.size(); i++) {
            distance += distanceCalc.calcDist(gap.get(i - 1).lat, gap.get(i - 1).lon, gap.get(i).lat, gap.get(i).lon);
            accumDist.add(distance);
        }

        // when length of the gap is above 200m AND either the start or the end have no measure
        // we exclude the gap points from the original list
        if (distance > 200 && (gap.get(0).getTime() == 0 || gap.get(gap.size() - 1).getTime() == 0))
            return;

        // now, set speed and time per gap point and add it to fcdWithoutGaps list
        // case 1: Start has no measure. Use constant speed. Time = Distance/Speed.
        if (gap.get(0).getTime() == 0)
            for (int i = 0; i < gap.size() - 1; i++) {
                gap.get(i).setSpeed(gap.get(gap.size() - 1).getSpeed());
                gap.get(i).setTime(gap.get(gap.size() - 1).getTime() - (long) ((distance - accumDist.get(i)) / (gap.get(i).getSpeed() / 3.6) * 1000));
                fcdWithoutGaps.add(gap.get(i));
            }
            // case 2: End has no measure. Use constant speed. Time = Distance/Speed.
        else if (gap.get(gap.size() - 1).getTime() == 0)
            for (int i = 1; i < gap.size(); i++) {
                gap.get(i).setSpeed(gap.get(0).getSpeed());
                gap.get(i).setTime(gap.get(0).getTime() + (long) (accumDist.get(i) / (gap.get(i).getSpeed() / 3.6) * 1000));
                fcdWithoutGaps.add(gap.get(i));
            }
            // case 3: Boundary points have time and measure. Use constant acceleration. Time = 2*Distance/Speed
        else {
            double slope = (gap.get(0).getSpeed() - gap.get(gap.size()-1).getSpeed()) / (0 - distance);
            for (int i = 1; i < gap.size() - 1; i++) {
                gap.get(i).setSpeed((int) Math.round(slope * accumDist.get(i) + gap.get(0).getSpeed()));
                gap.get(i).setTime(gap.get(i - 1).getTime() + (long) (2 * accumDist.get(i) / (gap.get(i).getSpeed() / 3.6) * 1000));
                fcdWithoutGaps.add(gap.get(i));
            }
        }
    }

    public static List<FCDEntry> convertGPXEntryInFCDEntry (List<GPXEntry> gpxEntries) {
        return gpxEntries.stream().map(gpxEntry -> new FCDEntry(gpxEntry)).collect(Collectors.toList());
    }

    public static List<GPXEntry> convertFCDEntryInGPXEntry(List<FCDEntry> fcdEntriesNoGaps) {
        return fcdEntriesNoGaps.stream().map(fcdEntry -> fcdEntry.toGPXEntry()).collect(Collectors.toList());
    }
}

