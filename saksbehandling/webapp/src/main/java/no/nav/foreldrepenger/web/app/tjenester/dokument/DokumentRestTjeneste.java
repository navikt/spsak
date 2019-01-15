package no.nav.foreldrepenger.web.app.tjenester.dokument;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import no.nav.foreldrepenger.behandling.rest.SaksnummerDto;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.dokumentarkiv.ArkivDokument;
import no.nav.foreldrepenger.domene.dokumentarkiv.ArkivJournalPost;
import no.nav.foreldrepenger.domene.dokumentarkiv.DokumentArkivTjeneste;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.web.app.tjenester.dokument.dto.DokumentDto;
import no.nav.foreldrepenger.web.app.tjenester.dokument.dto.DokumentIdDto;
import no.nav.foreldrepenger.web.app.tjenester.dokument.dto.JournalpostIdDto;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = "dokument")
@Path("/dokument")
@RequestScoped
public class DokumentRestTjeneste {
    private DokumentArkivTjeneste dokumentArkivTjeneste;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private FagsakRepository fagsakRepository;

    public DokumentRestTjeneste() {
        // For Rest-CDI
    }

    @Inject
    public DokumentRestTjeneste(DokumentArkivTjeneste dokumentArkivTjeneste, InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                FagsakRepository fagsakRepository) {
        this.dokumentArkivTjeneste = dokumentArkivTjeneste;
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.fagsakRepository = fagsakRepository;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/hent-dokumentliste")
    @ApiOperation(value = "Henter dokumentlisten knyttet til en sak",
        notes = ("Oversikt over alle pdf dokumenter fra dokumentarkiv registrert for saksnummer."))
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Collection<DokumentDto> hentAlleDokumenterForSak(@NotNull @QueryParam("saksnummer") @ApiParam("Saksnummer") @Valid SaksnummerDto saksnummerDto) {
        try {
            Saksnummer saksnummer = new Saksnummer(saksnummerDto.getVerdi());
            final Optional<Fagsak> fagsak = fagsakRepository.hentSakGittSaksnummer(saksnummer);
            final Long fagsakId = fagsak.map(Fagsak::getId).orElse(null);
            if (fagsakId == null) {
                return new ArrayList<>();
            }

            Map<JournalpostId, Set<Long>> behandlingerPerInntektsmelding = inntektArbeidYtelseTjeneste.hentBehandlingerPerInntektsmeldingFor(fagsakId);
            // Burde brukt map på dokumentid, men den lagres ikke i praksis.

            List<ArkivJournalPost> journalPostList = dokumentArkivTjeneste.hentAlleDokumenterForVisning(saksnummer);
            List<DokumentDto> dokumentResultat = new ArrayList<>();
            journalPostList.forEach(arkivJournalPost -> dokumentResultat.addAll(mapFraArkivJournalPost(arkivJournalPost, behandlingerPerInntektsmelding)));
            dokumentResultat.sort(Comparator.comparing(DokumentDto::getTidspunkt, Comparator.nullsFirst(Comparator.reverseOrder())));

            return dokumentResultat;
        } catch (ManglerTilgangException e) {
            throw DokumentRestTjenesteFeil.FACTORY.applikasjonHarIkkeTilgangTilHentJournalpostListeTjeneste(e).toException();
        }
    }

    private List<DokumentDto> mapFraArkivJournalPost(ArkivJournalPost arkivJournalPost, Map<JournalpostId, Set<Long>> inntektsMeldinger) {
        List<DokumentDto> dokumentForJP = new ArrayList<>();
        if (arkivJournalPost.getHovedDokument() != null) {
            dokumentForJP.add(mapFraArkivDokument(arkivJournalPost, arkivJournalPost.getHovedDokument(), inntektsMeldinger));
        }
        if (arkivJournalPost.getAndreDokument() != null) {
            arkivJournalPost.getAndreDokument().forEach(dok -> dokumentForJP.add(mapFraArkivDokument(arkivJournalPost, dok, inntektsMeldinger)));
        }
        return dokumentForJP;
    }

    private DokumentDto mapFraArkivDokument(ArkivJournalPost arkivJournalPost, ArkivDokument arkivDokument, Map<JournalpostId, Set<Long>> inntektsMeldinger) {
        DokumentDto dto = new DokumentDto(arkivJournalPost, arkivDokument);
        if (DokumentTypeId.INNTEKTSMELDING.equals(arkivDokument.getDokumentTypeId()) && inntektsMeldinger.get(arkivJournalPost.getJournalpostId()) != null) {
            Set<Long> behandlinger = inntektsMeldinger.get(arkivJournalPost.getJournalpostId());
            dto.setBehandlinger(new ArrayList<>(behandlinger));

            Optional<String> virksomhet = inntektArbeidYtelseTjeneste.hentInnteksmeldingFor(dto.getJournalpostId())
                .stream()
                .filter(Objects::nonNull)
                .map(Inntektsmelding::getVirksomhet)
                .map(Virksomhet::getNavn)
                .filter(Objects::nonNull)
                .findFirst();
            virksomhet.ifPresent(dto::setGjelderFor);
        }
        return dto;
    }

    @GET
    @Path("/hent-dokument")
    @ApiOperation(value = "Søk etter dokument på JOARK-identifikatorene journalpostId og dokumentId",
        notes = ("Retunerer dokument som er tilknyttet saksnummer, journalpostId og dokumentId."))
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentDokument(@NotNull @QueryParam("saksnummer") @ApiParam("Saksnummer") @Valid SaksnummerDto saksnummer,
                                 @NotNull @QueryParam("journalpostId") @ApiParam("Unik identifikator av journalposten (forsendelsenivå)") @Valid JournalpostIdDto journalpostId,
                                 @NotNull @QueryParam("dokumentId") @ApiParam("Unik identifikator av DokumentInfo/Dokumentbeskrivelse (dokumentnivå)") @Valid DokumentIdDto dokumentId) {
        try {
            ResponseBuilder responseBuilder = Response.ok(new ByteArrayInputStream(dokumentArkivTjeneste.hentDokument(new JournalpostId(journalpostId.getJournalpostId()), dokumentId.getDokumentId())));
            responseBuilder.type("application/pdf");
            responseBuilder.header("Content-Disposition", "filename=dokument.pdf");
            return responseBuilder.build();
        } catch (TekniskException e) {
            throw DokumentRestTjenesteFeil.FACTORY.dokumentIkkeFunnet(journalpostId.getJournalpostId(), dokumentId.getDokumentId(), e).toException();
        } catch (ManglerTilgangException e) {
            throw DokumentRestTjenesteFeil.FACTORY.applikasjonHarIkkeTilgangTilHentDokumentTjeneste(e).toException();
        }
    }


}
