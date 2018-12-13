package no.nav.foreldrepenger.behandlingslager.kodeverk;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import no.nav.foreldrepenger.behandlingslager.diff.DiffIgnore;

@Entity
@Table(name = "KODELISTE_NAVN_I18N")
public class KodelisteNavnI18N extends KodeverkBaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_KODELISTE_NAVN_I18N")
    private Long id;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "kl_kodeverk", insertable = false, updatable = false),
        @JoinColumn(name = "kl_kode", insertable = false, updatable = false)
    })
    private Kodeliste kodeliste;

    @DiffIgnore
    @Column(name = "navn", updatable = false, insertable = false)
    private String navn;

    @DiffIgnore
    @Column(name = "sprak", nullable = false, updatable = false, insertable = false)
    private String språk;


    KodelisteNavnI18N() {
        // Hibernate trenger default constructor.
    }

    public KodelisteNavnI18N(String språk, String navn) {
        this.språk = språk;
        this.navn = navn;
    }

    public Long getId() {
        return id;
    }

    public String getSpråk() {
        return språk;
    }

    public Kodeliste getKodeliste() {
        return kodeliste;
    }

    public String getNavn() {
        return navn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KodelisteNavnI18N)) return false;
        KodelisteNavnI18N that = (KodelisteNavnI18N) o;
        return Objects.equals(kodeliste, that.kodeliste) &&
            Objects.equals(språk, that.språk);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kodeliste, språk);
    }

    @Override
    public String toString() {
        return "KodelisteNavnI18N{" +
            "kodeliste=" + kodeliste +
            ", navn='" + navn + '\'' +
            ", språk='" + språk + '\'' +
            '}';
    }
}
