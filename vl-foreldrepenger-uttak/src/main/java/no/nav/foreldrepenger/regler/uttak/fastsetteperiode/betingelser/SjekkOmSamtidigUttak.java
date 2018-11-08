package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.util.List;

import no.nav.foreldrepenger.perioder.PerioderUtenHelgUtil;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattPeriodeAnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmSamtidigUttak.ID)
public class SjekkOmSamtidigUttak extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 30.0.6";

    public SjekkOmSamtidigUttak() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        UttakPeriode uttakPeriode = grunnlag.hentPeriodeUnderBehandling();
        if (uttakPeriode.isSamtidigUttak() || harAnnenForelderHuketAvForSamtidigUttak(uttakPeriode, grunnlag.getTrekkdagertilstand().getUttakPerioderAnnenPart())) {
            return ja();
        }
        return nei();
    }

    private boolean harAnnenForelderHuketAvForSamtidigUttak(UttakPeriode uttakPeriode, List<FastsattPeriodeAnnenPart> perioderAnnenPart) {
        for (FastsattPeriodeAnnenPart periodeAnnenPart : perioderAnnenPart) {
            if (PerioderUtenHelgUtil.perioderUtenHelgOverlapper(uttakPeriode, periodeAnnenPart)) {
                if (periodeAnnenPart.isSamtidigUttak()) {
                    return true;
                }
            }
        }
        return false;
    }
}
