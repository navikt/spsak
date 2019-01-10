package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.domene.vedtak.VedtakTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.ForeslaVedtakAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;

@ApplicationScoped
@DtoTilServiceAdapter(dto = ForeslaVedtakAksjonspunktDto.class, adapter = AksjonspunktOppdaterer.class)
public class ForeslåVedtakAksjonspunktOppdaterer extends AbstractVedtaksbrevOverstyringshåndterer implements AksjonspunktOppdaterer<ForeslaVedtakAksjonspunktDto> {

    ForeslåVedtakAksjonspunktOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public ForeslåVedtakAksjonspunktOppdaterer(GrunnlagRepositoryProvider repositoryProvider, HistorikkTjenesteAdapter historikkApplikasjonTjeneste, TotrinnTjeneste totrinnTjeneste, VedtakTjeneste vedtakTjeneste) {
        super(repositoryProvider, historikkApplikasjonTjeneste, totrinnTjeneste, vedtakTjeneste);
    }

    @Override
    public OppdateringResultat oppdater(ForeslaVedtakAksjonspunktDto dto, Behandling behandling) {
        behandling.setAnsvarligSaksbehandler(getCurrentUserId());

        if (dto.isSkalBrukeOverstyrendeFritekstBrev()) {
            setToTrinnskontroll(behandling);
        }
        opprettAksjonspunktForFatterVedtak(behandling);
        totrinnTjeneste.settNyttTotrinnsgrunnlag(behandling);
        opprettHistorikkinnslag(behandling);
        return OppdateringResultat.utenOveropp();
    }

    protected String getCurrentUserId() {
        return SubjectHandler.getSubjectHandler().getUid();
    }
}
