package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;


public class SjekkOmPeriodeUavklartUtenomNoenTyper extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 30.5.1";

    private Konfigurasjon konfigurasjon;

    public SjekkOmPeriodeUavklartUtenomNoenTyper(Konfigurasjon konfigurasjon) {
        super(ID);
        this.konfigurasjon = konfigurasjon;

    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        UttakPeriode uttakPeriode = grunnlag.hentPeriodeUnderBehandling();
        // Filtrer ut perioder hvor det er søkt om gradering, overføring og far som søker fellesperiode&FK før uke 7 på termin&fødsel
        // fordi de håndteres i sine delregler.
        if (uttakPeriode.harGradering() || uttakPeriode.harSøktOmOverføringAvKvote() || tidligOppstart(grunnlag, uttakPeriode)) {
            return nei();
        } else if (PeriodeVurderingType.UAVKLART_PERIODE.equals(uttakPeriode.getPeriodeVurderingType())) {
            return ja();
        }
        return nei();
    }

    private boolean tidligOppstart(FastsettePeriodeGrunnlag grunnlag, UttakPeriode uttakPeriode) {
        int ukerReservertForMor = konfigurasjon.getParameter(Parametertype.UTTAK_MØDREKVOTE_ETTER_FØDSEL_UKER, grunnlag.getFamiliehendelse());

        return !grunnlag.isSøkerMor() &&
                (Stønadskontotype.FEDREKVOTE.equals(uttakPeriode.getStønadskontotype()) || Stønadskontotype.FELLESPERIODE.equals(uttakPeriode.getStønadskontotype()) ||
                Stønadskontotype.FORELDREPENGER.equals(uttakPeriode.getStønadskontotype()))
                && uttakPeriode.getFom().isBefore(grunnlag.getFamiliehendelse().plusWeeks(ukerReservertForMor));
    }
}
