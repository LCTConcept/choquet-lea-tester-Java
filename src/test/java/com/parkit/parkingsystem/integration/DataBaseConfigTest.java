package com.parkit.parkingsystem.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.parkit.parkingsystem.config.DataBaseConfig;

public class DataBaseConfigTest {

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    // We use a spy here to allow partial mocking of DataBaseConfig
    private DataBaseConfig dataBaseConfigSpy;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        dataBaseConfigSpy = spy(new DataBaseConfig());
    }

    @Test
    public void testGetConnectionSuccess() throws Exception {
        // Simulate the successful creation of a DB connection
        Connection connection = dataBaseConfigSpy.getConnection();
        assertNotNull(connection);
    }

    @Test
    public void testGetConnectionThrowsSQLException() throws Exception {
        // Simulate an SQLException being thrown during getConnection
        doThrow(new SQLException("DB connection failed")).when(dataBaseConfigSpy).getConnection();
        try {
            dataBaseConfigSpy.getConnection();
        } catch (SQLException e) {
            // Expected exception
            verify(dataBaseConfigSpy, times(1)).getConnection();
        }
    }

    @Test
    public void testCloseConnectionSuccess() throws SQLException {
        // Simulate closing a valid connection
        dataBaseConfigSpy.closeConnection(connection);
        verify(connection, times(1)).close();
    }

    @Test
    public void testCloseConnectionThrowsSQLException() throws SQLException {
        // Simulate an SQLException being thrown during closeConnection
        doThrow(new SQLException("Error closing connection")).when(connection).close();
        dataBaseConfigSpy.closeConnection(connection);
        verify(connection, times(1)).close();
    }

    @Test
    public void testCloseConnectionNull() throws SQLException {
        // Test closing a null connection (should not attempt to close)
        dataBaseConfigSpy.closeConnection(null);
        verify(connection, times(0)).close(); // No interaction with connection should occur
    }

    @Test
    public void testClosePreparedStatementSuccess() throws SQLException {
        // Simulate closing a valid PreparedStatement
        dataBaseConfigSpy.closePreparedStatement(preparedStatement);
        verify(preparedStatement, times(1)).close();
    }

    @Test
    public void testClosePreparedStatementThrowsSQLException() throws SQLException {
        // Simulate an SQLException being thrown during closePreparedStatement
        doThrow(new SQLException("Error closing PreparedStatement")).when(preparedStatement).close();
        dataBaseConfigSpy.closePreparedStatement(preparedStatement);
        verify(preparedStatement, times(1)).close();
    }

    @Test
    public void testClosePreparedStatementNull() throws SQLException {
        // Test closing a null PreparedStatement (should not attempt to close)
        dataBaseConfigSpy.closePreparedStatement(null);
        verify(preparedStatement, times(0)).close(); // No interaction with preparedStatement should occur
    }

    @Test
    public void testCloseResultSetSuccess() throws SQLException {
        // Simulate closing a valid ResultSet
        dataBaseConfigSpy.closeResultSet(resultSet);
        verify(resultSet, times(1)).close();
    }

    @Test
    public void testCloseResultSetThrowsSQLException() throws SQLException {
        // Simulate an SQLException being thrown during closeResultSet
        doThrow(new SQLException("Error closing ResultSet")).when(resultSet).close();
        dataBaseConfigSpy.closeResultSet(resultSet);
        verify(resultSet, times(1)).close();
    }

    @Test
    public void testCloseResultSetNull() throws SQLException {
        // Test closing a null ResultSet (should not attempt to close)
        dataBaseConfigSpy.closeResultSet(null);
        verify(resultSet, times(0)).close(); // No interaction with resultSet should occur
    }
}