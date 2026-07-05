package it.uniroma3.siw.torneocalcio.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentoRequest(
    @NotBlank(message = "Il testo non può essere vuoto")
    @Size(max = 1000, message = "Il testo non può superare i 1000 caratteri")
    String testo
) {}