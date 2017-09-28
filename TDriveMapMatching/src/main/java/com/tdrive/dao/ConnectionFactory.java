package com.tdrive.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionFactory {
    private static Connection connection = null;
    private static final String FILE_NAME_PROPERTIES = "config.properties";
    private static final String STR_DRIVER = "driver";
    private static final String STR_HOST = "host";
    private static final String STR_USER = "user";
    private static final String STR_PASS = "pass";

    public static Connection getConnection() throws ClassNotFoundException, SQLException, IOException {
        if (connection == null || connection.isClosed()) {
            Properties properties = new Properties();
            properties.load(new FileInputStream(new File(FILE_NAME_PROPERTIES)));

            Class.forName(properties.getProperty(STR_DRIVER));
            String host = properties.getProperty(STR_HOST);
            String user = properties.getProperty(STR_USER);
            String pass = properties.getProperty(STR_PASS);

            connection = DriverManager.getConnection(host, user, pass);
        }
        return connection;
    }
}
