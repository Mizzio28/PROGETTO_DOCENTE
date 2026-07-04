package it.uniroma3.siw.torneocalcio.repository;

import it.uniroma3.siw.torneocalcio.model.Arbitro;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface ArbitroRepository extends CrudRepository<Arbitro, Long> {
    List<Arbitro> findAllByOrderByCognomeAsc();
    boolean existsByCodiceArbitrale(String codiceArbitrale);
}
