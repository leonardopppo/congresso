package com.factoryStudios.gestione.congresso.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfrontoAnagraficoDTO {

    private String categoria;        // Es: "HCP - Diabetologo" oppure "Lombardia"
    private Long totalePartecipanti; // Quanti sono in quella categoria
    private Long visiteStand;        // Quanti hanno visitato lo stand
    private Long presenzeSimposio;   // Quanti erano al simposio
    private Double tassoPartecipazione; // % di ingaggio incrociata
}
