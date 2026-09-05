package com.factoryStudios.gestione.congresso.controllers;

import com.factoryStudios.gestione.congresso.services.EtlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/etl")
@CrossOrigin(origins = "*")
public class EtlController {

    @Autowired
    private EtlService etlService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Seleziona un file valido.");
        }

        try {
            etlService.importaDatiDaInputStream(file.getInputStream(), file.getOriginalFilename());
            return ResponseEntity.ok("File '" + file.getOriginalFilename() + "' importato con successo!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Errore durante l'importazione: " + e.getMessage());
        }
    }
}
