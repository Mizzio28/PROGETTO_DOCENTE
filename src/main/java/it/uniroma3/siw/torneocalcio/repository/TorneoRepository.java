package it.uniroma3.siw.torneocalcio.repository;

import it.uniroma3.siw.torneocalcio.model.Torneo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;

public interface TorneoRepository extends CrudRepository<Torneo, Long> {
    List<Torneo> findAllByOrderByAnnoDesc();

    // Nota: "squadre" e "partite" sono entrambe List (bag): Hibernate non può fare
    // fetch join di due bag contemporaneamente (MultipleBagFetchException), quindi
    // si carica eager solo "squadre" e si lascia "partite" lazy (via Open Session in View).
    @EntityGraph(attributePaths = {"squadre"})
    Optional<Torneo> findWithSquadreById(Long id);
}
