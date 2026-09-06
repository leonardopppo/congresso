package com.factoryStudios.gestione.congresso.repositories;

import com.factoryStudios.gestione.congresso.models.InterazionePartecipante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterazionePartecipanteRepository extends JpaRepository<InterazionePartecipante, Long> {

    // Conteggio partecipanti che hanno attivato un determinato touchpoint booleano (es. DEM aperta, Visita stand)
    @Query("SELECT COUNT(DISTINCT i.partecipante.id) FROM InterazionePartecipante i " +
            "WHERE i.touchpoint.codice = :codice " +
            "AND i.valoreBooleano = true " +
            "AND (:regione IS NULL OR i.partecipante.regione = :regione) " +
            "AND (:canale IS NULL OR i.partecipante.canaleIngaggio = :canale)")
    Long countPartecipantiByTouchpointAndFilters(
            @Param("codice") String codice,
            @Param("regione") String regione,
            @Param("canale") String canale
    );

    // Conteggio visite allo stand raggruppate per data (Andamento Giornaliero)
    @Query("SELECT i.valoreData, COUNT(i.id) FROM InterazionePartecipante i " +
            "WHERE LOWER(i.touchpoint.codice) LIKE '%giorno%' " +
            "AND i.valoreData IS NOT NULL " +
            "AND (:regione IS NULL OR :regione = '' OR LOWER(i.partecipante.regione) = LOWER(:regione)) " +
            "AND (:canale IS NULL OR :canale = '' OR LOWER(i.partecipante.canaleIngaggio) = LOWER(:canale)) " +
            "GROUP BY i.valoreData " +
            "ORDER BY i.valoreData ASC")
    List<Object[]> countVisitePerGiorno(
            @Param("regione") String regione,
            @Param("canale") String canale
    );

    // Incrocio 1: Prestazioni per canale di ingaggio (Canale vs Visita Stand e Simposio)
    @Query("SELECT p.canaleIngaggio, COUNT(DISTINCT p.id), " +
            "SUM(CASE WHEN t.codice = 'visita_stand' AND i.valoreBooleano = true THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN t.codice = 'presenza_simposio' AND i.valoreBooleano = true THEN 1 ELSE 0 END) " +
            "FROM Partecipante p " +
            "LEFT JOIN p.interazioni i " +
            "LEFT JOIN i.touchpoint t " +
            "WHERE (:regione IS NULL OR p.regione = :regione) " +
            "GROUP BY p.canaleIngaggio")
    List<Object[]> findAggregatesByCanale(@Param("regione") String regione);

    // Incrocio 2: Prestazioni per tipologia stakeholder
    @Query("SELECT p.tipologiaStakeholder, COUNT(DISTINCT p.id), " +
            "SUM(CASE WHEN t.codice = 'visita_stand' AND i.valoreBooleano = true THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN t.codice = 'presenza_simposio' AND i.valoreBooleano = true THEN 1 ELSE 0 END) " +
            "FROM Partecipante p " +
            "LEFT JOIN p.interazioni i " +
            "LEFT JOIN i.touchpoint t " +
            "WHERE (:regione IS NULL OR p.regione = :regione) " +
            "AND (:canale IS NULL OR p.canaleIngaggio = :canale) " +
            "GROUP BY p.tipologiaStakeholder")
    List<Object[]> findAggregatesByStakeholder(
            @Param("regione") String regione,
            @Param("canale") String canale
    );
}