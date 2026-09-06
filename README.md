# Dashboard Analytics - Congresso Medico 2025

Piattaforma web per la digitalizzazione, la normalizzazione e l'analisi dei dati di ingaggio dei partecipanti al Congresso Medico 2025. Il sistema importa i dati grezzi da file Excel, li mappa su un database relazionale e fornisce un'interfaccia con grafici interattivi e tabelle paginate.

---

## 1. Guida all'Avvio

### Requisiti di Sistema
* **Java**: JDK 25 (o superiore)
* **Maven / Gradle**: Gradle o Maven (con wrapper incluso nel progetto)
* **Browser Web**: Chrome, Firefox, Edge o Safari recenti

### Istruzioni Passaggio per Passaggio

1. **Posizionarsi nella cartella di progetto**:
   ```bash
   cd congresso/congressoMedico
Compilazione ed Esecuzione dell'Applicazione:
Con Gradle Wrapper (Linux/macOS):

Bash
./gradlew bootRun
Con Gradle Wrapper (Windows):

DOS
gradlew.bat bootRun
(Oppure tramite Maven se presente il wrapper ./mvnw clean spring-boot:run)

Inizializzazione del Database ed ETL:
All'avvio, il servizio EtlService rileva automaticamente il file Dataset_Evento_Congresso_2025.xlsx presente in src/main/resources/ (o percorso configurato in application.properties), crea lo schema relazionale tramite Hibernate/JPA e popola il database.

Accesso alla Dashboard:
Aprire il browser e navigare all'indirizzo:

Plaintext
http://localhost:8080
2. Scelte Tecniche e Architettura
Backend
Spring Boot 3 (Java 25): Sfrutta le ultime novità e performance del JDK 25, garantendo type safety, robustezza ed elevata manutenibilità.

Spring Data JPA / Hibernate: Gestione dell'ORM e query astratte tramite Repository.

Apache POI: Utilizzato da EtlService per il parsing e l'estrazione efficiente del dataset Excel.

DTO Pattern: Disaccoppiamento tra il modello di dominio JPA e il payload JSON restituito al frontend tramite classi DTO dedicate (PartecipanteDTO, FunnelStepDTO, ConfrontoAnagraficoDTO, AndamentoGiornalieroDTO).

Database
Database Relazionale (H2 / PostgreSQL):

Motivazione: I dati del congresso presentano forti relazioni strutturate tra l'anagrafica del partecipante, il catalogo dei touchpoint e i singoli eventi di interazione. Un modello relazionale garantisce l'integrità referenziale e permette aggregazioni efficienti (GROUP BY, COUNT, JOIN) eseguite a livello di database.

Frontend
HTML5 / CSS3 (Bootstrap 5): Layout responsive e componenti di interfaccia puliti.

JavaScript (ES6+): Chiamate asincrone (fetch) agli endpoint REST per il popolamento dinamico delle viste senza ricaricare la pagina.

Chart.js: Rendering dei grafici interattivi (Funnel, Confronto Tipologie Stakeholder, Timeline dell'andamento giornaliero).

3. Modello Dati e Relazioni
Per evitare la ridondanza di un modello "piatto" a 24 colonne, il dataset è stato normalizzato in 3 entità principali:

Partecipante: Contiene le informazioni anagrafiche principali (id, excelId, nomeCognome, email, tipologiaStakeholder, regione, canaleIngaggio, inDatabaseDem).

Touchpoint: Catalogo delle fasi del percorso (codice, nome, fase, tipoDato). Le fasi sono modellate secondo il dizionario di dominio (PRE_EVENTO, ON_SITE, SESSIONE, POST_EVENTO).

InterazionePartecipante: Tabella di unione (1:N tra Partecipante e Touchpoint) che registra la singola azione svolta. Contiene campi generici per valorizzare la risposta in base al tipo:

valoreBooleano (es. DEM inviata = 1)

valoreNumerico (es. Permanenza min = 45)

valoreTesto (es. Risposte wordcloud)

valoreData (es. Giorno visita = 2025-10-14)

4. Anomalie nei Dati e Gestione
Durante la fase di ingestion con EtlService, sono state individuate e risolte le seguenti anomalie del file Excel:

Celle vuote vs Zero: Le celle prive di valore nei touchpoint quantitativi (es. minuti di permanenza o focus rate) indicano la mancata partecipazione e sono state memorizzate come NULL a DB per non distorcere le medie aritmetiche nei calcoli statistici.

Formati di Data Eterogenei: Date registrate in forma di stringa (es. 14/10/2025) o di seriale numerico Excel. Risolto implementando un parser multi-formato con DateTimeFormatter e controlli su DateUtil.isCellDateFormatted.

Incoerenza Intestazioni LinkedIn: Le colonne dedicate a LinkedIn presentavano nomenclature variabili. Sono state normalizzate allineandole ai codici ufficiali del catalogo touchpoint (linkedin_reach, linkedin_interazione).

Canali d'Ingaggio Eterogenei: Mappatura diretta tra i valori del DB (Database DEM, LinkedIn, On-site (stand), On-site (simposio)) e le dimensioni visualizzate nei grafici frontend (Email Direct, LinkedIn, Agente/Rete).

5. Osservazioni sui Dati e Limiti
Osservazioni Principali
Conversione per Canale d'Ingaggio: Il canale Database DEM genera il volume assoluto più alto di partecipanti (1.846), ma la presenza fisica allo stand ed al simposio mostra una percentuale di conversione proporzionalmente più elevata tra chi è stato ingaggiato via LinkedIn o tramite contatto diretto On-site.

Distribuzione degli Stakeholder: I medici specializzati in HCP - Diabetologo representan il segmento numericamente più rilevante (723 partecipanti complessivi), seguito da HCP - Medico di medicina generale e HCP - Infermiere / Educatore.

Picchi di Presenza On-Site: Le interazioni registrate allo stand e nella sala VIP si concentrano principalmente nelle prime due giornate dell'evento, con un calo fisiologico durante la sessione conclusiva.

Limiti dell'Analisi
Mancanza di un identificatore temporale (timestamp preciso) per le azioni digitali pre-evento (es. ora esatta di apertura della DEM).

Il dataset non traccia i costi associati ai singoli canali d'ingaggio, impedendo il calcolo del ROI esatto per canale.

6. Sviluppi Futuri
Con più tempo a disposizione, le estensioni prioritarie sarebbero:

Autenticazione e Profilazione: Introduzione di Spring Security con JWT per proteggere gli endpoint API e gestire ruoli (Admin, Viewer).

Caching con Redis: Caching dei risultati delle query analitiche aggregate per velocizzare il caricamento della dashboard in presenza di milioni di record.

Esportazione Report: Funzionalità di esportazione dati in formato PDF/Excel con i filtri correnti applicati.

Test di Integrazione: Ampliamento della suite di test unitari e di integrazione (JUnit 5, Testcontainers) per la pipeline ETL.
