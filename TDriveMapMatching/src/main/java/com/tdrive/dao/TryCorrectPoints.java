package com.tdrive.dao;

import com.vividsolutions.jts.geom.Geometry;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TryCorrectPoints {

    public List<Long> getAllValues() throws SQLException, IOException, ClassNotFoundException {
        Connection connection = ConnectionFactory.getConnection();
        List<Long> all = new ArrayList<>();

        String query = "select row from taxi_graph_hopper_teste;";

        PreparedStatement statement = connection.prepareStatement(query);

        ResultSet result = statement.executeQuery();

        while (result.next())
            all.add((long) result.getInt(1));

        return all;
    }

    public String correctPointsByClosestLinestringTDrive(Long id) throws SQLException, IOException, ClassNotFoundException {
        Connection factory = ConnectionFactory.getConnection();

        String query =
                "SELECT st_geomfromtext(st_astext(st_closestpoint(st_transform(way, 4326), st_transform(geom, 4326)))) as novo_point, " +
                "st_distance(st_transform(taxi.geom, 4326), st_transform(line.way, 4326)) as distancia " +
		        "st_y(st_geomfromtext(st_astext(st_closestpoint(st_transform(way, 4326), st_transform(geom, 4326))))) as latitude, " +
		        "st_x(st_geomfromtext(st_astext(st_closestpoint(st_transform(way, 4326), st_transform(geom, 4326))))) as longitude " +
                "FROM taxi_graph_hopper_teste taxi, planet_osm_line line " +
                "WHERE st_transform(way, 4326) && st_expand(st_transform(geom, 4326), 10) and row = ? " +
                "ORDER BY distancia LIMIT 1";

        PreparedStatement statement = factory.prepareStatement(query);
        statement.setLong(1, id);

        ResultSet result = statement.executeQuery();

        result.next();
	
	System.out.println("Latitude: " + result.getDouble(3));
	System.out.println("Longitude: " + result.getDouble(4));

        return result.getString(1);
    }

    public void updateTable(String geom, Long row) throws SQLException, IOException, ClassNotFoundException {
        Connection connection = ConnectionFactory.getConnection();

        String sql = "UPDATE taxi_graph_hopper_teste SET new_geom = ? WHERE row = ?";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setString(1, geom);
        statement.setLong(2, row);

        statement.executeUpdate();
    }
}
