package it.uniroma3.siw.torneocalcio.repository;

import it.uniroma3.siw.torneocalcio.model.Partita;
import it.uniroma3.siw.torneocalcio.model.StatoPartita;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface PartitaRepository extends CrudRepository<Partita, Long> {
    List<Partita> findByTorneoIdOrderByDataOraAsc(Long torneoId);
    List<Partita> findByTorneoIdAndStatoOrderByDataOraAsc(Long torneoId, StatoPartita stato);

    @Query("SELECT p FROM Partita p LEFT JOIN FETCH p.commenti c LEFT JOIN FETCH c.autore WHERE p.id = :id")
    java.util.Optional<Partita> findWithCommentiById(Long id);
}
