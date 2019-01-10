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
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.IverksettingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
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
    private GrunnlagRepositoryProvider repositoryProvider;
    private ResultatRepositoryProvider resultatRepositoryProvider;
    private FagsakRepository fagsakRepositoryMock;
    private BehandlingRelatertInformasjonApplikasjonTjeneste behandlingRelatertInformasjonApplikasjonTjeneste;
    private BehandlingVedtakRepository behandlingVedtakRepositoryMock;
    private BehandlingRepository behandlingRepository;
    private static final AktørId AKTØR_ID_99 = new AktørId("99");

    @Before
    public void setUp() throws Exception {
        repositoryProvider = mock(GrunnlagRepositoryProvider.class);
        resultatRepositoryProvider = mock(ResultatRepositoryProvider.class);
        behandlingRepository = mock(BehandlingRepository.class);
        when(repositoryProvider.getBehandlingRepository()).thenReturn(behandlingRepository);
        when(resultatRepositoryProvider.getBehandlingRepository()).thenReturn(behandlingRepository);
        fagsakRepositoryMock = mock(FagsakRepository.class);
        behandlingVedtakRepositoryMock = mock(BehandlingVedtakRepository.class);
        when(repositoryProvider.getFagsakRepository()).thenReturn(fagsakRepositoryMock);
        when(resultatRepositoryProvider.getVedtakRepository()).thenReturn(behandlingVedtakRepositoryMock);
        behandlingRelatertInformasjonApplikasjonTjeneste = new BehandlingRelatertInformasjonApplikasjonTjenesteImpl(repositoryProvider, RELATERTE_YTELSER_VL_PERIODE_START);
    }

    @Test
    public void skal_returnere_tom_liste_når_ingen_tidligere_sak_for_søker() throws Exception {
        NavBruker navBruker = NavBruker.opprettNy(AKTØR_ID_99);
        Fagsak fagsakFødsel = lagFagsak(42L, navBruker);
        when(fagsakRepositoryMock.hentForBruker(Mockito.any(AktørId.class))).thenReturn(asList(fagsakFødsel));

        final List<TilgrensendeYtelserDto> resultatListe = behandlingRelatertInformasjonApplikasjonTjeneste.hentRelaterteYtelser(lagBehandling(fagsakFødsel), AKTØR_ID_99, false);

        assertThat(resultatListe).isEmpty();
    }

    @Test
    public void skal_returnere_tom_liste_når_ingen_behandling_inn_siste_3_yr() throws Exception {
        NavBruker navBruker = NavBruker.opprettNy(AKTØR_ID_99);
        Fagsak fagsakFødsel = lagFagsak(42L, navBruker);

        when(fagsakRepositoryMock.hentForBruker(Mockito.any(AktørId.class))).thenReturn(asList(fagsakFødsel, lagFagsak(66L)));
        when(behandlingRepository.hentSisteBehandlingForFagsakId(anyLong())).thenReturn(Optional.of(lagBehandling(fagsakFødsel)));
        when(behandlingVedtakRepositoryMock.hentVedtakFor(anyLong())).thenReturn(Optional.of(lagBehandlingVedtak(LocalDate.now().minusYears(4))));

        final List<TilgrensendeYtelserDto> resultatListe = behandlingRelatertInformasjonApplikasjonTjeneste.hentRelaterteYtelser(lagBehandling(fagsakFødsel), AKTØR_ID_99, false);

        assertThat(resultatListe).isEmpty();
    }

    @Test
    public void skal_returnere_tom_liste_når_ingen_tidligere_sak_for_medsøker() throws Exception {

        // bygg behandlinged annen part
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
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
        Fagsak fagsak = FagsakBuilder.nyFagsak().medSaksnummer(new Saksnummer(Long.toString(fagsakId*100))).build();
        fagsak.setId(fagsakId);
        return fagsak;
    }

    private Fagsak lagFagsak(Long fagsakId, NavBruker navBruker) {
        Fagsak fagsak = Fagsak.opprettNy(navBruker);
        fagsak.setId(fagsakId);
        return fagsak;
    }
}
