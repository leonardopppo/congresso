package com.factoryStudios.gestione.congresso.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfrontoAnagraficoDTO {

    private String categoria;           // Es: "HCP - Diabetologo"
    private Long totalePartecipanti;    // Totale partecipanti per categoria
    private Long visiteStand;           // Interazioni stand
    private Long presenzeSimposio;      // Interazioni simposio
    private Double tassoPartecipazione; // Percentuale

    // Campi richiesti dal grafico "Confronto Tipologie Stakeholder"
    private Long emailDirect;           // Conteggio Database DEM
    private Long linkedIn;              // Conteggio LinkedIn
    private Long agenteRete;            // Conteggio On-site (stand + simposio)
}
