package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.math.BigDecimal;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedFulltArbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class SjekkOmFulltArbeidForUtsettelse extends LeafSpecification<FastsettePeriodeGrunnlag> {
    public static final String ID = "FP_VK 18.2.4";

    public SjekkOmFulltArbeidForUtsettelse() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        UttakPeriode uttakPeriode = grunnlag.hentPeriodeUnderBehandling();
        // Søker har registrert stillingsprosent >100% i AA-registeret
        for (PeriodeMedFulltArbeid periodeMedFulltArbeid : grunnlag.getPerioderMedFulltArbeid()) {
            if (uttakPeriode.erOmsluttetAv(periodeMedFulltArbeid)) {
                return ja();
            }
        }

        // Søker har status som selvstendig næringsdrivende, frilans eller kombinasjon av de to
        for (AktivitetIdentifikator aktivitetIdentifikator : grunnlag.getAktiviteter()) {
            if (AktivitetType.FRILANS.equals(aktivitetIdentifikator.getAktivitetType())) {
                if (grunnlag.getArbeidsprosenter().getStillingsprosent(AktivitetIdentifikator.forFrilans(), uttakPeriode).compareTo(BigDecimal.valueOf(100)) >= 0) {
                    return ja();
                }
            }
            if (AktivitetType.SELVSTENDIG_NÆRINGSDRIVENDE.equals(aktivitetIdentifikator.getAktivitetType())) {
                if (grunnlag.getArbeidsprosenter().getStillingsprosent(AktivitetIdentifikator.forSelvstendigNæringsdrivende(), uttakPeriode).compareTo(BigDecimal.valueOf(100)) >= 0) {
                    return ja();
                }
            }
        }
        return nei();
    }
}
