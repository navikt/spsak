package no.nav.foreldrepenger.behandlingslager.behandling.beregning;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;


@Entity(name = "BeregningsresultatAndel")
@Table(name = "BEREGNINGSRESULTAT_ANDEL")
public class BeregningsresultatAndel extends BaseEntitet {

    @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BEREGNINGSRESULTAT_ANDEL")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @ManyToOne(optional = false)
    @JoinColumn(name = "br_periode_id", nullable = false, updatable = false)
    @JsonBackReference
    private BeregningsresultatPeriode beregningsresultatPeriode;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "bruker_er_mottaker", nullable = false)
    private Boolean brukerErMottaker;

    @ManyToOne(optional = true)
    @JoinColumn(name = "virksomhet_id", nullable = true, updatable = false)
    private VirksomhetEntitet virksomhet;

    @Embedded
    private ArbeidsforholdRef arbeidsforholdRef;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "arbeidsforhold_type", referencedColumnName = "kode"))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + OpptjeningAktivitetType.DISCRIMINATOR + "'"))
    private OpptjeningAktivitetType arbeidsforholdType;

    @Column(name = "dagsats", nullable = false, columnDefinition = "INT8")
    private int dagsats;

    @Column(name = "stillingsprosent", nullable = false)
    private BigDecimal stillingsprosent;

    @Column(name = "utbetalingsgrad", nullable = false)
    private BigDecimal utbetalingsgrad;

    @Column(name = "dagsats_fra_bg", nullable = false, columnDefinition = "INT8")
    private int dagsatsFraBg;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "beregningsresultatAndel", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<BeregningsresultatFeriepengerPrÅr> beregningsresultatFeriepengerPrÅrListe = new ArrayList<>();

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "aktivitet_status", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + AktivitetStatus.DISCRIMINATOR + "'"))
    private AktivitetStatus aktivitetStatus;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "inntektskategori", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + Inntektskategori.DISCRIMINATOR + "'"))
    private Inntektskategori inntektskategori;

    public Long getId() {
        return id;
    }

    public BeregningsresultatPeriode getBeregningsresultatPeriode() {
        return beregningsresultatPeriode;
    }

    public boolean erBrukerMottaker() {
        return brukerErMottaker;
    }

    public ArbeidsforholdRef getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    public String getArbeidsforholdOrgnr() {
        return virksomhet == null ? null : virksomhet.getOrgnr();
    }

    public Virksomhet getVirksomhet() {
        return virksomhet;
    }

    public OpptjeningAktivitetType getArbeidsforholdType() {
        return arbeidsforholdType;
    }

    public int getDagsats() {
        return dagsats;
    }

    public BigDecimal getStillingsprosent() {
        return stillingsprosent;
    }

    public BigDecimal getUtbetalingsgrad() {
        return utbetalingsgrad;
    }

    public int getDagsatsFraBg() {
        return dagsatsFraBg;
    }

    public List<BeregningsresultatFeriepengerPrÅr> getBeregningsresultatFeriepengerPrÅrListe() {
        return Collections.unmodifiableList(beregningsresultatFeriepengerPrÅrListe);
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BeregningsresultatAndel)) {
            return false;
        }
        BeregningsresultatAndel other = (BeregningsresultatAndel) obj;
        return Objects.equals(this.getVirksomhet(), other.getVirksomhet())
            && Objects.equals(this.getArbeidsforholdRef(), other.getArbeidsforholdRef())
            && Objects.equals(this.getArbeidsforholdType(), other.getArbeidsforholdType())
            && Objects.equals(this.getInntektskategori(), other.getInntektskategori())
            && Objects.equals(this.erBrukerMottaker(), other.erBrukerMottaker())
            && Objects.equals(this.getDagsats(), other.getDagsats())
            && Objects.equals(this.getStillingsprosent(), other.getStillingsprosent())
            && Objects.equals(this.getUtbetalingsgrad(), other.getUtbetalingsgrad())
            && Objects.equals(this.getDagsatsFraBg(), other.getDagsatsFraBg());
    }

    @Override
    public int hashCode() {
        return Objects.hash(brukerErMottaker, virksomhet, arbeidsforholdRef, arbeidsforholdType, dagsats, aktivitetStatus, dagsatsFraBg, inntektskategori);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(BeregningsresultatAndel eksisterendeBeregningsresultatAndel) {
        return new Builder(eksisterendeBeregningsresultatAndel);
    }

    public static class Builder {
        private BeregningsresultatAndel beregningsresultatAndelMal;

        public Builder() {
            beregningsresultatAndelMal = new BeregningsresultatAndel();
            beregningsresultatAndelMal.arbeidsforholdType = OpptjeningAktivitetType.UDEFINERT;
        }

        public Builder(BeregningsresultatAndel eksisterendeBeregningsresultatAndel) {
            beregningsresultatAndelMal = eksisterendeBeregningsresultatAndel;
        }

        public Builder medBrukerErMottaker(boolean brukerErMottaker) {
            beregningsresultatAndelMal.brukerErMottaker = brukerErMottaker;
            return this;
        }

        public Builder medVirksomhet(VirksomhetEntitet virksomhet) {
            beregningsresultatAndelMal.virksomhet = virksomhet;
            return this;
        }

        public Builder medArbforholdId(String arbforholdId) {
            beregningsresultatAndelMal.arbeidsforholdRef = arbforholdId == null ? null : ArbeidsforholdRef.ref(arbforholdId);
            return this;
        }

        public Builder medArbforholdType(OpptjeningAktivitetType arbforholdType) {
            beregningsresultatAndelMal.arbeidsforholdType = arbforholdType;
            return this;
        }

        public Builder medDagsats(int dagsats) {
            beregningsresultatAndelMal.dagsats = dagsats;
            return this;
        }

        public Builder medStillingsprosent(BigDecimal stillingsprosent) {
            beregningsresultatAndelMal.stillingsprosent = stillingsprosent;
            return this;
        }

        public Builder medUtbetalingsgrad(BigDecimal utbetalingsgrad) {
            beregningsresultatAndelMal.utbetalingsgrad = utbetalingsgrad;
            return this;
        }

        public Builder medDagsatsFraBg(int dagsatsFraBg) {
            beregningsresultatAndelMal.dagsatsFraBg = dagsatsFraBg;
            return this;
        }

        public Builder medAktivitetstatus(AktivitetStatus aktivitetStatus) {
            beregningsresultatAndelMal.aktivitetStatus = aktivitetStatus;
            return this;
        }

        public Builder medInntektskategori(Inntektskategori inntektskategori) {
            beregningsresultatAndelMal.inntektskategori = inntektskategori;
            return this;
        }

        public Builder leggTilBeregningsresultatFeriepengerPrÅr(BeregningsresultatFeriepengerPrÅr beregningsresultatFeriepengerPrÅr) {
            beregningsresultatAndelMal.beregningsresultatFeriepengerPrÅrListe.add(beregningsresultatFeriepengerPrÅr);
            return this;
        }

        public BeregningsresultatAndel build(BeregningsresultatPeriode beregningsresultatPeriode) {
            beregningsresultatAndelMal.beregningsresultatPeriode = beregningsresultatPeriode;
            verifyStateForBuild();
            beregningsresultatAndelMal.getBeregningsresultatPeriode()
                .addBeregningsresultatAndel(beregningsresultatAndelMal);
            return beregningsresultatAndelMal;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(beregningsresultatAndelMal.beregningsresultatPeriode, "beregningsresultatPeriode");
            Objects.requireNonNull(beregningsresultatAndelMal.brukerErMottaker, "brukerErMottaker");
            Objects.requireNonNull(beregningsresultatAndelMal.dagsats, "dagsats");
            Objects.requireNonNull(beregningsresultatAndelMal.stillingsprosent, "stillingsprosent");
            Objects.requireNonNull(beregningsresultatAndelMal.utbetalingsgrad, "uttaksgrad");
            Objects.requireNonNull(beregningsresultatAndelMal.dagsatsFraBg, "dagsatsFraBg");
            Objects.requireNonNull(beregningsresultatAndelMal.inntektskategori, "inntektskategori");
        }
    }
}
