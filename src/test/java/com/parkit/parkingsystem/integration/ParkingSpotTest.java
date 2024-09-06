package com.parkit.parkingsystem.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;

public class ParkingSpotTest {

    private ParkingSpot parkingSpot;

    @BeforeEach
    public void setUp() {
        parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
    }

    @Test
    public void testConstructor() {
        // Teste si le constructeur de ParkingSpot initialise correctement les attributs de l'objet.
        assertEquals(1, parkingSpot.getId());
        assertEquals(ParkingType.CAR, parkingSpot.getParkingType());
        assertTrue(parkingSpot.isAvailable());
    }

    @Test
    public void testGetAndSetId() {
        // Teste si les méthodes getId() et setId() fonctionnent correctement.
        parkingSpot.setId(2);
        assertEquals(2, parkingSpot.getId());
    }

    @Test
    public void testGetAndSetParkingType() {
        // Teste si les méthodes getParkingType() et setParkingType() fonctionnent correctement.
        parkingSpot.setParkingType(ParkingType.BIKE);
        assertEquals(ParkingType.BIKE, parkingSpot.getParkingType());
    }

    @Test
    public void testGetAndSetAvailability() {
        // Teste si les méthodes isAvailable() et setAvailable() fonctionnent correctement.
        parkingSpot.setAvailable(false);
        assertFalse(parkingSpot.isAvailable());

        parkingSpot.setAvailable(true);
        assertTrue(parkingSpot.isAvailable());
    }

    @Test
    public void testEqualsSameObject() {
        // Teste si la méthode equals() renvoie true pour le même objet.
        assertTrue(parkingSpot.equals(parkingSpot));
    }

    @Test
    public void testEqualsSameId() {
        // Teste si la méthode equals() renvoie true pour deux objets ayant le même ID.
        ParkingSpot anotherSpot = new ParkingSpot(1, ParkingType.CAR, true);
        assertTrue(parkingSpot.equals(anotherSpot));
    }

    @Test
    public void testNotEqualsDifferentId() {
        // Teste si la méthode equals() renvoie false pour deux objets ayant des IDs différents.
        ParkingSpot anotherSpot = new ParkingSpot(2, ParkingType.CAR, true);
        assertFalse(parkingSpot.equals(anotherSpot));
    }

    @Test
    public void testNotEqualsNull() {
        // Teste si la méthode equals() renvoie false lorsqu'elle compare l'objet avec null.
        assertFalse(parkingSpot.equals(null));
    }

    @Test
    public void testNotEqualsDifferentClass() {
        // Teste si la méthode equals() renvoie false lorsqu'elle compare l'objet avec une instance d'une classe différente.
        assertFalse(parkingSpot.equals("Some String"));
    }

    @Test
    public void testHashCode() {
        // Teste si la méthode hashCode() renvoie le même code pour deux objets égaux.
        ParkingSpot anotherSpot = new ParkingSpot(1, ParkingType.CAR, true);
        assertEquals(parkingSpot.hashCode(), anotherSpot.hashCode());
    }

    @Test
    public void testHashCodeDifferentId() {
        // Teste si la méthode hashCode() renvoie des codes différents pour des objets ayant des IDs différents.
        ParkingSpot anotherSpot = new ParkingSpot(2, ParkingType.CAR, true);
        assertNotEquals(parkingSpot.hashCode(), anotherSpot.hashCode());
    }
}