package com.factoryStudios.gestione.congresso.services;

import com.factoryStudios.gestione.congresso.dto.AndamentoGiornalieroDTO;
import com.factoryStudios.gestione.congresso.dto.ConfrontoAnagraficoDTO;
import com.factoryStudios.gestione.congresso.dto.FunnelStepDTO;
import com.factoryStudios.gestione.congresso.dto.PartecipanteDTO;
import com.factoryStudios.gestione.congresso.models.Partecipante;
import com.factoryStudios.gestione.congresso.repositories.InterazionePartecipanteRepository;
import com.factoryStudios.gestione.congresso.repositories.PartecipanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PartecipanteRepository partecipanteRepository;
    private final InterazionePartecipanteRepository interazioneRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Restituisce la lista paginata dei partecipanti convertiti in DTO
    public Page<PartecipanteDTO> getPartecipanti(String regione, String tipologia, String canale, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Partecipante> partecipantiPage = partecipanteRepository.findWithFilters(regione, tipologia, canale, pageRequest);

        return partecipantiPage.map(p -> PartecipanteDTO.builder()
                .id(p.getId())
                .excelId(p.getExcelId())
                .nomeCognome(p.getNomeCognome())
                .email(p.getEmail())
                .tipologiaStakeholder(p.getTipologiaStakeholder())
                .regione(p.getRegione())
                .canaleIngaggio(p.getCanaleIngaggio())
                .inDatabaseDem(p.getInDatabaseDem())
                .build());
    }

    // Calcola il Funnel del percorso partecipante
    public List<FunnelStepDTO> getFunnelData(String regione, String canale) {
        long totalePartecipanti = partecipanteRepository.count();
        if (totalePartecipanti == 0) return Collections.emptyList();

        List<FunnelStepDTO> funnel = new ArrayList<>();

        funnel.add(creaFunnelStep("DEM Inviata", "PRE_EVENTO", "dem_inviata", regione, canale, totalePartecipanti));
        funnel.add(creaFunnelStep("DEM Aperta", "PRE_EVENTO", "dem_aperta", regione, canale, totalePartecipanti));
        funnel.add(creaFunnelStep("Visita Stand", "ON_SITE", "visita_stand", regione, canale, totalePartecipanti));
        funnel.add(creaFunnelStep("Accesso Sala VIP", "ON_SITE", "accesso_sala_vip", regione, canale, totalePartecipanti));
        funnel.add(creaFunnelStep("Presenza Simposio", "SESSIONE", "presenza_simposio", regione, canale, totalePartecipanti));

        return funnel;
    }

    // Calcola l'aggregazione per Canale d'Ingaggio
    public List<ConfrontoAnagraficoDTO> getConfrontoCanali(String regione) {
        List<Object[]> risultati = interazioneRepository.findAggregatesByCanale(regione);
        return mappaAConfrontoDTO(risultati);
    }

    // Calcola l'aggregazione per Tipologia Stakeholder
    public List<ConfrontoAnagraficoDTO> getConfrontoStakeholder(String regione, String canale) {
        List<Object[]> risultati = interazioneRepository.findAggregatesByStakeholder(regione, canale);
        return mappaAConfrontoDTO(risultati);
    }

    // Calcola l'andamento giornaliero delle visite allo stand
    public List<AndamentoGiornalieroDTO> getAndamentoGiornaliero(String regione, String canale) {
        List<Object[]> risultati = interazioneRepository.countVisitePerGiorno(regione, canale);

        return risultati.stream().map(r -> {
            java.time.LocalDate date = (java.time.LocalDate) r[0];
            Long visite = (Long) r[1];
            return AndamentoGiornalieroDTO.builder()
                    .data(date != null ? date.format(DATE_FORMATTER) : "N/D")
                    .visiteStand(visite)
                    .totaleInterazioni(visite)
                    .build();
        }).collect(Collectors.toList());
    }

    // Metodi per popolare i filtri a tendina del frontend
    public List<String> getRegioni() { return partecipanteRepository.findDistinctRegioni(); }
    public List<String> getTipologie() { return partecipanteRepository.findDistinctTipologie(); }
    public List<String> getCanali() { return partecipanteRepository.findDistinctCanali(); }

    private FunnelStepDTO creaFunnelStep(String stepName, String fase, String codiceTouchpoint, String regione, String canale, long totale) {
        Long count = interazioneRepository.countPartecipantiByTouchpointAndFilters(codiceTouchpoint, regione, canale);
        double pct = totale > 0 ? Math.round((count * 100.0 / totale) * 100.0) / 100.0 : 0.0;

        return FunnelStepDTO.builder()
                .step(stepName)
                .fase(fase)
                .conteggio(count)
                .percentuale(pct)
                .build();
    }

    private List<ConfrontoAnagraficoDTO> mappaAConfrontoDTO(List<Object[]> risultati) {
        return risultati.stream().map(r -> {
            String cat = (String) r[0];
            Long tot = (Long) r[1];
            Long visite = (Long) r[2];
            Long presenze = (Long) r[3];
            double tasso = tot > 0 ? Math.round((visite * 100.0 / tot) * 100.0) / 100.0 : 0.0;

            return ConfrontoAnagraficoDTO.builder()
                    .categoria(cat)
                    .totalePartecipanti(tot)
                    .visiteStand(visite)
                    .presenzeSimposio(presenze)
                    .tassoPartecipazione(tasso)
                    .build();
        }).collect(Collectors.toList());
    }
}
