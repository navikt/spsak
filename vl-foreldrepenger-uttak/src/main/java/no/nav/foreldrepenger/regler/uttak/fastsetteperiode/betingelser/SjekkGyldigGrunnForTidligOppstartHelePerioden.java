package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkGyldigGrunnForTidligOppstartHelePerioden.ID)
public class SjekkGyldigGrunnForTidligOppstartHelePerioden extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 13.5.1";

    public SjekkGyldigGrunnForTidligOppstartHelePerioden() {
        super(ID);
    }

    @Override
    @SuppressWarnings("Duplicates")
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        UttakPeriode aktuellPeriode = grunnlag.hentPeriodeUnderBehandling();
        if (PeriodeVurderingType.PERIODE_OK.equals(aktuellPeriode.getPeriodeVurderingType()) || PeriodeVurderingType.ENDRE_PERIODE.equals(aktuellPeriode.getPeriodeVurderingType())) {
            return ja();
        }
        return nei();
    }
}
