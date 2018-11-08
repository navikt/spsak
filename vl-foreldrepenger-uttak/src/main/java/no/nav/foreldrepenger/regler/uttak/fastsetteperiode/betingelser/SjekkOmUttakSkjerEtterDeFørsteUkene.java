package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmUttakSkjerEtterDeFørsteUkene.ID)
public class SjekkOmUttakSkjerEtterDeFørsteUkene extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 19.2";

    private Konfigurasjon konfigurasjon;

    public SjekkOmUttakSkjerEtterDeFørsteUkene(Konfigurasjon konfigurasjon) {
        super(ID);
        this.konfigurasjon = konfigurasjon;
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        UttakPeriode aktuellPeriode = grunnlag.hentPeriodeUnderBehandling();
        int minsteKravTilMødrekvoteEtterFødsel = konfigurasjon.getParameter(Parametertype.UTTAK_MØDREKVOTE_ETTER_FØDSEL_UKER, grunnlag.getFamiliehendelse());
        LocalDate tidligsteStartDatoForFedrekvote = grunnlag.getFamiliehendelse().plusWeeks(minsteKravTilMødrekvoteEtterFødsel);
        if (!aktuellPeriode.getFom().isBefore(tidligsteStartDatoForFedrekvote)) {
            return ja();
        }
        return nei();
    }
}
