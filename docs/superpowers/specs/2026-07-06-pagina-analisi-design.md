# Design: pagina "Analisi" (confronto strategie di fetch)

## Obiettivo

Aggiungere alla navbar pubblica una voce "Analisi" che porta a una pagina di
report statico sull'esperimento già esistente in `benchmark/`
(`FetchStrategyBenchmark` / `FetchStrategyService`): confronto tra strategie di
accesso ai dati JPA/Hibernate (LAZY, EntityGraph, EntityGraph+batch) sul caso
d'uso Torneo → Squadre → Giocatori, con numeri reali di query eseguite e tempi
di esecuzione, e discussione delle scelte implementative.

Scopo didattico: dimostrare capacità di progettare l'accesso ai dati in modo
efficiente, comprendere il comportamento di JPA/Hibernate, motivare le scelte
implementative del progetto.

## Decisioni chiave

- **Dati statici, non live.** La pagina non esegue query al momento della
  visita: i numeri sono catturati una volta eseguendo il benchmark
  (`mvn -Dspring-boot.run.profiles=benchmark spring-boot:run`) e passati come
  costanti Java al model. Motivo: evitare un endpoint pubblico che genera
  carico DB ripetuto e nessun bisogno di refactoring del benchmark (oggi è un
  `CommandLineRunner` da CLI, non pensato per essere richiamato da un
  controller web).
- **Pagina pubblica**, in `layout.html::navbar` (non nella navbar admin),
  coerente con "Tornei" che è già pubblica.
- **Route:** `GET /analisi`.
- **Aggiornamento dati:** manuale. Se in futuro si rilancia il benchmark e i
  numeri cambiano, si aggiornano a mano le costanti nel controller. Nessuna
  infrastruttura di lettura file/DB per un dato che cambia raramente.
- **Il benchmark esistente (`benchmark/`) non viene modificato.** Resta lo
  strumento che genera i dati; la pagina `/analisi` ne è solo la vetrina.

## Dati catturati (run del 2026-07-06, 2 tornei, 30 ripetizioni/strategia)

| Strategia | Descrizione | tempo/run (ms) | query/run | query totali |
|---|---|---|---|---|
| A | LAZY (default, N+1) | 14,283 | 16,00 | 480 |
| B | EntityGraph (solo squadre, giocatori lazy) | 6,957 | 14,00 | 420 |
| C | EntityGraph + batch (2 query fisse) | 6,306 | 4,00 | 120 |

Dimostrazione negativa: `MultipleBagFetchException` riprodotta su entrambi i
tornei di test, in due scenari (fetch join di `squadre`+`partite` sulla stessa
entità Torneo; fetch join di `squadre`+`giocatori` su due livelli).

Nota di onestà per la sezione "Discussione": in produzione oggi si usa la
strategia B (`TorneoService` → `TorneoRepository.findWithSquadreById`,
`@EntityGraph(attributePaths = {"squadre"})`) per la pagina di dettaglio
torneo. La strategia C (batch) è dimostrata nel benchmark come pattern
ottimale per evitare N+1 su liste di collezioni annidate, ma non è
attualmente cablata in un endpoint di produzione — va detto esplicitamente
nella pagina, non taciuto.

## Architettura

- **Controller:** nuovo `AnalisiController` (`@Controller`, nessuna
  autenticazione richiesta), `GET /analisi` → template `analisi/index.html`.
  I dati della tabella sono passati al model come costanti Java (record o
  campi statici nel controller stesso — non serve un modello di dominio per
  un dato così ristretto e stabile).
- **Template:** `src/main/resources/templates/analisi/index.html`, usa i
  fragment esistenti `head` e `navbar` da `fragments/layout.html`, stile
  Bootstrap 5 + classi custom già presenti in `style.css` (verde brand
  `--brand-green`).
- **Navbar:** nuova voce `<li class="nav-item"><a th:href="@{/analisi}">Analisi</a></li>`
  in `fragments/layout.html::navbar`, accanto a "Tornei".

## Contenuto della pagina (sezioni)

1. **Obiettivo** — 2-3 righe sul perché confrontare strategie di fetch.
2. **Metodologia** — caso d'uso testato, 2 tornei reali, 30 ripetizioni per
   strategia, warm-up escluso dalla misura, statistiche raccolte via Hibernate
   `Statistics` (query count + tempo). Riferimento al percorso del codice
   sorgente del benchmark (`benchmark/FetchStrategyBenchmark.java`,
   `benchmark/FetchStrategyService.java`) per riproducibilità.
3. **Risultati** — tabella con i numeri esatti della sezione precedente + due
   mini bar-chart CSS (uno per query/run, uno per tempo/run):
   - Un solo hue (verde brand `--brand-green`, coerente col resto del sito,
     non la palette placeholder di default), niente asse doppio (due chart
     separati per le due unità di misura diverse).
   - Etichette dirette sulle barre (3 categorie, niente legenda necessaria).
   - Barre come `<div>` con larghezza proporzionale in CSS puro, nessuna
     libreria JS aggiuntiva.
4. **Dimostrazione negativa** — box che spiega `MultipleBagFetchException`
   (perché Hibernate non permette fetch join di due collezioni "bag" nella
   stessa query) con i due casi riprodotti e il messaggio di eccezione reale.
5. **Discussione e scelte implementative** — perché LAZY genera N+1, perché
   EntityGraph dimezza ma non elimina il problema, perché la strategia batch
   è la più efficiente e scala meglio (query costanti indipendenti dal numero
   di squadre), nota onesta su quale strategia è realmente in uso in
   produzione oggi (vedi sopra).

## Fuori scope

- Nessuna esecuzione live del benchmark da web.
- Nessuna modifica al benchmark esistente o al profilo `benchmark`.
- Nessun refactoring dei repository di produzione per allinearli alla
  strategia C (non richiesto, la pagina si limita a documentare/discutere).
