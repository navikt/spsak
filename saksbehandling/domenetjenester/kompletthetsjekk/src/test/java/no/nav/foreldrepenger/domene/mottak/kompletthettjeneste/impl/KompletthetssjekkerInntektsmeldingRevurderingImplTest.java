package no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.impl;

import static java.util.Collections.singletonList;
import static no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.impl.KompletthetssjekkerTestUtil.ARBGIVER1;
import static no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.impl.KompletthetssjekkerTestUtil.ARBGIVER2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.ManglendeVedlegg;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;

@Ignore("FIXME SP: trengs tilsvarende kompletthetssjekk i sykepenger avh. av hva bruker oppgir?")
public class KompletthetssjekkerInntektsmeldingRevurderingImplTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());

    private final KompletthetssjekkerTestUtil testUtil = new KompletthetssjekkerTestUtil(repositoryProvider, resultatRepositoryProvider);

    private final InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste = mockIayTjenesteMedToArbeidsgivere();

    private final KompletthetssjekkerInntektsmeldingRevurderingImpl kompletthetssjekker = new KompletthetssjekkerInntektsmeldingRevurderingImpl(
        inntektArbeidYtelseTjeneste, repositoryProvider);


    @Test
    public void skal_bare_utlede_manglende_inntektsmelding_for_arbeidsforholdet_som_er_berørt_av_gradering() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = testUtil.opprettRevurderingsscenario();
        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        testUtil.lagreSøknad(behandling);

        // Act
        List<ManglendeVedlegg> manglendeVedlegg = kompletthetssjekker.utledManglendeInntektsmeldinger(behandling);

        // Assert
        assertThat(manglendeVedlegg).hasSize(1);
        assertThat(manglendeVedlegg.get(0).getArbeidsgiver()).isEqualTo(ARBGIVER1);
    }


    @Test
    public void skal_bare_utlede_manglende_inntektsmeldinger_for_arbeidstakere() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = testUtil.opprettRevurderingsscenario();
        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        testUtil.lagreSøknad(behandling);

        // Act
        List<ManglendeVedlegg> manglendeVedlegg = kompletthetssjekker.utledManglendeInntektsmeldinger(behandling);

        // Assert
        assertThat(manglendeVedlegg).hasSize(0);
    }

    @Test
    public void skal_ikke_utlede_manglende_inntektsmelding_for_arbeidsforhold_berørt_av_gradering_når_den_er_mottatt() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = testUtil.opprettRevurderingsscenario();
        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        testUtil.lagreSøknad(behandling);

        Virksomhet virksomhet = new VirksomhetEntitet.Builder().medOrgnr(ARBGIVER1).build();
        Inntektsmelding inntektsmelding = InntektsmeldingBuilder.builder().medVirksomhet(virksomhet).medInnsendingstidspunkt(LocalDateTime.now()).build();
        when(inntektArbeidYtelseTjeneste.hentAlleInntektsmeldingerMottattEtterGjeldendeVedtak(behandling))
            .thenReturn(singletonList(inntektsmelding));

        // Act
        List<ManglendeVedlegg> manglendeVedlegg = kompletthetssjekker.utledManglendeInntektsmeldinger(behandling);

        // Assert
        assertThat(manglendeVedlegg).hasSize(0);
    }

    @Test
    public void skal_benytte_grunnlagstjenesten_til_å_utlede_manglende_inntektsmelding_ved_ferie() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = testUtil.opprettRevurderingsscenario();
        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        testUtil.lagreSøknad(behandling);

        // Act
        List<ManglendeVedlegg> manglendeVedlegg = kompletthetssjekker.utledManglendeInntektsmeldingerFraGrunnlag(behandling);

        // Assert
        assertThat(manglendeVedlegg).hasSize(1);
        assertThat(manglendeVedlegg.get(0).getArbeidsgiver()).isEqualTo(ARBGIVER2);
    }

    private static InntektArbeidYtelseTjeneste mockIayTjenesteMedToArbeidsgivere() {
        InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste = mock(InntektArbeidYtelseTjeneste.class);
        Set<String> arbeidsforholdIder1 = new HashSet<>();
        arbeidsforholdIder1.add("forhold1_1");
        arbeidsforholdIder1.add("forhold1_2");
        Set<String> arbeidsforholdIder2 = new HashSet<>();
        arbeidsforholdIder2.add("forhold1");

        Map<String, Set<String>> arbeidsforholdSomManglerImArkiv = new HashMap<>();
        arbeidsforholdSomManglerImArkiv.put(ARBGIVER1, arbeidsforholdIder1);
        arbeidsforholdSomManglerImArkiv.put(ARBGIVER2, arbeidsforholdIder2);

        Map<String, Set<String>> arbeidsforholdSomManglerImGrunnlag = new HashMap<>();
        arbeidsforholdSomManglerImGrunnlag.put(ARBGIVER2, arbeidsforholdIder2);

        when(inntektArbeidYtelseTjeneste.utledManglendeInntektsmeldingerFraArkiv(any(Behandling.class))).thenReturn(arbeidsforholdSomManglerImArkiv);
        when(inntektArbeidYtelseTjeneste.utledManglendeInntektsmeldingerFraGrunnlag(any(Behandling.class))).thenReturn(arbeidsforholdSomManglerImGrunnlag);
        return inntektArbeidYtelseTjeneste;
    }
}
