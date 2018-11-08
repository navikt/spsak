package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Oppholdårsaktype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class SjekkOmOppholdKvoteAnnenForelder extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 9.0.0";

    public SjekkOmOppholdKvoteAnnenForelder() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        UttakPeriode uttakPeriode = grunnlag.hentPeriodeUnderBehandling();
        if (uttakPeriode instanceof OppholdPeriode) {
            OppholdPeriode oppholdPeriode = (OppholdPeriode)uttakPeriode;
            if (Oppholdårsaktype.KVOTE_ANNEN_FORELDER.equals(oppholdPeriode.getOppholdårsaktype())) {
                return ja();
            }
        }
        return nei();
    }
}
