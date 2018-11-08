package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmUttakStarterFørUttakForForeldrepengerFørFødsel.ID)
public class SjekkOmUttakStarterFørUttakForForeldrepengerFørFødsel extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 27.5";

    private Konfigurasjon konfigurasjon;

    public SjekkOmUttakStarterFørUttakForForeldrepengerFørFødsel(Konfigurasjon konfigurasjon) {
        super(ID);
        this.konfigurasjon = konfigurasjon;
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        UttakPeriode aktuellPeriode = grunnlag.hentPeriodeUnderBehandling();
        LocalDate startDatoUttak = aktuellPeriode.getFom();
        int ukerFørFødselUttaksgrenseForeldrepenger = konfigurasjon.getParameter(Parametertype.UTTAK_FELLESPERIODE_FØR_FØDSEL_UKER, grunnlag.getFamiliehendelse());
        if (startDatoUttak.isBefore(grunnlag.getFamiliehendelse().minusWeeks(ukerFørFødselUttaksgrenseForeldrepenger))) {
            return ja();
        }
        return nei();
    }

}