package com.factoryStudios.gestione.congresso.services;

import com.factoryStudios.gestione.congresso.models.FaseEvento;
import com.factoryStudios.gestione.congresso.models.InterazionePartecipante;
import com.factoryStudios.gestione.congresso.models.Partecipante;
import com.factoryStudios.gestione.congresso.models.Touchpoint;
import com.factoryStudios.gestione.congresso.repositories.PartecipanteRepository;
import com.factoryStudios.gestione.congresso.repositories.TouchpointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EtlService {

    private final TouchpointRepository touchpointRepository;
    private final PartecipanteRepository partecipanteRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Importazione automatica del file Excel posizionato in src/main/resources.
     * Utilizza la ricerca dinamica per gestire qualsiasi variazione nel nome del file (.xlsx).
     */
    @Transactional
    public void importaDatiExcel() {
        try {
            // Caricamento diretto tramite ClassPathResource con il nome esatto
            Resource resource = new ClassPathResource("Dataset Evento Congresso 2025.xlsx");

            if (!resource.exists()) {
                throw new FileNotFoundException("File non trovato nel classpath: " + resource.getFilename());
            }

            log.info("Avvio importazione file da classpath: {}", resource.getFilename());

            try (InputStream is = resource.getInputStream()) {
                importaDatiDaInputStream(is, resource.getFilename());
            }
        } catch (Exception e) {
            log.error("Errore durante l'importazione ETL del file di default", e);
            throw new RuntimeException("Fallimento durante l'elaborazione ETL: " + e.getMessage(), e);
        }
    }

    /**
     * Importazione dinamica riutilizzabile sia da avvio statico che da upload via Controller HTTP.
     */
    @Transactional
    public void importaDatiDaInputStream(InputStream inputStream, String nomeFile) {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {

            // Recupero dei fogli per nome o per posizione di fallback
            Sheet sheetInterazioni = workbook.getSheet("01_Interazioni");
            if (sheetInterazioni == null && workbook.getNumberOfSheets() > 0) {
                sheetInterazioni = workbook.getSheetAt(0);
            }

            Sheet sheetPartecipanti = workbook.getSheet("02_Partecipanti");
            if (sheetPartecipanti == null && workbook.getNumberOfSheets() > 1) {
                sheetPartecipanti = workbook.getSheetAt(1);
            }

            if (sheetInterazioni == null || sheetPartecipanti == null) {
                throw new IllegalArgumentException("Il file Excel non contiene i fogli necessari per l'importazione.");
            }

            // 1. Lettura e popolamento Touchpoint dal foglio interazioni
            Map<String, Touchpoint> touchpointMap = caricaTouchpoints(sheetInterazioni);

            // 2. Lettura e popolamento Partecipanti e Interazioni dal foglio partecipanti
            caricaPartecipantiEInterazioni(sheetPartecipanti, touchpointMap);

            log.info("Importazione ETL completata con successo per il file: {}", nomeFile);
        } catch (Exception e) {
            log.error("Errore durante la lettura del file {}", nomeFile, e);
            throw new RuntimeException("Errore durante la lettura di " + nomeFile + ": " + e.getMessage(), e);
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
                continue;
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
            map.put(intestazioneFoglio02.trim(), tp);
        }
        return map;
    }

    private void caricaPartecipantiEInterazioni(Sheet sheet, Map<String, Touchpoint> touchpointMap) {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) return;

        List<String> headers = new ArrayList<>();
        for (Cell cell : headerRow) {
            headers.add(getCellValueAsString(cell).trim());
        }

        List<Partecipante> partecipantiBatch = new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Long idExcel = parseLong(row.getCell(0));
            String nomeCognome = getCellValueAsString(row.getCell(1));
            String email = getCellValueAsString(row.getCell(2));
            String tipologia = getCellValueAsString(row.getCell(3));
            String regione = getCellValueAsString(row.getCell(4));
            String canale = getCellValueAsString(row.getCell(5));
            Boolean inDbDem = parseBoolean(row.getCell(6)); // Colonna 6: In database DEM

            Partecipante partecipante = Partecipante.builder()
                    .excelId(idExcel)
                    .nomeCognome(nomeCognome)
                    .email(email)
                    .tipologiaStakeholder(tipologia)
                    .regione(regione)
                    .canaleIngaggio(canale)
                    .inDatabaseDem(inDbDem)
                    .interazioni(new ArrayList<>())
                    .build();

            // I touchpoint effettivi partono dalla colonna 7 (DEM inviata)
            for (int col = 7; col < headers.size(); col++) {
                String headerName = headers.get(col);
                Touchpoint tp = touchpointMap.get(headerName);

                if (tp == null) continue;

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

    private Boolean parseBoolean(Cell cell) {
        if (cell == null || isCellEmpty(cell)) return false;
        if (cell.getCellType() == CellType.BOOLEAN) return cell.getBooleanCellValue();
        if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue() == 1.0;

        String val = getCellValueAsString(cell).toLowerCase().trim();
        return "1".equals(val) || "true".equals(val) || "si".equals(val) || "sì".equals(val) || "x".equals(val) || "vero".equals(val);
    }

    private FaseEvento mappaFase(String faseStr) {
        if (faseStr == null) return FaseEvento.PRE_EVENTO;
        String f = faseStr.toLowerCase();
        if (f.contains("pre-evento")) return FaseEvento.PRE_EVENTO;
        if (f.contains("on-site")) return FaseEvento.ON_SITE;
        if (f.contains("sessione")) return FaseEvento.SESSIONE;
        if (f.contains("post-evento")) return FaseEvento.POST_EVENTO;
        return FaseEvento.PRE_EVENTO;
    }

    private void valorizzaInterazione(InterazionePartecipante interazione, String tipoDato, Cell cell) {
        if (tipoDato == null) return;
        switch (tipoDato.toLowerCase().trim()) {
            case "booleano":
                interazione.setValoreBooleano(parseBoolean(cell));
                break;
            case "conteggio":
            case "minuti":
                interazione.setValoreNumerico(parseInteger(cell));
                break;
            case "tasso da 0 a 1":
                interazione.setValoreDecimale(parseDouble(cell));
                break;
            case "data":
                interazione.setValoreData(parseLocalDate(cell));
                break;
            default:
                interazione.setValoreTesto(getCellValueAsString(cell));
                break;
        }
    }

    private static final DataFormatter DATA_FORMATTER_POI = new DataFormatter();

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        return DATA_FORMATTER_POI.formatCellValue(cell).trim();
    }

    private Integer parseInteger(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) return (int) cell.getNumericCellValue();
        String val = getCellValueAsString(cell);
        if (val.isBlank()) return null;
        try {
            return (int) Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long parseLong(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) return (long) cell.getNumericCellValue();
        String val = getCellValueAsString(cell);
        if (val.isBlank()) return null;
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseDouble(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();
        String val = getCellValueAsString(cell);
        if (val.isBlank()) return null;
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDate parseLocalDate(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        String val = getCellValueAsString(cell);
        if (val != null && !val.isBlank()) {
            try {
                return LocalDate.parse(val, DATE_FORMATTER);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private boolean isCellEmpty(Cell cell) {
        return cell == null || cell.getCellType() == CellType.BLANK ||
                (cell.getCellType() == CellType.STRING && cell.getStringCellValue().isBlank());
    }
}
