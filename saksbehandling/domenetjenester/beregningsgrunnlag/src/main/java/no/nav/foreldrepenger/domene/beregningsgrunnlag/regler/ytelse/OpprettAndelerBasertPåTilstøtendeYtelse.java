package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.ytelse;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagAndelTilstøtendeYtelse;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagFraTilstøtendeYtelse;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskategori;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.TilstøtendeYtelseAndel;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(OpprettAndelerBasertPåTilstøtendeYtelse.ID)
class OpprettAndelerBasertPåTilstøtendeYtelse extends LeafSpecification<BeregningsgrunnlagFraTilstøtendeYtelse> {

    static final String ID = "FP_BR 25.2";
    static final String BESKRIVELSE = "Opprette andeler basert på tilstøtende ytelse";

    OpprettAndelerBasertPåTilstøtendeYtelse() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagFraTilstøtendeYtelse beregningsgrunnlag) {
        Map<String, Object> resultater = new LinkedHashMap<>();
        BeregningsgrunnlagFraTilstøtendeYtelse.Builder bgBuilder = BeregningsgrunnlagFraTilstøtendeYtelse.builder(beregningsgrunnlag);
        beregningsgrunnlag.getTilstøtendeYtelse().getTilstøtendeYtelseAndelList().forEach((TilstøtendeYtelseAndel arbeidsforhold) ->
            {
                Optional<BeregningsgrunnlagAndelTilstøtendeYtelse> eksisterendeAndel = finnAndelHvisEksisterer(beregningsgrunnlag, arbeidsforhold);
                BeregningsgrunnlagAndelTilstøtendeYtelse.Builder builder = eksisterendeAndel.map(BeregningsgrunnlagAndelTilstøtendeYtelse::builder)
                    .orElse(BeregningsgrunnlagAndelTilstøtendeYtelse.builder());
                builder.medAktivitetStatus(arbeidsforhold.getAktivitetStatus())
                    .medArbeidsperiodeFom(arbeidsforhold.getArbeidsforholdFom())
                    .medArbeidsperiodeTom(arbeidsforhold.getArbeidsforholdTom())
                    .medBeløp(arbeidsforhold.getBeløp())
                    .medHyppighet(arbeidsforhold.getHyppighet())
                    .medInntektskategori(arbeidsforhold.getInntektskategori())
                    .medFraTilstøtendeYtelse();
                arbeidsforhold.getOrgNr().ifPresent(builder::medOrgnr);
                BeregningsgrunnlagAndelTilstøtendeYtelse beregningsgrunnlagAndel = builder.build();
                if (!eksisterendeAndel.isPresent()) {
                    bgBuilder.leggTilBeregningsgrunnlagAndel(beregningsgrunnlagAndel);
                }
                regelsporing(resultater, beregningsgrunnlagAndel);
            }
        );

        return beregnet(resultater);
    }

    Optional<BeregningsgrunnlagAndelTilstøtendeYtelse> finnAndelHvisEksisterer(BeregningsgrunnlagFraTilstøtendeYtelse beregningsgrunnlag, TilstøtendeYtelseAndel arbeidsforhold) {
        Optional<String> orgNrOpt = arbeidsforhold.getOrgNr();
        Optional<BeregningsgrunnlagAndelTilstøtendeYtelse> eksisterendeAndel;
        if (AktivitetStatus.ATFL.equals(arbeidsforhold.getAktivitetStatus())) {
            eksisterendeAndel = orgNrOpt.flatMap(orgNr ->
                beregningsgrunnlag.getBeregningsgrunnlagAndeler().stream()
                    .filter(andel -> AktivitetStatus.ATFL.equals(andel.getAktivitetStatus()))
                    .filter(andel -> andel.getOrgnr() != null && orgNr.equals(andel.getOrgnr())).findFirst());
            if (!orgNrOpt.isPresent() && arbeidsforhold.getInntektskategori().equals(Inntektskategori.FRILANSER)) {
                eksisterendeAndel = finnAndelBasertPåInntektkategori(beregningsgrunnlag, arbeidsforhold);
            }
        } else {
            eksisterendeAndel = orgNrOpt.flatMap(orgNr ->
                beregningsgrunnlag.getBeregningsgrunnlagAndeler().stream()
                    .filter(andel -> arbeidsforhold.getAktivitetStatus().equals(andel.getAktivitetStatus()))
                    .filter(andel -> arbeidsforhold.getInntektskategori().equals(andel.getInntektskategori()))
                    .filter(andel -> (andel.getOrgnr() != null && orgNr.equals(andel.getOrgnr())))
                    .findFirst());
            if (!orgNrOpt.isPresent()) {
                eksisterendeAndel = finnAndelBasertPåInntektkategori(beregningsgrunnlag, arbeidsforhold);
            }
        }
        return eksisterendeAndel;
    }

    private Optional<BeregningsgrunnlagAndelTilstøtendeYtelse> finnAndelBasertPåInntektkategori(BeregningsgrunnlagFraTilstøtendeYtelse beregningsgrunnlag, TilstøtendeYtelseAndel arbeidsforhold) {
        Optional<BeregningsgrunnlagAndelTilstøtendeYtelse> eksisterendeAndel;
        eksisterendeAndel = beregningsgrunnlag.getBeregningsgrunnlagAndeler().stream()
            .filter(andel -> arbeidsforhold.getAktivitetStatus().equals(andel.getAktivitetStatus()))
            .filter(andel -> arbeidsforhold.getInntektskategori().equals(andel.getInntektskategori()))
            .findFirst();
        return eksisterendeAndel;
    }

    private void regelsporing(Map<String, Object> resultater, BeregningsgrunnlagAndelTilstøtendeYtelse beregningsgrunnlagAndel) {
        String andelNavn = beregningsgrunnlagAndel.getAktivitetStatus().equals(AktivitetStatus.ATFL)
            ? "orgnr=" + beregningsgrunnlagAndel.getOrgnr() : beregningsgrunnlagAndel.getAktivitetStatus().name();
        String prefiks = "andel[" + andelNavn + "].";
        resultater.put(prefiks + "Beløp", beregningsgrunnlagAndel.getBeløp());
        resultater.put(prefiks + "Hyppighet", beregningsgrunnlagAndel.getHyppighet());
        resultater.put(prefiks + "AktivitetStatus", beregningsgrunnlagAndel.getAktivitetStatus());
        resultater.put(prefiks + "ArbeidsforholdId", beregningsgrunnlagAndel.getArbeidsforholdId());
        resultater.put(prefiks + "Fom", beregningsgrunnlagAndel.getArbeidsperiodeFom());
        resultater.put(prefiks + "Tom", beregningsgrunnlagAndel.getArbeidsperiodeTom());
        resultater.put(prefiks + "Inntektskategori", beregningsgrunnlagAndel.getInntektskategori().name());
        resultater.put(prefiks + "Refusjonskrav", beregningsgrunnlagAndel.getRefusjonskrav().orElse(null));
    }
}
