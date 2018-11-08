package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmUttaketStarterFørLovligUttakFørFødsel.ID)
public class SjekkOmUttaketStarterFørLovligUttakFørFødsel extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 27.2";

    private Konfigurasjon konfigurasjon;

    public SjekkOmUttaketStarterFørLovligUttakFørFødsel(Konfigurasjon konfigurasjon) {
        super(ID);
        this.konfigurasjon = konfigurasjon;
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        UttakPeriode aktuellPeriode = grunnlag.hentPeriodeUnderBehandling();
        LocalDate startDatoUttak = aktuellPeriode.getFom();
        int ukerFørFødselUttaksgrense = konfigurasjon.getParameter(Parametertype.LOVLIG_UTTAK_FØR_FØDSEL_UKER, grunnlag.getFamiliehendelse());
        if (startDatoUttak.isBefore(grunnlag.getFamiliehendelse().minusWeeks(ukerFørFødselUttaksgrense))) {
            return ja();
        }
        return nei();
    }

}