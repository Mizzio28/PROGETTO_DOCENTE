package it.uniroma3.siw.torneocalcio.controller.admin;

import it.uniroma3.siw.torneocalcio.model.Arbitro;
import it.uniroma3.siw.torneocalcio.service.ArbitroService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/arbitri")
public class AdminArbitroController {

    @Autowired private ArbitroService arbitroService;

    @GetMapping("")
    public String listaArbitri(Model model) {
        model.addAttribute("arbitri", arbitroService.getAllArbitri());
        return "admin/arbitri/lista";
    }

    @GetMapping("/nuovo")
    public String nuovoArbitroForm(Model model) {
        model.addAttribute("arbitro", new Arbitro());
        return "admin/arbitri/form";
    }

    @PostMapping("/nuovo")
    public String nuovoArbitro(@Valid @ModelAttribute("arbitro") Arbitro arbitro,
                               BindingResult result) {
        if (result.hasErrors()) return "admin/arbitri/form";
        arbitroService.saveArbitro(arbitro);
        return "redirect:/admin/arbitri";
    }

    @GetMapping("/{id}/modifica")
    public String modificaArbitroForm(@PathVariable Long id, Model model) {
        model.addAttribute("arbitro", arbitroService.getArbitro(id));
        return "admin/arbitri/form";
    }

    @PostMapping("/{id}/modifica")
    public String modificaArbitro(@PathVariable Long id,
                                  @Valid @ModelAttribute("arbitro") Arbitro arbitro,
                                  BindingResult result) {
        if (result.hasErrors()) return "admin/arbitri/form";
        arbitroService.updateArbitro(id, arbitro);
        return "redirect:/admin/arbitri";
    }

    @PostMapping("/{id}/elimina")
    public String eliminaArbitro(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            arbitroService.deleteArbitro(id);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/arbitri";
    }
}