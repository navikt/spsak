package no.nav.foreldrepenger.web.app.tjenester.behandling;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import no.finn.unleash.FakeUnleash;
import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.FagsakTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandling.rest.SaksnummerDto;
import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.Personopplysning;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.app.BehandlingsprosessApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.app.BehandlingsutredningApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.BehandlingDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.BehandlingDtoTjenesteImpl;

public class BehandlingRestTjenesteTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRestTjeneste behandlingRestTjeneste;
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;

    private BehandlingsutredningApplikasjonTjeneste behandlingutredningTjeneste = mock(BehandlingsutredningApplikasjonTjeneste.class);
    private BehandlingsprosessApplikasjonTjeneste behandlingsprosessTjenste = mock(BehandlingsprosessApplikasjonTjeneste.class);
    private FakeUnleash unleash = new FakeUnleash();

    @Before
    public void setUp() {
        unleash.disableAll();

        FagsakTjeneste fagsakTjeneste = new FagsakTjeneste(repositoryProvider, null);
        skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider, resultatRepositoryProvider);
        BehandlingDtoTjenesteImpl behandlingDtoTjeneste = new BehandlingDtoTjenesteImpl(resultatRepositoryProvider, skjæringstidspunktTjeneste, unleash);

        behandlingRestTjeneste = new BehandlingRestTjeneste(repositoryProvider,
            behandlingutredningTjeneste,
            behandlingsprosessTjenste,
            fagsakTjeneste,
            Mockito.mock(HenleggBehandlingTjeneste.class),
            behandlingDtoTjeneste);
    }

    @Test
    public void skal_hente_behandlinger_for_saksnummer() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();

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
        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        Saksnummer saksnummer = behandling.getFagsak().getSaksnummer();

        when(behandlingutredningTjeneste.hentBehandlingerForSaksnummer(saksnummer)).thenReturn(singletonList(behandling));

        List<BehandlingDto> dto = behandlingRestTjeneste.hentBehandlinger(new SaksnummerDto(saksnummer.getVerdi()));

        assertThat(dto).hasSize(1);
    }
}
