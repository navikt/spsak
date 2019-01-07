package no.nav.foreldrepenger.domene.beregning.ytelse.adapter;

import java.math.RoundingMode;
import java.util.Objects;

import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatFeriepenger;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatFeriepengerPrÅr;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregning.regelmodell.feriepenger.BeregningsresultatFeriepengerRegelModell;

public class MapBeregningsresultatFeriepengerFraRegelTilVL {
    private MapBeregningsresultatFeriepengerFraRegelTilVL() {
        // unused
    }

    public static void mapFra(BeregningsresultatPerioder resultat, BeregningsresultatFeriepengerRegelModell regelModell, BeregningsresultatFeriepenger beregningsresultatFeriepenger) {

        if (regelModell.getFeriepengerPeriode() == null) {
            return;
        }

        BeregningsresultatFeriepenger.builder(beregningsresultatFeriepenger)
            .medFeriepengerPeriodeFom(regelModell.getFeriepengerPeriode().getFomDato())
            .medFeriepengerPeriodeTom(regelModell.getFeriepengerPeriode().getTomDato())
            .build(resultat);

        regelModell.getBeregningsresultatPerioder().forEach(regelBeregningsresultatPeriode ->
            mapPeriode(resultat, beregningsresultatFeriepenger, regelBeregningsresultatPeriode));
    }

    private static void mapPeriode(BeregningsresultatPerioder resultat, BeregningsresultatFeriepenger beregningsresultatFeriepenger, no.nav.foreldrepenger.domene.beregning.regelmodell.BeregningsresultatPeriode regelBeregningsresultatPeriode) {
        BeregningsresultatPeriode vlBeregningsresultatPeriode = resultat.getBeregningsresultatPerioder().stream()
            .filter(periode -> periode.getBeregningsresultatPeriodeFom().equals(regelBeregningsresultatPeriode.getFom()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Fant ikke BeregningsresultatPeriode"));
        regelBeregningsresultatPeriode.getBeregningsresultatAndelList().forEach(regelAndel ->
            mapAndel(beregningsresultatFeriepenger, vlBeregningsresultatPeriode, regelAndel));
    }

    private static void mapAndel(BeregningsresultatFeriepenger beregningsresultatFeriepenger, BeregningsresultatPeriode vlBeregningsresultatPeriode,
                                 no.nav.foreldrepenger.domene.beregning.regelmodell.BeregningsresultatAndel regelAndel) {
        if (regelAndel.getBeregningsresultatFeriepengerPrÅrListe().isEmpty()) {
            return;
        }
        AktivitetStatus regelAndelAktivitetStatus = AktivitetStatusMapper.fraRegelTilVl(regelAndel);
        String regelOrgnr = regelAndel.getArbeidsforhold() != null ? regelAndel.getArbeidsforhold().getOrgnr() : null;
        String regelArbeidsforholdId = regelAndel.getArbeidsforhold() != null ? regelAndel.getArbeidsforhold().getArbeidsforholdId() : null;
        BeregningsresultatAndel andel = vlBeregningsresultatPeriode.getBeregningsresultatAndelList().stream()
            .filter(vlAndel -> {
                String vlArbeidsforholdRef = vlAndel.getArbeidsforholdRef() == null ? null : vlAndel.getArbeidsforholdRef().getReferanse();
                return Objects.equals(vlAndel.getAktivitetStatus(), regelAndelAktivitetStatus)
                    && Objects.equals(vlAndel.getArbeidsforholdOrgnr(), regelOrgnr)
                    && Objects.equals(vlArbeidsforholdRef, regelArbeidsforholdId)
                    && Objects.equals(vlAndel.erBrukerMottaker(), regelAndel.erBrukerMottaker());
            })
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Fant ikke " + regelAndel));
        regelAndel.getBeregningsresultatFeriepengerPrÅrListe().forEach(prÅr ->
            BeregningsresultatFeriepengerPrÅr.builder()
                .medOpptjeningsår(prÅr.getOpptjeningÅr())
                .medÅrsbeløp(prÅr.getÅrsbeløp().setScale(0, RoundingMode.HALF_UP).longValue())
                .build(beregningsresultatFeriepenger, andel));
    }
}
