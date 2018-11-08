package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.perioder.PerioderUtenHelgUtil;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattPeriodeAnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmHvisOverlapperSåSamtykkeMellomParter.ID)
public class SjekkOmHvisOverlapperSåSamtykkeMellomParter extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 30.0.2";

    public SjekkOmHvisOverlapperSåSamtykkeMellomParter() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        boolean ikkeOverlapperEllerOverlapperOgSamtykke = true;
        UttakPeriode uttakPeriode = grunnlag.hentPeriodeUnderBehandling();
        if (grunnlag.getTrekkdagertilstand() != null && !grunnlag.getTrekkdagertilstand().getUttakPerioderAnnenPart().isEmpty()) {
            for (FastsattPeriodeAnnenPart periodeAnnenPart : grunnlag.getTrekkdagertilstand().getUttakPerioderAnnenPart()) {
                if (PerioderUtenHelgUtil.perioderUtenHelgOverlapper(uttakPeriode, periodeAnnenPart)) {
                    if (!grunnlag.isSamtykke()) {
                        ikkeOverlapperEllerOverlapperOgSamtykke = false;
                    }
                }
            }
        }
        return ikkeOverlapperEllerOverlapperOgSamtykke ? ja() : nei();
    }
}
