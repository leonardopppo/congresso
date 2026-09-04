package com.factoryStudios.gestione.congresso.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "interazioni_partecipanti")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterazionePartecipante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partecipante_id", nullable = false)
    @ToString.Exclude
    private Partecipante partecipante;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "touchpoint_id", nullable = false)
    private Touchpoint touchpoint;

    // Valore per interazioni di tipo booleano (es. DEM aperta = true/false)
    private Boolean valoreBooleano;

    // Valore per conteggi ed interi (es. quiz completati = 2, visualizzazioni = 5)
    private Integer valoreNumerico;

    // Valore per percentuali o tassi decimali (es. focus rate = 0.67, permanenza min = 49.0)
    private Double valoreDecimale;

    // Valore per date (es. giorno visita = 16/10/2025)
    private LocalDate valoreData;

    // Valore eventuale in formato testo
    private String valoreTesto;
}