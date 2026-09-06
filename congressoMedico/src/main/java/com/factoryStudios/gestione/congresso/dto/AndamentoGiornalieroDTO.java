package com.factoryStudios.gestione.congresso.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AndamentoGiornalieroDTO {

    // Data in formato stringa
    private String data;
    private String giorno;  // Alias per JS (item.giorno)
    private String date;    // Alias per JS (item.date)

    // Conteggio visite
    private Long visiteStand;
    private Long visite;    // Alias per JS (item.visite)
    private Long totaleInterazioni;
    private Long count;     // Alias per JS (item.count)
}
