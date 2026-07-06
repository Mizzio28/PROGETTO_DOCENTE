# Pagina "Analisi" Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Aggiungere una pagina pubblica `/analisi` raggiungibile dalla navbar che presenta, come report statico, i risultati del benchmark esistente in `benchmark/` (confronto strategie di fetch JPA/Hibernate: LAZY, EntityGraph, EntityGraph+batch).

**Architecture:** Un nuovo `AnalisiController` (Spring MVC, `@Controller`) espone `GET /analisi` e passa al model due liste di record Java statici (dati catturati una volta dal benchmark, aggiornati a mano se rieseguito). Un nuovo template Thymeleaf `analisi/index.html` riusa i fragment esistenti `head`/`navbar` di `fragments/layout.html` e lo stile Bootstrap 5 + brand verde già presente in `style.css`. Nessuna query al DB avviene quando si visita `/analisi`.

**Tech Stack:** Spring Boot MVC, Thymeleaf, Bootstrap 5 (già in uso), CSS puro per i mini bar-chart (nessuna libreria JS aggiuntiva).

## Global Constraints

- Spec di riferimento: `docs/superpowers/specs/2026-07-06-pagina-analisi-design.md`.
- Dati statici, aggiornamento manuale — nessuna esecuzione live del benchmark da `/analisi`.
- Pagina pubblica (nessuna `@PreAuthorize`/`sec:authorize`), voce in `fragments/layout.html::navbar` (non in `adminNavbar`).
- Route: `GET /analisi`.
- Colore grafico: un solo hue, il verde brand esistente `var(--brand-green)` in `style.css` — non introdurre nuovi colori.
- Nessuna libreria di charting esterna; barre come `<div>` con larghezza percentuale CSS.
- **Adattamento al progetto:** il repository non ha infrastruttura di test automatici (nessuna dipendenza `spring-boot-starter-test`, nessuna cartella `src/test`). Non la introduciamo per questa feature isolata — coerente con lo stile esistente. Ogni task si verifica avviando l'app (`mvn spring-boot:run`, porta `8083` da `application.properties`) e ispezionando la risposta HTTP con `curl`, poi fermando il processo.

---

## Task 1: AnalisiController + template skeleton

**Files:**
- Create: `src/main/java/it/uniroma3/siw/torneocalcio/controller/AnalisiController.java`
- Create: `src/main/resources/templates/analisi/index.html`

**Interfaces:**
- Produces: `AnalisiController.RisultatoStrategia` record `(String nome, String descrizione, String tempoPerRun, String queryPerRun, int queryTotali, int tempoBarPercent, int queryBarPercent)` — usato da Task 4.
- Produces: `AnalisiController.DimostrazioneEccezione` record `(String scenario, String messaggio)` — usato da Task 3.
- Produces: model attributes `"risultati"` (`List<RisultatoStrategia>`) e `"dimostrazioni"` (`List<DimostrazioneEccezione>`) — usati dal template in Task 3/4.
- Produces: route `GET /analisi` → view `analisi/index`.

- [ ] **Step 1: Crea il controller con i dati statici catturati dal benchmark**

```java
package it.uniroma3.siw.torneocalcio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class AnalisiController {

    public record RisultatoStrategia(
        String nome,
        String descrizione,
        String tempoPerRun,
        String queryPerRun,
        int queryTotali,
        int tempoBarPercent,
        int queryBarPercent
    ) {}

    public record DimostrazioneEccezione(String scenario, String messaggio) {}

    private static final List<RisultatoStrategia> RISULTATI = List.of(
        new RisultatoStrategia(
            "A — LAZY (default, N+1)",
            "Ogni squadra genera una query separata per i giocatori.",
            "14,283 ms", "16,00", 480, 100, 100),
        new RisultatoStrategia(
            "B — EntityGraph (solo squadre)",
            "Le squadre sono caricate eager, i giocatori restano lazy per squadra.",
            "6,957 ms", "14,00", 420, 49, 88),
        new RisultatoStrategia(
            "C — EntityGraph + batch",
            "Una query per torneo+squadre, una query batch per tutti i giocatori.",
            "6,306 ms", "4,00", 120, 44, 25)
    );

    private static final List<DimostrazioneEccezione> DIMOSTRAZIONI = List.of(
        new DimostrazioneEccezione(
            "Fetch join di squadre + partite (stessa entità Torneo)",
            "org.hibernate.loader.MultipleBagFetchException: cannot simultaneously fetch multiple bags: [it.uniroma3.siw.torneocalcio.model.Torneo.partite, it.uniroma3.siw.torneocalcio.model.Torneo.squadre]"),
        new DimostrazioneEccezione(
            "Fetch join di squadre + giocatori (due livelli)",
            "org.hibernate.loader.MultipleBagFetchException: cannot simultaneously fetch multiple bags: [it.uniroma3.siw.torneocalcio.model.Squadra.giocatori, it.uniroma3.siw.torneocalcio.model.Torneo.squadre]")
    );

    @GetMapping("/analisi")
    public String analisi(Model model) {
        model.addAttribute("risultati", RISULTATI);
        model.addAttribute("dimostrazioni", DIMOSTRAZIONI);
        return "analisi/index";
    }
}
```

- [ ] **Step 2: Crea il template skeleton**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{fragments/layout :: head(title='Analisi')}"></head>
<body>
<nav th:replace="~{fragments/layout :: navbar}"></nav>

<div class="container py-5">
  <h1 class="mb-4">Analisi: strategie di accesso ai dati</h1>
</div>

</body>
</html>
```

- [ ] **Step 3: Compila e avvia l'app, verifica la route**

```bash
mvn -q compile
nohup mvn -q spring-boot:run > /tmp/analisi-app.log 2>&1 &
until curl -s -o /dev/null -w '%{http_code}' http://localhost:8083/analisi | grep -q 200; do sleep 2; done
curl -s http://localhost:8083/analisi | grep -o '<title>[^<]*</title>'
```

Expected: la seconda `curl` stampa `<title>Analisi - TorneoCalcio</title>` (il fragment `head` compone il titolo come `${title} + ' - TorneoCalcio'`).

- [ ] **Step 4: Ferma l'app**

```bash
pkill -f "spring-boot:run"
```

- [ ] **Step 5: Commit**

```bash
git add src/main/java/it/uniroma3/siw/torneocalcio/controller/AnalisiController.java src/main/resources/templates/analisi/index.html
git commit -m "Aggiunge route /analisi con dati statici del benchmark fetch"
```

---

## Task 2: Voce "Analisi" in navbar

**Files:**
- Modify: `src/main/resources/templates/fragments/layout.html` (fragment `navbar`, riga con `<li class="nav-item"><a class="nav-link" th:href="@{/tornei}">Tornei</a></li>`)

**Interfaces:**
- Consumes: route `GET /analisi` prodotta in Task 1.

- [ ] **Step 1: Aggiungi la voce di navbar dopo "Tornei"**

In `src/main/resources/templates/fragments/layout.html`, nel fragment `navbar` (non `adminNavbar`):

```html
      <ul class="navbar-nav me-auto">
        <li class="nav-item"><a class="nav-link" th:href="@{/tornei}">Tornei</a></li>
        <li class="nav-item"><a class="nav-link" th:href="@{/analisi}">Analisi</a></li>
      </ul>
```

(sostituisce il blocco `<ul class="navbar-nav me-auto">...</ul>` che oggi contiene solo la voce "Tornei", riga 25-27 di `layout.html`).

- [ ] **Step 2: Avvia l'app e verifica che il link compaia in home e nella pagina Analisi**

```bash
nohup mvn -q spring-boot:run > /tmp/analisi-app.log 2>&1 &
until curl -s -o /dev/null -w '%{http_code}' http://localhost:8083/ | grep -q 200; do sleep 2; done
curl -s http://localhost:8083/ | grep -o 'href="/analisi"'
curl -s http://localhost:8083/analisi | grep -o 'href="/analisi"'
```

Expected: entrambi i comandi `curl | grep` stampano `href="/analisi"` (il link è presente sia in home che nella pagina stessa, perché entrambe includono lo stesso fragment `navbar`).

- [ ] **Step 3: Ferma l'app**

```bash
pkill -f "spring-boot:run"
```

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/templates/fragments/layout.html
git commit -m "Aggiunge voce Analisi alla navbar pubblica"
```

---

## Task 3: Sezioni testuali (Obiettivo, Metodologia, Dimostrazione negativa, Discussione)

**Files:**
- Modify: `src/main/resources/templates/analisi/index.html` (sostituisce il body creato in Task 1)

**Interfaces:**
- Consumes: model attribute `"dimostrazioni"` (`List<AnalisiController.DimostrazioneEccezione>`, campi `scenario()` e `messaggio()`) prodotto in Task 1.

- [ ] **Step 1: Sostituisci il contenuto del `<div class="container py-5">` con le quattro sezioni testuali**

Sostituisci in `src/main/resources/templates/analisi/index.html`:

```html
<div class="container py-5">
  <h1 class="mb-4">Analisi: strategie di accesso ai dati</h1>
</div>
```

con:

```html
<div class="container py-5">
  <h1 class="mb-4">Analisi: strategie di accesso ai dati</h1>

  <section class="mb-5">
    <h2>Obiettivo</h2>
    <p>
      Questa pagina raccoglie i risultati di un esperimento didattico: confrontare diverse
      strategie con cui Spring Data JPA e Hibernate possono caricare un grafo di dati collegato
      — in questo caso un Torneo, le sue Squadre e i Giocatori di ciascuna squadra — misurandone
      l'impatto in termini di numero di query eseguite e tempo di esecuzione. L'obiettivo non è
      trovare "la" strategia migliore in assoluto, ma mostrare come scelte di fetching diverse
      producano comportamenti molto diversi a parità di dato restituito, e motivare le scelte
      fatte in questo progetto.
    </p>
  </section>

  <section class="mb-5">
    <h2>Metodologia</h2>
    <p>
      L'esperimento carica lo stesso caso d'uso — Torneo → Squadre → Giocatori — con tre
      strategie di fetch diverse, sugli stessi 2 tornei presenti nel database, ripetendo
      ciascuna strategia 30 volte per attenuare il rumore di misura. Prima di ogni misura viene
      eseguito un giro di "warm-up" (per scaldare pool di connessioni e JIT) che non contribuisce
      al risultato. Il numero di query SQL eseguite viene contato tramite le statistiche interne
      di Hibernate (<code>org.hibernate.stat.Statistics</code>), il tempo con
      <code>System.nanoTime()</code>.
    </p>
    <ul>
      <li><strong>Strategia A — LAZY (default):</strong> nessun fetch esplicito, ogni collezione viene caricata al primo accesso.</li>
      <li><strong>Strategia B — EntityGraph su squadre:</strong> le squadre vengono caricate insieme al torneo in un'unica query, i giocatori restano lazy.</li>
      <li><strong>Strategia C — EntityGraph + batch:</strong> le squadre vengono caricate insieme al torneo, poi un'unica query aggiuntiva carica tutti i giocatori di tutte le squadre insieme (<code>WHERE squadra_id IN (...)</code>).</li>
    </ul>
    <p class="text-muted small">
      Esperimento eseguibile con <code>mvn spring-boot:run -Dspring-boot.run.profiles=benchmark</code>.
      Codice sorgente: <code>benchmark/FetchStrategyBenchmark.java</code> (orchestrazione e misura)
      e <code>benchmark/FetchStrategyService.java</code> (le tre strategie).
    </p>
  </section>

  <section class="mb-5">
    <h2>Dimostrazione negativa: MultipleBagFetchException</h2>
    <p>
      Un tentativo naturale per evitare l'N+1 sarebbe fare fetch join di più collezioni nella
      stessa query JPQL. Hibernate però non lo permette quando due delle collezioni coinvolte
      sono di tipo <code>List</code> (una "bag" in terminologia Hibernate): il motore non sa
      come costruire il prodotto cartesiano dei risultati e lancia
      <code>MultipleBagFetchException</code>. Ecco la stessa eccezione riprodotta dal vivo su
      due scenari diversi:
    </p>
    <div class="alert alert-secondary" th:each="d : ${dimostrazioni}">
      <strong th:text="${d.scenario()}"></strong>
      <div><code th:text="${d.messaggio()}"></code></div>
    </div>
  </section>

  <section>
    <h2>Discussione e scelte implementative</h2>
    <p>
      La strategia A (LAZY) è quella che genera il classico problema N+1: una query per il
      torneo, una per le squadre, e poi una query aggiuntiva per i giocatori di OGNI squadra.
      È il costo più alto misurato, sia in numero di query che in tempo.
    </p>
    <p>
      La strategia B (EntityGraph su squadre) riduce il problema: il torneo e le sue squadre
      arrivano in un'unica query, ma i giocatori restano lazy e vengono comunque caricati una
      squadra alla volta. Il numero di query scende, ma l'N+1 sui giocatori non è eliminato.
    </p>
    <p>
      La strategia C (EntityGraph + batch) è quella più efficiente: dopo aver caricato
      torneo+squadre in una query, un'unica query batch (<code>findBySquadraIdIn</code>)
      recupera tutti i giocatori di tutte le squadre insieme. Il numero di query resta
      costante — sempre 2 — indipendentemente da quante squadre partecipano al torneo, il che
      la rende anche la più scalabile.
    </p>
    <p>
      Ad oggi il progetto usa in produzione la strategia B
      (<code>TorneoService.getTorneoConDettagli</code>, tramite
      <code>TorneoRepository.findWithSquadreById</code> con
      <code>@EntityGraph(attributePaths = {"squadre"})</code>) per la pagina di dettaglio
      torneo. La strategia C, pur risultando la più efficiente in questo esperimento, non è al
      momento cablata in un endpoint di produzione: è dimostrata qui come pattern di riferimento
      per evitare N+1 su collezioni annidate, utile ad esempio se in futuro si volesse mostrare
      l'elenco completo dei giocatori di tutte le squadre di un torneo in un'unica vista.
    </p>
  </section>
</div>
```

- [ ] **Step 2: Avvia l'app e verifica che tutte le sezioni siano presenti**

```bash
nohup mvn -q spring-boot:run > /tmp/analisi-app.log 2>&1 &
until curl -s -o /dev/null -w '%{http_code}' http://localhost:8083/analisi | grep -q 200; do sleep 2; done
curl -s http://localhost:8083/analisi | grep -oE '<h2>[^<]*</h2>'
curl -s http://localhost:8083/analisi | grep -c 'MultipleBagFetchException'
```

Expected: il primo comando stampa in ordine `<h2>Obiettivo</h2>`, `<h2>Metodologia</h2>`, `<h2>Dimostrazione negativa: MultipleBagFetchException</h2>`, `<h2>Discussione e scelte implementative</h2>`; il secondo stampa `2` (le due dimostrazioni).

- [ ] **Step 3: Ferma l'app**

```bash
pkill -f "spring-boot:run"
```

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/templates/analisi/index.html
git commit -m "Aggiunge sezioni testuali alla pagina Analisi"
```

---

## Task 4: Sezione Risultati (tabella + bar chart CSS)

**Files:**
- Modify: `src/main/resources/templates/analisi/index.html` (inserisce la sezione "Risultati" prima di "Dimostrazione negativa")
- Modify: `src/main/resources/static/css/style.css` (aggiunge le classi `.fetch-chart*`)

**Interfaces:**
- Consumes: model attribute `"risultati"` (`List<AnalisiController.RisultatoStrategia>`, campi `nome()`, `descrizione()`, `tempoPerRun()`, `queryPerRun()`, `queryTotali()`, `tempoBarPercent()`, `queryBarPercent()`) prodotto in Task 1.
- Consumes: variabile CSS `--brand-green` già definita in `style.css:2`.

- [ ] **Step 1: Aggiungi le classi CSS per i mini bar-chart**

Aggiungi in fondo a `src/main/resources/static/css/style.css`:

```css

.fetch-chart {
  margin-bottom: 14px;
}
.fetch-chart-label {
  display: flex;
  justify-content: space-between;
  font-size: 0.9rem;
  margin-bottom: 4px;
}
.fetch-bar-track {
  background-color: #e1e0d9;
  border-radius: 4px;
  height: 10px;
  overflow: hidden;
}
.fetch-bar-fill {
  background-color: var(--brand-green);
  height: 100%;
  border-radius: 4px;
}
```

- [ ] **Step 2: Inserisci la sezione "Risultati" (tabella + bar chart) prima di "Dimostrazione negativa"**

In `src/main/resources/templates/analisi/index.html`, inserisci subito prima di `<section class="mb-5">\n    <h2>Dimostrazione negativa: MultipleBagFetchException</h2>`:

```html
  <section class="mb-5">
    <h2>Risultati</h2>
    <div class="table-responsive mb-4">
      <table class="table table-bordered align-middle">
        <thead>
          <tr>
            <th>Strategia</th>
            <th>Descrizione</th>
            <th>Tempo / run</th>
            <th>Query / run</th>
            <th>Query totali</th>
          </tr>
        </thead>
        <tbody>
          <tr th:each="r : ${risultati}">
            <td th:text="${r.nome()}"></td>
            <td th:text="${r.descrizione()}"></td>
            <td th:text="${r.tempoPerRun()}"></td>
            <td th:text="${r.queryPerRun()}"></td>
            <td th:text="${r.queryTotali()}"></td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="row g-4">
      <div class="col-md-6">
        <h3 class="h6 text-muted">Tempo per esecuzione</h3>
        <div class="fetch-chart" th:each="r : ${risultati}">
          <div class="fetch-chart-label">
            <span th:text="${r.nome()}"></span>
            <span th:text="${r.tempoPerRun()}"></span>
          </div>
          <div class="fetch-bar-track">
            <div class="fetch-bar-fill" th:style="'width: ' + ${r.tempoBarPercent()} + '%'"></div>
          </div>
        </div>
      </div>
      <div class="col-md-6">
        <h3 class="h6 text-muted">Query per esecuzione</h3>
        <div class="fetch-chart" th:each="r : ${risultati}">
          <div class="fetch-chart-label">
            <span th:text="${r.nome()}"></span>
            <span th:text="${r.queryPerRun()}"></span>
          </div>
          <div class="fetch-bar-track">
            <div class="fetch-bar-fill" th:style="'width: ' + ${r.queryBarPercent()} + '%'"></div>
          </div>
        </div>
      </div>
    </div>
  </section>

```

- [ ] **Step 3: Avvia l'app e verifica tabella e bar chart**

```bash
nohup mvn -q spring-boot:run > /tmp/analisi-app.log 2>&1 &
until curl -s -o /dev/null -w '%{http_code}' http://localhost:8083/analisi | grep -q 200; do sleep 2; done
curl -s http://localhost:8083/analisi | grep -c 'fetch-bar-fill'
curl -s http://localhost:8083/analisi | grep -o 'width: 100%'
curl -s http://localhost:8083/analisi | grep -o '14,283 ms'
```

Expected: il primo comando stampa `6` (3 strategie × 2 chart), il secondo trova almeno un `width: 100%` (la barra della strategia A, baseline), il terzo trova `14,283 ms` (valore della strategia A in tabella e in chart).

- [ ] **Step 4: Ferma l'app e apri la pagina nel browser per un controllo visivo**

```bash
pkill -f "spring-boot:run"
```

Apri manualmente `http://localhost:8083/analisi` in un browser (con l'app riavviata) per controllare che non ci siano sovrapposizioni di testo, che le barre siano leggibili e che il verde brand sia coerente col resto del sito.

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/templates/analisi/index.html src/main/resources/static/css/style.css
git commit -m "Aggiunge tabella e bar chart dei risultati alla pagina Analisi"
```
