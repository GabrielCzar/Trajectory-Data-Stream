package com.tdrive.dao;

import com.graphhopper.util.GPXEntry;
import com.graphhopper.util.shapes.GHPoint;

import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Repository {

    public Map<Integer, List<GPXEntry>> getAllEntriesAsGPX(String tableName) throws ClassNotFoundException, SQLException, IOException {
        Connection connection = ConnectionFactory.getConnection();
        String query = "select taxi_id, date_time, new_lon as longitude, new_lat as latitude from " + tableName + " order by date_time";
        PreparedStatement statement = connection.prepareStatement(query);

        Map<Integer, List<GPXEntry>> trajectories = new HashMap<>();
        ArrayList<GPXEntry> entries = new ArrayList<>();

        int _id = -1;

        ResultSet result = statement.executeQuery();

        while (result.next()) {
            if (_id == -1) {
                _id = result.getInt(1);
            } else if (_id != result.getInt(1)) {
                _id = result.getInt(1);
                trajectories.put(_id, entries);
                entries = new ArrayList<>();
            }

            entries.add(
                    new GPXEntry(
                            new GHPoint(
                                    result.getDouble("latitude"),
                                    result.getDouble("longitude")),
                            getDateTime(result.getString("date_time")).getTime()));
        }

        trajectories.put(_id, entries);

        connection.close();

        return trajectories;
    }

    private Timestamp getDateTime(String date_time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date parsedDate = null;
        try {
            parsedDate = dateFormat.parse(date_time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Timestamp(parsedDate.getTime());
    }
}
