package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    public static void setUp() throws Exception {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    public void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    public static void tearDown() {
    }

    @Test
    public void testParkingACar() {
        //Condition du test
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        //Execution du test
        parkingService.processIncomingVehicle();
        //Condition de réussite du test "Le ticket s'enregistre dans la BDD"
        Ticket testSavedTicket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(testSavedTicket);
        assertEquals("ABCDEF", testSavedTicket.getVehicleRegNumber());
        //Condition de réussite du test "La place de parking met à jour sa disponibilité dans la BDD"
        ParkingSpot testParkingSpot = testSavedTicket.getParkingSpot();
        assertNotNull(testParkingSpot);
        assertFalse(testParkingSpot.isAvailable());
    }

    @Test
    public void testParkingLotExit() throws Exception {

        //Condition du test + "entrée du véhicule"
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        //Récup et mise à jour du ticket
        Ticket entryTicket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(entryTicket);
        entryTicket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticketDAO.updateTicket(entryTicket);

        //Execution du test
        parkingService.processExitingVehicle();

        //Mise à jour du ticket à la sortie
        Ticket exitTicket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(exitTicket);

        //Vérification que l'heure de sortie est mise à jour
        assertNotNull(exitTicket.getOutTime());
        assertTrue(exitTicket.getOutTime().after(exitTicket.getInTime()));

        //Vérification du calcul du prix
        assertTrue(exitTicket.getPrice() > 0);

        //Condition de réussite "Mise à jour dans la BDD de la sortie"
        ParkingSpot testParkingSpot = exitTicket.getParkingSpot();
        assertTrue(parkingSpotDAO.updateParking(testParkingSpot));
    }

}
