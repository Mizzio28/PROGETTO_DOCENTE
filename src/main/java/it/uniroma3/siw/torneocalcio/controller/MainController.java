package it.uniroma3.siw.torneocalcio.controller;

import it.uniroma3.siw.torneocalcio.model.User;
import it.uniroma3.siw.torneocalcio.service.TorneoService;
import it.uniroma3.siw.torneocalcio.service.SquadraService;
import it.uniroma3.siw.torneocalcio.service.PartitaService;
import it.uniroma3.siw.torneocalcio.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class MainController {

    @Autowired private TorneoService torneoService;
    @Autowired private SquadraService squadraService;
    @Autowired private PartitaService partitaService;
    @Autowired private UserService userService;

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
        model.addAttribute("classifica", torneoService.calcolaClassifica(id));
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal()))) {
            User user = userService.getUserByUsername(authentication.getName());
            if (user != null) {
                model.addAttribute("currentUserEmail", user.getEmail());
            }
        }
        return "partite/dettaglio";
    }
}
