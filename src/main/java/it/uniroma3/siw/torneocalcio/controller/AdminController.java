package it.uniroma3.siw.torneocalcio.controller;

import it.uniroma3.siw.torneocalcio.model.*;
import it.uniroma3.siw.torneocalcio.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private TorneoService torneoService;
    @Autowired private SquadraService squadraService;
    @Autowired private GiocatoreService giocatoreService;
    @Autowired private ArbitroService arbitroService;
    @Autowired private PartitaService partitaService;

    // ---- DASHBOARD ----
    @GetMapping("")
    public String dashboard(Model model) {
        model.addAttribute("tornei", torneoService.getAllTornei());
        model.addAttribute("squadre", squadraService.getAllSquadre());
        return "admin/index";
    }

    // ===== TORNEI =====
    @GetMapping("/tornei")
    public String listaTornei(Model model) {
        model.addAttribute("tornei", torneoService.getAllTornei());
        return "admin/tornei/lista";
    }

    @GetMapping("/tornei/nuovo")
    public String nuovoTorneoForm(Model model) {
        model.addAttribute("torneo", new Torneo());
        return "admin/tornei/form";
    }

    @PostMapping("/tornei/nuovo")
    public String nuovoTorneo(@Valid @ModelAttribute("torneo") Torneo torneo,
                              BindingResult result) {
        if (result.hasErrors()) return "admin/tornei/form";
        torneoService.saveTorneo(torneo);
        return "redirect:/admin/tornei";
    }

    @GetMapping("/tornei/{id}/modifica")
    public String modificaTorneoForm(@PathVariable Long id, Model model) {
        model.addAttribute("torneo", torneoService.getTorneo(id));
        return "admin/tornei/form";
    }

    @PostMapping("/tornei/{id}/modifica")
    public String modificaTorneo(@PathVariable Long id,
                                 @Valid @ModelAttribute("torneo") Torneo torneo,
                                 BindingResult result) {
        if (result.hasErrors()) return "admin/tornei/form";
        torneoService.updateTorneo(id, torneo);
        return "redirect:/admin/tornei";
    }

    // ===== SQUADRE =====
    @GetMapping("/squadre")
    public String listaSquadre(Model model) {
        model.addAttribute("squadre", squadraService.getAllSquadre());
        return "admin/squadre/lista";
    }

    @GetMapping("/squadre/nuova")
    public String nuovaSquadraForm(Model model) {
        model.addAttribute("squadra", new Squadra());
        return "admin/squadre/form";
    }

    @PostMapping("/squadre/nuova")
    public String nuovaSquadra(@Valid @ModelAttribute("squadra") Squadra squadra,
                               BindingResult result) {
        if (result.hasErrors()) return "admin/squadre/form";
        squadraService.saveSquadra(squadra);
        return "redirect:/admin/squadre";
    }

    @GetMapping("/squadre/{id}/modifica")
    public String modificaSquadraForm(@PathVariable Long id, Model model) {
        model.addAttribute("squadra", squadraService.getSquadra(id));
        return "admin/squadre/form";
    }

    @PostMapping("/squadre/{id}/modifica")
    public String modificaSquadra(@PathVariable Long id,
                                  @Valid @ModelAttribute("squadra") Squadra squadra,
                                  BindingResult result) {
        if (result.hasErrors()) return "admin/squadre/form";
        squadraService.updateSquadra(id, squadra);
        return "redirect:/admin/squadre";
    }

    @PostMapping("/squadre/{id}/elimina")
    public String eliminaSquadra(@PathVariable Long id) {
        squadraService.deleteSquadra(id);
        return "redirect:/admin/squadre";
    }

    @GetMapping("/squadre/{squadraId}/aggiungi-a-torneo")
    public String aggiungiSquadraATorneoForm(@PathVariable Long squadraId, Model model) {
        model.addAttribute("squadra", squadraService.getSquadra(squadraId));
        model.addAttribute("tornei", torneoService.getAllTornei());
        return "admin/squadre/aggiungi-torneo";
    }

    @PostMapping("/squadre/{squadraId}/aggiungi-a-torneo")
    public String aggiungiSquadraATorneo(@PathVariable Long squadraId,
                                         @RequestParam Long torneoId) {
        squadraService.aggiungiSquadraATorneo(squadraId, torneoId);
        return "redirect:/admin/squadre";
    }

    // ===== GIOCATORI =====
    @GetMapping("/squadre/{squadraId}/giocatori/nuovo")
    public String nuovoGiocatoreForm(@PathVariable Long squadraId, Model model) {
        model.addAttribute("giocatore", new Giocatore());
        model.addAttribute("squadra", squadraService.getSquadra(squadraId));
        return "admin/giocatori/form";
    }

    @PostMapping("/squadre/{squadraId}/giocatori/nuovo")
    public String nuovoGiocatore(@PathVariable Long squadraId,
                                  @Valid @ModelAttribute("giocatore") Giocatore giocatore,
                                  BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("squadra", squadraService.getSquadra(squadraId));
            return "admin/giocatori/form";
        }
        giocatoreService.saveGiocatore(giocatore, squadraId);
        return "redirect:/admin/squadre";
    }

    @GetMapping("/giocatori/{id}/modifica")
    public String modificaGiocatoreForm(@PathVariable Long id, Model model) {
        Giocatore g = giocatoreService.getGiocatore(id);
        model.addAttribute("giocatore", g);
        model.addAttribute("squadra", g.getSquadra());
        return "admin/giocatori/form";
    }

    @PostMapping("/giocatori/{id}/modifica")
    public String modificaGiocatore(@PathVariable Long id,
                                    @Valid @ModelAttribute("giocatore") Giocatore giocatore,
                                    BindingResult result) {
        if (result.hasErrors()) return "admin/giocatori/form";
        giocatoreService.updateGiocatore(id, giocatore);
        return "redirect:/admin/squadre";
    }

    // ===== PARTITE =====
    @GetMapping("/partite/nuova")
    public String nuovaPartitaForm(Model model) {
        model.addAttribute("partita", new Partita());
        model.addAttribute("tornei", torneoService.getAllTornei());
        model.addAttribute("squadre", squadraService.getAllSquadre());
        model.addAttribute("arbitri", arbitroService.getAllArbitri());
        return "admin/partite/form";
    }

    @PostMapping("/partite/nuova")
    public String nuovaPartita(@ModelAttribute("partita") Partita partita,
                               @RequestParam Long torneoId,
                               @RequestParam Long squadraHomeId,
                               @RequestParam Long squadraAwayId,
                               @RequestParam Long arbitroId) {
        partitaService.registraPartita(partita, torneoId, squadraHomeId, squadraAwayId, arbitroId);
        return "redirect:/admin";
    }

    @GetMapping("/partite/{id}/risultato")
    public String risultatoForm(@PathVariable Long id, Model model) {
        model.addAttribute("partita", partitaService.getPartita(id));
        return "admin/partite/risultato";
    }

    @PostMapping("/partite/{id}/risultato")
    public String inserisciRisultato(@PathVariable Long id,
                                     @RequestParam Integer goalsHome,
                                     @RequestParam Integer goalsAway) {
        partitaService.inserisciRisultato(id, goalsHome, goalsAway);
        return "redirect:/admin";
    }

    @PostMapping("/partite/{id}/elimina")
    public String eliminaPartita(@PathVariable Long id) {
        partitaService.deletePartita(id);
        return "redirect:/admin";
    }

    // ===== ARBITRI =====
    @GetMapping("/arbitri/nuovo")
    public String nuovoArbitroForm(Model model) {
        model.addAttribute("arbitro", new Arbitro());
        return "admin/arbitri/form";
    }

    @PostMapping("/arbitri/nuovo")
    public String nuovoArbitro(@Valid @ModelAttribute("arbitro") Arbitro arbitro,
                               BindingResult result) {
        if (result.hasErrors()) return "admin/arbitri/form";
        arbitroService.saveArbitro(arbitro);
        return "redirect:/admin";
    }
}
