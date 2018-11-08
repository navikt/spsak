package no.nav.foreldrepenger.web.app.tjenester.brev;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.UPDATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerApplikasjonTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BestillBrevDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BrevmalDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.BehandlingIdDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = "brev")
@Path("/brev")
@RequestScoped
@Transaction
public class BrevRestTjeneste {

    private DokumentBestillerApplikasjonTjeneste dokumentBestillerApplikasjonTjeneste;

    public BrevRestTjeneste() {
        // For Rest-CDI
    }

    @Inject
    public BrevRestTjeneste(DokumentBestillerApplikasjonTjeneste dokumentBestillerApplikasjonTjeneste) {
        this.dokumentBestillerApplikasjonTjeneste = dokumentBestillerApplikasjonTjeneste;
    }

    @POST
    @Timed
    @Path("/forhandsvis")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Returnerer en pdf som er en forhåndsvisning av brevet")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentForhåndsvisningDokument(
        @ApiParam("Inneholder kode til brevmal og data som skal flettes inn i brevet") @Valid BestillBrevDto bestillBrevDto) { // NOSONAR
        byte[] dokument = dokumentBestillerApplikasjonTjeneste.hentForhåndsvisningDokument(bestillBrevDto);
        Response.ResponseBuilder responseBuilder = Response.ok(dokument);
        responseBuilder.type("application/pdf");
        responseBuilder.header("Content-Disposition", "filename=dokument.pdf");
        return responseBuilder.build();
    }

    @POST
    @Timed
    @Path("/bestill")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Bestiller generering og sending av brevet")
    @BeskyttetRessurs(action = UPDATE, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public void bestillDokument(
        @ApiParam("Inneholder kode til brevmal og data som skal flettes inn i brevet") @Valid BestillBrevDto bestillBrevDto) { // NOSONAR
        dokumentBestillerApplikasjonTjeneste.bestillDokument(bestillBrevDto, HistorikkAktør.SAKSBEHANDLER);
        if (DokumentMalType.REVURDERING_DOK.equals(bestillBrevDto.getBrevmalkode())) {
            settBehandlingPåVent(bestillBrevDto, Venteårsak.AVV_RESPONS_REVURDERING);
        }
    }

    private void settBehandlingPåVent(BestillBrevDto bestillBrevDto, Venteårsak avvResponsRevurdering) {
        dokumentBestillerApplikasjonTjeneste.settBehandlingPåVent(bestillBrevDto.getBehandlingId(), avvResponsRevurdering);
    }

    @POST
    @Timed
    @Path("/mottakere")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @ApiOperation(value = "Henter liste med tilgjengelige mottakere av melding.")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK, sporingslogg = false)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<String> hentMottakere(@Valid BehandlingIdDto dto) {
        return dokumentBestillerApplikasjonTjeneste.hentMottakere(dto.getBehandlingId()); // NOSONAR
    }

    @POST
    @Timed
    @Path("/maler")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @ApiOperation(value = "Henter liste over tilgjengelige brevtyper")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK, sporingslogg = false)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<BrevmalDto> hentMaler(@Valid BehandlingIdDto dto) {
        return dokumentBestillerApplikasjonTjeneste.hentBrevmalerFor(dto.getBehandlingId()); // NOSONAR
    }

    @POST
    @Timed
    @Path("/varsel/revurdering")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @ApiOperation(value = "Sjekk har varsel sendt om revurdering")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Boolean harSendtVarselOmRevurdering(@Valid BehandlingIdDto dto) {
        return dokumentBestillerApplikasjonTjeneste.erDokumentProdusert(dto.getBehandlingId(), DokumentMalType.REVURDERING_DOK); // NOSONAR
    }
}
