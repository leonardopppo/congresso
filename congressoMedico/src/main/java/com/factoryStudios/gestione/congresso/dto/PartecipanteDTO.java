package com.factoryStudios.gestione.congresso.dto;

import com.factoryStudios.gestione.congresso.models.InterazionePartecipante;
import com.factoryStudios.gestione.congresso.models.Partecipante;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartecipanteDTO {

    private Long id;
    private Long excelId;
    private String nomeCognome;
    private String email;
    private String regione;
    private Boolean inDatabaseDem;

    // Nomi standard Backend
    private String tipologiaStakeholder;
    private String canaleIngaggio;

    // ALIAS per il Frontend JavaScript (risolvono qualsiasi nome cercato in index.html)
    private String stakeholder;
    private String tipologia;
    private String canale;

    // Touchpoint booleani
    private Boolean emailAperta;
    private Boolean visitaStand;
    private Boolean salaRiservata;
    private Boolean presenzaSimposio;

    @Builder.Default
    private Map<String, Object> interazioni = new HashMap<>();

    public static PartecipanteDTO fromEntity(Partecipante p) {
        Map<String, Object> interazioniMappa = new HashMap<>();

        boolean emailApertaVal = false;
        boolean standVal = false;
        boolean salaRiservataVal = false;
        boolean simposioVal = false;

        if (p.getInterazioni() != null) {
            for (InterazionePartecipante interazione : p.getInterazioni()) {
                if (interazione.getTouchpoint() == null) continue;

                String nomeTp = interazione.getTouchpoint().getNome();
                Object valore = estraiValoreInterazione(interazione);

                if (nomeTp != null) {
                    interazioniMappa.put(nomeTp, valore);
                    String nomeLower = nomeTp.toLowerCase().trim();

                    if (isValorePositivo(valore)) {
                        if (nomeLower.contains("dem aperta")) emailApertaVal = true;
                        if (nomeLower.contains("visita stand")) standVal = true;
                        if (nomeLower.contains("hospitality suite") || nomeLower.contains("sala vip")) salaRiservataVal = true;
                        if (nomeLower.contains("presenza simposio")) simposioVal = true;
                    }
                }
            }
        }

        return PartecipanteDTO.builder()
                .id(p.getId())
                .excelId(p.getExcelId())
                .nomeCognome(p.getNomeCognome())
                .email(p.getEmail())
                .regione(p.getRegione())
                .inDatabaseDem(p.getInDatabaseDem())
                // Mappatura di tutte le varianti di nome per JS
                .tipologiaStakeholder(p.getTipologiaStakeholder())
                .stakeholder(p.getTipologiaStakeholder())
                .tipologia(p.getTipologiaStakeholder())
                .canaleIngaggio(p.getCanaleIngaggio())
                .canale(p.getCanaleIngaggio())
                // Touchpoint booleani
                .emailAperta(emailApertaVal)
                .visitaStand(standVal)
                .salaRiservata(salaRiservataVal)
                .presenzaSimposio(simposioVal)
                .interazioni(interazioniMappa)
                .build();
    }

    private static Object estraiValoreInterazione(InterazionePartecipante i) {
        if (i.getValoreBooleano() != null) return i.getValoreBooleano();
        if (i.getValoreNumerico() != null) return i.getValoreNumerico();
        if (i.getValoreDecimale() != null) return i.getValoreDecimale();
        if (i.getValoreData() != null) return i.getValoreData();
        return i.getValoreTesto();
    }

    private static boolean isValorePositivo(Object valore) {
        if (valore instanceof Boolean b) return b;
        if (valore instanceof Number n) return n.doubleValue() > 0;
        if (valore instanceof String s) return !s.isBlank() && !s.equalsIgnoreCase("false") && !s.equals("0");
        return valore != null;
    }
}
