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

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static junit.framework.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @Mock
    private static Ticket ticket;

    @BeforeAll
    public static void setUp() throws Exception {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
        ticket = new Ticket();
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
        // Condition du test + "entrée du véhicule"
        TicketDAO ticketDAOspy = spy(ticketDAO);
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAOspy);


        parkingService.processIncomingVehicle();

        // Récup et mise à jour du ticket en base
        Ticket entryTicket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(entryTicket);

        // Espion basé sur le vrai ticket pour forcer l'heure de sortie a 1h plus tard
        Ticket entryTicketSpy = spy(entryTicket);
        Date currentTime = new Date();
        Instant currentInstant = currentTime.toInstant();
        Instant outInstant = currentInstant.plus
                (Duration.ofHours(1));
        Date outTime = Date.from(outInstant);
        // Quand la méthode de calcul aura besoin de l'heure de sortie, on renvoie la
        // modifiée
        when(entryTicketSpy.getOutTime()).thenReturn(outTime);

        // Forcer le retour du ticket avec le comportement modifié pour tester le calcul
        // du prix
        when(ticketDAOspy.getTicket("ABCDEF")).thenReturn(entryTicketSpy);

        parkingService.processExitingVehicle();

        // Mise à jour du ticket à la sortie
        Ticket exitTicket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(exitTicket);

        // Vérification que l'heure de sortie est mise à jour
        assertNotNull(exitTicket.getOutTime());
        assertEquals(1.5,entryTicketSpy.getPrice(),0.1);
    }

    @Test
    public void testParkingLotExitRecurringUser() throws Exception {
        TicketDAO ticketDAOspy = spy(ticketDAO);

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAOspy);
        // On fait rentrer le vehicule une premiere fois
        parkingService.processIncomingVehicle();

        Ticket firstTicket = ticketDAO.getTicket("ABCDEF");
        // Espion basé sur le vrai ticket pour forcer l'heure de sortie à 25 min plus tard
        Ticket firstTicketSpy = spy(firstTicket);
        Date currentTime = new Date();
        Instant currentInstant = currentTime.toInstant();
        Instant outInstant = currentInstant.plus
                (Duration.ofMinutes(25));
        Date outTime = Date.from(outInstant);
        // Quand la méthode de calcul aura besoin de l'heure de sortie, on renvoie la
        // modifiée
        when(firstTicketSpy.getOutTime()).thenReturn(outTime);
        when(ticketDAOspy.getTicket("ABCDEF")).thenReturn(firstTicketSpy);
        parkingService.processExitingVehicle();

        // Puis on le refait rentrer
        parkingService.processIncomingVehicle();

        // On récupère le ticket pour modifier la date de sortie
        Ticket secondTicket = ticketDAO.getTicket("ABCDEF");
        // Espion basé sur le vrai ticket pour forcer l'heure de sortie a 1h plus tard
        Ticket secondTicketSpy = spy(secondTicket);
        currentTime = new Date();
        currentInstant = currentTime.toInstant();
        outInstant = currentInstant.plus
                        (Duration.ofHours(1));
        outTime = Date.from(outInstant);
        // Quand la méthode de calcul aura besoin de l'heure de sortie, on renvoie la
        // modifiée
        when(secondTicketSpy.getOutTime()).thenReturn(outTime);

        // Forcer le retour du ticket avec le comportement modifié pour tester le calcul
        // du prix
        when(ticketDAOspy.getTicket("ABCDEF")).thenReturn(secondTicketSpy);

        parkingService.processExitingVehicle();

        // On test que le véhicule est bien passé 2x
        int count = ticketDAO.countTicketByVehicleRegNumber("ABCDEF");
        assertEquals(count, 2);
        // On test les prix des différents passages
        assertEquals(firstTicketSpy.getPrice(), 0.0);
        assertEquals(1.5,secondTicketSpy.getPrice(),0.1);
    }


}
