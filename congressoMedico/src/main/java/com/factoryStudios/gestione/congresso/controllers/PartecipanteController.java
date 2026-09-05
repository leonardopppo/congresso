package com.factoryStudios.gestione.congresso.controllers;


import com.factoryStudios.gestione.congresso.dto.PartecipanteDTO;
import com.factoryStudios.gestione.congresso.models.Partecipante;
import com.factoryStudios.gestione.congresso.repositories.PartecipanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/partecipanti")
@RequiredArgsConstructor
public class PartecipanteController {

    private final PartecipanteRepository partecipanteRepository;

    @GetMapping
    public ResponseEntity<Page<PartecipanteDTO>> getPartecipanti(Pageable pageable) {
        Page<Partecipante> partecipantiPage = partecipanteRepository.findAll(pageable);

        // Converte ogni Partecipante nel DTO arricchito con le interazioni
        Page<PartecipanteDTO> dtoPage = partecipantiPage.map(PartecipanteDTO::fromEntity);

        return ResponseEntity.ok(dtoPage);
    }
}
