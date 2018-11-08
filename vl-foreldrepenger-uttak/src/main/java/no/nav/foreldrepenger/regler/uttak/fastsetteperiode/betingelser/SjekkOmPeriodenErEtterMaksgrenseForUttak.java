package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmPeriodenErEtterMaksgrenseForUttak.ID)
public class SjekkOmPeriodenErEtterMaksgrenseForUttak extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 15.6";
    private Konfigurasjon konfigurasjon;

    public SjekkOmPeriodenErEtterMaksgrenseForUttak(Konfigurasjon konfigurasjon) {
        super(ID);
        this.konfigurasjon = konfigurasjon;
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        UttakPeriode uttakPeriode = grunnlag.hentPeriodeUnderBehandling();
        LocalDate grense = grunnlag.getMaksgrenseForLovligeUttaksdag(konfigurasjon);
        if (uttakPeriode.getFom().isAfter(grense) || uttakPeriode.getFom().equals(grense)) {
            return ja();
        }
        return nei();
    }
}
