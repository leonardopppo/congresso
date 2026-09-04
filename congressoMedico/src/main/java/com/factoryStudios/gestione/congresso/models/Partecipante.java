package com.factoryStudios.gestione.congresso.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "partecipanti")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Partecipante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // L'ID originale presente nel file Excel
    private Long excelId;

    private String nomeCognome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "tipologia_stakeholder")
    private String tipologiaStakeholder; // Es: "HCP - Diabetologo", "Farmacia ospedaliera"

    private String regione;

    @Column(name = "canale_ingaggio")
    private String canaleIngaggio; // Es: "Database DEM", "LinkedIn"

    @Column(name = "in_database_dem")
    private Boolean inDatabaseDem;

    @OneToMany(mappedBy = "partecipante", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude // Evita cicli infiniti nel toString con Lombok
    private List<InterazionePartecipante> interazioni = new ArrayList<>();
}
