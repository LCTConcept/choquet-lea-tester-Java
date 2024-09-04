package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ParkingSpotTest {

    private ParkingSpot parkingSpot;

    @Before
    public void setUp() {
        parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
    }

    @Test
    public void testConstructor() {
        assertEquals(1, parkingSpot.getId());
        assertEquals(ParkingType.CAR, parkingSpot.getParkingType());
        assertTrue(parkingSpot.isAvailable());
    }

    @Test
    public void testGetAndSetId() {
        parkingSpot.setId(2);
        assertEquals(2, parkingSpot.getId());
    }

    @Test
    public void testGetAndSetParkingType() {
        parkingSpot.setParkingType(ParkingType.BIKE);
        assertEquals(ParkingType.BIKE, parkingSpot.getParkingType());
    }

    @Test
    public void testGetAndSetAvailability() {
        parkingSpot.setAvailable(false);
        assertFalse(parkingSpot.isAvailable());

        parkingSpot.setAvailable(true);
        assertTrue(parkingSpot.isAvailable());
    }

    @Test
    public void testEqualsSameObject() {
        assertTrue(parkingSpot.equals(parkingSpot));
    }

    @Test
    public void testEqualsSameId() {
        ParkingSpot anotherSpot = new ParkingSpot(1, ParkingType.CAR, true);
        assertTrue(parkingSpot.equals(anotherSpot));
    }

    @Test
    public void testNotEqualsDifferentId() {
        ParkingSpot anotherSpot = new ParkingSpot(2, ParkingType.CAR, true);
        assertFalse(parkingSpot.equals(anotherSpot));
    }

    @Test
    public void testNotEqualsNull() {
        assertFalse(parkingSpot.equals(null));
    }

    @Test
    public void testNotEqualsDifferentClass() {
        assertFalse(parkingSpot.equals("Some String"));
    }

    @Test
    public void testHashCode() {
        ParkingSpot anotherSpot = new ParkingSpot(1, ParkingType.CAR, true);
        assertEquals(parkingSpot.hashCode(), anotherSpot.hashCode());
    }

    @Test
    public void testHashCodeDifferentId() {
        ParkingSpot anotherSpot = new ParkingSpot(2, ParkingType.CAR, true);
        assertNotEquals(parkingSpot.hashCode(), anotherSpot.hashCode());
    }
}
