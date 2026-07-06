package it.uniroma3.siw.torneocalcio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class AnalisiController {

    public record RisultatoStrategia(
        String nome,
        String descrizione,
        String tempoPerRun,
        String queryPerRun,
        int queryTotali,
        int tempoBarPercent,
        int queryBarPercent
    ) {}

    public record DimostrazioneEccezione(String scenario, String messaggio) {}

    private static final List<RisultatoStrategia> RISULTATI = List.of(
        new RisultatoStrategia(
            "A — LAZY (default, N+1)",
            "Ogni squadra genera una query separata per i giocatori.",
            "14,283 ms", "16,00", 480, 100, 100),
        new RisultatoStrategia(
            "B — EntityGraph (solo squadre)",
            "Le squadre sono caricate eager, i giocatori restano lazy per squadra.",
            "6,957 ms", "14,00", 420, 49, 88),
        new RisultatoStrategia(
            "C — EntityGraph + batch",
            "Una query per torneo+squadre, una query batch per tutti i giocatori.",
            "6,306 ms", "4,00", 120, 44, 25)
    );

    private static final List<DimostrazioneEccezione> DIMOSTRAZIONI = List.of(
        new DimostrazioneEccezione(
            "Fetch join di squadre + partite (stessa entità Torneo)",
            "org.hibernate.loader.MultipleBagFetchException: cannot simultaneously fetch multiple bags: [it.uniroma3.siw.torneocalcio.model.Torneo.partite, it.uniroma3.siw.torneocalcio.model.Torneo.squadre]"),
        new DimostrazioneEccezione(
            "Fetch join di squadre + giocatori (due livelli)",
            "org.hibernate.loader.MultipleBagFetchException: cannot simultaneously fetch multiple bags: [it.uniroma3.siw.torneocalcio.model.Squadra.giocatori, it.uniroma3.siw.torneocalcio.model.Torneo.squadre]")
    );

    @GetMapping("/analisi")
    public String analisi(Model model) {
        model.addAttribute("risultati", RISULTATI);
        model.addAttribute("dimostrazioni", DIMOSTRAZIONI);
        return "analisi/index";
    }
}
