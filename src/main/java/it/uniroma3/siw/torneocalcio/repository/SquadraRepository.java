package it.uniroma3.siw.torneocalcio.repository;

import it.uniroma3.siw.torneocalcio.model.Squadra;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;

public interface SquadraRepository extends CrudRepository<Squadra, Long> {
    List<Squadra> findAllByOrderByNomeAsc();
    boolean existsByNome(String nome);

    @Query("SELECT s FROM Squadra s LEFT JOIN FETCH s.giocatori WHERE s.id = :id")
    Optional<Squadra> findWithGiocatoriById(Long id);
}
