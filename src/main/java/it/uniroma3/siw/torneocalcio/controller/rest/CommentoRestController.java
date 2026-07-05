package it.uniroma3.siw.torneocalcio.controller.rest;

import it.uniroma3.siw.torneocalcio.dto.CommentoDTO;
import it.uniroma3.siw.torneocalcio.dto.CommentoRequest;
import it.uniroma3.siw.torneocalcio.model.Commento;
import it.uniroma3.siw.torneocalcio.service.CommentoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/partite/{partitaId}/commenti")
public class CommentoRestController {

    @Autowired private CommentoService commentoService;

    @GetMapping
    public List<CommentoDTO> getCommenti(@PathVariable Long partitaId) {
        return commentoService.getCommentiByPartita(partitaId).stream()
            .map(CommentoDTO::from)
            .toList();
    }

    @PostMapping
    public ResponseEntity<CommentoDTO> creaCommento(@PathVariable Long partitaId,
                                                     @Valid @RequestBody CommentoRequest request,
                                                     Authentication authentication) {
        Commento commento = commentoService.addCommento(request.testo(), partitaId, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(CommentoDTO.from(commento));
    }

    @PutMapping("/{id}")
    public CommentoDTO modificaCommento(@PathVariable Long partitaId,
                                        @PathVariable Long id,
                                        @Valid @RequestBody CommentoRequest request,
                                        Authentication authentication) {
        Commento commento = commentoService.updateCommento(id, request.testo(), authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Non puoi modificare questo commento"));
        return CommentoDTO.from(commento);
    }
}