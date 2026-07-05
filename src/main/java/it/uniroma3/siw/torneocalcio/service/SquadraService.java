package it.uniroma3.siw.torneocalcio.service;

import it.uniroma3.siw.torneocalcio.model.Squadra;
import it.uniroma3.siw.torneocalcio.model.Torneo;
import it.uniroma3.siw.torneocalcio.repository.SquadraRepository;
import it.uniroma3.siw.torneocalcio.repository.TorneoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class SquadraService {

    @Autowired
    private SquadraRepository squadraRepository;
    @Autowired
    private TorneoRepository torneoRepository;

    @Transactional(readOnly = true)
    public List<Squadra> getAllSquadre() {
        return squadraRepository.findAllByOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public Squadra getSquadra(Long id) {
        return squadraRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public Squadra getSquadraConGiocatori(Long id) {
        return squadraRepository.findWithGiocatoriById(id).orElseThrow();
    }

    @Transactional
    public Squadra saveSquadra(Squadra squadra) {
        return squadraRepository.save(squadra);
    }

    @Transactional
    public Squadra updateSquadra(Long id, Squadra aggiornata) {
        Squadra existing = squadraRepository.findById(id).orElseThrow();
        existing.setNome(aggiornata.getNome());
        existing.setAnnoFondazione(aggiornata.getAnnoFondazione());
        existing.setCitta(aggiornata.getCitta());
        return squadraRepository.save(existing);
    }

    @Transactional
    public void deleteSquadra(Long id) {
        squadraRepository.deleteById(id);
    }

    @Transactional
    public void aggiungiSquadraATorneo(Long squadraId, Long torneoId) {
        Squadra squadra = squadraRepository.findById(squadraId).orElseThrow();
        Torneo torneo = torneoRepository.findById(torneoId).orElseThrow();
        if (!squadra.getTornei().contains(torneo)) {
            squadra.getTornei().add(torneo);
            squadraRepository.save(squadra);
        }
    }
}
