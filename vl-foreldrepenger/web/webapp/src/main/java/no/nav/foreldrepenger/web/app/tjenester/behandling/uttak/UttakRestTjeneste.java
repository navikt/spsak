package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

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
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.BehandlingIdDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app.KontrollerFaktaPeriodeTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app.SaldoerDtoTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app.StønadskontoTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app.UttakPeriodegrenseDtoTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app.UttakPerioderDtoTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.KontrollerFaktaDataDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.StønadskontoerDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.UttakPeriodegrenseDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.UttakResultatPerioderDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.stønadskonto.dto.SaldoerDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = {"uttak"})
@Path("/behandling/uttak")
@RequestScoped
@Transaction
public class UttakRestTjeneste {

    private BehandlingRepository behandlingRepository;
    private StønadskontoTjeneste stønadskontoTjeneste;
    private SaldoerDtoTjeneste saldoerDtoTjeneste;
    private KontrollerFaktaPeriodeTjeneste kontrollerFaktaPeriodeTjeneste;
    private UttakPerioderDtoTjeneste uttakResultatPerioderDtoTjeneste;
    private UttakPeriodegrenseDtoTjeneste uttakPeriodegrenseDtoTjeneste;

    public UttakRestTjeneste() {
        // for CDI proxy
    }

    @Inject
    public UttakRestTjeneste(BehandlingRepositoryProvider behandlingRepositoryProvider,
                             StønadskontoTjeneste stønadskontoTjeneste,
                             SaldoerDtoTjeneste saldoerDtoTjeneste,
                             KontrollerFaktaPeriodeTjeneste kontrollerFaktaPeriodeTjeneste,
                             UttakPerioderDtoTjeneste uttakResultatPerioderDtoTjeneste,
                             UttakPeriodegrenseDtoTjeneste uttakPeriodegrenseDtoTjeneste
                             ) {
        this.uttakPeriodegrenseDtoTjeneste = uttakPeriodegrenseDtoTjeneste;
        this.behandlingRepository = behandlingRepositoryProvider.getBehandlingRepository();
        this.stønadskontoTjeneste = stønadskontoTjeneste;
        this.kontrollerFaktaPeriodeTjeneste = kontrollerFaktaPeriodeTjeneste;
        this.uttakResultatPerioderDtoTjeneste = uttakResultatPerioderDtoTjeneste;
        this.saldoerDtoTjeneste = saldoerDtoTjeneste;
    }


    @POST
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/stonadskontoer/")
    @ApiOperation(value = "Hent informasjon om stønadskontoer for behandling", notes = ("Returnerer stønadskontoer for behandling."))
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public SaldoerDto getStonadskontoer(@NotNull @ApiParam("BehandlingId for aktuell behandling") @Valid BehandlingIdDto behandlingId) {
        return saldoerDtoTjeneste.lagStønadskontoerDto(behandlingRepository.hentBehandling(behandlingId.getBehandlingId()));
    }

    @POST
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/kontroller-fakta-perioder")
    @ApiOperation(value = "Hent perioder for å kontrollere fakta ifbm uttak. ")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public KontrollerFaktaDataDto hentKontrollerFaktaPerioder(@NotNull @ApiParam("BehandlingId for aktuell behandling") @Valid BehandlingIdDto behandlingId) {
        return kontrollerFaktaPeriodeTjeneste.hentKontrollerFaktaPerioder(behandlingId.getBehandlingId());
    }

    @POST
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/resultat-perioder")
    @ApiOperation(value = "Henter uttaks resultat perioder", notes = ("Returnerer uttaks resultat perioder"))
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public UttakResultatPerioderDto hentUttakResultatPerioder(@NotNull @ApiParam("BehandlingId for aktuell behandling") @Valid BehandlingIdDto behandlingId) {
        return uttakResultatPerioderDtoTjeneste.mapFra(behandlingRepository.hentBehandling(behandlingId.getBehandlingId())).orElse(null);
    }

    @POST
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/periode-grense")
    @ApiOperation(value = "Henter uttakperiodegrense", notes = ("Returnerer uttakperiodegrense"))
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public UttakPeriodegrenseDto hentUttakPeriodegrense(@NotNull @ApiParam("BehandlingId for aktuell behandling") @Valid BehandlingIdDto behandlingIdDto) {
        Long behandlingId = behandlingIdDto.getBehandlingId();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        return uttakPeriodegrenseDtoTjeneste.mapFra(behandling).orElse(null);
    }
}
