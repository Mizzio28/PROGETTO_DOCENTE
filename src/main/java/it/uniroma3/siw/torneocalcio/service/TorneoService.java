package it.uniroma3.siw.torneocalcio.service;

import it.uniroma3.siw.torneocalcio.viewmodel.RigaClassifica;
import it.uniroma3.siw.torneocalcio.model.Partita;
import it.uniroma3.siw.torneocalcio.model.Squadra;
import it.uniroma3.siw.torneocalcio.model.StatoPartita;
import it.uniroma3.siw.torneocalcio.model.Torneo;
import it.uniroma3.siw.torneocalcio.repository.PartitaRepository;
import it.uniroma3.siw.torneocalcio.repository.TorneoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TorneoService {

    @Autowired
    private TorneoRepository torneoRepository;
    @Autowired
    private PartitaRepository partitaRepository;

    @Transactional(readOnly = true)
    public List<Torneo> getAllTornei() {
        return torneoRepository.findAllByOrderByAnnoDesc();
    }

    @Transactional(readOnly = true)
    public long count() {
        return torneoRepository.count();
    }

    @Transactional(readOnly = true)
    public Torneo getTorneo(Long id) {
        return torneoRepository.findById(id).orElse(null);
    }

    /** Carica torneo con squadre via EntityGraph. */
    @Transactional(readOnly = true)
    public Torneo getTorneoConDettagli(Long id) {
        return torneoRepository.findWithSquadreById(id).orElseThrow();
    }

    @Transactional
    public Torneo saveTorneo(Torneo torneo) {
        return torneoRepository.save(torneo);
    }

    @Transactional
    public Torneo updateTorneo(Long id, Torneo aggiornato) {
        Torneo existing = torneoRepository.findById(id).orElseThrow();
        existing.setNome(aggiornato.getNome());
        existing.setAnno(aggiornato.getAnno());
        existing.setDescrizione(aggiornato.getDescrizione());
        return torneoRepository.save(existing);
    }

    /** Calcola la classifica del torneo (3 pt vittoria, 1 pt pareggio) dalle partite giocate. */
    @Transactional(readOnly = true)
    public List<RigaClassifica> calcolaClassifica(Long torneoId) {
        Torneo torneo = torneoRepository.findWithSquadreById(torneoId).orElseThrow();
        Map<Long, RigaClassifica> righe = new LinkedHashMap<>();
        for (Squadra squadra : torneo.getSquadre()) {
            righe.put(squadra.getId(), new RigaClassifica(squadra));
        }

        List<Partita> giocate = partitaRepository.findByTorneoIdAndStatoOrderByDataOraAsc(torneoId, StatoPartita.PLAYED);
        for (Partita partita : giocate) {
            RigaClassifica home = righe.get(partita.getSquadraHome().getId());
            RigaClassifica away = righe.get(partita.getSquadraAway().getId());
            if (home == null || away == null) continue;

            int golHome = partita.getGoalsHome();
            int golAway = partita.getGoalsAway();
            if (golHome > golAway) {
                home.registraVittoria(golHome, golAway);
                away.registraSconfitta(golAway, golHome);
            } else if (golHome < golAway) {
                away.registraVittoria(golAway, golHome);
                home.registraSconfitta(golHome, golAway);
            } else {
                home.registraPareggio(golHome, golAway);
                away.registraPareggio(golAway, golHome);
            }
        }

        List<RigaClassifica> classifica = new ArrayList<>(righe.values());
        classifica.sort(Comparator.comparingInt(RigaClassifica::getPunti)
            .thenComparingInt(RigaClassifica::getDifferenzaReti)
            .reversed());
        return classifica;
    }

    @Transactional
    public void deleteTorneo(Long id) {
        Torneo torneo = torneoRepository.findWithSquadreById(id).orElseThrow();
        if (!torneo.getPartite().isEmpty()) {
            throw new IllegalStateException(
                "Impossibile eliminare: il torneo ha " + torneo.getPartite().size() + " partite collegate");
        }
        for (Squadra squadra : new ArrayList<>(torneo.getSquadre())) {
            squadra.getTornei().remove(torneo);
        }
        torneoRepository.delete(torneo);
    }
}
