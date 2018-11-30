package no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Arbeidsforhold;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskategori;

public class BeregningsgrunnlagPrArbeidsforhold {
    private BigDecimal naturalytelseBortfaltPrÅr;
    private BigDecimal naturalytelseTilkommetPrÅr;
    private BigDecimal beregnetPrÅr;
    private BigDecimal overstyrtPrÅr;
    private BigDecimal bruttoPrÅr;
    private BigDecimal avkortetPrÅr;
    private BigDecimal redusertPrÅr;
    private Periode beregningsperiode;
    private Arbeidsforhold arbeidsforhold;
    private BigDecimal refusjonskravPrÅr;
    private BigDecimal maksimalRefusjonPrÅr;
    private BigDecimal avkortetRefusjonPrÅr;
    private BigDecimal redusertRefusjonPrÅr;
    private BigDecimal avkortetBrukersAndelPrÅr;
    private BigDecimal redusertBrukersAndelPrÅr;
    private Long dagsatsBruker;
    private Long dagsatsArbeidsgiver;
    private Boolean tidsbegrensetArbeidsforhold;
    private Boolean fastsattAvSaksbehandler;
    private Boolean lagtTilAvSaksbehandler;
    private Long andelNr;
    private Inntektskategori inntektskategori;

    private BeregningsgrunnlagPrArbeidsforhold() {
    }

    public String getArbeidsgiverId() {
        if (arbeidsforhold.getAktørId() != null) {
            return arbeidsforhold.getAktørId();
        } else if (arbeidsforhold.getOrgnr() != null) {
            return arbeidsforhold.getOrgnr();
        }
        return null;
    }


    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public boolean erFrilanser() {
        return arbeidsforhold.erFrilanser();
    }

    public Optional<BigDecimal> getNaturalytelseBortfaltPrÅr() {
        return Optional.ofNullable(naturalytelseBortfaltPrÅr);
    }

    public Optional<BigDecimal> getNaturalytelseTilkommetPrÅr() {
        return Optional.ofNullable(naturalytelseTilkommetPrÅr);
    }

    public BigDecimal getBeregnetPrÅr() {
        return beregnetPrÅr;
    }

    public String getBeskrivelse() {
        return (erFrilanser() ? "FL:" : "AT:") + getArbeidsgiverId();
    }

    public BigDecimal getOverstyrtPrÅr() {
        return overstyrtPrÅr;
    }

    public BigDecimal getBruttoPrÅr() {
        return bruttoPrÅr;
    }

    public BigDecimal getBruttoInkludertNaturalytelsePrÅr() {
        if (bruttoPrÅr == null) {
            return null;
        }
        BigDecimal bortfaltNaturalytelse = naturalytelseBortfaltPrÅr != null ? naturalytelseBortfaltPrÅr : BigDecimal.ZERO;
        BigDecimal tilkommetNaturalytelse = naturalytelseTilkommetPrÅr != null ? naturalytelseTilkommetPrÅr : BigDecimal.ZERO;
        return bruttoPrÅr.add(bortfaltNaturalytelse).subtract(tilkommetNaturalytelse);
    }

    public BigDecimal getAvkortetPrÅr() {
        return avkortetPrÅr;
    }

    public BigDecimal getRedusertPrÅr() {
        return redusertPrÅr;
    }

    public Periode getBeregningsperiode() {
        return beregningsperiode;
    }

    public Arbeidsforhold getArbeidsforhold() {
        return arbeidsforhold;
    }

    public Optional<BigDecimal> getRefusjonskravPrÅr() {
        return Optional.ofNullable(refusjonskravPrÅr);
    }

    public BigDecimal getMaksimalRefusjonPrÅr() {
        return maksimalRefusjonPrÅr;
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

    public Long getDagsatsBruker() {
        return dagsatsBruker;
    }

    public Long getDagsatsArbeidsgiver() {
        return dagsatsArbeidsgiver;
    }

    public Boolean getTidsbegrensetArbeidsforhold() {
        return tidsbegrensetArbeidsforhold;
    }

    public Boolean getFastsattAvSaksbehandler() {
        return fastsattAvSaksbehandler;
    }

    public Boolean getLagtTilAvSaksbehandler() {
        return lagtTilAvSaksbehandler;
    }

    public Long getAndelNr() {
        return andelNr;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(BeregningsgrunnlagPrArbeidsforhold af) {
        return new Builder(af);
    }

    public static class Builder {
        private BeregningsgrunnlagPrArbeidsforhold mal;

        public Builder() {
            mal = new BeregningsgrunnlagPrArbeidsforhold();
        }

        public Builder(BeregningsgrunnlagPrArbeidsforhold af) {
            mal = af;
        }

        public Builder medArbeidsforhold(Arbeidsforhold arbeidsforhold) {
            mal.arbeidsforhold = arbeidsforhold;
            return this;
        }

        public Builder medBeregnetPrÅr(BigDecimal beregnetPrÅr) {
            mal.beregnetPrÅr = beregnetPrÅr;
            mal.bruttoPrÅr = beregnetPrÅr;
            return this;
        }

        public Builder medOverstyrtPrÅr(BigDecimal overstyrtPrÅr) {
            mal.overstyrtPrÅr = overstyrtPrÅr;
            if (overstyrtPrÅr != null) {
                mal.bruttoPrÅr = overstyrtPrÅr;
            }
            return this;
        }

        public Builder medAvkortetPrÅr(BigDecimal avkortetPrÅr) {
            mal.avkortetPrÅr = avkortetPrÅr;
            return this;
        }

        public Builder medRedusertPrÅr(BigDecimal redusertPrÅr) {
            mal.redusertPrÅr = redusertPrÅr;
            return this;
        }

        public Builder medNaturalytelseBortfaltPrÅr(BigDecimal naturalytelseBortfaltPrÅr) {
            mal.naturalytelseBortfaltPrÅr = naturalytelseBortfaltPrÅr;
            return this;
        }

        public Builder medNaturalytelseTilkommetPrÅr(BigDecimal naturalytelseTilkommetPrÅr) {
            mal.naturalytelseTilkommetPrÅr = naturalytelseTilkommetPrÅr;
            return this;
        }

        public Builder medBeregningsperiode(Periode beregningsperiode) {
            mal.beregningsperiode = beregningsperiode;
            return this;
        }

        public Builder medRefusjonskravPrÅr(BigDecimal refusjonskravPrÅr) {
            mal.refusjonskravPrÅr = refusjonskravPrÅr;
            return this;
        }

        public Builder medMaksimalRefusjonPrÅr(BigDecimal maksimalRefusjonPrÅr) {
            mal.maksimalRefusjonPrÅr = maksimalRefusjonPrÅr;
            return this;
        }

        public Builder medAvkortetRefusjonPrÅr(BigDecimal avkortetRefusjonPrÅr) {
            mal.avkortetRefusjonPrÅr = avkortetRefusjonPrÅr;
            return this;
        }

        public Builder medRedusertRefusjonPrÅr(BigDecimal redusertRefusjonPrÅr) {
            mal.redusertRefusjonPrÅr = redusertRefusjonPrÅr;
            mal.dagsatsArbeidsgiver = redusertRefusjonPrÅr == null ? null : Math.round(redusertRefusjonPrÅr.doubleValue() / 260);
            return this;
        }

        public Builder medAvkortetBrukersAndelPrÅr(BigDecimal avkortetBrukersAndelPrÅr) {
            mal.avkortetBrukersAndelPrÅr = avkortetBrukersAndelPrÅr;
            return this;
        }

        public Builder medRedusertBrukersAndelPrÅr(BigDecimal redusertBrukersAndelPrÅr) {
            mal.redusertBrukersAndelPrÅr = redusertBrukersAndelPrÅr;
            mal.dagsatsBruker = redusertBrukersAndelPrÅr == null ? null : Math.round(redusertBrukersAndelPrÅr.doubleValue() / 260);
            return this;
        }

        public Builder medErTidsbegrensetArbeidsforhold(Boolean tidsbegrensetArbeidsforhold) {
            mal.tidsbegrensetArbeidsforhold = tidsbegrensetArbeidsforhold;
            return this;
        }

        public Builder medAndelNr(long andelNr) {
            mal.andelNr = andelNr;
            return this;
        }

        public Builder medInntektskategori(Inntektskategori inntektskategori) {
            mal.inntektskategori = inntektskategori;
            return this;
        }

        public Builder medFastsattAvSaksbehandler(Boolean fastsattAvSaksbehandler) {
            mal.fastsattAvSaksbehandler = fastsattAvSaksbehandler;
            return this;
        }

        public Builder medLagtTilAvSaksbehandler(Boolean lagtTilAvSaksbehandler) {
            mal.lagtTilAvSaksbehandler = lagtTilAvSaksbehandler;
            return this;
        }

        public BeregningsgrunnlagPrArbeidsforhold build() {
            verifyStateForBuild();
            return mal;
        }

        private void verifyStateForBuild() {
            Objects.requireNonNull(mal.arbeidsforhold, "arbeidsforhold");
            Objects.requireNonNull(mal.andelNr, "andelNr");
        }
    }
}
