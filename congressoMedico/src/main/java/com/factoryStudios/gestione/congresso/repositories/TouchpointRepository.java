package com.factoryStudios.gestione.congresso.repositories;


import com.factoryStudios.gestione.congresso.models.FaseEvento;
import com.factoryStudios.gestione.congresso.models.Touchpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TouchpointRepository extends JpaRepository<Touchpoint, Long> {

    // Trova un touchpoint dato il suo codice univoco (es. "dem_aperta", "visita_stand")
    Optional<Touchpoint> findByCodice(String codice);

    // Recupera tutti i touchpoint appartenenti a una determinata fase (es. PRE_EVENTO, ON_SITE)
    List<Touchpoint> findByFase(FaseEvento fase);
}
