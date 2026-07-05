package it.uniroma3.siw.torneocalcio.service;

import it.uniroma3.siw.torneocalcio.model.*;
import it.uniroma3.siw.torneocalcio.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class CommentoService {

    @Autowired private CommentoRepository commentoRepository;
    @Autowired private PartitaRepository partitaRepository;
    @Autowired private UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<Commento> getCommentiByPartita(Long partitaId) {
        return commentoRepository.findByPartitaIdOrderByDataCommentoDesc(partitaId);
    }

    @Transactional
    public Commento addCommento(String testo, Long partitaId, String username) {
        Commento commento = new Commento();
        commento.setTesto(testo);
        commento.setPartita(partitaRepository.findById(partitaId).orElseThrow());
        User autore = userRepository.findByUsername(username).orElseThrow();
        commento.setAutore(autore);
        return commentoRepository.save(commento);
    }

    /** Aggiorna il commento solo se appartiene all'utente indicato; altrimenti non fa nulla. */
    @Transactional
    public Optional<Commento> updateCommento(Long commentoId, String nuovoTesto, String username) {
        return getCommentoDiUtente(commentoId, username).map(commento -> {
            commento.setTesto(nuovoTesto);
            return commentoRepository.save(commento);
        });
    }

    @Transactional(readOnly = true)
    public Commento getCommento(Long id) {
        return commentoRepository.findById(id).orElse(null);
    }

    /** Restituisce il commento solo se appartiene all'utente indicato (controllo di proprietà). */
    @Transactional(readOnly = true)
    public Optional<Commento> getCommentoDiUtente(Long commentoId, String username) {
        User utente = userRepository.findByUsername(username).orElseThrow();
        return commentoRepository.findByIdAndAutoreId(commentoId, utente.getId());
    }
}