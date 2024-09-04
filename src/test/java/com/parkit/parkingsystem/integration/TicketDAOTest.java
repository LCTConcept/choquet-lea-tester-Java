package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TicketDAOTest {

    @InjectMocks
    private TicketDAO ticketDAO;

    @Mock
    private DataBaseConfig dataBaseConfig;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private Ticket ticket;

    @Before
    public void setUp() throws Exception {
        ticket = new Ticket();
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
        ticket.setId(1);
        ticket.setVehicleRegNumber("ABC123");
        ticket.setPrice(1.5);
        ticket.setInTime(new Date());
        ticket.setOutTime(new Date());

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
        //Vérifie que la mise à jour échoue lorsque l'heure de sortie du ticket est nulle.
        ticket.setOutTime(null);

        boolean result = ticketDAO.updateTicket(ticket);
        assertFalse(result);

        verify(preparedStatement, never()).execute();
        verify(dataBaseConfig, times(1)).closeConnection(connection);
    }

    @Test
    public void testUpdateTicketException() throws Exception {
        //Vérifie qu'une exception lors de la mise à jour du ticket entraîne un échec et un nettoyage correct des ressources.
        when(preparedStatement.execute()).thenThrow(new RuntimeException("Exception in update"));

        boolean result = ticketDAO.updateTicket(ticket);
        assertFalse(result);

        verify(preparedStatement, times(1)).setDouble(1, ticket.getPrice());
        verify(preparedStatement, never()).setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
        verify(preparedStatement, times(1)).setInt(3, ticket.getId());
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
        // Vérifie qu'une exception pendant le comptage des tickets renvoie -1 et nettoie les ressources.
        when(preparedStatement.executeQuery()).thenThrow(new RuntimeException("Exception in count"));

        int count = ticketDAO.countTicketByVehicleRegNumber("ABC123");
        assertEquals(-1, count);

        verify(dataBaseConfig, times(1)).closeConnection(connection);
    }

    @Test
    public void testUpdateTicketConnectionException() throws Exception {
        // Simuler une exception lorsque la connexion à la base de données échoue
        when(dataBaseConfig.getConnection()).thenThrow(new RuntimeException("Exception in connection"));

        boolean result = ticketDAO.updateTicket(ticket);
        assertFalse(result);

        verify(dataBaseConfig, times(1)).closeConnection(null);
    }

    @Test
    public void testUpdateTicketPreparedStatementException() throws Exception {
        // Simuler une exception lors de la préparation de la requête
        when(preparedStatement.execute()).thenThrow(new RuntimeException("Exception in execute"));

        boolean result = ticketDAO.updateTicket(ticket);
        assertFalse(result);

        verify(dataBaseConfig, times(1)).closeConnection(connection);
    }


}
