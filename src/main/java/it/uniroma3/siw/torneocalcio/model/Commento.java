package it.uniroma3.siw.torneocalcio.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
public class Commento {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    @Column(length = 1000)
    private String testo;

    private LocalDateTime dataCommento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partita_id")
    private Partita partita;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User autore;

    @PrePersist
    public void prePersist() {
        this.dataCommento = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTesto() { return testo; }
    public void setTesto(String testo) { this.testo = testo; }
    public LocalDateTime getDataCommento() { return dataCommento; }
    public void setDataCommento(LocalDateTime dataCommento) { this.dataCommento = dataCommento; }
    public Partita getPartita() { return partita; }
    public void setPartita(Partita partita) { this.partita = partita; }
    public User getAutore() { return autore; }
    public void setAutore(User autore) { this.autore = autore; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Commento)) return false;
        Commento c = (Commento) o;
        return this.id != null && this.id.equals(c.id);
    }
    @Override
    public int hashCode() { return getClass().hashCode(); }
}
