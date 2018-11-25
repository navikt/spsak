package no.nav.foreldrepenger.behandlingslager.behandling.søknad;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
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
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "Søknad")
@Table(name = "SO_SOEKNAD")
public class SøknadEntitet extends BaseEntitet implements Søknad {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SOEKNAD")
    private Long id;

    @Column(name = "soeknadsdato", nullable = false)
    private LocalDate søknadsdato;

    @Column(name = "kilde_ref")
    private String kildeReferanse;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "elektronisk_registrert", nullable = false)
    private boolean elektroniskRegistrert;

    @Column(name = "mottatt_dato")
    private LocalDate mottattDato;

    @Column(name = "tilleggsopplysninger")
    private String tilleggsopplysninger;

    @OneToOne(fetch = FetchType.LAZY, cascade = { /* NONE! */ })
    @JoinColumn(name = "medlemskap_oppg_tilknyt_id", unique = true)
    private OppgittTilknytningEntitet oppgittTilknytning;

    @OneToMany(cascade = { CascadeType.ALL }, mappedBy = "søknad")
    private Set<SøknadVedleggEntitet> søknadVedlegg = new HashSet<>(2);

    @Column(name = "begrunnelse_for_sen_innsending")
    private String begrunnelseForSenInnsending;

    @OneToOne
    @JoinColumn(name = "oppgitt_opptjening_id", updatable = false, unique = true)
    private OppgittOpptjeningEntitet oppgittOpptjening;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "er_endringssoeknad", nullable = false)
    private boolean erEndringssøknad;

    SøknadEntitet() {
        // hibernate
    }

    /**
     * Deep copy.
     */
    SøknadEntitet(Søknad søknadMal) {
        this.begrunnelseForSenInnsending = søknadMal.getBegrunnelseForSenInnsending();
        this.elektroniskRegistrert = søknadMal.getElektroniskRegistrert();
        this.kildeReferanse = søknadMal.getKildeReferanse();
        this.mottattDato = søknadMal.getMottattDato();
        this.søknadsdato = søknadMal.getSøknadsdato();
        this.erEndringssøknad = søknadMal.erEndringssøknad();
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
    public String getKildeReferanse() {
        return kildeReferanse;
    }

    void setKildeReferanse(String kildeReferanse) {
        this.kildeReferanse = kildeReferanse;
    }

    @Override
    public boolean getElektroniskRegistrert() {
        return elektroniskRegistrert;
    }

    void setElektroniskRegistrert(boolean elektroniskRegistrert) {
        this.elektroniskRegistrert = elektroniskRegistrert;
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
    public String getBegrunnelseForSenInnsending() {
        return begrunnelseForSenInnsending;
    }

    void setBegrunnelseForSenInnsending(String begrunnelseForSenInnsending) {
        this.begrunnelseForSenInnsending = begrunnelseForSenInnsending;
    }

    @Override
    public boolean erEndringssøknad() {
        return erEndringssøknad;
    }

    void setErEndringssøknad(boolean endringssøknad) {
        this.erEndringssøknad = endringssøknad;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof SøknadEntitet)) {
            return false;
        }
        SøknadEntitet other = (SøknadEntitet) obj;
        return Objects.equals(this.elektroniskRegistrert, other.elektroniskRegistrert)
            && Objects.equals(this.kildeReferanse, other.kildeReferanse)
            && Objects.equals(this.mottattDato, other.mottattDato)
            // Dette er ikke en god måte å gjøre ting på, men det er en løsning på at PersistentSet.equals ikke følger spec'en.
            && Objects.equals(this.søknadsdato, other.søknadsdato)
            && Objects.equals(this.oppgittTilknytning, other.oppgittTilknytning)
            && Objects.equals(this.erEndringssøknad, other.erEndringssøknad)
            && Objects.equals(this.tilleggsopplysninger, other.tilleggsopplysninger)
            && Objects.equals(this.søknadVedlegg, other.søknadVedlegg)
            && Objects.equals(this.oppgittOpptjening, other.oppgittOpptjening)
            && Objects.equals(this.begrunnelseForSenInnsending, other.begrunnelseForSenInnsending);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elektroniskRegistrert, kildeReferanse, mottattDato,
            søknadsdato, oppgittTilknytning, erEndringssøknad, tilleggsopplysninger, søknadVedlegg, oppgittOpptjening, begrunnelseForSenInnsending);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
            "<søknadsdato=" + søknadsdato //$NON-NLS-1$
            + ", kildeReferanse=" + kildeReferanse
            + ", elektroniskRegistrert=" + elektroniskRegistrert
            + ", mottattDato=" + mottattDato
            + ", erEndringssøknad=" + erEndringssøknad
            + ", tilleggsopplysninger=" + tilleggsopplysninger
            + ", begrunnelseForSenInnsending=" + begrunnelseForSenInnsending
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

        public Builder medElektroniskRegistrert(boolean elektroniskRegistrert) {
            søknadMal.setElektroniskRegistrert(elektroniskRegistrert);
            return this;
        }

        public Builder medKildeReferanse(String kildeReferanse) {
            søknadMal.setKildeReferanse(kildeReferanse);
            return this;
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

        public Builder medBegrunnelseForSenInnsending(String begrunnelseForSenInnsending) {
            søknadMal.setBegrunnelseForSenInnsending(begrunnelseForSenInnsending);
            return this;
        }

        public Builder medErEndringssøknad(boolean erEndringssøknad) {
            søknadMal.setErEndringssøknad(erEndringssøknad);
            return this;
        }

        public Søknad build() {
            return søknadMal;
        }
    }
}
