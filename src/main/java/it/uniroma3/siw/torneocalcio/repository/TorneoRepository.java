package it.uniroma3.siw.torneocalcio.repository;

import it.uniroma3.siw.torneocalcio.model.Torneo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;

public interface TorneoRepository extends CrudRepository<Torneo, Long> {
    List<Torneo> findAllByOrderByAnnoDesc();

    // Per analisi fetch: eager loading con EntityGraph
    // Nota: "squadre" e "partite" sono entrambe List (bag): Hibernate non può fare
    // fetch join di due bag contemporaneamente (MultipleBagFetchException), quindi
    // si carica eager solo "squadre" e si lascia "partite" lazy (via Open Session in View).
    @EntityGraph(attributePaths = {"squadre"})
    Optional<Torneo> findWithSquadreById(Long id);

    // Per analisi fetch: join fetch JPQL
    @Query("SELECT DISTINCT t FROM Torneo t LEFT JOIN FETCH t.squadre s LEFT JOIN FETCH s.giocatori WHERE t.id = :id")
    Optional<Torneo> findTorneoWithSquadreAndGiocatori(Long id);
}
