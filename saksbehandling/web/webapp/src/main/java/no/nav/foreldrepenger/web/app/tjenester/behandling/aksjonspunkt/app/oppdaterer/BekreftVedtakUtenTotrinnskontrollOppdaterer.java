package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.domene.vedtak.VedtakTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftVedtakUtenTotrinnskontrollDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

@ApplicationScoped
@DtoTilServiceAdapter(dto = BekreftVedtakUtenTotrinnskontrollDto.class, adapter=AksjonspunktOppdaterer.class)
class BekreftVedtakUtenTotrinnskontrollOppdaterer extends AbstractVedtaksbrevOverstyringshåndterer implements AksjonspunktOppdaterer<BekreftVedtakUtenTotrinnskontrollDto> {

    BekreftVedtakUtenTotrinnskontrollOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public BekreftVedtakUtenTotrinnskontrollOppdaterer(GrunnlagRepositoryProvider repositoryProvider,
                                                       HistorikkTjenesteAdapter historikkApplikasjonTjeneste,
                                                       TotrinnTjeneste totrinnTjeneste,
                                                       VedtakTjeneste vedtakTjeneste) {
        super(repositoryProvider, historikkApplikasjonTjeneste, totrinnTjeneste, vedtakTjeneste);
    }

    @Override
    public OppdateringResultat oppdater(BekreftVedtakUtenTotrinnskontrollDto dto, Behandling behandling) {
        if (dto.isSkalBrukeOverstyrendeFritekstBrev()) {
            avbrytVedtakUtenTotrinnskontroll(dto, behandling);
            settFritekstBrev(behandling, dto.getOverskrift(), dto.getFritekstBrev());
            setToTrinnskontroll(behandling);
            opprettAksjonspunktForFatterVedtak(behandling);
            totrinnTjeneste.settNyttTotrinnsgrunnlag(behandling);
            opprettHistorikkinnslag(behandling);
            return OppdateringResultat.medFremoverHopp(FellesTransisjoner.FREMHOPP_TIL_FATTE_VEDTAK);
        }
        return OppdateringResultat.utenOveropp();
    }

    private void avbrytVedtakUtenTotrinnskontroll(BekreftVedtakUtenTotrinnskontrollDto dto, Behandling behandling) {
        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());
        behandling.getAksjonspunktMedDefinisjonOptional(aksjonspunktDefinisjon).ifPresent(a -> aksjonspunktRepository.fjernAksjonspunkt(behandling, a.getAksjonspunktDefinisjon()));
        behandling.setToTrinnsBehandling();
    }
}
