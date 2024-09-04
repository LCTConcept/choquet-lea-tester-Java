package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.config.DataBaseConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.*;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class DataBaseConfigTest {

    private DataBaseConfig dataBaseConfig;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @Before
    public void setUp() {
        dataBaseConfig = new DataBaseConfig();
    }

    @Test
    public void testGetConnectionSuccess() throws Exception {
        // Vérifie qu'une connexion valide a été créée avec succès.
        Connection connection = dataBaseConfig.getConnection();
        assertNotNull(connection);
    }

    @Test(expected = SQLException.class)
    public void testGetConnectionSQLException() throws Exception {
        //Vérifie qu'une exception SQLException est levée en cas de problème lors de la création d'une connexion à une base de données.
        doThrow(SQLException.class).when(DriverManager.getConnection(anyString(), anyString(), anyString()));
        dataBaseConfig.getConnection();
    }

    @Test
    public void testCloseConnectionIfConnectionIsNull() throws SQLException {
        //Vérifie qu'une connexion à une base de données est fermée avec succès.
        dataBaseConfig.closeConnection(connection);
        verify(connection, times(0)).close();
    }


    @Test
    public void testCloseConnectionException() throws SQLException {
        //Vérifie que SQLException est gérée correctement lors de la fermeture d'une connexion à une base de données.
        doThrow(SQLException.class).when(connection).close();
        dataBaseConfig.closeConnection(connection);
        verify(connection, times(1)).close();
    }

    @Test
    public void testClosePreparedStatementSuccess() throws SQLException {
        //Vérifie qu'un PreparedStatement est fermé avec succès.
        dataBaseConfig.closePreparedStatement(preparedStatement);
        verify(preparedStatement, times(1)).close();
    }

    @Test
    public void testClosePreparedStatementException() throws SQLException {
        //Vérifie que l'exception SQLException est gérée correctement lors de la fermeture d'un PreparedStatement.
        doThrow(SQLException.class).when(preparedStatement).close();
        dataBaseConfig.closePreparedStatement(preparedStatement);
        verify(preparedStatement, times(1)).close();
    }

    @Test
    public void testCloseResultSetSuccess() throws SQLException {
        //Vérifie qu'un ResultSet est fermé avec succès.
        dataBaseConfig.closeResultSet(resultSet);
        verify(resultSet, times(1)).close();
    }

    @Test
    public void testCloseResultSetException() throws SQLException {
        //Vérifie que SQLException est gérée correctement lors de la fermeture d'un ResultSet.
        doThrow(SQLException.class).when(resultSet).close();
        dataBaseConfig.closeResultSet(resultSet);
        verify(resultSet, times(1)).close();
    }
}
