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
}
