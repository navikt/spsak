package no.nav.foreldrepenger.web.app.tjenester.behandling.personopplysning;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.time.LocalDate;
import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import no.finn.unleash.Unleash;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeRepository;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.BehandlingIdDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.medlem.MedlemDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.medlem.MedlemDtoTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.medlem.MedlemV2Dto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = {"behandling/person"})
@Path("/behandling/person")
@RequestScoped
@Transaction
public class PersonRestTjeneste {

    private VergeRepository vergeRepository;
    private VergeDtoTjeneste vergeDtoTjenesteImpl;
    private MedlemDtoTjeneste medlemDtoTjeneste;
    private PersonopplysningDtoPersonIdentTjeneste personopplysningFnrFinder;
    private Unleash unleash;
    private PersonopplysningDtoTjeneste personopplysningDtoTjeneste;

    public PersonRestTjeneste() {
        // for CDI proxy
    }

    @Inject
    public PersonRestTjeneste(BehandlingRepositoryProvider repositoryProvider,
                              VergeDtoTjeneste vergeTjeneste,
                              MedlemDtoTjeneste medlemTjeneste,
                              PersonopplysningDtoTjeneste personopplysningTjeneste,
                              PersonopplysningDtoPersonIdentTjeneste personopplysningFnrFinder,
                              Unleash unleash) {
        this.vergeRepository = repositoryProvider.getVergeGrunnlagRepository();
        this.medlemDtoTjeneste = medlemTjeneste;
        this.vergeDtoTjenesteImpl = vergeTjeneste;
        this.personopplysningDtoTjeneste = personopplysningTjeneste;
        this.personopplysningFnrFinder = personopplysningFnrFinder;
        this.unleash = unleash;
    }

    @POST
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/verge")
    @ApiOperation(value = "Hent informasjon verge for søker", notes = ("Returnerer informasjon om verge knyttet til søker for denne behandlingen."))
    @ApiResponses({@ApiResponse(code = 200, message = "Returnerer Verge, null hvis ikke eksisterer (GUI støtter ikke NOT_FOUND p.t.)")})
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public VergeDto getVerge(@NotNull @ApiParam("BehandlingId for aktuell behandling") @Valid BehandlingIdDto behandlingIdDto) {
        Long behandlingId = behandlingIdDto.getBehandlingId();
        Optional<VergeAggregat> vergeAggregat = vergeRepository.hentAggregat(behandlingId);
        Optional<VergeDto> vergeDto = vergeDtoTjenesteImpl.lagVergeDto(vergeAggregat);

        return vergeDto.orElse(null);
    }

    @POST
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/medlemskap")
    @ApiOperation(value = "Hent informasjon om medlemskap i Folketrygden for søker i behandling", notes = ("Returnerer informasjon om medlemskap for søker i behandling."))
    @ApiResponses({@ApiResponse(code = 200, message = "Returnerer Medlemskap, null hvis ikke finnes (GUI støtter ikke NOT_FOUND p.t.)")})
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public MedlemDto getMedlemskap(@NotNull @ApiParam("BehandlingId for aktuell behandling") @Valid BehandlingIdDto behandlingIdDto) {
        Long behandlingId = behandlingIdDto.getBehandlingId();
        Optional<MedlemDto> medlemDto = medlemDtoTjeneste.lagMedlemDto(behandlingId);
        return medlemDto.orElse(null);
    }

    @POST
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/medlemskap-v2")
    @ApiOperation(value = "Hent informasjon om medlemskap i Folketrygden for søker i behandling", notes = ("Returnerer informasjon om medlemskap for søker i behandling."))
    @ApiResponses({@ApiResponse(code = 200, message = "Returnerer Medlemskap, null hvis ikke finnes (GUI støtter ikke NOT_FOUND p.t.)")})
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public MedlemV2Dto hentMedlemskap(@NotNull @ApiParam("BehandlingId for aktuell behandling") @Valid BehandlingIdDto behandlingIdDto) {
        if (unleash.isEnabled("fpsak.lopende-medlemskap")) {
            Long behandlingId = behandlingIdDto.getBehandlingId();
            Optional<MedlemV2Dto> medlemDto = medlemDtoTjeneste.lagMedlemPeriodisertDto(behandlingId);
            return medlemDto.orElse(null);
        }
        return null;
    }

    @POST
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/personopplysninger")
    @ApiOperation(value = "Hent informasjon om personopplysninger søker i behandling", notes = ("Returnerer informasjon om personopplysninger for søker i behandling."))
    @ApiResponses({@ApiResponse(code = 200, message = "Returnerer Personopplysninger, null hvis ikke finnes (GUI støtter ikke NOT_FOUND p.t.)")})
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public PersonopplysningDto getPersonopplysninger(@NotNull @ApiParam("BehandlingId for aktuell behandling") @Valid BehandlingIdDto behandlingIdDto) {
        Long behandlingId = behandlingIdDto.getBehandlingId();
        Optional<PersonopplysningDto> personopplysningDto = personopplysningDtoTjeneste.lagPersonopplysningDto(behandlingId, LocalDate.now());
        if (personopplysningDto.isPresent()) {
            PersonopplysningDto pers = personopplysningDto.get();
            personopplysningFnrFinder.oppdaterMedPersonIdent(pers);
            return pers;
        } else {
            return null;
        }
    }

}
