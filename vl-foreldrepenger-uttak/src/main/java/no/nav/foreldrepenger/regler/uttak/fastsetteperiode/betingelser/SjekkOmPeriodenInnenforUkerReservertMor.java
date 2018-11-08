package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmPeriodenInnenforUkerReservertMor.ID)
public class SjekkOmPeriodenInnenforUkerReservertMor extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 10.3";

    private Konfigurasjon konfigurasjon;

    public SjekkOmPeriodenInnenforUkerReservertMor(Konfigurasjon konfigurasjon) {
        super(SjekkOmPeriodenInnenforUkerReservertMor.class.getSimpleName());
        this.konfigurasjon = konfigurasjon;
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        UttakPeriode aktuellPeriode = grunnlag.hentPeriodeUnderBehandling();

        LocalDate familiehendelse = grunnlag.getFamiliehendelse();

        int antallUkerEtterFødsel = konfigurasjon.getParameter(Parametertype.UTTAK_MØDREKVOTE_ETTER_FØDSEL_UKER, familiehendelse);

        LukketPeriode periodeEtterFødselNormaltReservertMor = new LukketPeriode(familiehendelse, familiehendelse.plusWeeks(antallUkerEtterFødsel).minusDays(1));
        if (periodeEtterFødselNormaltReservertMor.overlapper(aktuellPeriode)) {
            if (aktuellPeriode.erOmsluttetAv(periodeEtterFødselNormaltReservertMor)) {
                return ja();
            } else {
                throw new IllegalArgumentException("Utvikler-feil: periode er ikke knekt riktig fom=" + aktuellPeriode.getFom() + " tom=" + aktuellPeriode.getTom());
            }
        } else {
            return nei();
        }
    }
}
