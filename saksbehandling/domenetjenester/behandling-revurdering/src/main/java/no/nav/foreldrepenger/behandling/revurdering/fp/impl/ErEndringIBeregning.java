package no.nav.foreldrepenger.behandling.revurdering.fp.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;

class ErEndringIBeregning {
    private ErEndringIBeregning() {}

    public static boolean vurder(Optional<Beregningsgrunnlag> revurderingsGrunnlag, Optional<Beregningsgrunnlag> originaltGrunnlag) {
        if (!revurderingsGrunnlag.isPresent() && !originaltGrunnlag.isPresent()) {
            return false;
        } else if (!revurderingsGrunnlag.isPresent() || !originaltGrunnlag.isPresent()) {
            return true;
        }

        List<BeregningsgrunnlagPeriode> originalePerioder = originaltGrunnlag.get().getBeregningsgrunnlagPerioder();
        List<BeregningsgrunnlagPeriode> revurderingsPerioder = revurderingsGrunnlag.get().getBeregningsgrunnlagPerioder();

        List<LocalDate> allePeriodeDatoer = finnAllePeriodersStartdatoer(revurderingsPerioder, originalePerioder);

        for (LocalDate dato : allePeriodeDatoer) {
            Long dagsatsRevurderingsgrunnlag = finnGjeldendeDagsatsForDenneDatoen(dato, revurderingsPerioder);
            Long dagsatsOriginaltGrunnlag = finnGjeldendeDagsatsForDenneDatoen(dato, originalePerioder);
            if (!dagsatsRevurderingsgrunnlag.equals(dagsatsOriginaltGrunnlag)) {
                return true;
            }
        }
        return false;
    }

    private static List<LocalDate> finnAllePeriodersStartdatoer(List<BeregningsgrunnlagPeriode> revurderingsPerioder, List<BeregningsgrunnlagPeriode> originalePerioder) {
        List<LocalDate> startDatoer = new ArrayList<>();
        for (BeregningsgrunnlagPeriode periode : revurderingsPerioder) {
            startDatoer.add(periode.getBeregningsgrunnlagPeriodeFom());
        }
        for (BeregningsgrunnlagPeriode periode : originalePerioder) {
            if (!startDatoer.contains(periode.getBeregningsgrunnlagPeriodeFom())) {
                startDatoer.add(periode.getBeregningsgrunnlagPeriodeFom());
            }
        }
        return startDatoer;
    }

    private static Long finnGjeldendeDagsatsForDenneDatoen(LocalDate dato, List<BeregningsgrunnlagPeriode> perioder) {
        // Hvis dato er før starten på den første perioden bruker vi første periodes dagsats
        Optional<BeregningsgrunnlagPeriode> førsteKronologiskePeriode = perioder.stream()
            .min(Comparator.comparing(BeregningsgrunnlagPeriode::getBeregningsgrunnlagPeriodeFom));
        if (førsteKronologiskePeriode.filter(periode -> dato.isBefore(periode.getBeregningsgrunnlagPeriodeFom())).isPresent()) {
            return førsteKronologiskePeriode.get().getDagsats();
        }
        for (BeregningsgrunnlagPeriode periode : perioder) {
            if (periode.getPeriode().inkluderer(dato)) {
                return periode.getDagsats();
            }
        }
        throw new IllegalStateException("Finner ikke dagsats for denne perioden");
    }

}
