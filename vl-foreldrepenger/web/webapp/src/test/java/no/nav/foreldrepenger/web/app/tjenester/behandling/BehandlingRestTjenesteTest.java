package no.nav.foreldrepenger.web.app.tjenester.behandling;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import no.finn.unleash.FakeUnleash;
import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.FagsakTjenesteImpl;
import no.nav.foreldrepenger.behandling.impl.RegisterInnhentingIntervallEndringTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittDekningsgrad;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittDekningsgradEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.app.BehandlingsprosessApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.app.BehandlingsutredningApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.BehandlingDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.BehandlingDtoTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.fagsak.dto.SaksnummerDto;

public class BehandlingRestTjenesteTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRestTjeneste behandlingRestTjeneste;
    private BehandlingRepositoryProvider repositoryProvider;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;

    private BehandlingsutredningApplikasjonTjeneste behandlingutredningTjeneste = mock(BehandlingsutredningApplikasjonTjeneste.class);
    private BehandlingsprosessApplikasjonTjeneste behandlingsprosessTjenste = mock(BehandlingsprosessApplikasjonTjeneste.class);
    private FakeUnleash unleash = new FakeUnleash();

    @Before
    public void setUp() {
        unleash.disableAll();
        repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
        FagsakTjenesteImpl fagsakTjeneste = new FagsakTjenesteImpl(repositoryProvider, null);
        skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider,
            new RegisterInnhentingIntervallEndringTjeneste(Period.of(1, 0, 0), Period.of(0, 4, 0)),
            Period.of(0, 3, 0),
            Period.of(0, 10, 0));
        BehandlingDtoTjenesteImpl behandlingDtoTjeneste = new BehandlingDtoTjenesteImpl(repositoryProvider, skjæringstidspunktTjeneste, unleash);

        behandlingRestTjeneste = new BehandlingRestTjeneste(repositoryProvider,
            behandlingutredningTjeneste,
            behandlingsprosessTjenste,
            fagsakTjeneste,
            Mockito.mock(HenleggBehandlingTjeneste.class),
            behandlingDtoTjeneste);
    }

    @Test
    public void skal_hente_behandlinger_for_saksnummer() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();

        Personinfo person = new Personinfo.Builder()
            .medNavn("Helga")
            .medAktørId(new AktørId("123"))
            .medPersonIdent(new PersonIdent("12312411252"))
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medSivilstandType(SivilstandType.SAMBOER)
            .medFødselsdato(LocalDate.now())
            .medRegion(Region.NORDEN)
            .build();

        PersonInformasjon personInformasjon = scenario.opprettBuilderForRegisteropplysninger()
            .leggTilPersonopplysninger(Personopplysning.builder()
                .aktørId(person.getAktørId())
                .navn(person.getNavn())
                .fødselsdato(person.getFødselsdato())
                .sivilstand(SivilstandType.SAMBOER)
                .region(Region.NORDEN)
                .brukerKjønn(person.getKjønn())).build();


        scenario.medRegisterOpplysninger(personInformasjon);
        Behandling behandling = scenario.lagre(repositoryProvider);
        Saksnummer saksnummer = behandling.getFagsak().getSaksnummer();

        OppgittPeriode periode1 = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medPeriode(LocalDate.now(), LocalDate.now().plusWeeks(6))
            .build();

        OppgittPeriode periode2 = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medPeriode(LocalDate.now().plusWeeks(6).plusDays(1), LocalDate.now().plusWeeks(10))
            .build();

        OppgittDekningsgrad dekningsgrad = OppgittDekningsgradEntitet.bruk100();
        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, dekningsgrad);

        OppgittFordelingEntitet fordeling = new OppgittFordelingEntitet(Arrays.asList(periode1, periode2), true);
        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, fordeling);

        when(behandlingutredningTjeneste.hentBehandlingerForSaksnummer(saksnummer)).thenReturn(singletonList(behandling));

        List<BehandlingDto> dto = behandlingRestTjeneste.hentBehandlinger(new SaksnummerDto(saksnummer.getVerdi()));

        assertThat(dto).hasSize(1);
    }
}
