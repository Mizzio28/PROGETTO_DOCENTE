package it.uniroma3.siw.torneocalcio.controller.admin;

import it.uniroma3.siw.torneocalcio.model.Giocatore;
import it.uniroma3.siw.torneocalcio.service.GiocatoreService;
import it.uniroma3.siw.torneocalcio.service.SquadraService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminGiocatoreController {

    @Autowired private GiocatoreService giocatoreService;
    @Autowired private SquadraService squadraService;

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
}