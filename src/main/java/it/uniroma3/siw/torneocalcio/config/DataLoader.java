package it.uniroma3.siw.torneocalcio.config;

import it.uniroma3.siw.torneocalcio.model.*;
import it.uniroma3.siw.torneocalcio.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Popola il database con un dataset di esempio (tornei, squadre, giocatori, arbitri,
 * partite, utenti e commenti) solo se il database è vuoto, così resta riproducibile
 * su ogni ambiente pulito senza duplicare dati ad ogni riavvio.
 *
 * Passa esclusivamente dal service layer (mai dai repository direttamente), come
 * farebbe un qualunque client applicativo, per restare coerente con l'architettura
 * a livelli del progetto.
 */
@Component
public class DataLoader implements CommandLineRunner {

    @Autowired private TorneoService torneoService;
    @Autowired private SquadraService squadraService;
    @Autowired private GiocatoreService giocatoreService;
    @Autowired private ArbitroService arbitroService;
    @Autowired private PartitaService partitaService;
    @Autowired private UserService userService;
    @Autowired private CommentoService commentoService;

    private static final String[] NOMI = {
        "Marco", "Luca", "Andrea", "Matteo", "Davide", "Simone",
        "Alessandro", "Francesco", "Lorenzo", "Giacomo", "Riccardo", "Federico"
    };
    private static final String[] COGNOMI = {
        "Rossi", "Bianchi", "Verdi", "Ferrari", "Russo", "Colombo",
        "Ricci", "Marino", "Greco", "Bruno", "Gallo", "Conti"
    };
    private static final String[] RUOLI = {
        "Portiere", "Difensore", "Difensore", "Difensore", "Difensore",
        "Centrocampista", "Centrocampista", "Centrocampista", "Centrocampista",
        "Attaccante", "Attaccante"
    };

    @Override
    @Transactional
    public void run(String... args) {
        if (torneoService.count() > 0) {
            return;
        }

        creaAdmin();

        Torneo coppaEstate = creaTorneo("Coppa Estate 2026", 2026,
            "Torneo estivo a eliminazione diretta tra le migliori squadre amatoriali della città.");
        Torneo campionatoRegionale = creaTorneo("Campionato Regionale 2026", 2026,
            "Campionato a girone unico tra le squadre amatoriali della regione.");

        List<Squadra> squadre = creaSquadre();
        // Coppa Estate: prime 6 squadre
        for (int i = 0; i < 6; i++) {
            squadraService.aggiungiSquadraATorneo(squadre.get(i).getId(), coppaEstate.getId());
        }
        // Campionato Regionale: squadre 3-8 (con sovrapposizione sulle squadre 3,4,5,6)
        for (int i = 2; i < 8; i++) {
            squadraService.aggiungiSquadraATorneo(squadre.get(i).getId(), campionatoRegionale.getId());
        }

        for (Squadra squadra : squadre) {
            creaRosaGiocatori(squadra);
        }

        List<Arbitro> arbitri = creaArbitri();

        List<Partita> partitePlayed = new ArrayList<>();
        partitePlayed.addAll(creaCalendarioPartite(coppaEstate, squadre.subList(0, 6), arbitri));
        partitePlayed.addAll(creaCalendarioPartite(campionatoRegionale, squadre.subList(2, 8), arbitri));

        List<String> usernames = creaUtenti();
        creaCommenti(partitePlayed, usernames);
    }

    private void creaAdmin() {
        User admin = new User();
        admin.setName("Admin");
        admin.setSurname("Admin");
        admin.setEmail("admin@torneocalcio.local");
        admin.setUsername("admin");
        admin.setPassword("admin123");
        userService.registerAdmin(admin);
    }

    private Torneo creaTorneo(String nome, int anno, String descrizione) {
        Torneo torneo = new Torneo();
        torneo.setNome(nome);
        torneo.setAnno(anno);
        torneo.setDescrizione(descrizione);
        return torneoService.saveTorneo(torneo);
    }

    private List<Squadra> creaSquadre() {
        String[][] datiSquadre = {
            {"Real Fontana FC", "Roma", "1998"},
            {"Atletico Garbatella", "Roma", "2003"},
            {"Vecchia Guardia United", "Roma", "1995"},
            {"Nuova Stella Calcio", "Roma", "2010"},
            {"Polisportiva Trastevere", "Roma", "2001"},
            {"San Lorenzo Boys", "Roma", "2008"},
            {"Ostiense Warriors", "Roma", "2015"},
            {"Testaccio Rangers", "Roma", "1999"}
        };
        List<Squadra> squadre = new ArrayList<>();
        for (String[] dati : datiSquadre) {
            Squadra squadra = new Squadra();
            squadra.setNome(dati[0]);
            squadra.setCitta(dati[1]);
            squadra.setAnnoFondazione(Integer.parseInt(dati[2]));
            squadre.add(squadraService.saveSquadra(squadra));
        }
        return squadre;
    }

    private void creaRosaGiocatori(Squadra squadra) {
        int base = (int) (squadra.getId() % 5);
        for (int i = 0; i < 11; i++) {
            Giocatore giocatore = new Giocatore();
            giocatore.setNome(NOMI[(base * 11 + i) % NOMI.length]);
            giocatore.setCognome(COGNOMI[(base * 7 + i) % COGNOMI.length]);
            giocatore.setRuolo(RUOLI[i]);
            giocatore.setDataNascita(LocalDate.of(1992 + (i % 12), 1 + (i % 12), 1 + (i * 2) % 27));
            giocatore.setAltezza(1.68 + (i % 6) * 0.04);
            giocatoreService.saveGiocatore(giocatore, squadra.getId());
        }
    }

    private List<Arbitro> creaArbitri() {
        List<Arbitro> arbitri = new ArrayList<>();
        arbitri.add(salvaArbitro("Mario", "Rossi", "ARB001"));
        arbitri.add(salvaArbitro("Luca", "Bianchi", "ARB002"));
        arbitri.add(salvaArbitro("Giovanni", "Verdi", "ARB003"));
        return arbitri;
    }

    private Arbitro salvaArbitro(String nome, String cognome, String codice) {
        Arbitro arbitro = new Arbitro();
        arbitro.setNome(nome);
        arbitro.setCognome(cognome);
        arbitro.setCodiceArbitrale(codice);
        return arbitroService.saveArbitro(arbitro);
    }

    private List<Partita> creaCalendarioPartite(Torneo torneo, List<Squadra> squadreTorneo, List<Arbitro> arbitri) {
        List<Partita> giocate = new ArrayList<>();
        LocalDateTime oggi = LocalDateTime.now().withHour(18).withMinute(0).withSecond(0).withNano(0);
        int numSquadre = squadreTorneo.size();
        int numPartite = numSquadre; // una partita per squadra circa, alternando giocate/programmate

        for (int i = 0; i < numPartite; i++) {
            Squadra home = squadreTorneo.get(i % numSquadre);
            Squadra away = squadreTorneo.get((i + 1) % numSquadre);
            if (home.equals(away)) continue;

            boolean giocata = i % 2 == 0;

            Partita partita = new Partita();
            partita.setLuogo("Stadio Comunale " + home.getCitta());
            partita.setDataOra(giocata ? oggi.minusDays(21 - (i * 3)) : oggi.plusDays(7 + (i * 3)));

            Arbitro arbitro = arbitri.get(i % arbitri.size());
            Partita salvata = partitaService.registraPartita(
                partita, torneo.getId(), home.getId(), away.getId(), arbitro.getId());

            if (giocata) {
                int golHome = (i * 2) % 4;
                int golAway = (i * 3) % 4;
                salvata = partitaService.inserisciRisultato(salvata.getId(), golHome, golAway);
                giocate.add(salvata);
            }
        }
        return giocate;
    }

    private List<String> creaUtenti() {
        List<String> usernames = new ArrayList<>();
        usernames.add(creaUtente("Marco", "Lupi", "marco.lupi@example.com", "marco.lupi"));
        usernames.add(creaUtente("Giulia", "Neri", "giulia.neri@example.com", "giulia.neri"));
        usernames.add(creaUtente("Davide", "Ferri", "davide.ferri@example.com", "davide.ferri"));
        return usernames;
    }

    private String creaUtente(String nome, String cognome, String email, String username) {
        User user = new User();
        user.setName(nome);
        user.setSurname(cognome);
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword("password123");
        userService.registerUser(user);
        return username;
    }

    private void creaCommenti(List<Partita> partitePlayed, List<String> usernames) {
        String[] testi = {
            "Bella partita, ottimo secondo tempo!",
            "L'arbitro ha diretto bene, complimenti.",
            "Che gol nel finale, non me lo aspettavo.",
            "Squadra in grande crescita quest'anno."
        };
        for (int i = 0; i < partitePlayed.size(); i++) {
            Partita partita = partitePlayed.get(i);
            String username = usernames.get(i % usernames.size());
            commentoService.addCommento(testi[i % testi.length], partita.getId(), username);
        }
    }
}