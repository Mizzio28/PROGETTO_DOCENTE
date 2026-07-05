package it.uniroma3.siw.torneocalcio.controller;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.NoSuchElementException;

/**
 * Gestisce le eccezioni sollevate dalle pagine Thymeleaf (MVC), mostrando una
 * pagina di errore invece della whitelabel error page di default. Le rotte
 * REST (package controller.rest) hanno il proprio RestExceptionHandler, con
 * priorità più alta, che risponde in JSON.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NoSuchElementException ex, Model model) {
        model.addAttribute("messaggio", "La risorsa richiesta non è stata trovata.");
        return "error/not-found";
    }

    /** Rotta/risorsa statica inesistente: deve restituire 404, non finire nel catch-all generico. */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoResourceFound(NoResourceFoundException ex, Model model) {
        model.addAttribute("messaggio", "Pagina non trovata.");
        return "error/not-found";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneric(Exception ex, Model model) {
        model.addAttribute("messaggio", "Si è verificato un errore imprevisto.");
        return "error/not-found";
    }
}