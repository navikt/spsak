package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerRepository;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.person.TpsAdapter;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.BehandlendeEnhetTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VergeDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.app.BehandlingsutredningApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

public class VergeOppdatererTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    private PersonopplysningTjeneste personTjeneste = Mockito.mock(PersonopplysningTjeneste.class);
    private BehandlendeEnhetTjeneste behandlendeEnhetTjeneste = Mockito.mock(BehandlendeEnhetTjeneste.class);
    private BehandlingsutredningApplikasjonTjeneste behandlingsutrednAppTjeneste = Mockito.mock(BehandlingsutredningApplikasjonTjeneste.class);
    private HistorikkTjenesteAdapter historikkTjeneste = Mockito.mock(HistorikkTjenesteAdapter.class);
    private TpsTjeneste tpsTjeneste = Mockito.mock(TpsTjeneste.class);

    private TpsAdapter tpsAdapter;
    private NavBrukerRepository navBrukerRepository;

    private NavBruker vergeBruker;
    private Personinfo pInfo;

    @Before
    public void oppsett() {
        tpsAdapter = mock(TpsAdapter.class);
        navBrukerRepository = mock(NavBrukerRepository.class);

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();

        @SuppressWarnings("unused")
        Behandling behandling = scenario.lagre(repositoryProvider);

        pInfo = new Personinfo.Builder()
            .medNavn("Verger Vergusen")
            .medAktørId(new AktørId("20170907"))
            .medPersonIdent(new PersonIdent("07098218895"))
            .medFødselsdato(LocalDate.now().minusYears(33))
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medForetrukketSpråk(Språkkode.nb)
            .build();

        vergeBruker = NavBruker.opprettNy(pInfo);

        when(tpsAdapter.hentAktørIdForPersonIdent(Mockito.any())).thenReturn(Optional.of(new AktørId("20170907")));
        when(navBrukerRepository.hent(Mockito.any())).thenReturn(Optional.of(vergeBruker));
    }

    @Test
    public void lagre_verge() {
        new VergeBuilder()
            .medVergeType(VergeType.BARN)
            .medBruker(vergeBruker)
            .medMandatTekst("Mandatet")
            .medStønadMottaker(false)
            .medVedtaksdato(LocalDate.now())
            .build();

//        VergeDto dto = VergeDto.fraTotrinnsvurdering(verge);
//
//        vergeOppdaterer.oppdater(dto, behandling, null);

    }

    @Test
    public void skal_generere_historikkinnslag_ved_bekreftet() {
        // Behandling
        Behandling behandling = opprettBehandling();
        VergeDto dto = opprettDtoVerge();
        new VergeOppdaterer(personTjeneste, behandlendeEnhetTjeneste,
            behandlingsutrednAppTjeneste, historikkTjeneste,
            repositoryProvider, tpsTjeneste).oppdater(dto, behandling);

        // Verifiserer HistorikkinnslagDto
        ArgumentCaptor<Historikkinnslag> historikkCapture = ArgumentCaptor.forClass(Historikkinnslag.class);
        verify(historikkTjeneste).lagInnslag(historikkCapture.capture());
        Historikkinnslag historikkinnslag = historikkCapture.getValue();
        assertThat(historikkinnslag.getType()).isEqualTo(HistorikkinnslagType.REGISTRER_OM_VERGE);
        assertThat(historikkinnslag.getAktør()).isEqualTo(HistorikkAktør.SAKSBEHANDLER);
        HistorikkinnslagDel del = historikkinnslag.getHistorikkinnslagDeler().get(0);
        assertThat(del.getSkjermlenke()).as("skjermlenke").hasValueSatisfying(skjermlenke -> assertThat(skjermlenke).isEqualTo(SkjermlenkeType.FAKTA_OM_VERGE.getKode()));
        assertThat(del.getHendelse()).as("hendelse").hasValueSatisfying(hendelse -> assertThat(hendelse.getNavn()).as("navn").isEqualTo(HistorikkinnslagType.REGISTRER_OM_VERGE.getKode()));
    }

    private VergeDto opprettDtoVerge() {
        VergeDto dto = new VergeDto();
        dto.setNavn("Navn");
        dto.setFnr("12121221222");
        dto.setMandatTekst("mandat");
        dto.setGyldigFom(LocalDate.now().minusDays(10));
        dto.setGyldigTom(LocalDate.now().plusDays(10));
        dto.setVergeType(VergeType.BARN);
        dto.setSokerErKontaktPerson(true);
        return dto;
    }

    private Behandling opprettBehandling() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        scenario.medSøknad();
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_VERGE,
            BehandlingStegType.KONTROLLER_FAKTA);
        scenario.lagre(repositoryProvider);

        return scenario.getBehandling();
    }

}

