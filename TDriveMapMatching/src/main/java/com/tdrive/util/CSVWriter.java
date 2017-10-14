package com.tdrive.util;

        import com.graphhopper.util.GPXEntry;

        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.PrintStream;
        import java.util.List;

public class CSVWriter {

    public static void writer (String filename, List<FCDEntry> points, Integer trajectoryID) {
        try {
            PrintStream pt = new PrintStream(new FileOutputStream(filename, true));

            pt.println("taxi_id, latitude, longitude, ele, date_time");
            for (GPXEntry entry : points) {
                pt.println(formatGpxEntry(entry, trajectoryID));
            }

            System.out.println("Saved " + points.size() + " points.");
        } catch (IOException e) {
            System.out.println("Error Writting GPX"  + e);
        }
    }

    private static String formatGpxEntry (GPXEntry gpxEntry, int trajectoryID) {
        return trajectoryID + ", " + gpxEntry.toString();
    }
}
