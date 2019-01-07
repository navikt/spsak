package no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "BGAndelArbeidsforhold")
@Table(name = "BG_ANDEL_ARBEIDSFORHOLD")
public class BGAndelArbeidsforhold extends BaseEntitet {

    @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BG_ANDEL_ARBEIDSFORHOLD")
    private Long id;

    @JsonBackReference
    @OneToOne(optional = false)
    @JoinColumn(name = "bg_andel_id", nullable = false, updatable = false)
    private BeregningsgrunnlagPrStatusOgAndel beregningsgrunnlagPrStatusOgAndel;

    @Embedded
    private Arbeidsgiver arbeidsgiver;

    @Embedded
    private ArbeidsforholdRef arbeidsforholdRef;

    @Column(name = "refusjonskrav_pr_aar")
    private BigDecimal refusjonskravPrÅr;

    @Column(name = "naturalytelse_bortfalt_pr_aar")
    private BigDecimal naturalytelseBortfaltPrÅr;

    @Column(name = "naturalytelse_tilkommet_pr_aar")
    private BigDecimal naturalytelseTilkommetPrÅr;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "tidsbegrenset_arbeidsforhold")
    private Boolean erTidsbegrensetArbeidsforhold;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "loennsendring_i_perioden")
    private Boolean lønnsendringIBeregningsperioden;

    @Column(name = "arbeidsperiode_fom")
    private LocalDate arbeidsperiodeFom;

    @Column(name = "arbeidsperiode_tom")
    private LocalDate arbeidsperiodeTom;


    public Long getId() {
        return id;
    }

    public BeregningsgrunnlagPrStatusOgAndel getBeregningsgrunnlagPrStatusOgAndel() {
        return beregningsgrunnlagPrStatusOgAndel;
    }

    public Optional<Virksomhet> getVirksomhet() {
        return Optional.ofNullable(arbeidsgiver).map(Arbeidsgiver::getVirksomhet);
    }

    public Optional<ArbeidsforholdRef> getArbeidsforholdRef() {
        return Optional.ofNullable(arbeidsforholdRef);
    }

    public BigDecimal getRefusjonskravPrÅr() {
        return refusjonskravPrÅr;
    }

    public Optional<BigDecimal> getNaturalytelseBortfaltPrÅr() {
        return Optional.ofNullable(naturalytelseBortfaltPrÅr);
    }

    public Optional<BigDecimal> getNaturalytelseTilkommetPrÅr() {
        return Optional.ofNullable(naturalytelseTilkommetPrÅr);
    }

    public Boolean getErTidsbegrensetArbeidsforhold() {
        return erTidsbegrensetArbeidsforhold;
    }

    public Boolean erLønnsendringIBeregningsperioden() {
        return lønnsendringIBeregningsperioden;
    }

    public LocalDate getArbeidsperiodeFom() {
        return arbeidsperiodeFom;
    }

    public Optional<LocalDate> getArbeidsperiodeTom() {
        return Optional.ofNullable(arbeidsperiodeTom);
    }

    public String getArbeidsforholdOrgnr() {
        return getVirksomhet().map(Virksomhet::getOrgnr).orElse(null);
    }

    public Optional<Arbeidsgiver> getArbeidsgiver() {
        return Optional.ofNullable(arbeidsgiver);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BGAndelArbeidsforhold)) {
            return false;
        }
        BGAndelArbeidsforhold other = (BGAndelArbeidsforhold) obj;
        return Objects.equals(this.getArbeidsgiver(), other.getArbeidsgiver())
            && Objects.equals(this.getArbeidsforholdRef(), other.getArbeidsforholdRef());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getArbeidsgiver(), arbeidsforholdRef);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
            "id=" + id + ", " //$NON-NLS-2$ //$NON-NLS-3$
            + "orgnr=" + getArbeidsforholdOrgnr() + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "arbeidsforholdRef=" + arbeidsforholdRef + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "arbeidsforholdRef=" + arbeidsforholdRef + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "naturalytelseBortfaltPrÅr=" + naturalytelseBortfaltPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "naturalytelseTilkommetPrÅr=" + naturalytelseTilkommetPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "refusjonskravPrÅr=" + refusjonskravPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "arbeidsperiodeFom=" + arbeidsperiodeFom //$NON-NLS-1$ //$NON-NLS-2$
            + "arbeidsperiodeTom=" + arbeidsperiodeTom //$NON-NLS-1$ //$NON-NLS-2$
            + ">"; //$NON-NLS-1$
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(BGAndelArbeidsforhold bgAndelArbeidsforhold) {
        return bgAndelArbeidsforhold == null ? new Builder() : new Builder(bgAndelArbeidsforhold);
    }

    public static class Builder {
        private BGAndelArbeidsforhold bgAndelArbeidsforhold;

        private Builder() {
            bgAndelArbeidsforhold = new BGAndelArbeidsforhold();
        }

        private Builder(BGAndelArbeidsforhold eksisterendeBGAndelArbeidsforhold) {
            bgAndelArbeidsforhold = eksisterendeBGAndelArbeidsforhold;
        }

        public Builder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
            bgAndelArbeidsforhold.arbeidsgiver= arbeidsgiver;
            return this;
        }

        public Builder medArbforholdRef(String arbeidsforholdRef) {
            bgAndelArbeidsforhold.arbeidsforholdRef = arbeidsforholdRef == null ? null : ArbeidsforholdRef.ref(arbeidsforholdRef);
            return this;
        }

        public Builder medNaturalytelseBortfaltPrÅr(BigDecimal naturalytelseBortfaltPrÅr) {
            bgAndelArbeidsforhold.naturalytelseBortfaltPrÅr = naturalytelseBortfaltPrÅr;
            return this;
        }

        public Builder medNaturalytelseTilkommetPrÅr(BigDecimal naturalytelseTilkommetPrÅr) {
            bgAndelArbeidsforhold.naturalytelseTilkommetPrÅr = naturalytelseTilkommetPrÅr;
            return this;
        }

        public Builder medRefusjonskravPrÅr(BigDecimal refusjonskravPrÅr) {
            bgAndelArbeidsforhold.refusjonskravPrÅr = refusjonskravPrÅr;
            return this;
        }

        public BGAndelArbeidsforhold.Builder medTidsbegrensetArbeidsforhold(Boolean erTidsbegrensetArbeidsforhold) {
            bgAndelArbeidsforhold.erTidsbegrensetArbeidsforhold = erTidsbegrensetArbeidsforhold;
            return this;
        }

        public Builder medLønnsendringIBeregningsperioden(Boolean lønnsendringIBeregningsperioden) {
            bgAndelArbeidsforhold.lønnsendringIBeregningsperioden = lønnsendringIBeregningsperioden;
            return this;
        }
        public Builder medArbeidsperiodeFom(LocalDate arbeidsperiodeFom) {
            bgAndelArbeidsforhold.arbeidsperiodeFom = arbeidsperiodeFom;
            return this;
        }

        public Builder medArbeidsperiodeTom(LocalDate arbeidsperiodeTom) {
            bgAndelArbeidsforhold.arbeidsperiodeTom = arbeidsperiodeTom;
            return this;
        }

        public BGAndelArbeidsforhold build(BeregningsgrunnlagPrStatusOgAndel andel) {
            bgAndelArbeidsforhold.beregningsgrunnlagPrStatusOgAndel = andel;
            return bgAndelArbeidsforhold;
        }
    }
}
