package it.uniroma3.siw.torneocalcio.controller.dto;

import it.uniroma3.siw.torneocalcio.model.Commento;

import java.time.LocalDateTime;

public record CommentoDTO(
    Long id,
    String testo,
    LocalDateTime dataCommento,
    String autoreNome,
    String autoreCognome,
    String autoreEmail
) {
    public static CommentoDTO from(Commento c) {
        return new CommentoDTO(
            c.getId(),
            c.getTesto(),
            c.getDataCommento(),
            c.getAutore().getName(),
            c.getAutore().getSurname(),
            c.getAutore().getEmail()
        );
    }
}