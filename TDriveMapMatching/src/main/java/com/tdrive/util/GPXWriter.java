package com.tdrive.util;

import com.graphhopper.util.GPXEntry;
import com.sun.org.apache.xpath.internal.SourceTree;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GPXWriter {
    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    private static final String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" " +
            "standalone=\"no\" ?><gpx xmlns=\"http://www.topografix.com/GPX/1/1\" " +
            "creator=\"MapSource 6.15.5\" version=\"1.1\" " +
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\"><trk>\n";
    private static final String footer = "</trkseg></trk></gpx>";
    
    public static void writer(String filename, List<GPXEntry> points) {
        String name = "<name>" + filename + "</name><trkseg>\n";

        String segments = "";
        for (GPXEntry entry : points)
            segments += getSegment(entry.getLat(), entry.getLon(), df.format(new Date(entry.getTime())));

        try {
            PrintStream pt = new PrintStream(
                    new FileOutputStream(filename, true));
            pt.println(header);
            pt.append(name);
            pt.append(segments);
            pt.append(footer);
            pt.flush();
            pt.close();

            System.out.println("Saved " + points.size() + " points.");
        } catch (IOException e) {
            System.out.println("Error Writting GPX"  + e);
        }

    }

    private static String getSegment (Double latitude, Double longitude, String datetime) {
        return String.format("<trkpt lat=\"%lf\" lon=\"%lf\"><time>%s</time></trkpt>\n",
                latitude, longitude, datetime);
    }
}
