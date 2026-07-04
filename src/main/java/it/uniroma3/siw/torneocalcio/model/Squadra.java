package it.uniroma3.siw.torneocalcio.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Squadra {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    private String nome;

    @NotNull
    private Integer annoFondazione;

    @NotBlank
    private String citta;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "squadra_torneo",
        joinColumns = @JoinColumn(name = "squadra_id"),
        inverseJoinColumns = @JoinColumn(name = "torneo_id")
    )
    private List<Torneo> tornei = new ArrayList<>();

    @OneToMany(mappedBy = "squadra", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Giocatore> giocatori = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Integer getAnnoFondazione() { return annoFondazione; }
    public void setAnnoFondazione(Integer annoFondazione) { this.annoFondazione = annoFondazione; }
    public String getCitta() { return citta; }
    public void setCitta(String citta) { this.citta = citta; }
    public List<Torneo> getTornei() { return tornei; }
    public void setTornei(List<Torneo> tornei) { this.tornei = tornei; }
    public List<Giocatore> getGiocatori() { return giocatori; }
    public void setGiocatori(List<Giocatore> giocatori) { this.giocatori = giocatori; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Squadra)) return false;
        Squadra s = (Squadra) o;
        return this.id != null && this.id.equals(s.id);
    }
    @Override
    public int hashCode() { return getClass().hashCode(); }
}
