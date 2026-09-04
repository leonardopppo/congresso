package com.factoryStudios.gestione.congresso.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FunnelStepDTO {

    private String step;         // Es: "DEM Inviata", "DEM Aperta", "Visita Stand", "Accesso VIP", "Presenza Simposio"
    private String fase;         // Es: "PRE_EVENTO", "ON_SITE", "SESSIONE"
    private Long conteggio;      // Numero totale di persone che hanno compiuto l'azione
    private Double percentuale;  // Percentuale sul totale dei partecipanti
}
