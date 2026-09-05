package com.factoryStudios.gestione.congresso.controllers;
import com.factoryStudios.gestione.congresso.dto.AndamentoGiornalieroDTO;
import com.factoryStudios.gestione.congresso.dto.ConfrontoAnagraficoDTO;
import com.factoryStudios.gestione.congresso.dto.FunnelStepDTO;
import com.factoryStudios.gestione.congresso.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // 1. Dati per il grafico Funnel
    @GetMapping("/funnel")
    public ResponseEntity<List<FunnelStepDTO>> getFunnel(
            @RequestParam(required = false) String regione,
            @RequestParam(required = false) String canale) {
        return ResponseEntity.ok(dashboardService.getFunnelData(regione, canale));
    }

    // 2. Dati per il confronto tra Canali di Ingaggio
    @GetMapping("/confronto-canali")
    public ResponseEntity<List<ConfrontoAnagraficoDTO>> getConfrontoCanali(
            @RequestParam(required = false) String regione) {
        return ResponseEntity.ok(dashboardService.getConfrontoCanali(regione));
    }

    // 3. Dati per il confronto tra Tipologie Stakeholder
    @GetMapping("/confronto-stakeholder")
    public ResponseEntity<List<ConfrontoAnagraficoDTO>> getConfrontoStakeholder(
            @RequestParam(required = false) String regione,
            @RequestParam(required = false) String canale) {
        return ResponseEntity.ok(dashboardService.getConfrontoStakeholder(regione, canale));
    }

    // 4. Dati per il grafico dell'andamento giornaliero
    @GetMapping("/andamento-giornaliero")
    public ResponseEntity<List<AndamentoGiornalieroDTO>> getAndamentoGiornaliero(
            @RequestParam(required = false) String regione,
            @RequestParam(required = false) String canale) {
        return ResponseEntity.ok(dashboardService.getAndamentoGiornaliero(regione, canale));
    }

    // --- Endpoint per popolare i filtri dinamici nel frontend ---

    @GetMapping("/filtri/regioni")
    public ResponseEntity<List<String>> getRegioni() {
        return ResponseEntity.ok(dashboardService.getRegioni());
    }

    @GetMapping("/filtri/tipologie")
    public ResponseEntity<List<String>> getTipologie() {
        return ResponseEntity.ok(dashboardService.getTipologie());
    }

    @GetMapping("/filtri/canali")
    public ResponseEntity<List<String>> getCanali() {
        return ResponseEntity.ok(dashboardService.getCanali());
    }
}
