package no.nav.foreldrepenger.behandlingslager.behandling.søknad;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.OppgittOpptjeningEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.OppgittOpptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittTilknytning;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittTilknytningEntitet;

@Entity(name = "Søknad")
@Table(name = "SO_SOEKNAD")
public class SøknadEntitet extends BaseEntitet implements Søknad {

    @Id
    @Column(name = "id", columnDefinition = "NUMERIC", length = 19)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SOEKNAD")
    private Long id;

    @Column(name = "soeknadsdato", nullable = false)
    private LocalDate søknadsdato;

    @Version
    @Column(name = "versjon", nullable = false, columnDefinition = "NUMERIC", length = 19)
    private long versjon;

    @Column(name = "mottatt_dato")
    private LocalDate mottattDato;

    @Column(name = "tilleggsopplysninger")
    private String tilleggsopplysninger;

    @OneToOne(fetch = FetchType.LAZY, cascade = { /* NONE! */ })
    @JoinColumn(name = "medlemskap_oppg_tilknyt_id", unique = true)
    private OppgittTilknytningEntitet oppgittTilknytning;

    @OneToMany(cascade = { CascadeType.ALL }, mappedBy = "søknad")
    private Set<SøknadVedleggEntitet> søknadVedlegg = new HashSet<>(2);

    @OneToOne
    @JoinColumn(name = "oppgitt_opptjening_id", updatable = false, unique = true)
    private OppgittOpptjeningEntitet oppgittOpptjening;

    SøknadEntitet() {
        // hibernate
    }

    /**
     * Deep copy.
     */
    SøknadEntitet(Søknad søknadMal) {
        this.mottattDato = søknadMal.getMottattDato();
        this.søknadsdato = søknadMal.getSøknadsdato();
        this.tilleggsopplysninger = søknadMal.getTilleggsopplysninger();

        if (søknadMal.getOppgittTilknytning() != null) {
            this.oppgittTilknytning = new OppgittTilknytningEntitet(søknadMal.getOppgittTilknytning());
        }
        for (SøknadVedlegg aSøknadVedlegg : søknadMal.getSøknadVedlegg()) {
            SøknadVedleggEntitet kopi = new SøknadVedleggEntitet(aSøknadVedlegg);
            kopi.setSøknad(this);
            this.søknadVedlegg.add(kopi);
        }

        this.oppgittOpptjening = (OppgittOpptjeningEntitet) søknadMal.getOppgittOpptjening();
    }

    public Long getId() {
        return id;
    }

    @Override
    public LocalDate getSøknadsdato() {
        return søknadsdato;
    }

    void setSøknadsdato(LocalDate søknadsdato) {
        this.søknadsdato = søknadsdato;
    }

    @Override
    public LocalDate getMottattDato() {
        return mottattDato;
    }

    void setMottattDato(LocalDate mottattDato) {
        this.mottattDato = mottattDato;
    }

    @Override
    public String getTilleggsopplysninger() {
        return tilleggsopplysninger;
    }

    void setTilleggsopplysninger(String tilleggsopplysninger) {
        this.tilleggsopplysninger = tilleggsopplysninger;
    }

    @Override
    public OppgittTilknytningEntitet getOppgittTilknytning() {
        return oppgittTilknytning;
    }

    void setOppgittTilknytning(OppgittTilknytning oppgittTilknytning) {
        this.oppgittTilknytning = (OppgittTilknytningEntitet) oppgittTilknytning;
    }

    @Override
    public Set<SøknadVedlegg> getSøknadVedlegg() {
        return Collections.unmodifiableSet(søknadVedlegg);
    }

    @Override
    public OppgittOpptjening getOppgittOpptjening() {
        return oppgittOpptjening;
    }

    public void setOppgittOpptjening(OppgittOpptjening oppgittOpptjening) {
        this.oppgittOpptjening = (OppgittOpptjeningEntitet) oppgittOpptjening;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof SøknadEntitet)) {
            return false;
        }
        SøknadEntitet other = (SøknadEntitet) obj;
        return Objects.equals(this.mottattDato, other.mottattDato)
            // Dette er ikke en god måte å gjøre ting på, men det er en løsning på at PersistentSet.equals ikke følger spec'en.
            && Objects.equals(this.søknadsdato, other.søknadsdato)
            && Objects.equals(this.oppgittTilknytning, other.oppgittTilknytning)
            && Objects.equals(this.tilleggsopplysninger, other.tilleggsopplysninger)
            && Objects.equals(this.søknadVedlegg, other.søknadVedlegg)
            && Objects.equals(this.oppgittOpptjening, other.oppgittOpptjening);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mottattDato, søknadsdato, oppgittTilknytning, tilleggsopplysninger,
            søknadVedlegg, oppgittOpptjening);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
            "<søknadsdato=" + søknadsdato //$NON-NLS-1$
            + ", mottattDato=" + mottattDato
            + ", tilleggsopplysninger=" + tilleggsopplysninger
            + ">"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static class Builder {
        private SøknadEntitet søknadMal;

        public Builder() {
            this(new SøknadEntitet());
        }

        public Builder(Søknad søknad) {
            if (søknad != null) {
                this.søknadMal = new SøknadEntitet(søknad);
            } else {
                this.søknadMal = new SøknadEntitet();
            }
        }

        public Builder medMottattDato(LocalDate mottattDato) {
            søknadMal.setMottattDato(mottattDato);
            return this;
        }

        public Builder medSøknadsdato(LocalDate søknadsdato) {
            søknadMal.setSøknadsdato(søknadsdato);
            return this;
        }

        public Builder medOppgittTilknytning(OppgittTilknytning oppgittTilknytning) {
            søknadMal.setOppgittTilknytning(oppgittTilknytning);
            return this;
        }

        public Builder medOppgittOpptjening(OppgittOpptjening oppgittOpptjening) {
            søknadMal.setOppgittOpptjening(oppgittOpptjening);
            return this;
        }

        public Builder medTilleggsopplysninger(String tilleggsopplysninger) {
            søknadMal.setTilleggsopplysninger(tilleggsopplysninger);
            return this;
        }

        public Builder leggTilVedlegg(SøknadVedlegg søknadVedlegg) {
            SøknadVedleggEntitet sve = new SøknadVedleggEntitet(søknadVedlegg);
            søknadMal.søknadVedlegg.add(sve);
            sve.setSøknad(søknadMal);
            return this;
        }

        public Søknad build() {
            return søknadMal;
        }
    }
}
