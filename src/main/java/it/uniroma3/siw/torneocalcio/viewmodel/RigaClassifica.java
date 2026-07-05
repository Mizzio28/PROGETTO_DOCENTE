package it.uniroma3.siw.torneocalcio.viewmodel;

import it.uniroma3.siw.torneocalcio.model.Squadra;

/**
 * Riga della classifica di un torneo: statistiche di una squadra calcolate
 * a partire dalle partite giocate. Non è un DTO (non viene mai serializzato
 * in JSON), ma un oggetto di supporto alla vista Thymeleaf.
 */
public class RigaClassifica {

    private final Squadra squadra;
    private int vittorie;
    private int pareggi;
    private int sconfitte;
    private int golFatti;
    private int golSubiti;

    public RigaClassifica(Squadra squadra) {
        this.squadra = squadra;
    }

    public void registraVittoria(int golFatti, int golSubiti) {
        this.vittorie++;
        this.golFatti += golFatti;
        this.golSubiti += golSubiti;
    }

    public void registraPareggio(int golFatti, int golSubiti) {
        this.pareggi++;
        this.golFatti += golFatti;
        this.golSubiti += golSubiti;
    }

    public void registraSconfitta(int golFatti, int golSubiti) {
        this.sconfitte++;
        this.golFatti += golFatti;
        this.golSubiti += golSubiti;
    }

    public Squadra getSquadra() { return squadra; }
    public int getVittorie() { return vittorie; }
    public int getPareggi() { return pareggi; }
    public int getSconfitte() { return sconfitte; }
    public int getGolFatti() { return golFatti; }
    public int getGolSubiti() { return golSubiti; }
    public int getDifferenzaReti() { return golFatti - golSubiti; }
    public int getPunti() { return vittorie * 3 + pareggi; }
}