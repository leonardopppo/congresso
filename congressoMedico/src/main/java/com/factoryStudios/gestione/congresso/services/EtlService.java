package com.factoryStudios.gestione.congresso.services;

import com.factoryStudios.gestione.congresso.models.FaseEvento;
import com.factoryStudios.gestione.congresso.models.InterazionePartecipante;
import com.factoryStudios.gestione.congresso.models.Partecipante;
import com.factoryStudios.gestione.congresso.models.Touchpoint;
import com.factoryStudios.gestione.congresso.repositories.InterazionePartecipanteRepository;
import com.factoryStudios.gestione.congresso.repositories.PartecipanteRepository;
import com.factoryStudios.gestione.congresso.repositories.TouchpointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EtlService implements CommandLineRunner {

    private final TouchpointRepository touchpointRepository;
    private final PartecipanteRepository partecipanteRepository;
    private final InterazionePartecipanteRepository interazioneRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void run(String... args) {
        if (partecipanteRepository.count() == 0) {
            log.info("Database vuoto. Avvio importazione ETL da Excel...");
            importaDatiExcel();
        } else {
            log.info("Database già popolato. Salto l'importazione ETL.");
        }
    }

    @Transactional
    public void importaDatiExcel() {
        try {
            InputStream is = new ClassPathResource("Dataset Evento Congresso 2025.xlsx").getInputStream();
            Workbook workbook = WorkbookFactory.create(is);

            // 1. Lettura e popolamento Touchpoints (Foglio 01_Interazioni)
            Sheet sheetInterazioni = workbook.getSheet("01_Interazioni");
            Map<String, Touchpoint> touchpointMap = caricaTouchpoints(sheetInterazioni);

            // 2. Lettura e popolamento Partecipanti e Interazioni (Foglio 02_Partecipanti)
            Sheet sheetPartecipanti = workbook.getSheet("02_Partecipanti");
            caricaPartecipantiEInterazioni(sheetPartecipanti, touchpointMap);

            workbook.close();
            log.info("Importazione ETL completata con successo!");
        } catch (Exception e) {
            log.error("Errore durante l'importazione ETL dell'Excel", e);
        }
    }

    private Map<String, Touchpoint> caricaTouchpoints(Sheet sheet) {
        Map<String, Touchpoint> map = new HashMap<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String intestazioneFoglio02 = getCellValueAsString(row.getCell(1));
            String nomeTecnico = getCellValueAsString(row.getCell(2));
            String tipoDato = getCellValueAsString(row.getCell(3));
            String faseStringa = getCellValueAsString(row.getCell(4));
            String descrizione = getCellValueAsString(row.getCell(5));

            if ("Anagrafica".equalsIgnoreCase(faseStringa)) {
                continue; // Le colonne anagrafiche non sono touchpoint
            }

            FaseEvento faseEnum = mappaFase(faseStringa);

            Touchpoint tp = Touchpoint.builder()
                    .codice(nomeTecnico)
                    .nome(intestazioneFoglio02)
                    .fase(faseEnum)
                    .tipoDato(tipoDato)
                    .descrizione(descrizione)
                    .build();

            tp = touchpointRepository.save(tp);
            map.put(intestazioneFoglio02, tp);
        }
        return map;
    }

    private void caricaPartecipantiEInterazioni(Sheet sheet, Map<String, Touchpoint> touchpointMap) {
        Row headerRow = sheet.getRow(0);
        List<String> headers = new ArrayList<>();
        for (Cell cell : headerRow) {
            headers.add(cell.getStringCellValue().trim());
        }

        List<Partecipante> partecipantiBatch = new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Long idExcel = (long) row.getCell(0).getNumericCellValue();
            String nomeCognome = getCellValueAsString(row.getCell(1));
            String email = getCellValueAsString(row.getCell(2));
            String tipologia = getCellValueAsString(row.getCell(3));
            String regione = getCellValueAsString(row.getCell(4));
            String canale = getCellValueAsString(row.getCell(5));
            Boolean inDbDem = parseBoolean(row.getCell(6));

            Partecipante partecipante = Partecipante.builder()
                    .excelId(idExcel)
                    .nomeCognome(nomeCognome)
                    .email(email)
                    .tipologiaStakeholder(tipologia)
                    .regione(regione)
                    .canaleIngaggio(canale)
                    .inDatabaseDem(inDbDem)
                    .build();

            for (int col = 6; col < headers.size(); col++) {
                String headerName = headers.get(col);
                Touchpoint tp = touchpointMap.get(headerName);

                if (tp == null) continue; // Colonna anagrafica o non censita nei touchpoint

                Cell cell = row.getCell(col);
                if (cell == null || isCellEmpty(cell)) continue;

                InterazionePartecipante interazione = InterazionePartecipante.builder()
                        .partecipante(partecipante)
                        .touchpoint(tp)
                        .build();

                valorizzaInterazione(interazione, tp.getTipoDato(), cell);
                partecipante.getInterazioni().add(interazione);
            }

            partecipantiBatch.add(partecipante);
        }

        partecipanteRepository.saveAll(partecipantiBatch);
    }

    private FaseEvento mappaFase(String faseStr) {
        String f = faseStr.toLowerCase();
        if (f.contains("pre-evento")) return FaseEvento.PRE_EVENTO;
        if (f.contains("on-site")) return FaseEvento.ON_SITE;
        if (f.contains("sessione")) return FaseEvento.SESSIONE;
        if (f.contains("post-evento")) return FaseEvento.POST_EVENTO;
        return FaseEvento.PRE_EVENTO;
    }

    private void valorizzaInterazione(InterazionePartecipante interazione, String tipoDato, Cell cell) {
        switch (tipoDato.toLowerCase()) {
            case "booleano":
                interazione.setValoreBooleano(parseBoolean(cell));
                break;
            case "conteggio":
            case "minuti":
                interazione.setValoreNumerico((int) cell.getNumericCellValue());
                break;
            case "tasso da 0 a 1":
                interazione.setValoreDecimale(cell.getNumericCellValue());
                break;
            case "data":
                interazione.setValoreData(parseLocalDate(cell));
                break;
            default:
                interazione.setValoreTesto(getCellValueAsString(cell));
                break;
        }
    }

    private Boolean parseBoolean(Cell cell) {
        if (cell == null) return false;
        if (cell.getCellType() == CellType.BOOLEAN) return cell.getBooleanCellValue();
        if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue() == 1.0;
        return "1".equalsIgnoreCase(getCellValueAsString(cell));
    }

    private LocalDate parseLocalDate(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        String val = getCellValueAsString(cell);
        if (val != null && !val.isBlank()) {
            return LocalDate.parse(val, DATE_FORMATTER);
        }
        return null;
    }

    private boolean isCellEmpty(Cell cell) {
        return cell.getCellType() == CellType.BLANK ||
                (cell.getCellType() == CellType.STRING && cell.getStringCellValue().isBlank());
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((long) cell.getNumericCellValue());
        return "";
    }
}
