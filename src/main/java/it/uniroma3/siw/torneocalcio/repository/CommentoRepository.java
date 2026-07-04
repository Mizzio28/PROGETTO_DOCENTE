package it.uniroma3.siw.torneocalcio.repository;

import it.uniroma3.siw.torneocalcio.model.Commento;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;

public interface CommentoRepository extends CrudRepository<Commento, Long> {
    List<Commento> findByPartitaIdOrderByDataCommentoDesc(Long partitaId);
    Optional<Commento> findByIdAndAutoreId(Long id, Long autoreId);
}
