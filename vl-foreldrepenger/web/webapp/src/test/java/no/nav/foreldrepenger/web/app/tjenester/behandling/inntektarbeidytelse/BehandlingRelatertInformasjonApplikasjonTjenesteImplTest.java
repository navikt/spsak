package no.nav.foreldrepenger.web.app.tjenester.behandling.inntektarbeidytelse;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

import javax.enterprise.inject.Instance;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.IverksettingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.testutilities.aktør.NavBrukerBuilder;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.testutilities.Whitebox;
import no.nav.vedtak.felles.testutilities.cdi.UnitTestInstanceImpl;

@SuppressWarnings("deprecation")
public class BehandlingRelatertInformasjonApplikasjonTjenesteImplTest {
    private static final Instance<Period> RELATERTE_YTELSER_VL_PERIODE_START = new UnitTestInstanceImpl<>(Period.parse("P36M"));
    private BehandlingRepositoryProvider repositoryProvider;
    private FagsakRepository fagsakRepositoryMock;
    private BehandlingRelatertInformasjonApplikasjonTjeneste behandlingRelatertInformasjonApplikasjonTjeneste;
    private BehandlingVedtakRepository behandlingVedtakRepositoryMock;
    private BehandlingRepository behandlingRepository;
    private static final AktørId AKTØR_ID_99 = new AktørId("99");

    @Before
    public void setUp() throws Exception {
        repositoryProvider = mock(BehandlingRepositoryProvider.class);
        behandlingRepository = mock(BehandlingRepository.class);
        when(repositoryProvider.getBehandlingRepository()).thenReturn(behandlingRepository);
        fagsakRepositoryMock = mock(FagsakRepository.class);
        behandlingVedtakRepositoryMock = mock(BehandlingVedtakRepository.class);
        when(repositoryProvider.getFagsakRepository()).thenReturn(fagsakRepositoryMock);
        when(repositoryProvider.getBehandlingVedtakRepository()).thenReturn(behandlingVedtakRepositoryMock);
        behandlingRelatertInformasjonApplikasjonTjeneste = new BehandlingRelatertInformasjonApplikasjonTjenesteImpl(repositoryProvider, RELATERTE_YTELSER_VL_PERIODE_START);
    }

    @Test
    public void skal_returnere_tom_liste_når_ingen_tidligere_sak_for_søker() throws Exception {
        NavBruker navBruker = new NavBrukerBuilder().medAktørId(AKTØR_ID_99).build();
        Fagsak fagsakFødsel = lagFagsak(42L, navBruker);
        when(fagsakRepositoryMock.hentForBruker(Mockito.any(AktørId.class))).thenReturn(asList(fagsakFødsel));

        final List<TilgrensendeYtelserDto> resultatListe = behandlingRelatertInformasjonApplikasjonTjeneste.hentRelaterteYtelser(lagBehandling(fagsakFødsel), AKTØR_ID_99, false);

        assertThat(resultatListe).isEmpty();
    }

    @Test
    public void skal_returnere_relatert_ytelser_når_behandling_inn_siste_3_yr() throws Exception {
        NavBruker navBruker = new NavBrukerBuilder().medAktørId(AKTØR_ID_99).build();
        Fagsak fagsakFødsel = lagFagsak(42L, navBruker);
        Fagsak fagsak66 = lagFagsak(66L);
        when(fagsakRepositoryMock.hentForBruker(Mockito.any(AktørId.class))).thenReturn(asList(fagsakFødsel, fagsak66));
        final LocalDate vedtaksdato = LocalDate.now().minusYears(2);
        Behandling behandling = lagBehandling(fagsakFødsel);
        Behandlingsresultat.Builder behandlingsresultatBuilder = Behandlingsresultat.builderForInngangsvilkår();
        BehandlingVedtak.Builder builder = BehandlingVedtak.builder();
        Behandlingsresultat behandlingsresultat = behandlingsresultatBuilder.buildFor(behandling);

        BehandlingVedtak vedtak = builder
            .medBehandlingsresultat(behandlingsresultat)
            .medVedtaksdato(vedtaksdato)
            .medAnsvarligSaksbehandler("Tester")
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medIverksettingStatus(IverksettingStatus.IKKE_IVERKSATT)
            .build();

        when(behandlingRepository.hentAbsoluttAlleBehandlingerForSaksnummer(fagsak66.getSaksnummer())).thenReturn(asList(behandling));
        Whitebox.setInternalState(behandlingsresultat, "behandlingVedtak", vedtak);

        final List<TilgrensendeYtelserDto> resultatListe = behandlingRelatertInformasjonApplikasjonTjeneste.hentRelaterteYtelser(lagBehandling(fagsakFødsel), AKTØR_ID_99, false);

        assertThat(resultatListe).hasSize(1);
        assertThat(resultatListe.get(0).getPeriodeFraDato()).isEqualTo(vedtaksdato);
        assertThat(resultatListe.get(0).getStatus()).isEqualTo(fagsak66.getStatus().getKode());
    }

    @Test
    public void skal_returnere_tom_liste_når_ingen_behandling_inn_siste_3_yr() throws Exception {
        NavBruker navBruker = new NavBrukerBuilder().medAktørId(AKTØR_ID_99).build();
        Fagsak fagsakFødsel = lagFagsak(42L, navBruker);

        when(fagsakRepositoryMock.hentForBruker(Mockito.any(AktørId.class))).thenReturn(asList(fagsakFødsel, lagFagsak(66L)));
        when(behandlingRepository.hentSisteBehandlingForFagsakId(anyLong())).thenReturn(Optional.of(lagBehandling(fagsakFødsel)));
        when(behandlingVedtakRepositoryMock.hentBehandlingvedtakForBehandlingId(anyLong())).thenReturn(Optional.of(lagBehandlingVedtak(LocalDate.now().minusYears(4))));

        final List<TilgrensendeYtelserDto> resultatListe = behandlingRelatertInformasjonApplikasjonTjeneste.hentRelaterteYtelser(lagBehandling(fagsakFødsel), AKTØR_ID_99, false);

        assertThat(resultatListe).isEmpty();
    }

    @Test
    public void skal_returnere_tom_liste_når_ingen_tidligere_sak_for_medsøker() throws Exception {

        // bygg behandlinged annen part
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medBruker(AKTØR_ID_99, NavBrukerKjønn.KVINNE);
        PersonopplysningRepository personopplysningRepository = mock(PersonopplysningRepository.class);
        when(repositoryProvider.getPersonopplysningRepository()).thenReturn(personopplysningRepository);

        // legg til annen part
        AktørId annenPartAktørId = new AktørId("123");

        PersonInformasjon personInformasjon = scenario.opprettBuilderForRegisteropplysninger()
            .leggTilPersonopplysninger(
                no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.Personopplysning.builder()
                    .aktørId(AKTØR_ID_99)
                    .navn("Søker")
            )
            .leggTilPersonopplysninger(
                no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.Personopplysning.builder()
                    .aktørId(annenPartAktørId)
                    .navn("Annen Forelder")
            )
            .build();

        scenario.medRegisterOpplysninger(personInformasjon);
        Behandling behandling = scenario.lagMocked();

        // tjenester
        BehandlingRelatertInformasjonApplikasjonTjeneste tjeneste = new BehandlingRelatertInformasjonApplikasjonTjenesteImpl(repositoryProvider, RELATERTE_YTELSER_VL_PERIODE_START);

        // Act
        final List<TilgrensendeYtelserDto> resultatListe = tjeneste.hentRelaterteYtelser(behandling, AKTØR_ID_99, false);

        // Assert
        assertThat(resultatListe).isEmpty();
    }

    private BehandlingVedtak lagBehandlingVedtak(LocalDate vedtaksdato) {
        return BehandlingVedtak.builder()
            .medVedtaksdato(vedtaksdato)
            .medAnsvarligSaksbehandler("Tester")
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medIverksettingStatus(IverksettingStatus.IKKE_IVERKSATT)
            .build();
    }

    private Behandling lagBehandling(Fagsak fagsak) {
        Behandling.Builder behandlingBuilder = Behandling.forFørstegangssøknad(fagsak);
        Behandling behandling = behandlingBuilder.build();
        Whitebox.setInternalState(behandling, "id", 99L);
        return behandlingBuilder.build();
    }

    private Fagsak lagFagsak(Long fagsakId) {
        Fagsak fagsak = FagsakBuilder.nyEngangstønadForMor().medSaksnummer(new Saksnummer(Long.toString(fagsakId*100))).build();
        fagsak.setId(fagsakId);
        return fagsak;
    }

    private Fagsak lagFagsak(Long fagsakId, NavBruker navBruker) {
        Fagsak fagsak = Fagsak.opprettNy(FagsakYtelseType.FORELDREPENGER, navBruker);
        fagsak.setId(fagsakId);
        return fagsak;
    }
}
