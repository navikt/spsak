package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmTomForAlleSineKontoer.ID)
public class SjekkOmTomForAlleSineKontoer extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 10.5.1";

    public SjekkOmTomForAlleSineKontoer() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        boolean tomForAlleSineKontoer = true;
        for (Stønadskontotype stønadskontotype : hentSøkerSineKonto(grunnlag)) {
            for (AktivitetIdentifikator aktivitet : grunnlag.getAktiviteter()) {
                int saldo = grunnlag.getTrekkdagertilstand().saldo(aktivitet, stønadskontotype);
                if (saldo > 0) {
                    tomForAlleSineKontoer = false;
                }
            }
        }
        return tomForAlleSineKontoer ? ja() : nei();

    }

    List<Stønadskontotype> hentSøkerSineKonto(FastsettePeriodeGrunnlag grunnlag) {
        return hentSøkerSineKontoer(grunnlag);
    }

    private List<Stønadskontotype> hentSøkerSineKontoer(FastsettePeriodeGrunnlag grunnlag) {
        final List<Stønadskontotype> søkerSineKonto;
        if (grunnlag.isFarRett() && grunnlag.isMorRett()) {
            if (grunnlag.isSøkerMor()) {
                søkerSineKonto = Arrays.asList(Stønadskontotype.MØDREKVOTE, Stønadskontotype.FELLESPERIODE, Stønadskontotype.FORELDREPENGER); // 1 og 5
            } else {
                søkerSineKonto = Arrays.asList(Stønadskontotype.FEDREKVOTE, Stønadskontotype.FELLESPERIODE, Stønadskontotype.FORELDREPENGER); // 3 og 7
            }
        } else { // en har rett
            søkerSineKonto = Collections.singletonList(Stønadskontotype.FORELDREPENGER); // 2 4 6 og 8
        }
        return søkerSineKonto;
    }

}
