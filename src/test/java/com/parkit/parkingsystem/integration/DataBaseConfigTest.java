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

    // Utilisation d'un espion pour permettre un mocking partiel de DataBaseConfig
    private DataBaseConfig dataBaseConfigSpy;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        dataBaseConfigSpy = spy(new DataBaseConfig());
    }

    @Test
    public void testGetConnectionSuccess() throws Exception {
        // Simulation la création réussie d'une connexion à la base de données
        Connection connection = dataBaseConfigSpy.getConnection();
        assertNotNull(connection);
    }

    @Test
    public void testGetConnectionThrowsSQLException() throws Exception {
        // Simulation d'une exception SQLException lors de l'opération getConnection
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
        // Simule la fermeture d'une connexion valide
        dataBaseConfigSpy.closeConnection(connection);
        verify(connection, times(1)).close();
    }

    @Test
    public void testCloseConnectionThrowsSQLException() throws SQLException {
        // Simule une exception SQLException lors de la fermeture de la connexion
        doThrow(new SQLException("Error closing connection")).when(connection).close();
        dataBaseConfigSpy.closeConnection(connection);
        verify(connection, times(1)).close();
    }

    @Test
    public void testCloseConnectionNull() throws SQLException {
        // Test de fermeture d'une connexion nulle
        dataBaseConfigSpy.closeConnection(null);
        verify(connection, times(0)).close();
    }

    @Test
    public void testClosePreparedStatementSuccess() throws SQLException {
        // Simule la fermeture d'un PreparedStatement valide
        dataBaseConfigSpy.closePreparedStatement(preparedStatement);
        verify(preparedStatement, times(1)).close();
    }

    @Test
    public void testClosePreparedStatementThrowsSQLException() throws SQLException {
        // Simulation d'une exception SQLException lors d'un closePreparedStatement
        doThrow(new SQLException("Error closing PreparedStatement")).when(preparedStatement).close();
        dataBaseConfigSpy.closePreparedStatement(preparedStatement);
        verify(preparedStatement, times(1)).close();
    }

    @Test
    public void testClosePreparedStatementNull() throws SQLException {
        // Test de fermeture d'un PreparedStatement nul (ne devrait pas essayer de fermer)
        dataBaseConfigSpy.closePreparedStatement(null);
        verify(preparedStatement, times(0)).close(); // No interaction with preparedStatement should occur
    }

    @Test
    public void testCloseResultSetSuccess() throws SQLException {
        // Simule la fermeture d'un ResultSet valide
        dataBaseConfigSpy.closeResultSet(resultSet);
        verify(resultSet, times(1)).close();
    }

    @Test
    public void testCloseResultSetThrowsSQLException() throws SQLException {
        // Simulation d'une exception SQLException lors de closeResultSet
        doThrow(new SQLException("Error closing ResultSet")).when(resultSet).close();
        dataBaseConfigSpy.closeResultSet(resultSet);
        verify(resultSet, times(1)).close();
    }

    @Test
    public void testCloseResultSetNull() throws SQLException {
        // Test de fermeture d'un ResultSet nul (ne devrait pas essayer de fermer)
        dataBaseConfigSpy.closeResultSet(null);
        verify(resultSet, times(0)).close(); // No interaction with resultSet should occur
    }
}