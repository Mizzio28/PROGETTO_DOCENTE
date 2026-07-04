package it.uniroma3.siw.torneocalcio.service;

import it.uniroma3.siw.torneocalcio.model.Torneo;
import it.uniroma3.siw.torneocalcio.repository.TorneoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class TorneoService {

    @Autowired
    private TorneoRepository torneoRepository;

    @Transactional(readOnly = true)
    public List<Torneo> getAllTornei() {
        return torneoRepository.findAllByOrderByAnnoDesc();
    }

    @Transactional(readOnly = true)
    public Torneo getTorneo(Long id) {
        return torneoRepository.findById(id).orElse(null);
    }

    /** Carica torneo con squadre+partite via EntityGraph (analisi fetch) */
    @Transactional(readOnly = true)
    public Torneo getTorneoConDettagli(Long id) {
        return torneoRepository.findWithSquadreAndPartiteById(id).orElse(null);
    }

    /** Carica torneo con squadre+giocatori via JOIN FETCH JPQL (analisi fetch) */
    @Transactional(readOnly = true)
    public Torneo getTorneoConGiocatori(Long id) {
        return torneoRepository.findTorneoWithSquadreAndGiocatori(id).orElse(null);
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

    @Transactional
    public void deleteTorneo(Long id) {
        torneoRepository.deleteById(id);
    }
}
