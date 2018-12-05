package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.EgenNæring;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.UtenlandskVirksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.kodeverk.VirksomhetType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.behandlingslager.diff.IndexKey;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;


@Table(name = "IAY_EGEN_NAERING")
@Entity(name = "EgenNæring")
class EgenNæringEntitet extends BaseEntitet implements EgenNæring, IndexKey {

    @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_EGEN_NAERING")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "oppgitt_opptjening_id", nullable = false, updatable = false)
    private OppgittOpptjeningEntitet oppgittOpptjening;

    @Embedded
    @ChangeTracked
    private DatoIntervallEntitet periode;

    @ManyToOne
    @JoinColumn(name = "virksomhet_id", updatable = false)
    @ChangeTracked
    private VirksomhetEntitet virksomhet;

    @ManyToOne
    @JoinColumnOrFormula(column = @JoinColumn(name = "virksomhet_type", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + VirksomhetType.DISCRIMINATOR + "'"))
    private VirksomhetType virksomhetType;

    @Column(name = "regnskapsfoerer_navn")
    private String regnskapsførerNavn;

    @Column(name = "regnskapsfoerer_tlf")
    private String regnskapsførerTlf;

    @Column(name = "endring_dato")
    private LocalDate endringDato;

    @Column(name = "begrunnelse")
    private String begrunnelse;

    @Column(name = "brutto_inntekt")
    private BigDecimal bruttoInntekt;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "nyoppstartet", nullable = false)
    private boolean nyoppstartet;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "varig_endring", nullable = false)
    private boolean varigEndring;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "naer_relasjon", nullable = false)
    private boolean nærRelasjon;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "ny_i_arbeidslivet", nullable = false)
    private boolean nyIArbeidslivet;

    @Embedded
    private UtenlandskVirksomhetEntitet utenlandskVirksomhet = new UtenlandskVirksomhetEntitet();

    EgenNæringEntitet() {
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(periode, virksomhet, utenlandskVirksomhet);
    }

    @Override
    public LocalDate getFraOgMed() {
        return periode.getFomDato();
    }

    @Override
    public LocalDate getTilOgMed() {
        return periode.getTomDato();
    }

    @Override
    public DatoIntervallEntitet getPeriode() {
        return periode;
    }

    @Override
    public VirksomhetType getVirksomhetType() {
        return virksomhetType;
    }

    void setVirksomhetType(VirksomhetType virksomhetType) {
        this.virksomhetType = virksomhetType;
    }

    @Override
    public Virksomhet getVirksomhet() {
        return virksomhet;
    }

    void setVirksomhet(Virksomhet virksomhet) {
        this.virksomhet = (VirksomhetEntitet) virksomhet;
    }

    @Override
    public String getRegnskapsførerNavn() {
        return regnskapsførerNavn;
    }

    void setRegnskapsførerNavn(String regnskapsførerNavn) {
        this.regnskapsførerNavn = regnskapsførerNavn;
    }

    @Override
    public String getRegnskapsførerTlf() {
        return regnskapsførerTlf;
    }

    void setRegnskapsførerTlf(String regnskapsførerTlf) {
        this.regnskapsførerTlf = regnskapsførerTlf;
    }

    @Override
    public LocalDate getEndringDato() {
        return endringDato;
    }

    void setEndringDato(LocalDate endringDato) {
        this.endringDato = endringDato;
    }

    @Override
    public BigDecimal getBruttoInntekt() {
        return bruttoInntekt;
    }

    void setBruttoInntekt(BigDecimal bruttoInntekt) {
        this.bruttoInntekt = bruttoInntekt;
    }

    @Override
    public String getBegrunnelse() {
        return begrunnelse;
    }

    void setBegrunnelse(String begrunnelse) {
        this.begrunnelse = begrunnelse;
    }

    @Override
    public boolean getNyoppstartet() {
        return nyoppstartet;
    }

    void setNyoppstartet(boolean nyoppstartet) {
        this.nyoppstartet = nyoppstartet;
    }

    void setNyIArbeidslivet(boolean nyIArbeidslivet) {
        this.nyIArbeidslivet = nyIArbeidslivet;
    }

    @Override
    public boolean getNyIArbeidslivet() {
        return nyIArbeidslivet;
    }

    @Override
    public boolean getVarigEndring() {
        return varigEndring;
    }

    void setVarigEndring(boolean varigEndring) {
        this.varigEndring = varigEndring;
    }

    @Override
    public boolean getNærRelasjon() {
        return nærRelasjon;
    }

    void setNærRelasjon(boolean nærRelasjon) {
        this.nærRelasjon = nærRelasjon;
    }

    @Override
    public UtenlandskVirksomhet getUtenlandskVirksomhet() {
        return utenlandskVirksomhet;
    }

    void setUtenlandskVirksomhet(UtenlandskVirksomhet utenlandskVirksomhet) {
        this.utenlandskVirksomhet = (UtenlandskVirksomhetEntitet) utenlandskVirksomhet;
    }

    public void setOppgittOpptjening(OppgittOpptjeningEntitet oppgittOpptjening) {
        this.oppgittOpptjening = oppgittOpptjening;
    }

    void setPeriode(DatoIntervallEntitet periode) {
        this.periode = periode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EgenNæringEntitet that = (EgenNæringEntitet) o;
        return Objects.equals(periode, that.periode) &&
            Objects.equals(virksomhet, that.virksomhet) &&
            Objects.equals(nyoppstartet, that.nyoppstartet) &&
            Objects.equals(virksomhetType, that.virksomhetType) &&
            Objects.equals(regnskapsførerNavn, that.regnskapsførerNavn) &&
            Objects.equals(regnskapsførerTlf, that.regnskapsførerTlf) &&
            Objects.equals(endringDato, that.endringDato) &&
            Objects.equals(begrunnelse, that.begrunnelse) &&
            Objects.equals(bruttoInntekt, that.bruttoInntekt) &&
            Objects.equals(utenlandskVirksomhet, that.utenlandskVirksomhet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, virksomhet, virksomhetType, nyoppstartet, regnskapsførerNavn, regnskapsførerTlf, endringDato, begrunnelse,
            bruttoInntekt, utenlandskVirksomhet);
    }

    @Override
    public String toString() {
        return "EgenNæringEntitet{" +
            "id=" + id +
            ", periode=" + periode +
            ", virksomhet=" + virksomhet +
            ", nyoppstartet=" + nyoppstartet +
            ", virksomhetType=" + virksomhetType +
            ", regnskapsførerNavn='" + regnskapsførerNavn + '\'' +
            ", regnskapsførerTlf='" + regnskapsførerTlf + '\'' +
            ", endringDato=" + endringDato +
            ", begrunnelse='" + begrunnelse + '\'' +
            ", bruttoInntekt=" + bruttoInntekt +
            ", utenlandskVirksomhet=" + utenlandskVirksomhet +
            '}';
    }
}
