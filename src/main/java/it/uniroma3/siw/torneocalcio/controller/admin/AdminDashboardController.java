package it.uniroma3.siw.torneocalcio.controller.admin;

import it.uniroma3.siw.torneocalcio.service.ArbitroService;
import it.uniroma3.siw.torneocalcio.service.PartitaService;
import it.uniroma3.siw.torneocalcio.service.SquadraService;
import it.uniroma3.siw.torneocalcio.service.TorneoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired private TorneoService torneoService;
    @Autowired private SquadraService squadraService;
    @Autowired private ArbitroService arbitroService;
    @Autowired private PartitaService partitaService;

    @GetMapping("")
    public String dashboard(Model model) {
        model.addAttribute("tornei", torneoService.getAllTornei());
        model.addAttribute("squadre", squadraService.getAllSquadre());
        model.addAttribute("arbitri", arbitroService.getAllArbitri());
        model.addAttribute("partite", partitaService.getAllPartite());
        return "admin/index";
    }
}