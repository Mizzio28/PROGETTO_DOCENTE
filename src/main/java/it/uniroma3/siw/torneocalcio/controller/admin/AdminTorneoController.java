package it.uniroma3.siw.torneocalcio.controller.admin;

import it.uniroma3.siw.torneocalcio.model.Torneo;
import it.uniroma3.siw.torneocalcio.service.TorneoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/tornei")
public class AdminTorneoController {

    @Autowired private TorneoService torneoService;

    @GetMapping("")
    public String listaTornei(Model model) {
        model.addAttribute("tornei", torneoService.getAllTornei());
        return "admin/tornei/lista";
    }

    @GetMapping("/nuovo")
    public String nuovoTorneoForm(Model model) {
        model.addAttribute("torneo", new Torneo());
        return "admin/tornei/form";
    }

    @PostMapping("/nuovo")
    public String nuovoTorneo(@Valid @ModelAttribute("torneo") Torneo torneo,
                              BindingResult result) {
        if (result.hasErrors()) return "admin/tornei/form";
        torneoService.saveTorneo(torneo);
        return "redirect:/admin/tornei";
    }

    @GetMapping("/{id}/modifica")
    public String modificaTorneoForm(@PathVariable Long id, Model model) {
        model.addAttribute("torneo", torneoService.getTorneo(id));
        return "admin/tornei/form";
    }

    @PostMapping("/{id}/modifica")
    public String modificaTorneo(@PathVariable Long id,
                                 @Valid @ModelAttribute("torneo") Torneo torneo,
                                 BindingResult result) {
        if (result.hasErrors()) return "admin/tornei/form";
        torneoService.updateTorneo(id, torneo);
        return "redirect:/admin/tornei";
    }

    @PostMapping("/{id}/elimina")
    public String eliminaTorneo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            torneoService.deleteTorneo(id);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tornei";
    }
}