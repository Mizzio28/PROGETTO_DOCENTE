# Design: conversione della sezione commenti in React

Data: 2026-07-05

## Contesto

Il progetto attuale gestisce i commenti alle partite interamente lato server con Thymeleaf
(`MainController`, template `partite/dettaglio.html` e `partite/modifica-commento.html`).
L'utente vuole una parte del progetto realizzata in React, seguendo esattamente il pattern
architetturale usato in un progetto di riferimento (`~/Documents/GitHub/SIW_Coppia/progetto_SIW_coppia`,
**da non modificare in alcun modo** — solo consultato come modello).

Il progetto di riferimento implementa "recensioni con voto" su un corso; qui la feature
equivalente sono i "commenti" su una partita (solo testo, nessun voto). Per decisione
esplicita dell'utente, si converte in React **solo la feature esistente** (creazione e
modifica del proprio commento), senza aggiungere un sistema di voto né la cancellazione
(funzionalità non presenti nell'app attuale).

## Pattern architetturale replicato dal riferimento

- Non una SPA separata: un singolo "isola" React montata in un punto preciso del DOM di una
  pagina Thymeleaf esistente.
- Sorgenti React in una cartella `frontend/` (progetto Vite separato, non sotto `src/`).
- Vite builda in **modalità libreria**, formato **IIFE**, con `outDir` puntato direttamente
  dentro `src/main/resources/static/react/<feature>/`, così il bundle finisce tra le risorse
  statiche di Spring Boot senza bisogno di un server Node separato.
- Build integrato in Maven tramite `frontend-maven-plugin` (fase `generate-resources`):
  scarica Node in locale (in `frontend/node/`, non serve Node di sistema) ed esegue
  `npm install` + `npm run build` automaticamente ad ogni `mvn package`/`spring-boot:run`.
- Il componente riceve i dati iniziali (id entità, utente corrente) via attributi `data-*`
  sul div host, renderizzati da Thymeleaf.
- CSRF gestito con due `<meta>` tag (`_csrf`, `_csrf_header`) letti da un modulo `api.js`
  condiviso, incluso automaticamente in ogni richiesta POST/PUT.
- Backend: `@RestController` dedicato sotto `/api/...`, DTO record separati per
  richiesta/risposta, `@RestControllerAdvice` scoped al package REST per risposte JSON
  di errore uniformi (`{"messaggio": "..."}`).

## Differenze deliberate rispetto al riferimento

- **Nessun voto**: `Commento` resta con solo `testo` (nessuna modifica al modello dati).
- **Nessuna eliminazione**: l'app attuale non espone cancellazione commenti; non la si
  aggiunge ora (fuori scope).
- **GET non pubblico**: nel riferimento `GET /api/corsi/{id}/recensioni` è pubblico; qui
  resta protetto da autenticazione, perché l'intera pagina `/partite/{id}` richiede già
  login (regola esistente in `SecurityConfiguration`, non modificata). Nessuna modifica
  alla configurazione di sicurezza è necessaria: la regola generale
  `anyRequest().authenticated()` copre già le nuove rotte `/api/**`.
- **Ownership via email, non username**: coerente con il fix IDOR già applicato
  (`CommentoService.getCommentoDiUtente`/`updateCommento`), il confronto di proprietà lato
  client usa l'email dell'utente autenticato, non lo username.
- **Stile**: classi Bootstrap (già adottate nel resto dell'app) invece di CSS custom puro,
  per coerenza visiva con le altre pagine.

## A. Frontend (`frontend/`)

- `package.json`: dependencies `react@^18`, `react-dom@^18`; devDependencies `vite`,
  `@vitejs/plugin-react`. Script `build: vite build`.
- `vite.config.js`: plugin React, build lib mode (`entry: src/main.jsx`, `formats: ['iife']`,
  `fileName: () => 'commenti.js'`), `outDir: '../src/main/resources/static/react/commenti'`,
  `emptyOutDir: true`.
- `src/main.jsx`: legge `#commenti-root`, estrae `data-partita-id` e `data-user-email` dal
  `dataset`, monta `<CommentiWidget partitaId={...} userEmail={...} />` con `createRoot`.
- `src/api.js`: funzioni `fetchCommenti(partitaId)`, `creaCommento(partitaId, testo)`,
  `modificaCommento(partitaId, id, testo)`; helper `csrfHeaders()` che legge i due `<meta>`
  tag; parsing errori che cerca `dati.messaggio` nel body JSON di risposta.
- `src/CommentiWidget.jsx`: stato `commenti`, `loading`, `errore`, `testo` (form nuovo
  commento), `editingId`/`editTesto` (form modifica inline). Effetto al mount per il fetch
  iniziale. Form di creazione visibile solo se `userEmail` è presente (utente autenticato).
  Bottone "Modifica" su una card visibile solo se `commento.autoreEmail === userEmail`.
  Markup con classi Bootstrap (`card`, `mb-3`, `form-control`, `btn btn-primary/secondary`).

## B. Backend

- Nuovo package `it.uniroma3.siw.torneocalcio.controller.rest`:
  - `CommentoRestController` (`@RestController`, `@RequestMapping("/api/partite/{partitaId}/commenti")`):
    - `GET` → `List<CommentoDTO>` da `commentoService.getCommentiByPartita(partitaId)`.
    - `POST` (`@Valid @RequestBody CommentoRequest`, `Authentication`) → chiama
      `commentoService.addCommento(testo, partitaId, authentication.getName())`, risponde
      `201 Created` con `CommentoDTO`.
    - `PUT /{id}` (`@Valid @RequestBody CommentoRequest`, `Authentication`) → chiama
      `commentoService.updateCommento(id, testo, authentication.getName())`; se
      `Optional` vuoto, lancia `ResponseStatusException(FORBIDDEN, "Non puoi modificare
      questo commento")`.
  - `RestExceptionHandler` (`@RestControllerAdvice(basePackages = "...controller.rest")`,
    `@Order(Ordered.HIGHEST_PRECEDENCE)`): gestisce `MethodArgumentNotValidException`
    (400, mappa campo→messaggio) e `ResponseStatusException` (status originale, body
    `{"messaggio": ex.getReason()}`).
- Nuovo package `it.uniroma3.siw.torneocalcio.dto` (già esistente per `RigaClassifica`):
  - `CommentoDTO` (record: `id, testo, dataCommento, autoreNome, autoreCognome, autoreEmail`),
    con factory statica `from(Commento c)`.
  - `CommentoRequest` (record: `@NotBlank @Size(max = 1000) String testo`).
- `CommentoService`: **nessuna modifica** — il nuovo controller riusa
  `getCommentiByPartita`, `addCommento`, `updateCommento` così come sono oggi.
- `MainController`: rimossi i metodi `addCommento`, `showModificaCommento`,
  `modificaCommento` (sostituiti dalla API REST). Resta `dettaglioPartita` (con il calcolo
  di `currentUserEmail` già presente, riusato per il `data-user-email`).
- Rimosso il template `partite/modifica-commento.html` (non più usato: la modifica ora è
  inline nel widget React).

## C. Template

- `fragments/layout.html`: aggiunti i due `<meta name="_csrf" th:content="${_csrf.token}"/>`
  e `<meta name="_csrf_header" th:content="${_csrf.headerName}"/>` nel fragment `head`,
  disponibili su tutte le pagine.
- `partite/dettaglio.html`: la sezione "Commenti" (attualmente il blocco
  `sec:authorize="isAuthenticated()"` con `th:each` sui commenti e il form di aggiunta)
  viene sostituita da:
  ```html
  <div id="commenti-root"
       th:attr="data-partita-id=${partita.id}, data-user-email=${currentUserEmail}">
  </div>
  <script src="/react/commenti/commenti.js"></script>
  ```
  Il messaggio "Accedi per vedere i commenti" per gli utenti anonimi resta invariato
  (comportamento preesistente, fuori scope: l'intera pagina richiede comunque login).

## D. Build

- `pom.xml`: aggiunto `frontend-maven-plugin` (com.github.eirslett) con working directory
  `frontend`, tre execution in fase `generate-resources`: `install-node-and-npm`,
  `npm install`, `npm run build`. Versione Node fissata (es. v20.x, la stessa major del
  riferimento) per riproducibilità.

## Testing / verifica

- `mvn spring-boot:run` deve buildare React automaticamente e produrre
  `target/classes/static/react/commenti/commenti.js`.
- Verifica manuale nel browser: lista commenti visibile, form di creazione funzionante,
  bottone "Modifica" visibile solo sul proprio commento, salvataggio funzionante.
- Verifica via `curl` degli endpoint REST (`GET`/`POST`/`PUT`), inclusi i casi di errore
  (403 su modifica di un commento altrui, 400 su testo vuoto).
- Verifica che i vecchi endpoint MVC rimossi non siano più referenziati da nessun template.