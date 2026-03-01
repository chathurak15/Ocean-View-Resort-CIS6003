package com.oceanview.controller;

import com.oceanview.dto.Reservation.CreateReservationDTO;
import com.oceanview.validation.ValidatorContext;
import com.oceanview.validation.reservation.CreateReservationValidationStrategy;

import javax.servlet.annotation.WebServlet;

@WebServlet(name = "ReservationServlet", urlPatterns = {"/api/reservations/*"})
public class ReservationController extends BaseServlet{
    private final ValidatorContext validatorContext = new ValidatorContext();

    @Override
    public void init() {
        validatorContext.register(CreateReservationDTO.class, new CreateReservationValidationStrategy());
    }
}
