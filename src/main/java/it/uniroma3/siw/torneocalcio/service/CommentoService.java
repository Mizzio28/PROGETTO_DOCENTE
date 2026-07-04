package it.uniroma3.siw.torneocalcio.service;

import it.uniroma3.siw.torneocalcio.model.*;
import it.uniroma3.siw.torneocalcio.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CommentoService {

    @Autowired private CommentoRepository commentoRepository;
    @Autowired private PartitaRepository partitaRepository;
    @Autowired private CredentialsRepository credentialsRepository;

    @Transactional(readOnly = true)
    public List<Commento> getCommentiByPartita(Long partitaId) {
        return commentoRepository.findByPartitaIdOrderByDataCommentoDesc(partitaId);
    }

    @Transactional
    public Commento addCommento(String testo, Long partitaId, String username) {
        Commento commento = new Commento();
        commento.setTesto(testo);
        commento.setPartita(partitaRepository.findById(partitaId).orElseThrow());
        Credentials creds = credentialsRepository.findByUsername(username).orElseThrow();
        commento.setAutore(creds.getUser());
        return commentoRepository.save(commento);
    }

    @Transactional
    public Commento updateCommento(Long commentoId, String nuovoTesto, String username) {
        Credentials creds = credentialsRepository.findByUsername(username).orElseThrow();
        Commento commento = commentoRepository.findByIdAndAutoreId(commentoId, creds.getUser().getId())
                .orElseThrow(() -> new IllegalStateException("Non puoi modificare questo commento"));
        commento.setTesto(nuovoTesto);
        return commentoRepository.save(commento);
    }

    @Transactional(readOnly = true)
    public Commento getCommento(Long id) {
        return commentoRepository.findById(id).orElse(null);
    }
}
