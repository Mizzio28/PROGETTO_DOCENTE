package it.uniroma3.siw.torneocalcio.service;

import it.uniroma3.siw.torneocalcio.model.Arbitro;
import it.uniroma3.siw.torneocalcio.repository.ArbitroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ArbitroService {

    @Autowired
    private ArbitroRepository arbitroRepository;

    @Transactional(readOnly = true)
    public List<Arbitro> getAllArbitri() {
        return arbitroRepository.findAllByOrderByCognomeAsc();
    }

    @Transactional(readOnly = true)
    public Arbitro getArbitro(Long id) {
        return arbitroRepository.findById(id).orElse(null);
    }

    @Transactional
    public Arbitro saveArbitro(Arbitro arbitro) {
        return arbitroRepository.save(arbitro);
    }

    @Transactional
    public Arbitro updateArbitro(Long id, Arbitro aggiornato) {
        Arbitro existing = arbitroRepository.findById(id).orElseThrow();
        existing.setNome(aggiornato.getNome());
        existing.setCognome(aggiornato.getCognome());
        existing.setCodiceArbitrale(aggiornato.getCodiceArbitrale());
        return arbitroRepository.save(existing);
    }

    @Transactional
    public void deleteArbitro(Long id) {
        Arbitro arbitro = arbitroRepository.findById(id).orElseThrow();
        if (!arbitro.getPartite().isEmpty()) {
            throw new IllegalStateException(
                "Impossibile eliminare: arbitro assegnato a " + arbitro.getPartite().size() + " partite");
        }
        arbitroRepository.delete(arbitro);
    }
}
