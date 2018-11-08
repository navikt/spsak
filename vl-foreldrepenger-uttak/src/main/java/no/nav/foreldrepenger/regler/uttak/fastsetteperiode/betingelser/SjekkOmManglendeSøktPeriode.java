package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Oppholdårsaktype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmManglendeSøktPeriode.ID)
public class SjekkOmManglendeSøktPeriode extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 30.2.1";

    public SjekkOmManglendeSøktPeriode() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        UttakPeriode aktuellPeriode = grunnlag.hentPeriodeUnderBehandling();
        if(aktuellPeriode instanceof OppholdPeriode) {
            OppholdPeriode oppholdPeriode = (OppholdPeriode) aktuellPeriode;
            return Oppholdårsaktype.MANGLENDE_SØKT_PERIODE.equals(oppholdPeriode.getOppholdårsaktype()) ? ja() : nei();
        }
        return nei();
    }
}
