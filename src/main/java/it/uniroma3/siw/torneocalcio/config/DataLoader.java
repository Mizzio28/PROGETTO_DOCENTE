package it.uniroma3.siw.torneocalcio.config;

import it.uniroma3.siw.torneocalcio.model.*;
import it.uniroma3.siw.torneocalcio.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
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
 */
@Component
public class DataLoader implements CommandLineRunner {

    @Autowired private TorneoRepository torneoRepository;
    @Autowired private SquadraRepository squadraRepository;
    @Autowired private GiocatoreRepository giocatoreRepository;
    @Autowired private ArbitroRepository arbitroRepository;
    @Autowired private PartitaRepository partitaRepository;
    @Autowired private CredentialsRepository credentialsRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

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
        if (torneoRepository.count() > 0) {
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
            associaSquadraATorneo(squadre.get(i), coppaEstate);
        }
        // Campionato Regionale: squadre 3-8 (con sovrapposizione sulle squadre 3,4,5,6)
        for (int i = 2; i < 8; i++) {
            associaSquadraATorneo(squadre.get(i), campionatoRegionale);
        }

        for (Squadra squadra : squadre) {
            creaRosaGiocatori(squadra);
        }

        List<Arbitro> arbitri = creaArbitri();

        List<Partita> partitePlayed = new ArrayList<>();
        partitePlayed.addAll(creaCalendarioPartite(coppaEstate, squadre.subList(0, 6), arbitri));
        partitePlayed.addAll(creaCalendarioPartite(campionatoRegionale, squadre.subList(2, 8), arbitri));

        List<User> utenti = creaUtenti();
        creaCommenti(partitePlayed, utenti);
    }

    private void creaAdmin() {
        Credentials admin = new Credentials();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Credentials.ADMIN_ROLE);
        credentialsRepository.save(admin);
    }

    private Torneo creaTorneo(String nome, int anno, String descrizione) {
        Torneo torneo = new Torneo();
        torneo.setNome(nome);
        torneo.setAnno(anno);
        torneo.setDescrizione(descrizione);
        return torneoRepository.save(torneo);
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
            squadre.add(squadraRepository.save(squadra));
        }
        return squadre;
    }

    private void associaSquadraATorneo(Squadra squadra, Torneo torneo) {
        squadra.getTornei().add(torneo);
        squadraRepository.save(squadra);
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
            giocatore.setSquadra(squadra);
            giocatoreRepository.save(giocatore);
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
        return arbitroRepository.save(arbitro);
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

            Partita partita = new Partita();
            partita.setTorneo(torneo);
            partita.setSquadraHome(home);
            partita.setSquadraAway(away);
            partita.setArbitro(arbitri.get(i % arbitri.size()));
            partita.setLuogo("Stadio Comunale " + home.getCitta());

            boolean giocata = i % 2 == 0;
            if (giocata) {
                partita.setDataOra(oggi.minusDays(21 - (i * 3)));
                partita.setStato(StatoPartita.PLAYED);
                partita.setGoalsHome((i * 2) % 4);
                partita.setGoalsAway((i * 3) % 4);
            } else {
                partita.setDataOra(oggi.plusDays(7 + (i * 3)));
                partita.setStato(StatoPartita.SCHEDULED);
            }

            Partita salvata = partitaRepository.save(partita);
            if (giocata) {
                giocate.add(salvata);
            }
        }
        return giocate;
    }

    private List<User> creaUtenti() {
        List<User> utenti = new ArrayList<>();
        utenti.add(creaUtente("Marco", "Lupi", "marco.lupi@example.com", "marco.lupi"));
        utenti.add(creaUtente("Giulia", "Neri", "giulia.neri@example.com", "giulia.neri"));
        utenti.add(creaUtente("Davide", "Ferri", "davide.ferri@example.com", "davide.ferri"));
        return utenti;
    }

    private User creaUtente(String nome, String cognome, String email, String username) {
        User user = new User();
        user.setName(nome);
        user.setSurname(cognome);
        user.setEmail(email);
        user = userRepository.save(user);

        Credentials credentials = new Credentials();
        credentials.setUsername(username);
        credentials.setPassword(passwordEncoder.encode("password123"));
        credentials.setRole(Credentials.DEFAULT_ROLE);
        credentials.setUser(user);
        credentialsRepository.save(credentials);

        return user;
    }

    private void creaCommenti(List<Partita> partitePlayed, List<User> utenti) {
        String[] testi = {
            "Bella partita, ottimo secondo tempo!",
            "L'arbitro ha diretto bene, complimenti.",
            "Che gol nel finale, non me lo aspettavo.",
            "Squadra in grande crescita quest'anno."
        };
        for (int i = 0; i < partitePlayed.size(); i++) {
            Partita partita = partitePlayed.get(i);
            User autore = utenti.get(i % utenti.size());
            Commento commento = new Commento();
            commento.setTesto(testi[i % testi.length]);
            commento.setPartita(partita);
            commento.setAutore(autore);
            partita.getCommenti().add(commento);
        }
        partitaRepository.saveAll(partitePlayed);
    }
}