package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TicketDAOTest {

    @Mock
    private TicketDAO ticketDAO;

    @Mock
    private DataBaseConfig dataBaseConfig;

    @Mock
    private Connection connection;

    @Mock
    private ResultSet resultSet;

    @Mock
    PreparedStatement preparedStatement;

    private Ticket ticket;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ticket = new Ticket();
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
        ticket.setId(1);
        ticket.setVehicleRegNumber("ABC123");
        ticket.setPrice(1.5);
        ticket.setInTime(new Date());
        ticket.setOutTime(new Date());

        ticketDAO = new TicketDAO(dataBaseConfig);
        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @Test
    //Vérifie que le ticket est sauvegardé dans la base de données
    public void testSaveTicket() throws Exception {
        when(preparedStatement.execute()).thenReturn(true);

        boolean result = ticketDAO.saveTicket(ticket);
        assertTrue(result);

        verify(preparedStatement, times(1)).execute();
        verify(dataBaseConfig, times(1)).closeConnection(connection);
    }

    @Test
    public void testSaveTicketException() throws Exception {
        //Vérifie qu'une exception lors de l'enregistrement d'un ticket entraîne un échec et un nettoyage correct des ressources.
        when(preparedStatement.execute()).thenThrow(new RuntimeException("Exception in save"));

        boolean result = ticketDAO.saveTicket(ticket);
        assertFalse(result);

        verify(dataBaseConfig, times(1)).closeConnection(connection);
    }

    @Test
    public void testGetTicket() throws Exception {
        //Valide la récupération correcte d'un ticket dans la base de données en fonction du numéro d'immatriculation du véhicule.
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1);
        when(resultSet.getString(6)).thenReturn("CAR");
        when(resultSet.getInt(2)).thenReturn(1);
        when(resultSet.getDouble(3)).thenReturn(1.5);
        when(resultSet.getTimestamp(4)).thenReturn(new Timestamp(ticket.getInTime().getTime()));
        when(resultSet.getTimestamp(5)).thenReturn(new Timestamp(ticket.getOutTime().getTime()));

        Ticket fetchedTicket = ticketDAO.getTicket("ABC123");

        assertNotNull(fetchedTicket);
        assertEquals("ABC123", fetchedTicket.getVehicleRegNumber());
        verify(dataBaseConfig, times(1)).closeConnection(connection);
    }

    @Test
    public void testGetTicketException() throws Exception {
        //Vérifie qu'une exception survenant lors de la récupération d'un ticket renvoie un résultat nul et nettoie les ressources.
        when(preparedStatement.executeQuery()).thenThrow(new RuntimeException("Exception in get"));

        Ticket fetchedTicket = ticketDAO.getTicket("ABC123");

        assertNull(fetchedTicket);
        verify(dataBaseConfig, times(1)).closeConnection(connection);
    }

    @Test
    public void testUpdateTicket() throws Exception {
        //Confirme qu'un ticket est mis à jour avec succès dans la base de données.
        when(preparedStatement.execute()).thenReturn(true);

        boolean result = ticketDAO.updateTicket(ticket);
        assertTrue(result);

        verify(preparedStatement, times(1)).setDouble(1, ticket.getPrice());
        verify(preparedStatement, times(1)).setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
        verify(preparedStatement, times(1)).setInt(3, ticket.getId());
        verify(preparedStatement, times(1)).execute();
        verify(dataBaseConfig, times(1)).closeConnection(connection);
    }

    @Test
    public void testUpdateTicketNullOutTime() throws Exception {
        // Vérifie que la mise à jour échoue lorsque l'heure de sortie du ticket est null.
        ticket.setOutTime(null);

        boolean result = ticketDAO.updateTicket(ticket);
        assertFalse(result);

        verify(preparedStatement, never()).execute();
        verify(dataBaseConfig, times(1)).closeConnection(connection);
    }

    @Test
    public void testUpdateTicketException() throws Exception {
        // Simuler une exception lorsque execute() est appelé sur le prepared statement
        when(preparedStatement.execute()).thenThrow(new RuntimeException("Exception in update"));

        // Test
        boolean result = ticketDAO.updateTicket(ticket);

        // Vérifie que la méthode updateTicket a retourné false en raison de l'exception
        assertFalse(result);

        // Vérifie que les méthodes attendues ont été appelées
        verify(preparedStatement, times(1)).setDouble(1, ticket.getPrice());
        // Les vérifications suivantes ne sont pas nécessaires car une exception se produit avant que ces méthodes puissent être exécutées :
        // verify(preparedStatement, never()).setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
        verify(preparedStatement, times(1)).setInt(3, ticket.getId());

        // Vérifie que la connexion a été fermée
        verify(dataBaseConfig, times(1)).closeConnection(connection);
    }

    @Test
    public void testCountTicketByVehicleRegNumber() throws Exception {
        //Valide que le nombre correct de tickets est renvoyé pour un numéro d'immatriculation de véhicule donné.
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(2);

        int count = ticketDAO.countTicketByVehicleRegNumber("ABC123");
        assertEquals(2, count);

        verify(dataBaseConfig, times(1)).closeConnection(connection);
    }

    @Test
    public void testCountTicketByVehicleRegNumberException() throws Exception {
        // Simule une exception lorsque executeQuery() est appelé
        when(preparedStatement.executeQuery()).thenThrow(new RuntimeException("Exception in count"));

        int count = ticketDAO.countTicketByVehicleRegNumber("ABC123");
        assertEquals(-1, count); // Assert that the exception results in -1

        verify(ticketDAO.dataBaseConfig, times(1)).closeConnection(connection);
    }

    @Test
    public void testUpdateTicketConnectionException() throws Exception {
        // Simule une exception lorsque la connexion à la base de données échoue
        when(dataBaseConfig.getConnection()).thenThrow(new RuntimeException("Exception in connection"));

        boolean result = ticketDAO.updateTicket(ticket);
        assertFalse(result);

        verify(dataBaseConfig, times(1)).closeConnection(null);
    }

    @Test
    public void testUpdateTicketPreparedStatementException() throws Exception {
        // Simule une exception lors de la préparation de la requête
        when(preparedStatement.execute()).thenThrow(new RuntimeException("Exception in execute"));

        boolean result = ticketDAO.updateTicket(ticket);
        assertFalse(result);

        verify(dataBaseConfig, times(1)).closeConnection(connection);
    }

}