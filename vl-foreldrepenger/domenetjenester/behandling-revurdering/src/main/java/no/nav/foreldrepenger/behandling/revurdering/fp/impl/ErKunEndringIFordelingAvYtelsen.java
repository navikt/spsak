package no.nav.foreldrepenger.behandling.revurdering.fp.impl;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.KonsekvensForYtelsen;
import no.nav.foreldrepenger.behandlingslager.behandling.RettenTil;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.Vedtaksbrev;

class ErKunEndringIFordelingAvYtelsen {
    private ErKunEndringIFordelingAvYtelsen() {}

    public static boolean vurder(boolean erEndringIBeregning, boolean erEndringIUttakFraEndringsdato, Optional<Beregningsgrunnlag> revurderingsGrunnlagOpt, Optional<Beregningsgrunnlag> originalGrunnlagOpt) {
        return !erEndringIBeregning
            && !erEndringIUttakFraEndringsdato
            && kontrollerEndringIFordelingAvYtelsen(revurderingsGrunnlagOpt, originalGrunnlagOpt);
    }

    public static Behandlingsresultat fastsett(Behandling revurdering, boolean erVarselOmRevurderingSendt) {
        Vedtaksbrev vedtaksbrev = utledVedtaksbrev(erVarselOmRevurderingSendt);
        Behandlingsresultat behandlingsresultat = revurdering.getBehandlingsresultat();
        Behandlingsresultat.Builder behandlingsresultatBuilder = Behandlingsresultat.builderEndreEksisterende(behandlingsresultat);
        behandlingsresultatBuilder.medBehandlingResultatType(BehandlingResultatType.FORELDREPENGER_ENDRET);
        behandlingsresultatBuilder.medVedtaksbrev(vedtaksbrev);
        behandlingsresultatBuilder.medRettenTil(RettenTil.HAR_RETT_TIL_FP);
        behandlingsresultatBuilder.leggTilKonsekvensForYtelsen(KonsekvensForYtelsen.ENDRING_I_FORDELING_AV_YTELSEN);
        return behandlingsresultatBuilder.buildFor(revurdering);
    }

    private static boolean kontrollerEndringIFordelingAvYtelsen(Optional<Beregningsgrunnlag> revurderingsGrunnlagOpt, Optional<Beregningsgrunnlag> originalGrunnlagOpt) {

        Beregningsgrunnlag revurderingsGrunnlag = revurderingsGrunnlagOpt.orElse(null);
        Beregningsgrunnlag originalgGrunnlag = originalGrunnlagOpt.orElse(null);

        if (revurderingsGrunnlag == null && originalgGrunnlag == null) {
            return false;
        } else if (revurderingsGrunnlag == null || originalgGrunnlag == null) {
            return true;
        }

        List<BeregningsgrunnlagPeriode> revurderingPerioder = revurderingsGrunnlag.getBeregningsgrunnlagPerioder();
        List<BeregningsgrunnlagPeriode> originalePerioder = originalgGrunnlag.getBeregningsgrunnlagPerioder();


        for (BeregningsgrunnlagPeriode periode : revurderingPerioder) {
            if (erUlikKorresponderendePeriode(originalePerioder, periode)) {
                return true;
            }
        }

        for (BeregningsgrunnlagPeriode periode : originalePerioder) {
            if (erUlikKorresponderendePeriode(revurderingPerioder, periode)) {
                return true;
            }
        }

        return false;
    }

    private static boolean erUlikKorresponderendePeriode(List<BeregningsgrunnlagPeriode> sammenlignPerioder, BeregningsgrunnlagPeriode periodeÅSammenligne) {
        LocalDate fom = periodeÅSammenligne.getBeregningsgrunnlagPeriodeFom();

        Optional<BeregningsgrunnlagPeriode> førsteKronologiskePeriode = sammenlignPerioder.stream().min(Comparator.comparing(BeregningsgrunnlagPeriode::getBeregningsgrunnlagPeriodeFom));
        if (førsteKronologiskePeriode.isPresent()) {
            if (fom.isBefore(førsteKronologiskePeriode.get().getBeregningsgrunnlagPeriodeFom())) {
                return harPerioderUlikeAndeler(periodeÅSammenligne, førsteKronologiskePeriode.get());
            }
        }

        BeregningsgrunnlagPeriode korresponderendePeriode = sammenlignPerioder.stream().filter(originalPeriode ->
            periodeInneholderDato(originalPeriode, fom)).findFirst()
            .orElseThrow(() -> new IllegalStateException("Fant ingen overlapp for beregningsgrunnlagperiode"));

        return harPerioderUlikeAndeler(periodeÅSammenligne, korresponderendePeriode);
    }

    private static boolean periodeInneholderDato(BeregningsgrunnlagPeriode periode, LocalDate dato) {
        if (periode.getBeregningsgrunnlagPeriodeTom() == null) {
            return !dato.isBefore(periode.getBeregningsgrunnlagPeriodeFom());
        }
        return !dato.isBefore(periode.getBeregningsgrunnlagPeriodeFom())
            && !dato.isAfter(periode.getBeregningsgrunnlagPeriodeTom());
    }


    private static boolean harPerioderUlikeAndeler(BeregningsgrunnlagPeriode revuderingPeriode, BeregningsgrunnlagPeriode originalPeriode) {
        List<BeregningsgrunnlagPrStatusOgAndel> revuderingAndeler = revuderingPeriode.getBeregningsgrunnlagPrStatusOgAndelList();
        List<BeregningsgrunnlagPrStatusOgAndel> originaleAndeler = originalPeriode.getBeregningsgrunnlagPrStatusOgAndelList();
        for (BeregningsgrunnlagPrStatusOgAndel andel : revuderingAndeler) {
            Optional<BeregningsgrunnlagPrStatusOgAndel> matchetAndel = finnMatchendeAndel(andel, originaleAndeler);
            if (!matchetAndel.isPresent() || !erAndelerLike(andel, matchetAndel.get())) {
                return true;
            }
        }
        return false;
    }

    private static Optional<BeregningsgrunnlagPrStatusOgAndel> finnMatchendeAndel(BeregningsgrunnlagPrStatusOgAndel andel, List<BeregningsgrunnlagPrStatusOgAndel> originaleAndeler) {
        return originaleAndeler.stream().filter(orginalAndel -> orginalAndel.equals(andel)).findFirst();
    }

    private static boolean erAndelerLike(BeregningsgrunnlagPrStatusOgAndel revurderingAndel, BeregningsgrunnlagPrStatusOgAndel originalAndel) {
        return (revurderingAndel.getDagsatsArbeidsgiver().equals(originalAndel.getDagsatsArbeidsgiver()))
            && (revurderingAndel.getDagsatsBruker().equals(originalAndel.getDagsatsBruker()));
    }

    // https://confluence.adeo.no/display/PK/PK-50504+-+02+-+Kravbeskrivelser
    private static Vedtaksbrev utledVedtaksbrev(boolean erVarselOmRevurderingSendt) {
        if (erVarselOmRevurderingSendt) {
            // Krav 17
            return Vedtaksbrev.AUTOMATISK;
        } else {
            // Krav 12
            return Vedtaksbrev.INGEN;
        }
    }
}
