package no.nav.foreldrepenger.web.app.tjenester.fagsak.app;

import static java.lang.String.valueOf;
import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollAsynkTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.aktør.NavBrukerBuilder;
import no.nav.foreldrepenger.behandlingslager.testutilities.aktør.NavPersoninfoBuilder;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.testutilities.Whitebox;

@SuppressWarnings("deprecation")
public class FagsakApplikasjonTjenesteTest {

    private static final String FNR = "12345678901";
    private static final AktørId AKTØR_ID = new AktørId("1");
    private static final Saksnummer SAKSNUMMER = new Saksnummer("123");

    private FagsakApplikasjonTjeneste tjeneste;
    private FagsakRepository fagsakRepository;
    private BehandlingRepository behandlingRepository;
    private TpsTjeneste tpsTjeneste;


    @Before
    public void oppsett() {
        tpsTjeneste = mock(TpsTjeneste.class);
        fagsakRepository = mock(FagsakRepository.class);
        behandlingRepository = mock(BehandlingRepository.class);

        BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjeneste = mock(BehandlingskontrollAsynkTjeneste.class);

        BehandlingRepositoryProvider repositoryProvider = mock(BehandlingRepositoryProvider.class);
        when(repositoryProvider.getFagsakRepository()).thenReturn(fagsakRepository);
        when(repositoryProvider.getBehandlingRepository()).thenReturn(behandlingRepository);

        tjeneste = new FagsakApplikasjonTjenesteImpl(repositoryProvider, behandlingskontrollAsynkTjeneste, tpsTjeneste);
    }

    @Test
    public void skal_hente_saker_på_fnr() {
        // Arrange
        Personinfo personinfo = new NavPersoninfoBuilder().medAktørId(AKTØR_ID).build();
        NavBruker navBruker = new NavBrukerBuilder().medPersonInfo(personinfo).build();
        when(tpsTjeneste.hentBrukerForFnr(new PersonIdent(FNR))).thenReturn(Optional.of(personinfo));

        Fagsak fagsak = FagsakBuilder.nyEngangstønad(RelasjonsRolleType.MORA).medBruker(navBruker).medSaksnummer(SAKSNUMMER).build();
        Whitebox.setInternalState(fagsak, "id", -1L);
        when(fagsakRepository.hentForBruker(AKTØR_ID)).thenReturn(Collections.singletonList(fagsak));

        LocalDate fødselsdato = LocalDate.of(2017, JANUARY, 1);
        when(behandlingRepository.hentSisteBehandlingForFagsakId(anyLong())).thenReturn(Optional.of(Behandling.forFørstegangssøknad(fagsak).build()));

        // Act
        FagsakSamlingForBruker view = tjeneste.hentSaker(FNR);

        // Assert
        assertThat(view.isEmpty()).isFalse();
        assertThat(view.getFagsakInfoer()).hasSize(1);
        FagsakSamlingForBruker.FagsakRad info = view.getFagsakInfoer().get(0);
        assertThat(info.getFagsak()).isEqualTo(fagsak);
        assertThat(info.getFødselsdato()).isEqualTo(fødselsdato);
    }

    @Test
    public void skal_hente_saker_på_saksreferanse() {
        // Arrange
        Personinfo personinfo = new NavPersoninfoBuilder().medAktørId(AKTØR_ID).build();
        NavBruker navBruker = new NavBrukerBuilder().medPersonInfo(personinfo).build();
        Fagsak fagsak = FagsakBuilder.nyEngangstønad(RelasjonsRolleType.MORA).medBruker(navBruker).medSaksnummer(SAKSNUMMER).build();
        Whitebox.setInternalState(fagsak, "id", -1L);
        when(fagsakRepository.hentSakGittSaksnummer(SAKSNUMMER)).thenReturn(Optional.of(fagsak));

        final LocalDate fødselsdato = LocalDate.of(2017, JANUARY, 1);
        when(behandlingRepository.hentSisteBehandlingForFagsakId(anyLong())).thenReturn(Optional.of(Behandling.forFørstegangssøknad(fagsak).build()));

        when(tpsTjeneste.hentBrukerForAktør(AKTØR_ID)).thenReturn(Optional.of(personinfo));

        // Act
        FagsakSamlingForBruker view = tjeneste.hentSaker(SAKSNUMMER.getVerdi());

        // Assert
        assertThat(view.isEmpty()).isFalse();
        assertThat(view.getFagsakInfoer()).hasSize(1);
        FagsakSamlingForBruker.FagsakRad info = view.getFagsakInfoer().get(0);
        assertThat(info.getFagsak()).isEqualTo(fagsak);
    }

    @Test
    public void skal_returnere_tomt_view_når_fagsakens_bruker_er_ukjent_for_tps() {
        // Arrange
        NavBruker navBruker = new NavBrukerBuilder().medAktørId(AKTØR_ID).build();
        Fagsak fagsak = FagsakBuilder.nyEngangstønad(RelasjonsRolleType.MORA).medBruker(navBruker).medSaksnummer(SAKSNUMMER).build();
        Whitebox.setInternalState(fagsak, "id", -1L);
        when(fagsakRepository.hentSakGittSaksnummer(SAKSNUMMER)).thenReturn(Optional.of(fagsak));

        when(tpsTjeneste.hentBrukerForAktør(AKTØR_ID)).thenReturn(Optional.empty()); // Ingen treff i TPS

        // Act
        FagsakSamlingForBruker view = tjeneste.hentSaker(valueOf(SAKSNUMMER));

        // Assert
        assertThat(view.isEmpty()).isTrue();
    }

    @Test
    public void skal_returnere_tomt_view_dersom_søkestreng_ikke_er_gyldig_fnr_eller_saksnr() {
        FagsakSamlingForBruker view = tjeneste.hentSaker("ugyldig_søkestreng");

        assertThat(view.isEmpty()).isTrue();
    }

    @Test
    public void skal_returnere_tomt_view_ved_ukjent_fnr() {
        when(tpsTjeneste.hentBrukerForFnr(new PersonIdent(FNR))).thenReturn(Optional.empty());

        FagsakSamlingForBruker view = tjeneste.hentSaker(FNR);

        assertThat(view.isEmpty()).isTrue();
    }

    @Test
    public void skal_returnere_tomt_view_ved_ukjent_saksnr() {
        when(fagsakRepository.hentSakGittSaksnummer(SAKSNUMMER)).thenReturn(Optional.empty());

        FagsakSamlingForBruker view = tjeneste.hentSaker(valueOf(SAKSNUMMER));

        assertThat(view.isEmpty()).isTrue();
    }
}
