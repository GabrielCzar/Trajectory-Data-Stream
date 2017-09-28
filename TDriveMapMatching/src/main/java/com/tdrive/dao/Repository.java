package com.tdrive.dao;

import com.graphhopper.util.GPXEntry;
import com.graphhopper.util.shapes.GHPoint;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Repository {

    public Map<Integer, List<GPXEntry>> getAllEntriesAsGPX(String tablename)  throws ClassNotFoundException, SQLException, IOException {
        Connection connection = ConnectionFactory.getConnection();
        String query = "select osm_id, data_time, longitude, latitude from " + tablename + " order by osm_id, date_time";
        PreparedStatement statement = connection.prepareStatement(query);

        Map<Integer, List<GPXEntry>> trajectories = new HashMap<Integer, List<GPXEntry>>();
        int _id = -1;

        ResultSet result = statement.executeQuery();

        ArrayList<GPXEntry> entries = null;

        while (result.next()) {
            if (_id == -1) {
                _id = result.getInt(1);
                entries = new ArrayList<GPXEntry>();
            } else {
                if (_id != result.getInt(1)) {
                    _id = result.getInt(1);
                    trajectories.put(_id, entries);
                    entries = new ArrayList<GPXEntry>();
                }
            }
            entries.add(
                    new GPXEntry(
                            new GHPoint(
                                    result.getDouble("latitude"),
                                    result.getDouble("longitude")),
                            result.getTimestamp("date_time").getTime()));
        }

        connection.close();
        return trajectories;
    }
}
