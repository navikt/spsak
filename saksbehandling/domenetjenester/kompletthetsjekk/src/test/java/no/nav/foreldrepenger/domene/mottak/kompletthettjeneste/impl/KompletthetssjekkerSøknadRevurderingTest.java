package no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.impl;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadVedleggEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.dokumentarkiv.DokumentArkivTjeneste;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.ManglendeVedlegg;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

public class KompletthetssjekkerSøknadRevurderingTest {

    private static final String LEGEERKLÆRING_KODE = "I000023";

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    private final KompletthetssjekkerTestUtil testUtil = new KompletthetssjekkerTestUtil(repositoryProvider);

    private final DokumentArkivTjeneste dokumentArkivTjeneste = mock(DokumentArkivTjeneste.class);
    private final KompletthetssjekkerSøknadRevurdering kompletthetssjekker = new KompletthetssjekkerSøknadRevurdering(dokumentArkivTjeneste, repositoryProvider,
        mock(SkjæringstidspunktTjeneste.class), 3);

    @Test
    public void skal_utlede_at_et_påkrevd_vedlegg_finnes_i_journal() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = testUtil.opprettRevurderingsscenario();
        scenario.medSøknad()
            .medSøknadReferanse(UUID.randomUUID().toString())
            .medSykemeldinReferanse(UUID.randomUUID().toString())
            .medSøknadsdato(LocalDate.now())
            .leggTilVedlegg(new SøknadVedleggEntitet.Builder().medSkjemanummer(LEGEERKLÆRING_KODE).medErPåkrevdISøknadsdialog(true).build())
            .build();
        Behandling behandling = scenario.lagre(repositoryProvider);

        // Matcher med søknad:
        Set<DokumentTypeId> dokumentListe = singleton(DokumentTypeId.LEGEERKLÆRING);
        when(dokumentArkivTjeneste.hentDokumentTypeIdForSak(any(Saksnummer.class), any(), any())).thenReturn(dokumentListe);

        // Act
        List<ManglendeVedlegg> manglendeVedlegg = kompletthetssjekker.utledManglendeVedleggForSøknad(behandling);

        // Assert
        assertThat(manglendeVedlegg).isEmpty();
    }

    @Test
    public void skal_utlede_at_et_påkrevd_vedlegg_ikke_finnes_i_journal() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = testUtil.opprettRevurderingsscenario();
        scenario.medSøknad()
            .medSøknadReferanse(UUID.randomUUID().toString())
            .medSykemeldinReferanse(UUID.randomUUID().toString())
            .medSøknadsdato(LocalDate.now())
            .leggTilVedlegg(new SøknadVedleggEntitet.Builder().medSkjemanummer(LEGEERKLÆRING_KODE).medErPåkrevdISøknadsdialog(true).build())
            .build();
        Behandling behandling = scenario.lagre(repositoryProvider);

        // Matcher ikke med søknad:
        Set<DokumentTypeId> dokumentListe = singleton(DokumentTypeId.ANNET);
        when(dokumentArkivTjeneste.hentDokumentTypeIdForSak(any(Saksnummer.class), any(), any())).thenReturn(dokumentListe);

        // Act
        List<ManglendeVedlegg> manglendeVedlegg = kompletthetssjekker.utledManglendeVedleggForSøknad(behandling);

        // Assert
        assertThat(manglendeVedlegg).hasSize(1);
        assertThat(manglendeVedlegg.get(0).getDokumentType().getOffisiellKode()).isEqualTo(LEGEERKLÆRING_KODE);
    }

    @Test
    public void skal_utlede_at_et_påkrevd_vedlegg_ikke_finnes_i_journal_når_det_ble_mottatt_før_gjeldende_vedtak() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = testUtil.opprettRevurderingsscenario();
        scenario.medSøknad()
            .medSøknadReferanse(UUID.randomUUID().toString())
            .medSykemeldinReferanse(UUID.randomUUID().toString())
            .medSøknadsdato(LocalDate.now())
            .leggTilVedlegg(new SøknadVedleggEntitet.Builder().medSkjemanummer(LEGEERKLÆRING_KODE).medErPåkrevdISøknadsdialog(true).build())
            .build();
        Behandling revurdering = scenario.lagre(repositoryProvider);

        // Matcher med søknad, men er mottatt ifbm førstegangsbehandlingen:
        Set<DokumentTypeId> dokumentListe = new HashSet<>();
        dokumentListe.add(DokumentTypeId.INNTEKTSMELDING);
        when(dokumentArkivTjeneste.hentDokumentTypeIdForSak(any(Saksnummer.class), any(), any())).thenReturn(dokumentListe);

        // Act
        List<ManglendeVedlegg> manglendeVedlegg = kompletthetssjekker.utledManglendeVedleggForSøknad(revurdering);

        // Assert
        assertThat(manglendeVedlegg).hasSize(1);
        assertThat(manglendeVedlegg.get(0).getDokumentType().getOffisiellKode()).isEqualTo(LEGEERKLÆRING_KODE);
    }

    @Test
    public void skal_utlede_at_et_påkrevd_vedlegg_som_ikke_finnes_i_mottatte_dokumenter_mangler_når_vedlegget_fra_journal_har_mottatt_dato_null() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = testUtil.opprettRevurderingsscenario();
        scenario.medSøknad()
            .medSøknadReferanse(UUID.randomUUID().toString())
            .medSykemeldinReferanse(UUID.randomUUID().toString())
            .medSøknadsdato(LocalDate.now())
            .leggTilVedlegg(new SøknadVedleggEntitet.Builder().medSkjemanummer(LEGEERKLÆRING_KODE).medErPåkrevdISøknadsdialog(true).build())
            .build();
        Behandling revurdering = scenario.lagre(repositoryProvider);

        // Matcher med søknad, men mangler mottatt dato:
        Set<DokumentTypeId> dokumentListe = new HashSet<>();
        dokumentListe.add(DokumentTypeId.LEGEERKLÆRING);
        when(dokumentArkivTjeneste.hentDokumentTypeIdForSak(any(Saksnummer.class), any(), any())).thenReturn(Collections.emptySet());

        // Act
        List<ManglendeVedlegg> manglendeVedlegg = kompletthetssjekker.utledManglendeVedleggForSøknad(revurdering);

        // Assert
        assertThat(manglendeVedlegg).hasSize(1);
        assertThat(manglendeVedlegg.get(0).getDokumentType().getOffisiellKode()).isEqualTo(LEGEERKLÆRING_KODE);
    }

    @Test
    public void skal_utlede_at_et_dokument_som_er_påkrevd_som_følger_av_utsettelse_finnes_i_journal() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = testUtil.opprettRevurderingsscenario();
        Behandling behandling = scenario.lagre(repositoryProvider);
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
