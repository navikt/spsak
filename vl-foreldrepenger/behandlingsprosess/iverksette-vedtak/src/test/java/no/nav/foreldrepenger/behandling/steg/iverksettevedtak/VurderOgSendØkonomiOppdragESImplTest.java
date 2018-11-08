package no.nav.foreldrepenger.behandling.steg.iverksettevedtak;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.økonomistøtte.api.ØkonomioppdragApplikasjonTjeneste;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class VurderOgSendØkonomiOppdragESImplTest {

    private static final long BEHANDLING_ID = 128L;

    private BehandlingVedtak behandlingVedtak;

    private ØkonomioppdragApplikasjonTjeneste økonomioppdragApplikasjonTjeneste;
    private Behandlingsresultat behandlingsresultat;

    private VurderOgSendØkonomiOppdrag vurderOgSendØkonomiOppdrag;

    @FagsakYtelseTypeRef("ES")
    @Inject
    private Instance<VurderØkonomiOppdrag> vurderØkonomiOppdragInstance;

    private VurderØkonomiOppdragProvider vurderØkonomiOppdragProvider;

    @Before
    public void oppsett() {
        økonomioppdragApplikasjonTjeneste = mock(ØkonomioppdragApplikasjonTjeneste.class);
        behandlingsresultat = mock(Behandlingsresultat.class);
    }

    private void initBehandling(BehandlingType behandlingType) {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel()
            .medBehandlingType(behandlingType);
        scenario.lagMocked();
        BehandlingRepositoryProvider repositoryProvider = scenario.mockBehandlingRepositoryProvider();
        behandlingVedtak = scenario.mockBehandlingVedtak();

        when(behandlingVedtak.getBehandlingsresultat()).thenReturn(behandlingsresultat);

        vurderØkonomiOppdragProvider = new VurderØkonomiOppdragProvider(repositoryProvider, vurderØkonomiOppdragInstance);
        vurderOgSendØkonomiOppdrag = new VurderOgSendØkonomiOppdragImpl(repositoryProvider, økonomioppdragApplikasjonTjeneste, vurderØkonomiOppdragProvider);
    }

    @Test
    public void testUtbetalingVedtakResultatInnvilget() {
        initBehandling(BehandlingType.FØRSTEGANGSSØKNAD);
        // Arrange
        vedtak(VedtakResultatType.INNVILGET, null);

        // Act
        boolean skalSende = vurderOgSendØkonomiOppdrag.skalSendeOppdrag(BEHANDLING_ID);

        // Assert
        assertThat(skalSende).isTrue();
    }

    @Test
    public void testUtbetalingVedtakResultatAvslag() {
        initBehandling(BehandlingType.FØRSTEGANGSSØKNAD);
        // Arrange
        vedtak(VedtakResultatType.AVSLAG, Avslagsårsak.FAR_HAR_IKKE_OMSORG_FOR_BARNET);

        // Act
        boolean skalSende = vurderOgSendØkonomiOppdrag.skalSendeOppdrag(BEHANDLING_ID);

        // Assert
        assertThat(skalSende).isFalse();
    }

    @Test
    public void revurderingMedVedtakSomErBeslutningSkalIkkeSendeOppdrag() {
        initBehandling(BehandlingType.REVURDERING);
        when(behandlingVedtak.isBeslutningsvedtak()).thenReturn(true);

        boolean skalSende = vurderOgSendØkonomiOppdrag.skalSendeOppdrag(BEHANDLING_ID);

        assertThat(skalSende).isFalse();
    }

    @Test
    public void revurderingMedVedtakSomIkkeErBeslutningSkalSendeOppdrag() {
        initBehandling(BehandlingType.REVURDERING);
        when(behandlingVedtak.isBeslutningsvedtak()).thenReturn(false);

        boolean skalSende = vurderOgSendØkonomiOppdrag.skalSendeOppdrag(BEHANDLING_ID);

        assertThat(skalSende).isTrue();
    }

    @Test
    public void avslagPåGrunnAvTidligereUtbetaltSkalIkkeSendeOppdrag() {
        initBehandling(BehandlingType.FØRSTEGANGSSØKNAD);
        vedtak(VedtakResultatType.AVSLAG, Avslagsårsak.ENGANGSSTØNAD_ALLEREDE_UTBETALT_TIL_MOR);

        boolean skalSende = vurderOgSendØkonomiOppdrag.skalSendeOppdrag(BEHANDLING_ID);

        assertThat(skalSende).isFalse();
    }

    private void vedtak(VedtakResultatType vedtakResultatType, Avslagsårsak avslagsårsak) {
        when(behandlingVedtak.getVedtakResultatType()).thenReturn(vedtakResultatType);
        when(behandlingsresultat.getAvslagsårsak()).thenReturn(avslagsårsak);
    }
}
