package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    public void setUpPerTest() {
        try {
            //Configuration communes à tous les tests à venir.

            lenient().when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");

            //Lenient permet de ne pas avoir d'erreurs si les conditions ne sont pas utilisées dans un test.
            lenient().when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            lenient().when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
            lenient().when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
            lenient().when(ticketDAO.countTicketByVehicleRegNumber("ABCDEF")).thenReturn(1);
            lenient().when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);


            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void processExitingVehicleTest() {
        //Condition du test = Setup commun.
        //Execution de la méthode à tester
        parkingService.processExitingVehicle();
        //Condition de réussite du test
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).countTicketByVehicleRegNumber("ABCDEF");
    }

    @Test
    public void testProcessIncomingVehicle() throws Exception {

        // Condition du test
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);  // La place de parking est actuellement disponible
        when(inputReaderUtil.readSelection()).thenReturn(1);  // Simuler la sélection du type de véhicule (voiture)
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);  // Simuler un emplacement disponible

        // Exécution de la méthode à tester
        parkingService.processIncomingVehicle();

        // Condition de réussite du test
        verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(any(ParkingType.class));
        verify(inputReaderUtil, Mockito.times(1)).readSelection();
        verify(inputReaderUtil, Mockito.times(1)).readVehicleRegistrationNumber();
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    }

    @Test
    public void processExitingVehicleTestUnableUpdate() {
        //Condition du test
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
        //Execution du test
        parkingService.processExitingVehicle();
        //Condition de réussite du test
        verify(parkingSpotDAO, never()).updateParking(parkingSpot);
    }

    @Test
    public void testGetNextParkingNumberIfAvailable() {
        //Condition du test
        ParkingSpot ParkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(ParkingSpot.getId());
        //Execution du test
        ParkingSpot resultParkingSpot = parkingService.getNextParkingNumberIfAvailable();
        // Condition de réussite du test
        assertNotNull(resultParkingSpot);
        assertEquals(1, resultParkingSpot.getId());
        assertTrue(resultParkingSpot.isAvailable());
        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);

    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
        //Condition du test
        ParkingSpot ParkingSpot = new ParkingSpot(-1, ParkingType.CAR, false);
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(ParkingSpot.getId());
        //Execution du test
        ParkingSpot resultParkingSpot = parkingService.getNextParkingNumberIfAvailable();
        // Condition de réussite du test
        assertNull(resultParkingSpot);
        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
        //Condition du test
        when(inputReaderUtil.readSelection()).thenReturn(3);
        //Execution du test
        ParkingSpot resultParkingSpot = parkingService.getNextParkingNumberIfAvailable();
        // Condition de réussite du test
        assertNull(resultParkingSpot);
        verify(parkingSpotDAO,never()).getNextAvailableSlot(any());
    }

    @Test
    public void testGetNextParkingNumberIfAvailableNoParkingAvailable(){
        //Condition du test
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(0);
        //Execution du test
        ParkingSpot resultParkingSpot = parkingService.getNextParkingNumberIfAvailable();
        // Condition de réussite du test
        assertNull(resultParkingSpot);
        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);
    }

}






