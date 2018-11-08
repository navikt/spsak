package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmPeriodenSlutterFørFamiliehendelse.ID)
public class SjekkOmPeriodenSlutterFørFamiliehendelse extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 11.5";

    public SjekkOmPeriodenSlutterFørFamiliehendelse() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        UttakPeriode aktuellPeriode = grunnlag.hentPeriodeUnderBehandling();
        LocalDate familiehendelse = grunnlag.getFamiliehendelse();
        if (aktuellPeriode.getTom().isBefore(familiehendelse)) {
            return ja();
        }
        return nei();
    }


}
