package com.factoryStudios.gestione.congresso.repositories;

import com.factoryStudios.gestione.congresso.models.Partecipante;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartecipanteRepository extends JpaRepository<Partecipante, Long>, JpaSpecificationExecutor<Partecipante> {

    // Ricerca partecipanti con filtri opzionali e paginazione
    @Query("SELECT p FROM Partecipante p WHERE " +
            "(:regione IS NULL OR p.regione = :regione) AND " +
            "(:tipologia IS NULL OR p.tipologiaStakeholder = :tipologia) AND " +
            "(:canale IS NULL OR p.canaleIngaggio = :canale)")
    Page<Partecipante> findWithFilters(
            @Param("regione") String regione,
            @Param("tipologia") String tipologia,
            @Param("canale") String canale,
            Pageable pageable
    );

    // Elenco di tutte le regioni distinte presenti nel DB (utilizzato per popolare le Select sul frontend)
    @Query("SELECT DISTINCT p.regione FROM Partecipante p WHERE p.regione IS NOT NULL ORDER BY p.regione")
    List<String> findDistinctRegioni();

    // Elenco di tutte le tipologie stakeholder distinte
    @Query("SELECT DISTINCT p.tipologiaStakeholder FROM Partecipante p WHERE p.tipologiaStakeholder IS NOT NULL ORDER BY p.tipologiaStakeholder")
    List<String> findDistinctTipologie();

    // Elenco dei canali d'ingaggio distinti
    @Query("SELECT DISTINCT p.canaleIngaggio FROM Partecipante p WHERE p.canaleIngaggio IS NOT NULL ORDER BY p.canaleIngaggio")
    List<String> findDistinctCanali();
}