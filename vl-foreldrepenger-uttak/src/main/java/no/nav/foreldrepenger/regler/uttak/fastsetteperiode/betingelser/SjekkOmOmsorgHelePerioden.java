package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class SjekkOmOmsorgHelePerioden extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 30.7";
    public static final String BESKRIVELSE = "Har søker omsorg for barnet?";

    public SjekkOmOmsorgHelePerioden() {
        super(SjekkOmOmsorgHelePerioden.class.getSimpleName());
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        UttakPeriode uttakPeriode = grunnlag.hentPeriodeUnderBehandling();
        for (LukketPeriode periodeUtenOmsorg : grunnlag.getPerioderUtenOmsorg()) {
            if (periodeUtenOmsorg.overlapper(uttakPeriode)) {
                return nei();
            }
        }
        return ja();
    }
}
