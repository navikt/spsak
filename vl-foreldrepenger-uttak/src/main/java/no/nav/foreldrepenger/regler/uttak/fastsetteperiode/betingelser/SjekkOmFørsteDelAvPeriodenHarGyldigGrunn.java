package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.util.List;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GyldigGrunnPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Periode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmFørsteDelAvPeriodenHarGyldigGrunn.ID)
public class SjekkOmFørsteDelAvPeriodenHarGyldigGrunn extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 13.5.3";

    public SjekkOmFørsteDelAvPeriodenHarGyldigGrunn() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        // TODO (hb) Gyldige grunner til tidlig oppstart ikke implementert i PK-48339 - implementeres i en senere historie


        UttakPeriode aktuellPeriode = grunnlag.hentPeriodeUnderBehandling();
        List<GyldigGrunnPeriode> aktuelleGyldigGrunnPeriode = grunnlag.getAktuelleGyldigeGrunnPerioder();

        if (aktuelleGyldigGrunnPeriode.isEmpty()) {
            return nei();
        }

        GyldigGrunnPeriode periode = aktuelleGyldigGrunnPeriode.get(0);

        if (starterGyldigGrunnEtterAktuellPeriode(periode, aktuellPeriode)) {
            return nei();
        }
        return ja();

    }


    private boolean starterGyldigGrunnEtterAktuellPeriode(Periode periode1, Periode periode2) {
        return periode1.getFom().isAfter(periode2.getFom());
    }
}
