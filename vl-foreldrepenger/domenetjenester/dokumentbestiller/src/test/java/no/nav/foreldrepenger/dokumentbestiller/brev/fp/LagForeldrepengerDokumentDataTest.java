package no.nav.foreldrepenger.dokumentbestiller.brev.fp;

import static no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType.REVURDERING_DOK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.KonsekvensForYtelsen;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentData;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dokumentbestiller.DokumentRepositoryRule;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentDataTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.UendretUtfallDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BestillVedtakBrevDto;

public class LagForeldrepengerDokumentDataTest {
    @Rule
    public final DokumentRepositoryRule repoRule = new DokumentRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private DokumentDataTjeneste dokumentDataTjeneste = mock(DokumentDataTjeneste.class);
    private DokumentMalType revurderingDok = mock(DokumentMalType.class);
    private Behandling behandling;

    @Before
    public void setUp() {
        AbstractTestScenario<?> scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        this.behandling = scenario.medBehandlingType(BehandlingType.REVURDERING).lagre(repositoryProvider);
        when(dokumentDataTjeneste.hentDokumentMalType(REVURDERING_DOK)).thenReturn(revurderingDok);
    }

    @Test
    public void skal_lage_dokument_om_uendret_utfall_om_behandlingsresultat_foreldrepenger_endret_sendt_varsel_om_revurdering_og_kun_endring_i_fordeling_av_ytelsen() {
        // Arrange
        long uendretDokId = 2312L;
        boolean uendretUtfall = false;
        DokumentData dokumentDataMedProdusertDok = DokumentData.builder().medBehandling(behandling).medBestiltTid(LocalDateTime.now())
            .medDokumentMalType(revurderingDok).build();
        List<DokumentData> dokumentDataList = new ArrayList<>();
        dokumentDataList.add(dokumentDataMedProdusertDok);
        when(dokumentDataTjeneste.hentDokumentDataListe(anyLong(), any())).thenReturn(dokumentDataList);
        when(dokumentDataTjeneste.lagreDokumentData(any(), any(UendretUtfallDokument.class))).thenReturn(uendretDokId);
        BehandlingVedtak behandlingVedtak = mock(BehandlingVedtak.class);
        Behandlingsresultat behandlingsresultat = mock(Behandlingsresultat.class);
        when(behandlingVedtak.isBeslutningsvedtak()).thenReturn(uendretUtfall);
        when(behandlingsresultat.getBehandlingVedtak()).thenReturn(behandlingVedtak);
        when(behandlingsresultat.isBehandlingsresultatForeldrepengerEndret()).thenReturn(true);
        when(behandlingsresultat.getKonsekvenserForYtelsen()).thenReturn(Collections.singletonList(KonsekvensForYtelsen.ENDRING_I_FORDELING_AV_YTELSEN));
        behandling.setBehandlingresultat(behandlingsresultat);
        LagForeldrepengerDokumentData foreldrepengerDokumentData = new LagForeldrepengerDokumentData(dokumentDataTjeneste, uendretUtfall);
        BestillVedtakBrevDto bestillVedtakBrevDto = new BestillVedtakBrevDto(behandling.getId(), "");

        // Act
        Long dokumentId = foreldrepengerDokumentData.lagDokumentData(true, bestillVedtakBrevDto, behandling, behandling.getBehandlingsresultat(), null);

        // Assert
        assertThat(dokumentId).isEqualTo(uendretDokId);
    }

}
