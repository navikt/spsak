package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmTilgjengeligeDager.ID)
public class SjekkOmTilgjengeligeDager extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 10.5";
    public static final String BESKRIVELSE = "Er det noen disponible stønadsdager på kvoten?";

    public SjekkOmTilgjengeligeDager() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        UttakPeriode aktuellPeriode = grunnlag.hentPeriodeUnderBehandling();
        Stønadskontotype stønadskontotype = aktuellPeriode.getStønadskontotype();

        for (AktivitetIdentifikator aktivitet : grunnlag.getAktiviteter()) {
            int saldo = grunnlag.getTrekkdagertilstand().saldo(aktivitet, stønadskontotype);
            if (saldo <= 0) {
                return nei();
            }
            if (aktuellPeriode.isFlerbarnsdager()) {
                int saldoFlerbarnsdager = grunnlag.getTrekkdagertilstand().saldo(aktivitet, Stønadskontotype.FLERBARNSDAGER);
                if (saldoFlerbarnsdager <= 0) {
                    return nei();
                }
            }

        }
        return ja();
    }

}
