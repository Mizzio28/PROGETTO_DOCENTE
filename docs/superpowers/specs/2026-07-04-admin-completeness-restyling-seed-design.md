# Design: completamento admin, restyling Bootstrap, fix bug commenti, seed dati

Data: 2026-07-04

## Contesto

Il progetto `torneo-calcio` (Spring Boot 3.2, Java 17, Thymeleaf, Spring Security, JPA/Postgres) è
funzionante ma:

- alcune funzionalità admin esistono nel controller ma non sono raggiungibili da nessuna pagina
  (nessun link punta lì), o mancano del tutto (nessun CRUD per Arbitro, nessuna lista Partite in
  admin, nessuna eliminazione Torneo);
- l'estetica è minimale: un solo file CSS custom di ~30 righe, nessun framework, nessun layout
  condiviso (ogni template duplica l'intera navbar);
- la homepage contiene la frase "Gestione tornei di calcio amatoriale. Esplora i tornei attivi."
  da rimuovere;
- esiste un bug nel confronto di autorizzazione per la modifica dei commenti
  (`templates/partite/dettaglio.html`): confronta `#authentication.name` (username) con
  `c.autore.email`, che sono campi distinti — il link "Modifica" quasi non compare mai al vero
  autore;
- il database è vuoto: nessun dato di esempio per popolare le schermate.

## Obiettivo

1. Completare il CRUD amministrativo per tutte le entità (Torneo, Squadra, Giocatore, Partita,
   Arbitro), rendendo ogni funzionalità raggiungibile dall'interfaccia.
2. Restyling grafico con Bootstrap 5, rimuovendo la duplicazione di navbar tramite un fragment
   Thymeleaf condiviso, e rendendo la homepage più curata (hero section) rimuovendo la frase
   indicata.
3. Correggere il bug di autorizzazione sui commenti.
4. Aggiungere un `DataLoader` (CommandLineRunner) che popola il DB con un dataset di esempio di
   dimensione media, solo se il DB è vuoto — includendo la creazione dell'utente admin (sostituendo
   così la necessità dell'insert manuale fatto in precedenza).

## A. Completamento CRUD Admin

### Arbitri
- `GET /admin/arbitri` → nuova pagina lista (`admin/arbitri/lista.html`), con link "modifica" ed
  "elimina" per ogni arbitro, e link "+ Nuovo arbitro" (riusa il form esistente).
- `GET /admin/arbitri/{id}/modifica`, `POST /admin/arbitri/{id}/modifica` → nuovi endpoint in
  `AdminController`, riusano il template `admin/arbitri/form.html` esistente (già usato per la
  creazione) passando l'arbitro da modificare.
- `POST /admin/arbitri/{id}/elimina` → nuovo endpoint. Se l'arbitro ha partite collegate
  (`arbitro.getPartite()` non vuota), l'eliminazione viene bloccata e si torna alla lista con un
  messaggio d'errore flash ("Impossibile eliminare: arbitro assegnato a N partite").

### Partite (vista admin)
- `GET /admin/partite` → nuova pagina lista (`admin/partite/lista.html`) con torneo, squadre,
  data/ora, stato, risultato; link "inserisci/modifica risultato" (verso l'endpoint esistente
  `GET /admin/partite/{id}/risultato`) e link "elimina" (verso l'endpoint esistente
  `POST /admin/partite/{id}/elimina`, già implementato ma irraggiungibile).
- Filtro semplice per torneo tramite query param opzionale (`?torneoId=`), non obbligatorio.

### Tornei
- `POST /admin/tornei/{id}/elimina` → nuovo endpoint. Bloccato con messaggio d'errore se il torneo
  ha partite collegate.

### Giocatori
- In `admin/squadre/lista.html`, aggiungere link "modifica" per ogni giocatore elencato, verso
  l'endpoint esistente `GET /admin/giocatori/{id}/modifica` (già implementato, solo non
  collegato).

### Dashboard admin
- Aggiornare `admin/index.html` con i link alle nuove pagine lista (Arbitri, Partite), se non
  già presenti in forma di card/collegamenti.

## B. Restyling Bootstrap 5

- Nuovo fragment Thymeleaf condiviso `templates/fragments/layout.html` con:
  - `<head>` comune (meta, title placeholder, Bootstrap 5 CSS da CDN, `style.css` custom per gli
    override di brand);
  - navbar comune (logo, link Tornei, area login/registrati/logout/admin) come `th:fragment`.
- Tutti i template esistenti vengono aggiornati per usare `th:replace`/`th:insert` sul fragment
  invece di duplicare il markup della navbar.
- Bootstrap JS bundle (per eventuali componenti collapse/dropdown) incluso da CDN in fondo al
  fragment.
- Conversione delle classi custom (`.card`, `.grid`, `.btn-*`, tabelle, badge stato partita) alle
  classi Bootstrap equivalenti (`card`, `row`/`col`, `btn btn-*`, `table table-striped`,
  `badge text-bg-*`), mantenendo `style.css` solo per un piccolo override della palette
  (verde brand `#1a6b2e` su elementi chiave come navbar/bottoni primari).
- Homepage (`index.html`):
  - rimossa la frase "Gestione tornei di calcio amatoriale. Esplora i tornei attivi.";
  - aggiunta una hero section (contenitore Bootstrap con sfondo colorato/gradiente, titolo,
    sottotitolo d'atmosfera, bottoni "Scopri i tornei" → `/tornei` e "Registrati" → `/register`,
    quest'ultimo visibile solo se anonimo);
  - sotto la hero, resta la sezione "Tornei recenti" con le card esistenti, ristilizzate.

## C. Fix bug autorizzazione commenti

- In `MainController.dettaglioPartita`, se l'utente è autenticato, recupero le sue Credentials
  (`credentialsService.getCredentials(authentication.getName())`) e aggiungo al model l'attributo
  `currentUserEmail` con `credentials.getUser().getEmail()` (null se non autenticato o senza
  User collegato).
- In `templates/partite/dettaglio.html`, il controllo per mostrare il link "Modifica" diventa:
  `th:if="${currentUserEmail != null and currentUserEmail == c.autore.email}"` al posto del
  confronto errato con `#authentication.name`.

## D. DataLoader (seed dati)

- Nuova classe `DataLoader` (package `it.uniroma3.siw.torneocalcio.config`, insieme a
  `SecurityConfiguration`), `@Component implements CommandLineRunner`.
- Guard iniziale: se `torneoRepository.count() > 0`, esce subito (idempotente, non duplica dati a
  ogni riavvio).
- Crea, in ordine di dipendenza:
  1. **Admin**: `Credentials` con `username=admin`, `password` codificata con `PasswordEncoder`
     (`admin123`), `role=ADMIN`, senza `User` collegato (come nell'insert manuale già fatto).
  2. **2 Tornei** (es. "Coppa Estate 2026", "Campionato Regionale 2026").
  3. **6-8 Squadre**, distribuite sui 2 tornei tramite la relazione ManyToMany (alcune squadre
     condivise tra i due tornei), con nome/città/anno fondazione plausibili.
  4. **~10-12 Giocatori per squadra**, con ruoli coerenti (portiere, difensori, centrocampisti,
     attaccanti), date di nascita ed altezza plausibili.
  5. **2-3 Arbitri** con codice arbitrale univoco.
  6. **Partite**: un calendario misto per ciascun torneo tra le squadre assegnate, con stato
     `PLAYED` (con risultato e arbitro assegnato) per le partite passate e `SCHEDULED` (senza
     risultato) per alcune future.
  7. **2-3 Utenti normali** (`Credentials` con `role=DEFAULT`, `User` collegato) con password
     codificate, usati come autori di alcuni **Commenti** di esempio sulle partite giocate.
- Tutte le password vengono codificate tramite il bean `PasswordEncoder` già esistente
  (iniettato nel DataLoader), non tramite hash hardcoded.
- Nota pratica: il DB locale attuale ha già una riga `Credentials` con `username=admin` inserita
  manualmente in una sessione precedente. Prima di eseguire il DataLoader su questo DB verrà
  ripulita quella riga (o l'intero DB verrà svuotato), così il DataLoader parte da uno stato
  davvero vuoto e non incontra conflitti sul vincolo unique di `username`.

## Fuori scope (esplicitamente confermato con l'utente)

- Il file `static/react/partita-stats.js` (widget orfano) resta invariato, non viene collegato
  né rimosso.
- La password del DB in `application.properties` resta in chiaro (rischio accettato per progetto
  d'esame); non viene spostata su variabili d'ambiente in questo giro di lavoro.

## Testing / verifica

- Avvio con `mvn spring-boot:run` su DB pulito: verificare che il DataLoader popoli i dati e che
  login admin (`admin`/`admin123`) funzioni.
- Percorrere manualmente: lista/modifica/elimina per Arbitri, Partite, Tornei, Giocatori da admin;
  verificare i blocchi di eliminazione quando ci sono partite collegate.
- Verificare che il link "Modifica" su un commento compaia per l'autore autenticato e non per
  altri utenti.
- Controllo visivo di homepage, liste, dettagli e form admin con il nuovo stile Bootstrap, sia
  da loggato che da anonimo, sia come utente normale che come admin.