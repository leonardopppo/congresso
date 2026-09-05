package com.factoryStudios.gestione.congresso.loaders;

import com.factoryStudios.gestione.congresso.repositories.PartecipanteRepository;
import com.factoryStudios.gestione.congresso.services.EtlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final EtlService etlService;
    private final PartecipanteRepository partecipanteRepository;

    @Override
    public void run(String... args) {
        log.info("Controllo presenza dati nel database...");

        if (partecipanteRepository.count() == 0) {
            log.info("Database vuoto. Avvio del processo ETL da file Excel...");
            long startTime = System.currentTimeMillis();

            etlService.importaDatiExcel();

            long duration = System.currentTimeMillis() - startTime;
            log.info("Caricamento ETL completato con successo in {} ms! Inseriti {} partecipanti.",
                    duration, partecipanteRepository.count());
        } else {
            log.info("Database già popolato con {} partecipanti. Salto l'importazione iniziale.",
                    partecipanteRepository.count());
        }
    }
}
