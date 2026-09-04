package com.factoryStudios.gestione.congresso.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AndamentoGiornalieroDTO {

    private String data;               // Es: "15/10/2025"
    private Long visiteStand;          // Visite effettuate in quella giornata
    private Long totaleInterazioni;    // Interazioni complessive registrate nella giornata
}
