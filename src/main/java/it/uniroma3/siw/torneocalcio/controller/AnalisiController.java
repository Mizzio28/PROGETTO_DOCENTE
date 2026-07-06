package it.uniroma3.siw.torneocalcio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class AnalisiController {

    public record ConfrontoLazyEager(
        String scenario,
        String serveIlDato,
        String lazy,
        String eager
    ) {}

    private static final List<ConfrontoLazyEager> CONFRONTO_LAZY_EAGER = List.of(
        new ConfrontoLazyEager(
            "Dettaglio torneo (squadre + giocatori richiesti)",
            "Sì",
            "14,283 ms/run · 16,00 query/run",
            "5,776 ms/run · 14,00 query/run"),
        new ConfrontoLazyEager(
            "Elenco tornei in homepage (squadre/giocatori non richiesti)",
            "No",
            "1,013 ms/run · 1,00 query/run",
            "4,428 ms/run · 11,00 query/run")
    );

    @GetMapping("/analisi")
    public String analisi(Model model) {
        model.addAttribute("confrontoLazyEager", CONFRONTO_LAZY_EAGER);
        return "analisi/index";
    }
}
