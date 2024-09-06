package com.parkit.parkingsystem.integration;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

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
        // Teste si le processus de parking d'une voiture enregistre correctement un ticket et met à jour la disponibilité de la place de parking dans la base de données.
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        // Entrée du véhicule
        parkingService.processIncomingVehicle();

        // Vérifie que le ticket est bien enregistré dans la base de données
        Ticket testSavedTicket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(testSavedTicket);
        assertEquals("ABCDEF", testSavedTicket.getVehicleRegNumber());

        // Vérifie que la place de parking est bien marquée comme indisponible dans la base de données
        ParkingSpot testParkingSpot = testSavedTicket.getParkingSpot();
        assertNotNull(testParkingSpot);
        assertFalse(testParkingSpot.isAvailable());
    }

    @Test
    public void testParkingLotExit() throws Exception {
        // Teste si le processus de sortie de parking met correctement à jour l'heure de sortie et calcule le tarif pour 1 heure de stationnement.

        // Condition du test + utilisation d'un spy pour simuler le comportement de TicketDAO
        TicketDAO ticketDAOspy = spy(ticketDAO);
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAOspy);

        //Entrée du véhicule
        parkingService.processIncomingVehicle();

        // Récupération et mise à jour du ticket en base
        Ticket entryTicket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(entryTicket);

        // Espion basé sur le vrai ticket pour forcer l'heure de sortie à 1h plus tard
        Ticket entryTicketSpy = spy(entryTicket);
        Date currentTime = new Date();
        Instant currentInstant = currentTime.toInstant();
        Instant outInstant = currentInstant.plus(Duration.ofHours(1));
        Date outTime = Date.from(outInstant);

        // Quand la méthode de calcul aura besoin de l'heure de sortie, on renvoie la modifiée
        when(entryTicketSpy.getOutTime()).thenReturn(outTime);

        // Forcer le retour du ticket avec le comportement modifié pour tester le calcul du prix
        when(ticketDAOspy.getTicket("ABCDEF")).thenReturn(entryTicketSpy);

        //Sortie du véhicule
        parkingService.processExitingVehicle();

        // Mise à jour du ticket à la sortie
        Ticket exitTicket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(exitTicket);

        // Vérification que l'heure de sortie est mise à jour et que le prix est le bon
        assertNotNull(exitTicket.getOutTime());
        assertEquals(1.5, entryTicketSpy.getPrice(), 0.1);
    }

    @Test
    public void testParkingLotExitRecurringUser() throws Exception {
        // Teste si un utilisateur récurrent est correctement géré avec deux sessions de parking, en vérifiant les heures de sortie et les tarifs pour chaque session.

        // Condition du test + utilisation d'un spy pour simuler le comportement de TicketDAO
        TicketDAO ticketDAOspy = spy(ticketDAO);
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAOspy);

        // Première entrée du véhicule
        parkingService.processIncomingVehicle();
        Ticket firstTicket = ticketDAO.getTicket("ABCDEF");

        // Espion basé sur le vrai ticket pour forcer l'heure de sortie à 25 min plus tard
        Ticket firstTicketSpy = spy(firstTicket);
        Date currentTime = new Date();
        Instant currentInstant = currentTime.toInstant();
        Instant outInstant = currentInstant.plus(Duration.ofMinutes(25));
        Date outTime = Date.from(outInstant);

        // Quand la méthode de calcul aura besoin de l'heure de sortie, on renvoie la modifiée
        when(firstTicketSpy.getOutTime()).thenReturn(outTime);
        when(ticketDAOspy.getTicket("ABCDEF")).thenReturn(firstTicketSpy);

        // Première sortie du véhicule
        parkingService.processExitingVehicle();

        // Deuxième entrée du véhicule
        parkingService.processIncomingVehicle();

        // On récupère le ticket pour modifier la date de sortie
        Ticket secondTicket = ticketDAO.getTicket("ABCDEF");

        // Espion basé sur le vrai ticket pour forcer l'heure de sortie à 1h plus tard
        Ticket secondTicketSpy = spy(secondTicket);
        currentTime = new Date();
        currentInstant = currentTime.toInstant();
        outInstant = currentInstant.plus(Duration.ofHours(1));
        outTime = Date.from(outInstant);

        // Quand la méthode de calcul aura besoin de l'heure de sortie, on renvoie la modifiée
        when(secondTicketSpy.getOutTime()).thenReturn(outTime);

        // Forcer le retour du ticket avec le comportement modifié pour tester le calcul du prix
        when(ticketDAOspy.getTicket("ABCDEF")).thenReturn(secondTicketSpy);

        //Deuxième sortie du véhicule
        parkingService.processExitingVehicle();

        // Vérifie que le véhicule a deux tickets dans la base de données
        int count = ticketDAO.countTicketByVehicleRegNumber("ABCDEF");
        assertEquals(count, 2);

        // Vérifie les prix des différents passages
        assertEquals(firstTicketSpy.getPrice(), 0.0);
        assertEquals(1.425, secondTicketSpy.getPrice(), 0.1);
    }

}