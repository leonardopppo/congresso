package com.factoryStudios.gestione.congresso.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartecipanteDTO {

    private Long id;
    private Long excelId;
    private String nomeCognome;
    private String email;
    private String tipologiaStakeholder;
    private String regione;
    private String canaleIngaggio;
    private Boolean inDatabaseDem;
}
