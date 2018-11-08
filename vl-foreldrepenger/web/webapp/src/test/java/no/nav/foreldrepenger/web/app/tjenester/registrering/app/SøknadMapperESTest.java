package no.nav.foreldrepenger.web.app.tjenester.registrering.app;

import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import static java.util.Collections.singletonList;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperUtil.oppdaterDtoForFødsel;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperUtil.opprettBruker;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.ManuellRegistreringEngangsstonadDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.UtenlandsoppholdDto;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Medlemskap;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.OppholdNorge;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.OppholdUtlandet;

public class SøknadMapperESTest {

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
    public void test_mapEngangstønad() {
        ManuellRegistreringEngangsstonadDto registreringEngangsstonadDto = new ManuellRegistreringEngangsstonadDto();
        oppdaterDtoForFødsel(registreringEngangsstonadDto, FamilieHendelseType.FØDSEL, true, LocalDate.now().minusWeeks(3), 1);
        when(tpsTjeneste.hentAktørForFnr(any())).thenReturn(Optional.ofNullable(STD_KVINNE_AKTØR_ID));
        SøknadMapperES.mapTilEngangsstønad(registreringEngangsstonadDto, opprettBruker(), tpsTjeneste);
    }

    @Test
    public void testMapperMedlemskapES_uten_utenlandsopphold() {

        ManuellRegistreringEngangsstonadDto registreringEngangsstonadDto = new ManuellRegistreringEngangsstonadDto();
        registreringEngangsstonadDto.setMottattDato(LocalDate.now());
        registreringEngangsstonadDto.setHarFremtidigeOppholdUtenlands(false);
        registreringEngangsstonadDto.setHarTidligereOppholdUtenlands(false);
        registreringEngangsstonadDto.setOppholdINorge(true);

        Medlemskap medlemskap = SøknadMapperES.mapMedlemskapES(registreringEngangsstonadDto);
        assertThat(medlemskap.isINorgeVedFoedselstidspunkt()).isTrue();
        assertThat(medlemskap.getOppholdUtlandet()).isEmpty();
        assertThat(medlemskap.getOppholdNorge()).as("Forventer at vi skal ha opphold norge når vi ikke har utenlandsopphold.").hasSize(2);
    }

    @Test
    public void testMapperMedlemskapES_med_FremtidigUtenlandsopphold() throws Exception {

        String land = "FRA";
        LocalDate periodeFom = LocalDate.now().plusMonths(2), periodeTom = LocalDate.now().plusMonths(5);

        ManuellRegistreringEngangsstonadDto registreringEngangsstonadDto = new ManuellRegistreringEngangsstonadDto();
        registreringEngangsstonadDto.setMottattDato(LocalDate.now());
        registreringEngangsstonadDto.setHarFremtidigeOppholdUtenlands(true);
        registreringEngangsstonadDto.setHarTidligereOppholdUtenlands(false);
        registreringEngangsstonadDto.setOppholdINorge(true);
        UtenlandsoppholdDto utenlandsoppholdDto = new UtenlandsoppholdDto();
        utenlandsoppholdDto.setPeriodeFom(periodeFom);
        utenlandsoppholdDto.setPeriodeTom(periodeTom);
        utenlandsoppholdDto.setLand(land);
        registreringEngangsstonadDto.setFremtidigeOppholdUtenlands(singletonList(utenlandsoppholdDto));

        Medlemskap medlemskap = SøknadMapperES.mapMedlemskapES(registreringEngangsstonadDto);
        assertThat(medlemskap.isINorgeVedFoedselstidspunkt()).isTrue();

        //Assert tidligere opphold i norge(siden vi ikke har tidligere utenlandsopphold.)
        List<OppholdNorge> oppholdNorgeListe = medlemskap.getOppholdNorge();
        assertThat(oppholdNorgeListe).isNotNull();
        assertThat(oppholdNorgeListe.size()).isEqualTo(1);

        List<OppholdUtlandet> alleOppholdUtlandet = medlemskap.getOppholdUtlandet();
        assertThat(alleOppholdUtlandet).isNotNull();
        assertThat(alleOppholdUtlandet).hasSize(1);

        OppholdUtlandet oppholdUtlandet = alleOppholdUtlandet.get(0);
        assertThat(oppholdUtlandet.getLand()).isNotNull();
        assertThat(oppholdUtlandet.getLand().getKode()).isEqualTo(land);
        assertThat(oppholdUtlandet.getPeriode()).isNotNull();
        assertThat(oppholdUtlandet.getPeriode().getFom()).isEqualTo(DateUtil.convertToXMLGregorianCalendar(periodeFom));
        assertThat(oppholdUtlandet.getPeriode().getTom()).isEqualTo(DateUtil.convertToXMLGregorianCalendar(periodeTom));
    }

    @Test
    public void testMapperMedlemskapES_med_TidligereUtenlandsopphold() throws Exception {

        final String land = "FRA";
        LocalDate periodeFom = LocalDate.now().minusMonths(6), periodeTom = LocalDate.now().minusMonths(3);

        ManuellRegistreringEngangsstonadDto registreringEngangsstonadDto = new ManuellRegistreringEngangsstonadDto();
        registreringEngangsstonadDto.setMottattDato(LocalDate.now());
        registreringEngangsstonadDto.setHarFremtidigeOppholdUtenlands(false); //Ikke fremtidige utenlandsopphold, så da får vi fremtidg opphold i norge
        registreringEngangsstonadDto.setHarTidligereOppholdUtenlands(true);
        registreringEngangsstonadDto.setOppholdINorge(true);
        UtenlandsoppholdDto utenlandsoppholdDto = new UtenlandsoppholdDto();
        utenlandsoppholdDto.setPeriodeFom(periodeFom);
        utenlandsoppholdDto.setPeriodeTom(periodeTom);
        utenlandsoppholdDto.setLand(land);
        registreringEngangsstonadDto.setTidligereOppholdUtenlands(singletonList(utenlandsoppholdDto));

        Medlemskap medlemskap = SøknadMapperES.mapMedlemskapES(registreringEngangsstonadDto);
        assertThat(medlemskap.isINorgeVedFoedselstidspunkt()).isTrue();

        //Assert fremtidg opphold i norge(siden vi ikke har fremtidig utenlandsopphold.
        List<OppholdNorge> oppholdNorgeListe = medlemskap.getOppholdNorge();
        assertThat(oppholdNorgeListe).isNotNull();
        assertThat(oppholdNorgeListe.size()).isEqualTo(1);


        List<OppholdUtlandet> oppholdUtenlandsListe = medlemskap.getOppholdUtlandet();
        assertThat(oppholdUtenlandsListe).isNotNull();
        assertThat(oppholdUtenlandsListe.size()).isEqualTo(1);
        OppholdUtlandet utenlandsopphold = oppholdUtenlandsListe.get(0);
        assertThat(utenlandsopphold.getLand()).isNotNull();
        assertThat(utenlandsopphold.getLand().getKode()).isEqualTo(land);
        assertThat(utenlandsopphold.getPeriode()).isNotNull();
        assertThat(utenlandsopphold.getPeriode().getFom()).isEqualTo(DateUtil.convertToXMLGregorianCalendar(periodeFom));
        assertThat(utenlandsopphold.getPeriode().getTom()).isEqualTo(DateUtil.convertToXMLGregorianCalendar(periodeTom));
    }
}
