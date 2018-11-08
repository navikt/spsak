package no.nav.foreldrepenger.web.app.tjenester.registrering.app;

import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperUtil.oppdaterDtoForFødsel;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperUtil.opprettAdosjonDto;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperUtil.opprettAnnenForelderDto;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperUtil.opprettBruker;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperUtil.opprettOmsorgDto;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperUtil.opprettTidsromPermisjonDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.FarSøkerType;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.ForeldreType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.ManuellRegistreringEngangsstonadDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.ManuellRegistreringForeldrepengerDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.RettigheterDto;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Adopsjon;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.AnnenForelder;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Bruker;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Foedsel;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Omsorgsovertakelse;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Rettigheter;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.SoekersRelasjonTilBarnet;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Termin;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.UkjentForelder;

public class SøknadMapperFellesTest {

    public static final AktørId STD_KVINNE_AKTØR_ID = new AktørId("9000000000036");
    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private TpsTjeneste tpsTjeneste;
    private VirksomhetTjeneste virksomhetTjeneste;

    @Before
    public void setUp() {
        tpsTjeneste = mock(TpsTjeneste.class);
        virksomhetTjeneste = mock(VirksomhetTjeneste.class);
        VirksomhetEntitet virksomhetEntitet = new VirksomhetEntitet.Builder().medOrgnr("123").medRegistrert(LocalDate.now()).build();
        when(virksomhetTjeneste.hentOgLagreOrganisasjon(anyString())).thenReturn(virksomhetEntitet);
    }

    @Test
    public void mapperBrukerBasertPåAktørIdOgBrukerrolle() {
        NavBruker navBruker = opprettBruker();

        ForeldreType søker = ForeldreType.MOR;

        Bruker bruker = SøknadMapperFelles.mapBruker(søker, navBruker);
        assertThat(bruker).isInstanceOf(Bruker.class);
        assertThat(bruker.getSoeknadsrolle().getKode()).isEqualTo(søker.getKode());
        assertThat(bruker.getAktoerId()).isEqualTo(navBruker.getAktørId().getId());
    }

    @Test
    public void test_mapRelasjonTilBarnet_adopsjon() {
        ManuellRegistreringEngangsstonadDto manuellRegistreringEngangsstonadDto = opprettAdosjonDto(FamilieHendelseType.ADOPSJON, LocalDate.now(), LocalDate.now().minusMonths(3), 1, LocalDate.now());
        manuellRegistreringEngangsstonadDto.setTema(FamilieHendelseType.ADOPSJON);
        SoekersRelasjonTilBarnet søkersRelasjonTilBarnet = SøknadMapperFelles.mapRelasjonTilBarnet(manuellRegistreringEngangsstonadDto);
        assertThat(søkersRelasjonTilBarnet).isInstanceOf(Adopsjon.class);
    }

    @Test
    public void test_mapRelasjonTilBarnet_fødsel_med_rettighet_knyttet_til_omsorgsovertakelse_satt() {
        ManuellRegistreringEngangsstonadDto manuellRegistreringEngangsstonadDto = opprettAdosjonDto(FamilieHendelseType.FØDSEL, LocalDate.now(), LocalDate.now().minusMonths(3), 1, LocalDate.now());
        manuellRegistreringEngangsstonadDto.setTema(FamilieHendelseType.FØDSEL);
        manuellRegistreringEngangsstonadDto.setSoker(ForeldreType.FAR);
        manuellRegistreringEngangsstonadDto.setRettigheter(RettigheterDto.OVERTA_FORELDREANSVARET_ALENE);
        SoekersRelasjonTilBarnet søkersRelasjonTilBarnet = SøknadMapperFelles.mapRelasjonTilBarnet(manuellRegistreringEngangsstonadDto);
        assertThat(søkersRelasjonTilBarnet).isInstanceOf(Omsorgsovertakelse.class);
    }

    @Test
    public void test_mapAdopsjon() throws Exception {
        final LocalDate omsorgsovertakelsesdato = LocalDate.now();
        final LocalDate fødselssdato = LocalDate.now().minusMonths(3);
        final LocalDate ankomstDato = LocalDate.now().minusDays(4);
        LocalDate.now().minusMonths(2);
        final int antallBarn = 1;

        ManuellRegistreringEngangsstonadDto manuellRegistreringEngangsstonadDto = opprettAdosjonDto(FamilieHendelseType.ADOPSJON, omsorgsovertakelsesdato, fødselssdato, antallBarn, ankomstDato);
        Adopsjon adopsjon = SøknadMapperFelles.mapAdopsjon(manuellRegistreringEngangsstonadDto);
        assertThat(adopsjon).isNotNull();
        assertThat(adopsjon.getOmsorgsovertakelsesdato()).isEqualTo(DateUtil.convertToXMLGregorianCalendar(omsorgsovertakelsesdato));
        assertThat(adopsjon.getAntallBarn()).isEqualTo(antallBarn);
        assertThat(adopsjon.getFoedselsdato()).first().isEqualTo(DateUtil.convertToXMLGregorianCalendar(fødselssdato));
        assertThat(adopsjon.getAnkomstdato()).isEqualTo(DateUtil.convertToXMLGregorianCalendar(ankomstDato));
    }

    @Test
    public void test_mapRelasjonTilBarnet_omsorg() {
        ManuellRegistreringEngangsstonadDto manuellRegistreringEngangsstonadDto = opprettOmsorgDto(FamilieHendelseType.OMSORG, LocalDate.now(), RettigheterDto.OVERTA_FORELDREANSVARET_ALENE, 1, LocalDate.now());
        SoekersRelasjonTilBarnet søkersRelasjonTilBarnet = SøknadMapperFelles.mapRelasjonTilBarnet(manuellRegistreringEngangsstonadDto);
        assertThat(søkersRelasjonTilBarnet).isInstanceOf(Omsorgsovertakelse.class);
    }

    @Test
    public void test_mapOmsorg() throws Exception {
        final LocalDate omsorgsovertakelsesdato = LocalDate.now();
        final LocalDate fødselsdato = LocalDate.now().minusDays(10);
        final int antallBarn = 1;

        ManuellRegistreringEngangsstonadDto manuellRegistreringEngangsstonadDto = opprettOmsorgDto(FamilieHendelseType.OMSORG, omsorgsovertakelsesdato, RettigheterDto.OVERTA_FORELDREANSVARET_ALENE, antallBarn, fødselsdato);
        Omsorgsovertakelse omsorgsovertakelse = SøknadMapperFelles.mapOmsorgsovertakelse(manuellRegistreringEngangsstonadDto);
        assertThat(omsorgsovertakelse).isNotNull();
        assertThat(omsorgsovertakelse.getOmsorgsovertakelsesdato()).isEqualTo(DateUtil.convertToXMLGregorianCalendar(omsorgsovertakelsesdato));
        assertThat(omsorgsovertakelse.getAntallBarn()).isEqualTo(antallBarn);
        assertThat(omsorgsovertakelse.getOmsorgsovertakelseaarsak().getKode()).isEqualTo(FarSøkerType.OVERTATT_OMSORG.getKode());
        assertThat(omsorgsovertakelse.getFoedselsdato()).hasSize(1);
        assertThat(omsorgsovertakelse.getFoedselsdato()).first().isEqualTo(DateUtil.convertToXMLGregorianCalendar(fødselsdato));
    }

    @Test
    public void test_mapRelasjonTilBarnet_fødsel() {
        ManuellRegistreringEngangsstonadDto manuellRegistreringEngangsstonadDto = new ManuellRegistreringEngangsstonadDto();
        oppdaterDtoForFødsel(manuellRegistreringEngangsstonadDto, FamilieHendelseType.FØDSEL, true, LocalDate.now(), 1);
        SoekersRelasjonTilBarnet søkersRelasjonTilBarnet = SøknadMapperFelles.mapRelasjonTilBarnet(manuellRegistreringEngangsstonadDto);
        assertThat(søkersRelasjonTilBarnet).isInstanceOf(Foedsel.class);
    }

    @Test
    public void test_mapFødsel() throws Exception {
        final LocalDate fødselssdato = LocalDate.now().minusMonths(3);
        final int antallBarn = 1;

        ManuellRegistreringEngangsstonadDto manuellRegistreringEngangsstonadDto = new ManuellRegistreringEngangsstonadDto();
        oppdaterDtoForFødsel(manuellRegistreringEngangsstonadDto, FamilieHendelseType.FØDSEL, true, fødselssdato, antallBarn);
        Foedsel foedsel = SøknadMapperFelles.mapFødsel(manuellRegistreringEngangsstonadDto);
        assertThat(foedsel).isNotNull();
        assertThat(foedsel.getFoedselsdato()).isEqualTo(DateUtil.convertToXMLGregorianCalendar(fødselssdato));
        assertThat(foedsel.getAntallBarn()).isEqualTo(antallBarn);
    }

    @Test
    public void test_mapRelasjonTilBarnet_termin() {
        ManuellRegistreringEngangsstonadDto manuellRegistreringEngangsstonadDto = new ManuellRegistreringEngangsstonadDto();
        oppdaterDtoForFødsel(manuellRegistreringEngangsstonadDto, FamilieHendelseType.FØDSEL, false, LocalDate.now(), 1);
        SoekersRelasjonTilBarnet søkersRelasjonTilBarnet = SøknadMapperFelles.mapRelasjonTilBarnet(manuellRegistreringEngangsstonadDto);
        assertThat(søkersRelasjonTilBarnet).isInstanceOf(Termin.class);
    }

    @Test
    public void test_mapTermin() throws Exception {
        final LocalDate terminbekreftelseDato = LocalDate.now();
        final LocalDate termindato = LocalDate.now().plusMonths(3);
        final int antallBarn = 1;

        ManuellRegistreringEngangsstonadDto manuellRegistreringEngangsstonadDto = new ManuellRegistreringEngangsstonadDto();
        manuellRegistreringEngangsstonadDto.setTerminbekreftelseDato(terminbekreftelseDato);
        manuellRegistreringEngangsstonadDto.setTermindato(termindato);
        manuellRegistreringEngangsstonadDto.setAntallBarnFraTerminbekreftelse(antallBarn);
        Termin termin = SøknadMapperFelles.mapTermin(manuellRegistreringEngangsstonadDto);
        assertThat(termin).isNotNull();
        assertThat(termin.getTermindato()).isEqualTo(DateUtil.convertToXMLGregorianCalendar(termindato));
        assertThat(termin.getUtstedtdato()).isEqualTo(DateUtil.convertToXMLGregorianCalendar(terminbekreftelseDato));
        assertThat(termin.getAntallBarn()).isEqualTo(antallBarn);
    }

    @Test
    public void test_mapAnnenForelder() {
        final LocalDate omsorgsovertakelsesdato = LocalDate.now();
        final int antallBarn = 1;

        ManuellRegistreringEngangsstonadDto manuellRegistreringEngangsstonadDto = opprettOmsorgDto(FamilieHendelseType.OMSORG, omsorgsovertakelsesdato, RettigheterDto.OVERTA_FORELDREANSVARET_ALENE, antallBarn, LocalDate.now());

        manuellRegistreringEngangsstonadDto.setAnnenForelder(opprettAnnenForelderDto(true));
        AnnenForelder annenForelder = SøknadMapperFelles.mapAnnenForelder(manuellRegistreringEngangsstonadDto, tpsTjeneste);
        assertThat(annenForelder).isInstanceOf(UkjentForelder.class);
    }

    @Test
    public void test_mapRettigheter_farRettPåFedrekvote() {
        ManuellRegistreringForeldrepengerDto manuellRegistreringForeldrepengerDto = new ManuellRegistreringForeldrepengerDto();
        oppdaterDtoForFødsel(manuellRegistreringForeldrepengerDto, FamilieHendelseType.FØDSEL, true, LocalDate.now(), 1);
        manuellRegistreringForeldrepengerDto.setTidsromPermisjon(opprettTidsromPermisjonDto(true, true, null));
        Rettigheter rettigheter = SøknadMapperFelles.mapRettigheter(manuellRegistreringForeldrepengerDto);
        assertThat(rettigheter).isNotNull();
        assertThat(rettigheter.isHarAnnenForelderRett()).isTrue();
        assertThat(rettigheter.isHarAleneomsorgForBarnet()).isTrue();
        assertThat(rettigheter.isHarOmsorgForBarnetIPeriodene()).isTrue();
    }

    @Test
    public void test_mapRettigheter_morAleneomsorg() {
        ManuellRegistreringForeldrepengerDto manuellRegistreringForeldrepengerDto = new ManuellRegistreringForeldrepengerDto();
        oppdaterDtoForFødsel(manuellRegistreringForeldrepengerDto, FamilieHendelseType.FØDSEL, true, LocalDate.now(), 1);
        manuellRegistreringForeldrepengerDto.setTidsromPermisjon(opprettTidsromPermisjonDto(true, false, null));
        Rettigheter rettigheter = SøknadMapperFelles.mapRettigheter(manuellRegistreringForeldrepengerDto);
        assertThat(rettigheter).isNotNull();
        assertThat(rettigheter.isHarAnnenForelderRett()).isFalse();
        assertThat(rettigheter.isHarOmsorgForBarnetIPeriodene()).isTrue();
    }
}
