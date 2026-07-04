package it.uniroma3.siw.torneocalcio.service;

import it.uniroma3.siw.torneocalcio.model.Giocatore;
import it.uniroma3.siw.torneocalcio.model.Squadra;
import it.uniroma3.siw.torneocalcio.repository.GiocatoreRepository;
import it.uniroma3.siw.torneocalcio.repository.SquadraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class GiocatoreService {

    @Autowired
    private GiocatoreRepository giocatoreRepository;
    @Autowired
    private SquadraRepository squadraRepository;

    @Transactional(readOnly = true)
    public Giocatore getGiocatore(Long id) {
        return giocatoreRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Giocatore> getGiocatoriBySquadra(Long squadraId) {
        return giocatoreRepository.findBySquadraId(squadraId);
    }

    @Transactional
    public Giocatore saveGiocatore(Giocatore giocatore, Long squadraId) {
        Squadra squadra = squadraRepository.findById(squadraId).orElseThrow();
        giocatore.setSquadra(squadra);
        return giocatoreRepository.save(giocatore);
    }

    @Transactional
    public Giocatore updateGiocatore(Long id, Giocatore aggiornato) {
        Giocatore existing = giocatoreRepository.findById(id).orElseThrow();
        existing.setNome(aggiornato.getNome());
        existing.setCognome(aggiornato.getCognome());
        existing.setDataNascita(aggiornato.getDataNascita());
        existing.setRuolo(aggiornato.getRuolo());
        existing.setAltezza(aggiornato.getAltezza());
        return giocatoreRepository.save(existing);
    }

    @Transactional
    public void deleteGiocatore(Long id) {
        giocatoreRepository.deleteById(id);
    }
}
