package no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.impl;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.domene.dokumentarkiv.DokumentArkivTjeneste;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.ManglendeVedlegg;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;

public class KompletthetssjekkerSøknadRevurderingTest {

    private static final String LEGEERKLÆRING_KODE = "I000023";

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private final DokumentArkivTjeneste dokumentArkivTjeneste = mock(DokumentArkivTjeneste.class);
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());
    private final KompletthetssjekkerTestUtil testUtil = new KompletthetssjekkerTestUtil(repositoryProvider, resultatRepositoryProvider);
    private final KompletthetssjekkerSøknadRevurdering kompletthetssjekker = new KompletthetssjekkerSøknadRevurdering(dokumentArkivTjeneste, repositoryProvider, resultatRepositoryProvider,
        mock(SkjæringstidspunktTjeneste.class), 3);

    @Test
    public void skal_utlede_at_et_dokument_som_er_påkrevd_som_følger_av_utsettelse_finnes_i_journal() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = testUtil.opprettRevurderingsscenario();
        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        testUtil.lagreSøknad(behandling);

        // Matcher med utsettelse:
        Set<DokumentTypeId> dokumentListe = singleton(DokumentTypeId.LEGEERKLÆRING);
        when(dokumentArkivTjeneste.hentDokumentTypeIdForSak(any(Saksnummer.class), any(), any())).thenReturn(dokumentListe);

        // Act
        List<ManglendeVedlegg> manglendeVedlegg = kompletthetssjekker.utledManglendeVedleggForSøknad(behandling);

        // Assert
        assertThat(manglendeVedlegg).isEmpty();
    }

}
