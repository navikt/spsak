package no.nav.foreldrepenger.behandlingslager.behandling.søknad;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.OppgittOpptjeningEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.OppgittOpptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.Sykefravær;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.SykefraværEntitet;

@Entity(name = "Søknad")
@Table(name = "SO_SOEKNAD")
public class SøknadEntitet extends BaseEntitet implements Søknad {

    @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SOEKNAD")
    private Long id;

    @Column(name = "soeknadsdato", nullable = false)
    private LocalDate søknadsdato;

    @Column(name = "mottatt_dato")
    private LocalDate mottattDato;

    @Embedded
    private Arbeidsgiver arbeidsgiver;

    @Column(name = "soeknad_referanse", nullable = false)
    private String søknadReferanse;

    @Column(name = "sykemelding_referanse", nullable = false)
    private String sykemeldingReferanse;

    @OneToOne
    @JoinColumn(name = "oppgitt_opptjening_id", updatable = false, unique = true)
    private OppgittOpptjeningEntitet oppgittOpptjening;

    @OneToOne
    @JoinColumn(name = "sykefravaer_id", updatable = false)
    private SykefraværEntitet sykefravær;


    @Column(name = "tilleggsopplysninger")
    private String tilleggsopplysninger;

    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "søknad")
    private Set<SøknadVedleggEntitet> søknadVedlegg = new HashSet<>(2);

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    SøknadEntitet() {
        // hibernate
    }

    /**
     * Deep copy.
     */
    SøknadEntitet(Søknad søknadMal) {
        this.mottattDato = søknadMal.getMottattDato();
        this.søknadsdato = søknadMal.getSøknadsdato();
        this.sykemeldingReferanse = søknadMal.getSykemeldingReferanse();
        this.søknadReferanse = søknadMal.getSøknadReferanse();
        this.oppgittOpptjening = (OppgittOpptjeningEntitet) søknadMal.getOppgittOpptjening();
        this.sykefravær = (SykefraværEntitet) søknadMal.getOppgittSykefravær();

        // TODO SP : Trenger vi disse?
        this.tilleggsopplysninger = søknadMal.getTilleggsopplysninger();
        for (SøknadVedlegg aSøknadVedlegg : søknadMal.getSøknadVedlegg()) {
            SøknadVedleggEntitet kopi = new SøknadVedleggEntitet(aSøknadVedlegg);
            kopi.setSøknad(this);
            this.søknadVedlegg.add(kopi);
        }
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
    public Set<SøknadVedlegg> getSøknadVedlegg() {
        return Collections.unmodifiableSet(søknadVedlegg);
    }

    @Override
    public OppgittOpptjening getOppgittOpptjening() {
        return oppgittOpptjening;
    }

    void setOppgittOpptjening(OppgittOpptjening oppgittOpptjening) {
        this.oppgittOpptjening = (OppgittOpptjeningEntitet) oppgittOpptjening;
    }

    @Override
    public Sykefravær getOppgittSykefravær() {
        return sykefravær;
    }

    void setOppgittSykefravær(Sykefravær sykefravær) {
        this.sykefravær = (SykefraværEntitet) sykefravær;
    }

    @Override
    public String getSøknadReferanse() {
        return søknadReferanse;
    }

    void setSøknadReferanse(String søknadReferanse) {
        this.søknadReferanse = søknadReferanse;
    }

    @Override
    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    void setArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        this.arbeidsgiver = arbeidsgiver;
    }

    @Override
    public String getSykemeldingReferanse() {
        return sykemeldingReferanse;
    }

    void setSykemeldingReferanse(String sykemeldingReferanse) {
        this.sykemeldingReferanse = sykemeldingReferanse;
    }

    @Override
    public String toString() {
        return "SøknadEntitet{" +
            "id=" + id +
            ", søknadsdato=" + søknadsdato +
            ", mottattDato=" + mottattDato +
            ", arbeidsgiver=" + arbeidsgiver +
            ", søknadReferanse='" + søknadReferanse + '\'' +
            ", sykemeldingReferanse='" + sykemeldingReferanse + '\'' +
            '}';
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

        public Builder medOppgittOpptjening(OppgittOpptjening oppgittOpptjening) {
            søknadMal.setOppgittOpptjening(oppgittOpptjening);
            return this;
        }

        public Builder medOppgittSykefravær(Sykefravær oppgittSykefravær) {
            søknadMal.setOppgittSykefravær(oppgittSykefravær);
            return this;
        }

        public Builder medSøknadReferanse(String søknadReferanse) {
            søknadMal.setSøknadReferanse(søknadReferanse);
            return this;
        }

        public Builder medSykemeldinReferanse(String sykemeldingReferanse) {
            søknadMal.setSykemeldingReferanse(sykemeldingReferanse);
            return this;
        }

        public Builder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
            søknadMal.setArbeidsgiver(arbeidsgiver);
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
