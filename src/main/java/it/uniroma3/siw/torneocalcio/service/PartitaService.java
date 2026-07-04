package it.uniroma3.siw.torneocalcio.service;

import it.uniroma3.siw.torneocalcio.model.*;
import it.uniroma3.siw.torneocalcio.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class PartitaService {

    @Autowired private PartitaRepository partitaRepository;
    @Autowired private TorneoRepository torneoRepository;
    @Autowired private SquadraRepository squadraRepository;
    @Autowired private ArbitroRepository arbitroRepository;

    @Transactional(readOnly = true)
    public List<Partita> getPartiteByTorneo(Long torneoId) {
        return partitaRepository.findByTorneoIdOrderByDataOraAsc(torneoId);
    }

    @Transactional(readOnly = true)
    public List<Partita> getAllPartite() {
        return partitaRepository.findAllByOrderByDataOraDesc();
    }

    @Transactional(readOnly = true)
    public List<Partita> getPartiteByTorneoDesc(Long torneoId) {
        return partitaRepository.findByTorneoIdOrderByDataOraDesc(torneoId);
    }

    @Transactional(readOnly = true)
    public Partita getPartita(Long id) {
        return partitaRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public Partita getPartitaConCommenti(Long id) {
        return partitaRepository.findWithCommentiById(id).orElse(null);
    }

    @Transactional
    public Partita registraPartita(Partita partita, Long torneoId,
                                   Long squadraHomeId, Long squadraAwayId, Long arbitroId) {
        partita.setTorneo(torneoRepository.findById(torneoId).orElseThrow());
        partita.setSquadraHome(squadraRepository.findById(squadraHomeId).orElseThrow());
        partita.setSquadraAway(squadraRepository.findById(squadraAwayId).orElseThrow());
        partita.setArbitro(arbitroRepository.findById(arbitroId).orElseThrow());
        partita.setStato(StatoPartita.SCHEDULED);
        return partitaRepository.save(partita);
    }

    @Transactional
    public Partita inserisciRisultato(Long partitaId, Integer goalsHome, Integer goalsAway) {
        Partita partita = partitaRepository.findById(partitaId).orElseThrow();
        partita.setGoalsHome(goalsHome);
        partita.setGoalsAway(goalsAway);
        partita.setStato(StatoPartita.PLAYED);
        return partitaRepository.save(partita);
    }

    @Transactional
    public void deletePartita(Long id) {
        partitaRepository.deleteById(id);
    }
}
