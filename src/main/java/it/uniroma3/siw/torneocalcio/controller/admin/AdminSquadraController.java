package it.uniroma3.siw.torneocalcio.controller.admin;

import it.uniroma3.siw.torneocalcio.model.Squadra;
import it.uniroma3.siw.torneocalcio.service.SquadraService;
import it.uniroma3.siw.torneocalcio.service.TorneoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/squadre")
public class AdminSquadraController {

    @Autowired private SquadraService squadraService;
    @Autowired private TorneoService torneoService;

    @GetMapping("")
    public String listaSquadre(Model model) {
        model.addAttribute("squadre", squadraService.getAllSquadre());
        return "admin/squadre/lista";
    }

    @GetMapping("/nuova")
    public String nuovaSquadraForm(Model model) {
        model.addAttribute("squadra", new Squadra());
        return "admin/squadre/form";
    }

    @PostMapping("/nuova")
    public String nuovaSquadra(@Valid @ModelAttribute("squadra") Squadra squadra,
                               BindingResult result) {
        if (result.hasErrors()) return "admin/squadre/form";
        squadraService.saveSquadra(squadra);
        return "redirect:/admin/squadre";
    }

    @GetMapping("/{id}/modifica")
    public String modificaSquadraForm(@PathVariable Long id, Model model) {
        model.addAttribute("squadra", squadraService.getSquadra(id));
        return "admin/squadre/form";
    }

    @PostMapping("/{id}/modifica")
    public String modificaSquadra(@PathVariable Long id,
                                  @Valid @ModelAttribute("squadra") Squadra squadra,
                                  BindingResult result) {
        if (result.hasErrors()) return "admin/squadre/form";
        squadraService.updateSquadra(id, squadra);
        return "redirect:/admin/squadre";
    }

    @PostMapping("/{id}/elimina")
    public String eliminaSquadra(@PathVariable Long id) {
        squadraService.deleteSquadra(id);
        return "redirect:/admin/squadre";
    }

    @GetMapping("/{squadraId}/aggiungi-a-torneo")
    public String aggiungiSquadraATorneoForm(@PathVariable Long squadraId, Model model) {
        model.addAttribute("squadra", squadraService.getSquadra(squadraId));
        model.addAttribute("tornei", torneoService.getAllTornei());
        return "admin/squadre/aggiungi-torneo";
    }

    @PostMapping("/{squadraId}/aggiungi-a-torneo")
    public String aggiungiSquadraATorneo(@PathVariable Long squadraId,
                                         @RequestParam Long torneoId) {
        squadraService.aggiungiSquadraATorneo(squadraId, torneoId);
        return "redirect:/admin/squadre";
    }
}