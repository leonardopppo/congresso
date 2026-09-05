package com.factoryStudios.gestione.congresso.controllers;


import com.factoryStudios.gestione.congresso.dto.PartecipanteDTO;
import com.factoryStudios.gestione.congresso.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/partecipanti")
@RequiredArgsConstructor
public class PartecipanteController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<Page<PartecipanteDTO>> getPartecipanti(
            @RequestParam(required = false) String regione,
            @RequestParam(required = false) String tipologia,
            @RequestParam(required = false) String canale,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<PartecipanteDTO> risultato = dashboardService.getPartecipanti(regione, tipologia, canale, page, size);
        return ResponseEntity.ok(risultato);
    }
}
