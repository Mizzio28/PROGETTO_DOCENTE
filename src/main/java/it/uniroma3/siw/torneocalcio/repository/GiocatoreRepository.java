package it.uniroma3.siw.torneocalcio.repository;

import it.uniroma3.siw.torneocalcio.model.Giocatore;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface GiocatoreRepository extends CrudRepository<Giocatore, Long> {
    List<Giocatore> findBySquadraId(Long squadraId);
}
