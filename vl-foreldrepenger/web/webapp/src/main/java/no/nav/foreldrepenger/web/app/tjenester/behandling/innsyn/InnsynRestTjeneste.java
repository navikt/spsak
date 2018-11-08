package no.nav.foreldrepenger.web.app.tjenester.behandling.innsyn;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.InnsynDokumentEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.InnsynEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.lagretvedtak.LagretVedtakMedBehandlingType;
import no.nav.foreldrepenger.domene.vedtak.VedtakTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.BehandlingIdDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.VedtaksdokumentasjonDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = { "innsyn" })
@Path("/behandling/innsyn")
@RequestScoped
@Transaction
public class InnsynRestTjeneste {

    private BehandlingRepository behandlingRepository;
    private VedtakTjeneste vedtakTjeneste;

    public InnsynRestTjeneste() {
        // for CDI proxy
    }

    @Inject
    public InnsynRestTjeneste(BehandlingRepositoryProvider behandlingRepositoryProvider,
                              VedtakTjeneste vedtakTjeneste) {
        this.vedtakTjeneste = vedtakTjeneste;
        this.behandlingRepository = behandlingRepositoryProvider.getBehandlingRepository();
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Returnerer vurdering av en klage fra ulike instanser") })
    @ApiOperation(value = "Hent informasjon om klagevurdering for en klagebehandling", notes = ("Returnerer info om vurdering av klage"), response=InnsynsbehandlingDto.class)
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response getInnsynsbehandling(@NotNull @QueryParam("behandlingId") @Valid BehandlingIdDto behandlingIdDto) {
        Long behandlingId = behandlingIdDto.getBehandlingId();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        InnsynsbehandlingDto dto = mapFra(behandling);
        CacheControl cc = new CacheControl();
        cc.setNoCache(true);
        cc.setNoStore(true);
        cc.setMaxAge(0);
        return Response.ok(dto).cacheControl(cc).build();
    }

    private InnsynsbehandlingDto mapFra(Behandling behandling) {
        InnsynsbehandlingDto dto = new InnsynsbehandlingDto();
        List<LagretVedtakMedBehandlingType> lagreteVedtak = vedtakTjeneste.hentLagreteVedtakPåFagsak(behandling.getFagsakId());

        InnsynEntitet innsyn = behandling.getInnsyn();
        if (innsyn == null && lagreteVedtak.isEmpty()) {
            return null; // quick return
        }

        if (innsyn != null) {
            dto.setInnsynMottattDato(innsyn.getMottattDato());
            dto.setInnsynResultatType(innsyn.getInnsynResultatType());

            List<InnsynDokumentDto> doks = new ArrayList<>();
            if (innsyn.getInnsynDokumenter() != null) {
                for (InnsynDokumentEntitet innsynDokument : innsyn.getInnsynDokumenter()) {
                    InnsynDokumentDto dokumentDto = new InnsynDokumentDto();
                    dokumentDto.setDokumentId(innsynDokument.getDokumentId());
                    dokumentDto.setJournalpostId(innsynDokument.getJournalpostId().getVerdi());
                    dokumentDto.setFikkInnsyn(innsynDokument.isFikkInnsyn());

                    doks.add(dokumentDto);
                }
            }

            dto.setDokumenter(doks);
        }

        lagreteVedtak.forEach(lagretVedtakMedBehandlingType -> {
            VedtaksdokumentasjonDto vedtaksdokumentasjonDto = new VedtaksdokumentasjonDto();
            vedtaksdokumentasjonDto.setDokumentId(lagretVedtakMedBehandlingType.getId().toString());
            vedtaksdokumentasjonDto.setOpprettetDato(lagretVedtakMedBehandlingType.getOpprettetDato());
            vedtaksdokumentasjonDto.setTittel(lagretVedtakMedBehandlingType.getBehandlingType());
            dto.getVedtaksdokumentasjon().add(vedtaksdokumentasjonDto);
        });

        return dto;

    }

}
