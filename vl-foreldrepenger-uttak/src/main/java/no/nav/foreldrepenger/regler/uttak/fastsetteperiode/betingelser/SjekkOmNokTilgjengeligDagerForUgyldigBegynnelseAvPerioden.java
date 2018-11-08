package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GyldigGrunnPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmNokTilgjengeligDagerForUgyldigBegynnelseAvPerioden.ID)
public class SjekkOmNokTilgjengeligDagerForUgyldigBegynnelseAvPerioden extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK XX3";

    public SjekkOmNokTilgjengeligDagerForUgyldigBegynnelseAvPerioden() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        int virkedager = finnAntallVirkedagerForUgyldigPeriode(grunnlag);

        for (AktivitetIdentifikator aktivitet : grunnlag.getAktiviteter()) {
            int saldo = grunnlag.getTrekkdagertilstand().saldo(aktivitet, Stønadskontotype.MØDREKVOTE);
            if (saldo < virkedager) {
                return nei();
            }
        }
        return ja();
    }

    private int finnAntallVirkedagerForUgyldigPeriode(FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag) {
        UttakPeriode aktuellperiode = fastsettePeriodeGrunnlag.hentPeriodeUnderBehandling();
        List<GyldigGrunnPeriode> aktuelleGyldigeGrunnPerioder = fastsettePeriodeGrunnlag.getAktuelleGyldigeGrunnPerioder();
        LocalDate fom = aktuellperiode.getFom();
        LocalDate tom = aktuelleGyldigeGrunnPerioder.get(0).getFom().minusDays(1);
        return Virkedager.beregnAntallVirkedager(fom, tom);
    }
}
