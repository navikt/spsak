package no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.impl.fp;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.impl.KompletthetssjekkerTestUtil.AKTØR_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.brev.SendVarselTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.InntektArbeidYtelseScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsforholdTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.AksjonspunktutlederForVurderOpptjening;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.InntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetResultat;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetssjekkerInntektsmelding;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetssjekkerSøknad;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.ManglendeVedlegg;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.impl.KompletthetssjekkerTestUtil;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.uttak.InntektsmeldingVilEndreUttakTjeneste;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;
import no.nav.vedtak.konfig.Tid;

public class KompletthetsjekkerFPRevurderingTest {

    private static final String ORGNR = "505050";

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private final MottatteDokumentRepository mottatteDokumentRepository = repositoryProvider.getMottatteDokumentRepository();
    private final InntektArbeidYtelseRepository inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
    private final VirksomhetRepository virksomhetRepository = repositoryProvider.getVirksomhetRepository();

    private final KompletthetssjekkerTestUtil testUtil = new KompletthetssjekkerTestUtil(repoRule, repositoryProvider);

    private final KompletthetssjekkerSøknad kompletthetssjekkerSøknad = mock(KompletthetssjekkerSøknad.class);
    private final KompletthetssjekkerInntektsmelding kompletthetssjekkerInntektsmelding = mock(KompletthetssjekkerInntektsmelding.class);
    private final InntektsmeldingVilEndreUttakTjeneste inntektsmeldingVilEndreUttakTjeneste = mock(InntektsmeldingVilEndreUttakTjeneste.class);

    private final InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider,
        mock(ArbeidsforholdTjeneste.class), mock(TpsTjeneste.class), mock(VirksomhetTjeneste.class), mock(SkjæringstidspunktTjeneste.class), mock(AksjonspunktutlederForVurderOpptjening.class));
    private final KompletthetsjekkerFPFelles kompletthetsjekkerFPFelles = new KompletthetsjekkerFPFelles(repositoryProvider, mock(SendVarselTjeneste.class));
    private final KompletthetsjekkerFPRevurdering kompletthetsjekkerFPRevurdering = new KompletthetsjekkerFPRevurdering(
        kompletthetssjekkerSøknad, kompletthetssjekkerInntektsmelding, kompletthetsjekkerFPFelles, inntektArbeidYtelseTjeneste,
        repositoryProvider, inntektsmeldingVilEndreUttakTjeneste);

    @Test
    public void skal_finne_at_endringssøknad_er_mottatt_og_sette_på_vent_når_inntektsmeldinger_mangler() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = testUtil.opprettRevurderingsscenarioForMor();
        Behandling behandling = scenario.lagre(repositoryProvider);
        testUtil.byggOgLagreSøknadMedNyOppgittFordeling(behandling, true);
        when(kompletthetssjekkerInntektsmelding.utledManglendeInntektsmeldinger(any(Behandling.class)))
            .thenReturn(singletonList(new ManglendeVedlegg(DokumentTypeId.INNTEKTSMELDING)));

        // Act
        assertThat(kompletthetsjekkerFPRevurdering).isNotNull();
        KompletthetResultat kompletthetResultat = kompletthetsjekkerFPRevurdering.vurderForsendelseKomplett(behandling);

        // Assert
        assertThat(kompletthetResultat.erOppfylt()).isFalse();
        assertThat(kompletthetResultat.getVenteårsak()).isEqualTo(Venteårsak.AVV_DOK);
        assertThat(kompletthetResultat.getVentefrist().toLocalDate()).isEqualTo(LocalDate.now().plusWeeks(3));
    }

    @Test
    public void skal_finne_at_endringssøknad_er_mottatt_og_sette_på_vent_når_vedlegg_mangler() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = testUtil.opprettRevurderingsscenarioForMor();
        Behandling behandling = scenario.lagre(repositoryProvider);
        testUtil.byggOgLagreSøknadMedNyOppgittFordeling(behandling, true);
        when(kompletthetssjekkerInntektsmelding.utledManglendeInntektsmeldinger(any(Behandling.class)))
            .thenReturn(emptyList());
        when(kompletthetssjekkerSøknad.utledManglendeVedleggForSøknad(any(Behandling.class)))
            .thenReturn(singletonList(new ManglendeVedlegg(DokumentTypeId.LEGEERKLÆRING)));

        // Act
        assertThat(kompletthetsjekkerFPRevurdering).isNotNull();
        KompletthetResultat kompletthetResultat = kompletthetsjekkerFPRevurdering.vurderForsendelseKomplett(behandling);

        // Assert
        assertThat(kompletthetResultat.erOppfylt()).isFalse();
        assertThat(kompletthetResultat.getVenteårsak()).isEqualTo(Venteårsak.AVV_DOK);
        assertThat(kompletthetResultat.getVentefrist().toLocalDate()).isEqualTo(LocalDate.now().plusWeeks(3));
    }

    @Test
    public void skal_finne_at_endringssøknad_er_mottatt_og_at_forsendelsen_er_komplett_når_ingen_vedlegg_mangler() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = testUtil.opprettRevurderingsscenarioForMor();
        Behandling behandling = scenario.lagre(repositoryProvider);
        testUtil.byggOgLagreSøknadMedNyOppgittFordeling(behandling, true);
        when(kompletthetssjekkerInntektsmelding.utledManglendeInntektsmeldinger(any(Behandling.class)))
            .thenReturn(emptyList());
        when(kompletthetssjekkerSøknad.utledManglendeVedleggForSøknad(any(Behandling.class)))
            .thenReturn(emptyList());

        // Act
        assertThat(kompletthetsjekkerFPRevurdering).isNotNull();
        KompletthetResultat kompletthetResultat = kompletthetsjekkerFPRevurdering.vurderForsendelseKomplett(behandling);

        // Assert
        assertThat(kompletthetResultat.erOppfylt()).isTrue();
    }

    @Test
    public void skal_finne_at_endringssøknad_ikke_er_mottatt_og_sende_brev_varsel_om_revurdering_som_følger_av_endring_i_gradering_i_inntektsmeldingen() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = testUtil.opprettRevurderingsscenarioForMor();
        opprettArbeidsforhold(scenario);
        Behandling behandling = scenario.lagre(repositoryProvider);
        opprettInntektsmelding(behandling);
        when(inntektsmeldingVilEndreUttakTjeneste.graderingVilEndreUttak(any(Behandling.class), any(Inntektsmelding.class))).thenReturn(true);

        // Act
        KompletthetResultat kompletthetResultat = kompletthetsjekkerFPRevurdering.vurderForsendelseKomplett(behandling);

        // Assert
        assertThat(kompletthetResultat.erOppfylt()).isFalse();
        assertThat(kompletthetResultat.getVenteårsak()).isEqualTo(Venteårsak.AVV_DOK);
        assertThat(kompletthetResultat.getVentefrist().toLocalDate()).isEqualTo(LocalDate.now().plusWeeks(4));
    }

    @Test
    public void skal_finne_at_endringssøknad_ikke_er_mottatt_og_sende_brev_varsel_om_revurdering_som_følger_av_utsettelse_arbeid_i_inntektsmeldingen() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = testUtil.opprettRevurderingsscenarioForMor();
        opprettArbeidsforhold(scenario);
        Behandling behandling = scenario.lagre(repositoryProvider);
        opprettInntektsmelding(behandling);
        when(inntektsmeldingVilEndreUttakTjeneste.utsettelseArbeidVilEndreUttak(any(Behandling.class), any(Inntektsmelding.class))).thenReturn(true);

        // Act
        KompletthetResultat kompletthetResultat = kompletthetsjekkerFPRevurdering.vurderForsendelseKomplett(behandling);

        // Assert
        assertThat(kompletthetResultat.erOppfylt()).isFalse();
        assertThat(kompletthetResultat.getVenteårsak()).isEqualTo(Venteårsak.AVV_DOK);
        assertThat(kompletthetResultat.getVentefrist().toLocalDate()).isEqualTo(LocalDate.now().plusWeeks(4));
    }

    @Test
    public void skal_finne_at_endringssøknad_ikke_er_mottatt_og_sende_brev_inntektsmelding_for_tidlig_som_følger_av_utsettelse_ferie_i_inntektsmeldingen() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = testUtil.opprettRevurderingsscenarioForMor();
        opprettArbeidsforhold(scenario);
        Behandling behandling = scenario.lagre(repositoryProvider);
        opprettInntektsmelding(behandling);
        when(inntektsmeldingVilEndreUttakTjeneste.utsettelseFerieVilEndreUttak(any(Behandling.class), any(Inntektsmelding.class))).thenReturn(true);

        // Act
        KompletthetResultat kompletthetResultat = kompletthetsjekkerFPRevurdering.vurderForsendelseKomplett(behandling);

        // Assert
        assertThat(kompletthetResultat.erOppfylt()).isFalse();
        assertThat(kompletthetResultat.getVenteårsak()).isEqualTo(Venteårsak.AVV_DOK);
        assertThat(kompletthetResultat.getVentefrist().toLocalDate()).isEqualTo(LocalDate.now().plusWeeks(4));
    }

    @Test
    public void skal_finne_at_endringssøknad_ikke_er_mottatt_men_likevel_fortsette_behandlingen_når_det_ikke_er_endringer_i_periodene_i_inntektsmeldingen() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = testUtil.opprettRevurderingsscenarioForMor();
        opprettArbeidsforhold(scenario);
        Behandling behandling = scenario.lagre(repositoryProvider);
        opprettInntektsmelding(behandling);
        when(inntektsmeldingVilEndreUttakTjeneste.graderingVilEndreUttak(any(Behandling.class), any(Inntektsmelding.class))).thenReturn(false);

        // Act
        KompletthetResultat kompletthetResultat = kompletthetsjekkerFPRevurdering.vurderForsendelseKomplett(behandling);

        // Assert
        assertThat(kompletthetResultat.erOppfylt()).isTrue();
    }

    private void opprettArbeidsforhold(ScenarioMorSøkerForeldrepenger scenario) {
        InntektArbeidYtelseScenario.InntektArbeidYtelseScenarioTestBuilder builder = scenario.getInntektArbeidYtelseScenarioTestBuilder();
        builder.medAktørId(AKTØR_ID);
        builder.medAktivitetsAvtaleFom(LocalDate.now());
        builder.medAktivitetsAvtaleTom(Tid.TIDENES_ENDE);
        builder.medOrgNr(ORGNR);
        builder.medOrgNavn("Virksomhet");
        builder.build();
    }

    private void opprettInntektsmelding(Behandling behandling) {
        MottattDokument mottattDokument = new MottattDokument.Builder()
            .medDokumentTypeId(DokumentTypeId.INNTEKTSMELDING)
            .medFagsakId(behandling.getFagsakId())
            .medMottattDato(LocalDate.now())
            .medBehandlingId(behandling.getId())
            .medElektroniskRegistrert(true)
            .medJournalPostId(new JournalpostId("2"))
            .medDokumentId("3")
            .build();
        mottatteDokumentRepository.lagre(mottattDokument);

        InntektsmeldingBuilder inntektsmeldingBuilder = InntektsmeldingBuilder.builder();
        inntektsmeldingBuilder.medStartDatoPermisjon(LocalDate.now());
        inntektsmeldingBuilder.medInnsendingstidspunkt(LocalDateTime.now());
        inntektsmeldingBuilder.medBeløp(BigDecimal.TEN);
        inntektsmeldingBuilder.medMottattDokument(mottattDokument);

        Optional<Virksomhet> hent = virksomhetRepository.hent(ORGNR);
        if (hent.isPresent()) {
            Virksomhet virksomhet = hent.get();
            inntektsmeldingBuilder.medVirksomhet(virksomhet);
        }

        inntektArbeidYtelseRepository.lagre(behandling, inntektsmeldingBuilder.build());
    }
}
