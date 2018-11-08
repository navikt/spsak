package no.nav.foreldrepenger.web.app.tjenester.dokumentbestiller;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.function.Predicate;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.codahale.metrics.annotation.Timed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import no.nav.foreldrepenger.behandling.revurdering.impl.RevurderingTjenesteProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.Vedtaksbrev;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerApplikasjonTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BestillVedtakBrevDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = "dokumentbestiller")
@Path("/dokumentbestiller")
@RequestScoped
@Transaction
public class DokumentBestillerRestTjeneste {
    private static final String ERROR_MESSAGE_STYLE_TAG = "<HTML></p><div style=\"background-color:#ba3a26\"><h2><center><strong>";
    private static final String ERROR_HTML_STYLE_TAG_CLOSE = "</strong></center></h2></div></HTML>";

    private DokumentBestillerApplikasjonTjeneste dokumentBestillerApplikasjonTjeneste;
    private RevurderingTjenesteProvider revurderingTjenesteProvider;

    public DokumentBestillerRestTjeneste() {
        // For Rest-CDI
    }

    @Inject
    public DokumentBestillerRestTjeneste(
                                         DokumentBestillerApplikasjonTjeneste dokumentBestillerApplikasjonTjeneste,
                                         RevurderingTjenesteProvider revurderingTjenesteProvider) {
        this.dokumentBestillerApplikasjonTjeneste = dokumentBestillerApplikasjonTjeneste;
        this.revurderingTjenesteProvider = revurderingTjenesteProvider;
    }

    @POST
    @Timed
    @Path("/forhandsvis-vedtaksbrev")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Forhåndsvis vedtaksbrev.",
        notes = ("Forhåndsvis vedtaksbrev basert på mal og metadata tilknyttet brevet."))
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response forhandsvisVedtaksbrev(@ApiParam("bestillVedtakBrevDto") @Valid BestillVedtakBrevDto bestillVedtakBrevDto) {
        BestillVedtakBrevDto bestillVedtakBrev = new BestillVedtakBrevDto(bestillVedtakBrevDto.getBehandlingId(),
            bestillVedtakBrevDto.getFritekst());
        bestillVedtakBrev.setFritekstBrev(bestillVedtakBrevDto.getFritekstBrev());
        bestillVedtakBrev.setOverskrift(bestillVedtakBrevDto.getOverskrift());
        bestillVedtakBrev.setSkalBrukeOverstyrendeFritekstBrev(bestillVedtakBrevDto.skalBrukeOverstyrendeFritekstBrev());

        Predicate<Behandling> revurderingMedUendretUtfallEllerFritekstBrev = (Behandling b) -> {
            final boolean skalBrukeManueltBrev = bestillVedtakBrevDto.skalBrukeOverstyrendeFritekstBrev();
            final boolean finnesAllerede = bestillVedtakBrevDto.finnesAllerede();

            // sjekk om det foreligger fritekstbrev i behandlingsresultatet
            Behandlingsresultat behandlingsresultat = b.getBehandlingsresultat();
            if (finnesAllerede && skalBrukeManueltBrev && Vedtaksbrev.FRITEKST.equals(behandlingsresultat.getVedtaksbrev())) {
                // Case: Manuelt brev, lagret
                bestillVedtakBrev.setSkalBrukeOverstyrendeFritekstBrev(true);
                bestillVedtakBrev.setOverskrift(behandlingsresultat.getOverskrift());
                bestillVedtakBrev.setFritekstBrev(behandlingsresultat.getFritekstbrev());
            } else if (finnesAllerede && !Vedtaksbrev.FRITEKST.equals(behandlingsresultat.getVedtaksbrev())) {
                // Case: Automatisk brev, lagret
                bestillVedtakBrev.setSkalBrukeOverstyrendeFritekstBrev(false);
            } else if (!finnesAllerede && !skalBrukeManueltBrev) {
                // Case: Automatisk brev, ikke lagret
                bestillVedtakBrev.setSkalBrukeOverstyrendeFritekstBrev(Vedtaksbrev.FRITEKST.equals(behandlingsresultat.getVedtaksbrev()));
            }

            // sjekk om behandlinge er revurdering med uendret utfall
            return revurderingTjenesteProvider.finnRevurderingTjenesteFor(b.getFagsak()).erRevurderingMedUendretUtfall(b);
        };

        return buildResponse(dokumentBestillerApplikasjonTjeneste.forhandsvisVedtaksbrev(bestillVedtakBrev, revurderingMedUendretUtfallEllerFritekstBrev));
    }

    private Response buildResponse(byte[] pdf) {
        ResponseBuilder responseBuilder;
        if (pdf != null) {
            responseBuilder = Response.ok(pdf);
            responseBuilder.type("application/pdf");
            responseBuilder.header("Content-Disposition", "filename=vedtak.pdf");
        } else {
            responseBuilder = Response.ok(ERROR_MESSAGE_STYLE_TAG + "Dokument kunne ikke vises." + ERROR_HTML_STYLE_TAG_CLOSE);
            responseBuilder.type(MediaType.TEXT_HTML);
        }
        return responseBuilder.build();
    }
}
