package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelsePeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utsettelseårsaktype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmUtsettelsePgaSykdomSkade.ID)
public class SjekkOmUtsettelsePgaSykdomSkade extends LeafSpecification<FastsettePeriodeGrunnlag> {
    public static final String ID = "FP_VK 18.3.1";

    public SjekkOmUtsettelsePgaSykdomSkade() {
        super(ID);
    }


    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        UttakPeriode uttakPeriode = grunnlag.hentPeriodeUnderBehandling();
        if (uttakPeriode instanceof UtsettelsePeriode) {
            UtsettelsePeriode utsettelsePeriode = (UtsettelsePeriode)uttakPeriode;
            if (Utsettelseårsaktype.SYKDOM_SKADE.equals(utsettelsePeriode.getUtsettelseårsaktype())) {
                return ja();
            }
        }
        return nei();
    }
}
