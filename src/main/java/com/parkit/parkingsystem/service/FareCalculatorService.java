package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        //Fix Léa : Changer le type de variable en long et modifier getHours(). en getTime().
        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();

        //Fix Léa : Changer le type de variable en double, puis convertir les millisecondes en heures.
        double duration = (outHour - inHour) /(1000.0 * 60 * 60);

        //Fix Léa : Ajout d'une condition de durée pour les 30 premières minutes gratuites.
        if (duration < 0.5) {
            ticket.setPrice(Fare.LESS_THAN_30_MINUTES);
            return;
        }

        //Si le user case rentre dans le if, on renvoie le prix à 0€. Sinon on rentre dans le switch.
        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                break;
            }
            case BIKE: {
                ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                break;
            }
            default: throw new IllegalArgumentException("Unknown Parking Type");
        }
    }
}