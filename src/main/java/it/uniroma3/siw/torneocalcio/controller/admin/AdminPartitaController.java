package it.uniroma3.siw.torneocalcio.controller.admin;

import it.uniroma3.siw.torneocalcio.model.Partita;
import it.uniroma3.siw.torneocalcio.service.ArbitroService;
import it.uniroma3.siw.torneocalcio.service.PartitaService;
import it.uniroma3.siw.torneocalcio.service.SquadraService;
import it.uniroma3.siw.torneocalcio.service.TorneoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/partite")
public class AdminPartitaController {

    @Autowired private PartitaService partitaService;
    @Autowired private TorneoService torneoService;
    @Autowired private SquadraService squadraService;
    @Autowired private ArbitroService arbitroService;

    @GetMapping("")
    public String listaPartite(@RequestParam(required = false) Long torneoId, Model model) {
        List<Partita> partite = (torneoId != null)
            ? partitaService.getPartiteByTorneoDesc(torneoId)
            : partitaService.getAllPartite();
        model.addAttribute("partite", partite);
        model.addAttribute("tornei", torneoService.getAllTornei());
        model.addAttribute("torneoId", torneoId);
        return "admin/partite/lista";
    }

    @GetMapping("/nuova")
    public String nuovaPartitaForm(Model model) {
        model.addAttribute("partita", new Partita());
        model.addAttribute("tornei", torneoService.getAllTornei());
        model.addAttribute("squadre", squadraService.getAllSquadre());
        model.addAttribute("arbitri", arbitroService.getAllArbitri());
        return "admin/partite/form";
    }

    @PostMapping("/nuova")
    public String nuovaPartita(@ModelAttribute("partita") Partita partita,
                               @RequestParam Long torneoId,
                               @RequestParam Long squadraHomeId,
                               @RequestParam Long squadraAwayId,
                               @RequestParam Long arbitroId) {
        partitaService.registraPartita(partita, torneoId, squadraHomeId, squadraAwayId, arbitroId);
        return "redirect:/admin/partite";
    }

    @GetMapping("/{id}/risultato")
    public String risultatoForm(@PathVariable Long id, Model model) {
        model.addAttribute("partita", partitaService.getPartita(id));
        return "admin/partite/risultato";
    }

    @PostMapping("/{id}/risultato")
    public String inserisciRisultato(@PathVariable Long id,
                                     @RequestParam Integer goalsHome,
                                     @RequestParam Integer goalsAway) {
        partitaService.inserisciRisultato(id, goalsHome, goalsAway);
        return "redirect:/admin/partite";
    }

    @PostMapping("/{id}/elimina")
    public String eliminaPartita(@PathVariable Long id) {
        partitaService.deletePartita(id);
        return "redirect:/admin/partite";
    }
}