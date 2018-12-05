package no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.domene.typer.Beløp;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;
import no.nav.vedtak.felles.jpa.tid.ÅpenDatoIntervallEntitet;

@Entity(name = "BeregningsgrunnlagPrStatusOgAndel")
@Table(name = "BG_PR_STATUS_OG_ANDEL")
public class BeregningsgrunnlagPrStatusOgAndel extends BaseEntitet {

    @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BG_PR_STATUS_OG_ANDEL")
    private Long id;

    @Column(name = "andelsnr", nullable = false)
    private Long andelsnr;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @JsonBackReference
    @ManyToOne(optional = false)
    @JoinColumn(name = "bg_periode_id", nullable = false, updatable = false)
    private BeregningsgrunnlagPeriode beregningsgrunnlagPeriode;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "aktivitet_status", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + AktivitetStatus.DISCRIMINATOR + "'"))
    private AktivitetStatus aktivitetStatus;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "fomDato", column = @Column(name = "beregningsperiode_fom")),
        @AttributeOverride(name = "tomDato", column = @Column(name = "beregningsperiode_tom"))
    })
    private ÅpenDatoIntervallEntitet beregningsperiode;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "arbeidsforhold_type", referencedColumnName = "kode"))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + OpptjeningAktivitetType.DISCRIMINATOR + "'"))
    private OpptjeningAktivitetType arbeidsforholdType;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "relatert_ytelse_type", referencedColumnName = "kode"))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + RelatertYtelseType.DISCRIMINATOR + "'"))
    private RelatertYtelseType ytelse;

    @Column(name = "brutto_pr_aar")
    private BigDecimal bruttoPrÅr;

    @Column(name = "overstyrt_pr_aar")
    private BigDecimal overstyrtPrÅr;

    @Column(name = "avkortet_pr_aar")
    private BigDecimal avkortetPrÅr;

    @Column(name = "redusert_pr_aar")
    private BigDecimal redusertPrÅr;

    @Column(name = "beregnet_pr_aar")
    private BigDecimal beregnetPrÅr;

    @Column(name = "maksimal_refusjon_pr_aar")
    private BigDecimal maksimalRefusjonPrÅr;

    @Column(name = "avkortet_refusjon_pr_aar")
    private BigDecimal avkortetRefusjonPrÅr;

    @Column(name = "redusert_refusjon_pr_aar")
    private BigDecimal redusertRefusjonPrÅr;

    @Column(name = "avkortet_brukers_andel_pr_aar")
    private BigDecimal avkortetBrukersAndelPrÅr;

    @Column(name = "redusert_brukers_andel_pr_aar")
    private BigDecimal redusertBrukersAndelPrÅr;

    @Column(name = "dagsats_bruker")
    private Long dagsatsBruker;

    @Column(name = "dagsats_arbeidsgiver")
    private Long dagsatsArbeidsgiver;

    @Column(name = "pgi_snitt")
    private BigDecimal pgiSnitt;

    @Column(name = "pgi1")
    private BigDecimal pgi1;

    @Column(name = "pgi2")
    private BigDecimal pgi2;

    @Column(name = "pgi3")
    private BigDecimal pgi3;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "aarsbeloep_tilstoetende_ytelse")))
    @ChangeTracked
    private Beløp årsbeløpFraTilstøtendeYtelse;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "ny_i_arbeidslivet")
    private Boolean nyIArbeidslivet;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "fastsatt_av_saksbehandler")
    private Boolean fastsattAvSaksbehandler;

    @Column(name = "besteberegning_pr_aar")
    private BigDecimal besteberegningPrÅr;

    @ManyToOne
    @JoinColumnOrFormula(column = @JoinColumn(name = "inntektskategori", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + Inntektskategori.DISCRIMINATOR + "'"))
    private Inntektskategori inntektskategori = Inntektskategori.UDEFINERT;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "lagt_til_av_saksbehandler", nullable = false)
    private Boolean lagtTilAvSaksbehandler = false;

    @OneToOne(mappedBy = "beregningsgrunnlagPrStatusOgAndel", cascade = CascadeType.PERSIST)
    private BGAndelArbeidsforhold bgAndelArbeidsforhold;

    public Long getId() {
        return id;
    }

    public BeregningsgrunnlagPeriode getBeregningsgrunnlagPeriode() {
        return beregningsgrunnlagPeriode;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public LocalDate getBeregningsperiodeFom() {
        return beregningsperiode != null ? beregningsperiode.getFomDato() : null;
    }

    public LocalDate getBeregningsperiodeTom() {
        return beregningsperiode != null ? beregningsperiode.getTomDato() : null;
    }

    public boolean gjelderSammeArbeidsforhold(BeregningsgrunnlagPrStatusOgAndel that) {
        if (!Objects.equals(this.getAktivitetStatus(), AktivitetStatus.ARBEIDSTAKER) || !Objects.equals(that.getAktivitetStatus(), AktivitetStatus.ARBEIDSTAKER)) {
            return false;
        }
        return gjelderSammeArbeidsforhold(that.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getVirksomhet), that.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef));
    }

    public boolean gjelderSammeArbeidsforhold(Yrkesaktivitet yrkesaktivitet) {
        return gjelderSammeArbeidsforhold(Optional.ofNullable(yrkesaktivitet.getArbeidsgiver()).map(Arbeidsgiver::getVirksomhet), yrkesaktivitet.getArbeidsforholdRef());
    }

    public boolean gjelderSammeArbeidsforhold(Virksomhet virksomhet, ArbeidsforholdRef arbeidsforholdRef) {
        return gjelderSammeArbeidsforhold(Optional.ofNullable(virksomhet), Optional.ofNullable(arbeidsforholdRef));
    }

    private boolean gjelderSammeArbeidsforhold(Optional<Virksomhet> virksomhet, Optional<ArbeidsforholdRef> arbeidsforholdRef) {
        if (!Objects.equals(getAktivitetStatus(), AktivitetStatus.ARBEIDSTAKER)) {
            return false;
        }
        if (!this.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef).isPresent() || !arbeidsforholdRef.isPresent()) {
            return Objects.equals(this.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getVirksomhet), virksomhet);
        }
        return Objects.equals(this.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef), arbeidsforholdRef);
    }

    public boolean matchUtenInntektskategori(BeregningsgrunnlagPrStatusOgAndel other) {
        return Objects.equals(this.getAktivitetStatus(), other.getAktivitetStatus())
            && Objects.equals(this.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getVirksomhet), other.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getVirksomhet))
            && Objects.equals(this.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef), other.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef))
            && Objects.equals(this.getArbeidsforholdType(), other.getArbeidsforholdType());
    }

    public OpptjeningAktivitetType getArbeidsforholdType() {
        return arbeidsforholdType;
    }

    public RelatertYtelseType getYtelse() {
        return ytelse;
    }

    public BigDecimal getBruttoPrÅr() {
        return bruttoPrÅr;
    }

    public BigDecimal getOverstyrtPrÅr() {
        return overstyrtPrÅr;
    }

    public BigDecimal getAvkortetPrÅr() {
        return avkortetPrÅr;
    }

    public BigDecimal getRedusertPrÅr() {
        return redusertPrÅr;
    }

    public BigDecimal getBeregnetPrÅr() {
        return beregnetPrÅr;
    }

    public BigDecimal getMaksimalRefusjonPrÅr() {
        return maksimalRefusjonPrÅr;
    }

    public BigDecimal getAvkortetRefusjonPrÅr() {
        return avkortetRefusjonPrÅr;
    }

    public BigDecimal getRedusertRefusjonPrÅr() {
        return redusertRefusjonPrÅr;
    }

    public BigDecimal getAvkortetBrukersAndelPrÅr() {
        return avkortetBrukersAndelPrÅr;
    }

    public BigDecimal getRedusertBrukersAndelPrÅr() {
        return redusertBrukersAndelPrÅr;
    }

    public Boolean getNyIArbeidslivet() {
        return nyIArbeidslivet;
    }

    public Boolean getFastsattAvSaksbehandler() {
        return fastsattAvSaksbehandler;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public BigDecimal getBruttoInkludertNaturalYtelser() {
        BigDecimal naturalytelseBortfalt = getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getNaturalytelseBortfaltPrÅr).orElse(BigDecimal.ZERO);
        BigDecimal naturalYtelseTilkommet = getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getNaturalytelseTilkommetPrÅr).orElse(BigDecimal.ZERO);
        BigDecimal brutto = bruttoPrÅr != null ? bruttoPrÅr : BigDecimal.ZERO;
        return brutto.add(naturalytelseBortfalt).subtract(naturalYtelseTilkommet);
    }

    public Long getDagsatsBruker() {
        return dagsatsBruker;
    }

    public Long getDagsatsArbeidsgiver() {
        return dagsatsArbeidsgiver;
    }

    public Long getDagsats() {
        if (dagsatsBruker == null) {
            return dagsatsArbeidsgiver;
        }
        if (dagsatsArbeidsgiver == null) {
            return dagsatsBruker;
        }
        return dagsatsBruker + dagsatsArbeidsgiver;
    }

    public BigDecimal getPgiSnitt() {
        return pgiSnitt;
    }

    public BigDecimal getPgi1() {
        return pgi1;
    }

    public BigDecimal getPgi2() {
        return pgi2;
    }

    public BigDecimal getPgi3() {
        return pgi3;
    }

    public Beløp getÅrsbeløpFraTilstøtendeYtelse() {
        return årsbeløpFraTilstøtendeYtelse;
    }

    public BigDecimal getÅrsbeløpFraTilstøtendeYtelseVerdi() {
        return Optional.ofNullable(getÅrsbeløpFraTilstøtendeYtelse())
            .map(Beløp::getVerdi).orElse(BigDecimal.ZERO);
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public BigDecimal getBesteberegningPrÅr() {
        return besteberegningPrÅr;
    }

    public Boolean getLagtTilAvSaksbehandler() {
        return lagtTilAvSaksbehandler;
    }

    public Optional<BGAndelArbeidsforhold> getBgAndelArbeidsforhold() {
        return Optional.ofNullable(bgAndelArbeidsforhold);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BeregningsgrunnlagPrStatusOgAndel)) {
            return false;
        }
        BeregningsgrunnlagPrStatusOgAndel other = (BeregningsgrunnlagPrStatusOgAndel) obj;
        return Objects.equals(this.getAktivitetStatus(), other.getAktivitetStatus())
            && Objects.equals(this.getInntektskategori(), other.getInntektskategori())
            && Objects.equals(this.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getVirksomhet),
            other.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getVirksomhet))
            && Objects.equals(this.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef),
            other.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef))
            && Objects.equals(this.getArbeidsforholdType(), other.getArbeidsforholdType());
    }


    @Override
    public int hashCode() {
        return Objects.hash(aktivitetStatus,
            inntektskategori,
            getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getVirksomhet),
            getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef),
            arbeidsforholdType);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
            "id=" + id + ", " //$NON-NLS-2$ //$NON-NLS-3$
            + "beregningsgrunnlagPeriode=" + beregningsgrunnlagPeriode + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "aktivitetStatus=" + aktivitetStatus + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "beregningsperiode=" + beregningsperiode + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "arbeidsforholdType=" + arbeidsforholdType + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "ytelse=" + ytelse + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "maksimalRefusjonPrÅr=" + maksimalRefusjonPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "avkortetRefusjonPrÅr=" + avkortetRefusjonPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "redusertRefusjonPrÅr=" + redusertRefusjonPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "avkortetBrukersAndelPrÅr=" + avkortetBrukersAndelPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "redusertBrukersAndelPrÅr=" + redusertBrukersAndelPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "beregnetPrÅr=" + beregnetPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "overstyrtPrÅr=" + overstyrtPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "bruttoPrÅr=" + bruttoPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "avkortetPrÅr=" + avkortetPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "redusertPrÅr=" + redusertPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "dagsatsBruker=" + dagsatsBruker + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "dagsatsArbeidsgiver=" + dagsatsArbeidsgiver + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "pgiSnitt=" + pgiSnitt + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "pgi1=" + pgi1 + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "pgi2=" + pgi2 + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "pgi3=" + pgi3 + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "årsbeløpFraTilstøtendeYtelse=" + årsbeløpFraTilstøtendeYtelse //$NON-NLS-1$ //$NON-NLS-2$
            + "besteberegningPrÅr=" + besteberegningPrÅr //$NON-NLS-1$ //$NON-NLS-2$
            + ">"; //$NON-NLS-1$
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(BeregningsgrunnlagPrStatusOgAndel eksisterendeBGPrStatusOgAndel) {
        return new Builder(eksisterendeBGPrStatusOgAndel);
    }

    public static class Builder {
        private BeregningsgrunnlagPrStatusOgAndel beregningsgrunnlagPrStatusOgAndelMal;

        public Builder() {
            beregningsgrunnlagPrStatusOgAndelMal = new BeregningsgrunnlagPrStatusOgAndel();
            beregningsgrunnlagPrStatusOgAndelMal.arbeidsforholdType = OpptjeningAktivitetType.UDEFINERT;
            medYtelse(null);
        }

        public Builder(BeregningsgrunnlagPrStatusOgAndel eksisterendeBGPrStatusOgAndelMal) {
            beregningsgrunnlagPrStatusOgAndelMal = eksisterendeBGPrStatusOgAndelMal;
        }

        public Builder medAktivitetStatus(AktivitetStatus aktivitetStatus) {
            beregningsgrunnlagPrStatusOgAndelMal.aktivitetStatus = aktivitetStatus;
            if (OpptjeningAktivitetType.UDEFINERT.equals(beregningsgrunnlagPrStatusOgAndelMal.arbeidsforholdType)) {
                if (AktivitetStatus.ARBEIDSTAKER.equals(aktivitetStatus)) {
                    beregningsgrunnlagPrStatusOgAndelMal.arbeidsforholdType = OpptjeningAktivitetType.ARBEID;
                } else if (AktivitetStatus.FRILANSER.equals(aktivitetStatus)) {
                    beregningsgrunnlagPrStatusOgAndelMal.arbeidsforholdType = OpptjeningAktivitetType.FRILANS;
                }
            }

            return this;
        }

        public Builder medBeregningsperiode(LocalDate beregningsperiodeFom, LocalDate beregningsperiodeTom) {
            beregningsgrunnlagPrStatusOgAndelMal.beregningsperiode = ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(beregningsperiodeFom, beregningsperiodeTom);
            return this;
        }

        public Builder medArbforholdType(OpptjeningAktivitetType arbforholdType) {
            beregningsgrunnlagPrStatusOgAndelMal.arbeidsforholdType = arbforholdType;
            return this;
        }

        public Builder medYtelse(RelatertYtelseType ytelse) {
            beregningsgrunnlagPrStatusOgAndelMal.ytelse = ytelse == null ? RelatertYtelseType.UDEFINERT : ytelse;
            return this;
        }

        public Builder medOverstyrtPrÅr(BigDecimal overstyrtPrÅr) {
            beregningsgrunnlagPrStatusOgAndelMal.overstyrtPrÅr = overstyrtPrÅr;
            if (overstyrtPrÅr != null) {
                beregningsgrunnlagPrStatusOgAndelMal.bruttoPrÅr = overstyrtPrÅr;
                if (beregningsgrunnlagPrStatusOgAndelMal.getBeregningsgrunnlagPeriode() != null) {
                    beregningsgrunnlagPrStatusOgAndelMal.beregningsgrunnlagPeriode.updateBruttoPrÅr();
                }
            }
            return this;
        }

        public Builder medAvkortetPrÅr(BigDecimal avkortetPrÅr) {
            beregningsgrunnlagPrStatusOgAndelMal.avkortetPrÅr = avkortetPrÅr;
            return this;
        }

        public Builder medRedusertPrÅr(BigDecimal redusertPrÅr) {
            beregningsgrunnlagPrStatusOgAndelMal.redusertPrÅr = redusertPrÅr;
            return this;
        }

        public Builder medMaksimalRefusjonPrÅr(BigDecimal maksimalRefusjonPrÅr) {
            beregningsgrunnlagPrStatusOgAndelMal.maksimalRefusjonPrÅr = maksimalRefusjonPrÅr;
            return this;
        }

        public Builder medAvkortetRefusjonPrÅr(BigDecimal avkortetRefusjonPrÅr) {
            beregningsgrunnlagPrStatusOgAndelMal.avkortetRefusjonPrÅr = avkortetRefusjonPrÅr;
            return this;
        }

        public Builder medRedusertRefusjonPrÅr(BigDecimal redusertRefusjonPrÅr) {
            beregningsgrunnlagPrStatusOgAndelMal.redusertRefusjonPrÅr = redusertRefusjonPrÅr;
            beregningsgrunnlagPrStatusOgAndelMal.dagsatsArbeidsgiver = redusertRefusjonPrÅr == null ?
                null : redusertRefusjonPrÅr.divide(BigDecimal.valueOf(260), 0, RoundingMode.HALF_UP).longValue();
            return this;
        }

        public Builder medAvkortetBrukersAndelPrÅr(BigDecimal avkortetBrukersAndelPrÅr) {
            beregningsgrunnlagPrStatusOgAndelMal.avkortetBrukersAndelPrÅr = avkortetBrukersAndelPrÅr;
            return this;
        }

        public Builder medRedusertBrukersAndelPrÅr(BigDecimal redusertBrukersAndelPrÅr) {
            beregningsgrunnlagPrStatusOgAndelMal.redusertBrukersAndelPrÅr = redusertBrukersAndelPrÅr;
            beregningsgrunnlagPrStatusOgAndelMal.dagsatsBruker = redusertBrukersAndelPrÅr == null ?
                null : redusertBrukersAndelPrÅr.divide(BigDecimal.valueOf(260), 0, RoundingMode.HALF_UP).longValue();
            return this;
        }

        public Builder medBeregnetPrÅr(BigDecimal beregnetPrÅr) {
            beregningsgrunnlagPrStatusOgAndelMal.beregnetPrÅr = beregnetPrÅr;
            beregningsgrunnlagPrStatusOgAndelMal.bruttoPrÅr = beregnetPrÅr;
            if (beregningsgrunnlagPrStatusOgAndelMal.getBeregningsgrunnlagPeriode() != null) {
                beregningsgrunnlagPrStatusOgAndelMal.beregningsgrunnlagPeriode.updateBruttoPrÅr();
            }
            return this;
        }

        public Builder medPgi(BigDecimal pgiSnitt, List<BigDecimal> pgiListe) {
            beregningsgrunnlagPrStatusOgAndelMal.pgiSnitt = pgiSnitt;
            beregningsgrunnlagPrStatusOgAndelMal.pgi1 = pgiListe.isEmpty() ? null : pgiListe.get(0);
            beregningsgrunnlagPrStatusOgAndelMal.pgi2 = pgiListe.isEmpty() ? null : pgiListe.get(1);
            beregningsgrunnlagPrStatusOgAndelMal.pgi3 = pgiListe.isEmpty() ? null : pgiListe.get(2);
            return this;
        }

        public Builder medÅrsbeløpFraTilstøtendeYtelse(BigDecimal årsbeløpFraTilstøtendeYtelse) {
            beregningsgrunnlagPrStatusOgAndelMal.årsbeløpFraTilstøtendeYtelse = new Beløp(årsbeløpFraTilstøtendeYtelse);
            return this;
        }

        public Builder medNyIArbeidslivet(Boolean nyIArbeidslivet) {
            beregningsgrunnlagPrStatusOgAndelMal.nyIArbeidslivet = nyIArbeidslivet;
            return this;
        }

        public Builder medInntektskategori(Inntektskategori inntektskategori) {
            beregningsgrunnlagPrStatusOgAndelMal.inntektskategori = inntektskategori;
            return this;
        }

        public Builder medFastsattAvSaksbehandler(Boolean fastsattAvSaksbehandler) {
            beregningsgrunnlagPrStatusOgAndelMal.fastsattAvSaksbehandler = fastsattAvSaksbehandler;
            return this;
        }

        public Builder medBesteberegningPrÅr(BigDecimal besteberegningPrÅr) {
            beregningsgrunnlagPrStatusOgAndelMal.besteberegningPrÅr = besteberegningPrÅr;
            return this;
        }

        public Builder medAndelsnr(Long andelsnr) {
            beregningsgrunnlagPrStatusOgAndelMal.andelsnr = andelsnr;
            return this;
        }

        public Builder nyttAndelsnr(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
            finnOgSettAndelsnr(beregningsgrunnlagPeriode);
            return this;
        }

        public Builder medLagtTilAvSaksbehandler(Boolean lagtTilAvSaksbehandler) {
            beregningsgrunnlagPrStatusOgAndelMal.lagtTilAvSaksbehandler = lagtTilAvSaksbehandler;
            return this;
        }

        public Builder medBGAndelArbeidsforhold(BGAndelArbeidsforhold.Builder bgAndelArbeidsforholdBuilder) {
            beregningsgrunnlagPrStatusOgAndelMal.bgAndelArbeidsforhold = bgAndelArbeidsforholdBuilder.build(beregningsgrunnlagPrStatusOgAndelMal);
            return this;
        }

        public BeregningsgrunnlagPrStatusOgAndel build(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
            beregningsgrunnlagPrStatusOgAndelMal.beregningsgrunnlagPeriode = beregningsgrunnlagPeriode;
            verifyStateForBuild();
            if (beregningsgrunnlagPrStatusOgAndelMal.andelsnr == null) {
                finnOgSettAndelsnr(beregningsgrunnlagPeriode);
            }
            if (beregningsgrunnlagPrStatusOgAndelMal.lagtTilAvSaksbehandler == null) {
                beregningsgrunnlagPrStatusOgAndelMal.lagtTilAvSaksbehandler = false;
            }
            verifiserAndelsnr();
            beregningsgrunnlagPeriode.addBeregningsgrunnlagPrStatusOgAndel(beregningsgrunnlagPrStatusOgAndelMal);
            beregningsgrunnlagPeriode.updateBruttoPrÅr();
            return beregningsgrunnlagPrStatusOgAndelMal;
        }

        private void finnOgSettAndelsnr(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
            Long forrigeAndelsnr = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .mapToLong(BeregningsgrunnlagPrStatusOgAndel::getAndelsnr)
                .max()
                .orElse(0L);
            Long nyttAndelsnr = forrigeAndelsnr + 1L;
            beregningsgrunnlagPrStatusOgAndelMal.andelsnr = nyttAndelsnr;
        }

        private void verifiserAndelsnr() {
            Set<Long> andelsnrIBruk = new HashSet<>();
            beregningsgrunnlagPrStatusOgAndelMal.beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .map(BeregningsgrunnlagPrStatusOgAndel::getAndelsnr)
                .forEach(andelsnr -> {
                    if (andelsnrIBruk.contains(andelsnr)) {
                        throw new IllegalStateException("Utviklerfeil: Kan ikke bygge andel. Andelsnr eksisterer allerede på en annen andel.");
                    }
                    andelsnrIBruk.add(andelsnr);
                });
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(beregningsgrunnlagPrStatusOgAndelMal.beregningsgrunnlagPeriode, "beregningsgrunnlagPeriode");
            Objects.requireNonNull(beregningsgrunnlagPrStatusOgAndelMal.aktivitetStatus, "aktivitetStatus");
        }
    }
}
