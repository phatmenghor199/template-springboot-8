package com.bkgoutchecker.api.helper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.*;

@Service
public class JdbcInternalConnection {
    @Value("${spring.datasource.secondary.driver-class-name}")
    private String ORACLE_CLASS;

    @Value("${spring.datasource.secondary.url}")
    private String ORACLE_DB_URL;

    @Value("${spring.datasource.secondary.username}")
    private String ORACLE_USER;

    @Value("${spring.datasource.secondary.password}")
    private String ORACLE_PASS;

    public ResultSet getData(String query, String extRef) throws SQLException {
        ResultSet result = null;
        Connection connection = null;
        PreparedStatement preparedStatement = null;  // Use PreparedStatement to set parameter

        try {
            // Load the database driver
            Class.forName(ORACLE_CLASS);

            // Establish the connection
            connection = DriverManager.getConnection(ORACLE_DB_URL, ORACLE_USER, ORACLE_PASS);

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, extRef);  // Set the extRef parameter dynamically

            // Execute query
            result = preparedStatement.executeQuery();

        } catch (Exception e) {
            throw new SQLException("Error executing query", e); // Re-throw to handle at the caller level
        }

        return result;  // Do not close here, let the caller close it.
    }
}
