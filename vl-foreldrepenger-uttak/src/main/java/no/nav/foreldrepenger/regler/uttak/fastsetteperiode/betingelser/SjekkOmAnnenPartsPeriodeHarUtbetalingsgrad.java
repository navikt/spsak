package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.math.BigDecimal;

import no.nav.foreldrepenger.perioder.PerioderUtenHelgUtil;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattPeriodeAnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriodeAktivitet;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmAnnenPartsPeriodeHarUtbetalingsgrad.ID)
public class SjekkOmAnnenPartsPeriodeHarUtbetalingsgrad extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 30.0.5";

    public SjekkOmAnnenPartsPeriodeHarUtbetalingsgrad() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        UttakPeriode uttakPeriode = grunnlag.hentPeriodeUnderBehandling();
        for (FastsattPeriodeAnnenPart periodeAnnenPart : grunnlag.getTrekkdagertilstand().getUttakPerioderAnnenPart()) {
            if (PerioderUtenHelgUtil.perioderUtenHelgOverlapper(uttakPeriode, periodeAnnenPart)) {
                if (finnesDetEnAktivitetMedUtbetalingsgradHøyereEnnNull(periodeAnnenPart)) {
                    return ja();
                }
            }
        }
        return nei();
    }

    private boolean finnesDetEnAktivitetMedUtbetalingsgradHøyereEnnNull(FastsattPeriodeAnnenPart periodeAnnenPart) {
        for (UttakPeriodeAktivitet periodeAktivitet : periodeAnnenPart.getUttakPeriodeAktiviteter()) {
            if (periodeAktivitet.getUtbetalingsgrad().compareTo(BigDecimal.ZERO) > 0) {
                return true;
            }
        }
        return false;
    }
}
