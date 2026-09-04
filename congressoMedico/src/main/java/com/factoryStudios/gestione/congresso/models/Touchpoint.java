package com.factoryStudios.gestione.congresso.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "touchpoints")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Touchpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Es: "dem_inviata", "visita_stand", "permanenza_min", "focus_rate"
    @Column(nullable = false, unique = true)
    private String codice;

    // Nome visualizzabile (es: "DEM Inviata", "Visita Stand")
    @Column(nullable = false)
    private String nome;

    // Fase dell'evento (PRE_EVENTO, ON_SITE, SESSIONE, POST_EVENTO)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FaseEvento fase;

    // Tipo dato atteso: booleano, conteggio, data, minuti, tasso
    private String tipoDato;

    @Column(length = 500)
    private String descrizione;
}
