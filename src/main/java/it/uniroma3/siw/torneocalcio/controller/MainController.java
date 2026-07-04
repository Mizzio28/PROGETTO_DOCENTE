package it.uniroma3.siw.torneocalcio.controller;

import it.uniroma3.siw.torneocalcio.service.TorneoService;
import it.uniroma3.siw.torneocalcio.service.SquadraService;
import it.uniroma3.siw.torneocalcio.service.PartitaService;
import it.uniroma3.siw.torneocalcio.service.CommentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class MainController {

    @Autowired private TorneoService torneoService;
    @Autowired private SquadraService squadraService;
    @Autowired private PartitaService partitaService;
    @Autowired private CommentoService commentoService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("tornei", torneoService.getAllTornei());
        return "index";
    }

    // ---- TORNEI ----
    @GetMapping("/tornei")
    public String listaTornei(Model model) {
        model.addAttribute("tornei", torneoService.getAllTornei());
        return "tornei/lista";
    }

    @GetMapping("/tornei/{id}")
    public String dettaglioTorneo(@PathVariable Long id, Model model) {
        model.addAttribute("torneo", torneoService.getTorneoConDettagli(id));
        model.addAttribute("partite", partitaService.getPartiteByTorneo(id));
        return "tornei/dettaglio";
    }

    @GetMapping("/tornei/{id}/classifica")
    public String classificaTorneo(@PathVariable Long id, Model model) {
        model.addAttribute("torneo", torneoService.getTorneoConDettagli(id));
        model.addAttribute("partite", partitaService.getPartiteByTorneo(id));
        return "tornei/classifica";
    }

    // ---- SQUADRE ----
    @GetMapping("/squadre/{id}")
    public String dettaglioSquadra(@PathVariable Long id, Model model) {
        model.addAttribute("squadra", squadraService.getSquadraConGiocatori(id));
        return "squadre/dettaglio";
    }

    // ---- PARTITE ----
    @GetMapping("/partite/{id}")
    public String dettaglioPartita(@PathVariable Long id, Model model) {
        model.addAttribute("partita", partitaService.getPartitaConCommenti(id));
        return "partite/dettaglio";
    }

    // ---- COMMENTI (utenti registrati) ----
    @PostMapping("/partite/{partitaId}/commenti")
    public String addCommento(@PathVariable Long partitaId,
                              @RequestParam String testo) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        commentoService.addCommento(testo, partitaId, username);
        return "redirect:/partite/" + partitaId;
    }

    @GetMapping("/commenti/{id}/modifica")
    public String showModificaCommento(@PathVariable Long id, Model model) {
        model.addAttribute("commento", commentoService.getCommento(id));
        return "partite/modifica-commento";
    }

    @PostMapping("/commenti/{id}/modifica")
    public String modificaCommento(@PathVariable Long id,
                                   @RequestParam String testo,
                                   @RequestParam Long partitaId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        commentoService.updateCommento(id, testo, username);
        return "redirect:/partite/" + partitaId;
    }
}
