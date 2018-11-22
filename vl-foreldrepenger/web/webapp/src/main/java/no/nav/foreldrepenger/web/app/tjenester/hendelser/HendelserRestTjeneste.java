package no.nav.foreldrepenger.web.app.tjenester.hendelser;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import no.nav.foreldrepenger.domene.mottak.hendelser.HendelseSorteringTjeneste;
import no.nav.foreldrepenger.domene.mottak.hendelser.MottattHendelseTjeneste;
import no.nav.foreldrepenger.domene.mottak.hendelser.kontrakt.FødselHendelse;
import no.nav.foreldrepenger.domene.mottak.hendelser.kontrakt.YtelseHendelse;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.kontrakter.abonnent.AktørIdDto;
import no.nav.foreldrepenger.kontrakter.abonnent.HendelseDto;
import no.nav.foreldrepenger.kontrakter.abonnent.HendelseWrapperDto;
import no.nav.foreldrepenger.kontrakter.abonnent.infotrygd.InfotrygdHendelseDto;
import no.nav.foreldrepenger.kontrakter.abonnent.tps.FødselHendelseDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.log.sporingslogg.Sporingsdata;
import no.nav.vedtak.log.sporingslogg.SporingsloggHelper;
import no.nav.vedtak.log.sporingslogg.SporingsloggId;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt;

@Api(tags = { "hendelser" })
@Path("/hendelser")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
@Transaction
public class HendelserRestTjeneste {

    private static final Logger LOGGER = LoggerFactory.getLogger(HendelserRestTjeneste.class);

    private MottattHendelseTjeneste mottattHendelseTjeneste;
    private HendelseSorteringTjeneste hendelseSorteringTjeneste;

    public HendelserRestTjeneste() {// For Rest-CDI
    }

    @Inject
    public HendelserRestTjeneste(MottattHendelseTjeneste mottattHendelseTjeneste, HendelseSorteringTjeneste hendelseSorteringTjeneste) {
        this.mottattHendelseTjeneste = mottattHendelseTjeneste;
        this.hendelseSorteringTjeneste = hendelseSorteringTjeneste;
    }

    @POST
    @Path("/ping")
    @ApiOperation(value = "Ping")
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.READ, ressurs = BeskyttetRessursResourceAttributt.DRIFT)
    public EnkelRespons ping() {
        return new EnkelRespons("pong");
    }

    @POST
    @Path("/hendelse")
    @ApiOperation(value = "Mottak av hendelser")
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, ressurs = BeskyttetRessursResourceAttributt.DRIFT)
    public EnkelRespons mottaHendelse(@ApiParam("Hendelse fra TPS eller Infotrygd") @Valid HendelseWrapperDto wrapperDto) {
        HendelseDto hendelseDto = wrapperDto.getHendelse();
        loggTypeHendelse(hendelseDto.getAvsenderSystem(), hendelseDto.getHendelsetype(), hendelseDto.getId());
        if (!mottattHendelseTjeneste.erHendelseNy(hendelseDto.getId())) {
            return new EnkelRespons("Hendelse ble ignorert. Hendelse med samme ID er allerede registrert");
        }
        return registrerHendelse(hendelseDto);
    }

    @POST
    @ApiOperation(value = "Grovsortering av aktørID-er", notes = ("Returnerer aktørID-er i listen som har en sak"))
    @Path("/grovsorter")
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.READ, ressurs = BeskyttetRessursResourceAttributt.DRIFT)
    public List<String> grovSorter(@ApiParam("Liste med aktør IDer som skal sorteres") @Valid List<AktørIdDto> aktoerIdListe) {
        List<AktørId> aktørIdList = aktoerIdListe.stream().map(AktørIdDto::getAktørId).map(AktørId::new).collect(Collectors.toList()); // NOSONAR
        return hendelseSorteringTjeneste.hentAktørIderTilknyttetSak(aktørIdList).stream().map(AktørId::getId).collect(Collectors.toList());
    }

    private EnkelRespons registrerHendelse(HendelseDto hendelseDto) {
        if (FødselHendelseDto.AVSENDER.equals(hendelseDto.getAvsenderSystem())) {
            FødselHendelseDto dto = (FødselHendelseDto) hendelseDto;
            return registrerFødselHendelse(dto);
        } else if (InfotrygdHendelseDto.AVSENDER.equals(hendelseDto.getAvsenderSystem())) {
            InfotrygdHendelseDto dto = (InfotrygdHendelseDto) hendelseDto;
            return registrerInfotrygdHendelse(dto);
        }
        return new EnkelRespons("Ukjent hendelse");
    }

    private EnkelRespons registrerFødselHendelse(FødselHendelseDto dto) {
        List<String> aktørIdListe = dto.getAktørIdForeldre().stream().map(AktørId::new).map(AktørId::getId).collect(Collectors.toList());
        FødselHendelse hendelse = new FødselHendelse(aktørIdListe, dto.getFødselsdato());
        mottattHendelseTjeneste.registrerHendelse(dto.getId(), hendelse);
        loggSporingsdata(dto);
        return new EnkelRespons("OK");
    }

    private EnkelRespons registrerInfotrygdHendelse(InfotrygdHendelseDto dto) {
        YtelseHendelse hendelse = new YtelseHendelse(dto.getHendelsetype(), dto.getTypeYtelse(), dto.getAktørId(), dto.getFom(), dto.getIdentdato());
        mottattHendelseTjeneste.registrerHendelse(dto.getId(), hendelse);
        loggSporingsdata(dto);
        return new EnkelRespons("OK");
    }

    private static void loggSporingsdata(FødselHendelseDto dto) {
        String actionType = "create";
        String endepunkt = HendelserRestTjeneste.class.getAnnotation(Path.class).value() + "/fodsel";
        dto.getAktørIdForeldre().stream().forEach(aktørIdForelder -> {
            Sporingsdata sd = Sporingsdata.opprett().leggTilId(SporingsloggId.AKTOR_ID, aktørIdForelder);
            SporingsloggHelper.logSporing(HendelserRestTjeneste.class, sd, actionType, endepunkt);
        });
    }

    private static void loggSporingsdata(InfotrygdHendelseDto dto) {
        String actionType = "create";
        String endepunkt = HendelserRestTjeneste.class.getAnnotation(Path.class).value() + "/hendelse";
        Sporingsdata sd = Sporingsdata.opprett().leggTilId(SporingsloggId.AKTOR_ID, dto.getAktørId());
        SporingsloggHelper.logSporing(HendelserRestTjeneste.class, sd, actionType, endepunkt);
    }

    private static void loggTypeHendelse(String hendelseFra, String hendelseType, String uuid) {
        LOGGER.info("Hendelse mottatt fra {} av typen {} med id/sekvensnummer: {}.", hendelseFra, hendelseType, uuid);// NOSONAR //$NON-NLS-1$
    }
}
