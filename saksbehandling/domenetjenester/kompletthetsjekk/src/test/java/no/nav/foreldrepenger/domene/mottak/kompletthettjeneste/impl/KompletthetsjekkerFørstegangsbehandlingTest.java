package no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.brev.SendVarselTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.dokumentarkiv.DokumentArkivTjeneste;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetResultat;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetssjekkerInntektsmelding;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;

public class KompletthetsjekkerFørstegangsbehandlingTest {

    private static final LocalDate STARTDATO_PERMISJON = LocalDate.now().plusWeeks(1);
    private static final String KODE_LEGEERKLÆRING = "I000023";
    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());
    private SøknadRepository søknadRepository = repositoryProvider.getSøknadRepository();

    private KompletthetssjekkerTestUtil testUtil = new KompletthetssjekkerTestUtil(repositoryProvider, resultatRepositoryProvider);

    @Mock
    private DokumentArkivTjeneste dokumentArkivTjeneste;
    @Mock
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    @Mock
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjenesteMock;

    private AbstractKompletthetssjekkerSøknad kompletthetssjekkerSøknadFP;
    private KompletthetssjekkerInntektsmelding kompletthetssjekkerInntektsmelding;
    private KompletthetsjekkerFelles kompletthetsjekkerFPFelles;
    private KompletthetsjekkerFørstegangsbehandling kompletthetsjekker;

    @Before
    public void before() {
        when(skjæringstidspunktTjeneste.utledSkjæringstidspunktForForeldrepenger(any(Behandling.class))).thenReturn(STARTDATO_PERMISJON);
        when(inntektArbeidYtelseTjenesteMock.utledManglendeInntektsmeldingerFraArkiv(any(Behandling.class))).thenReturn(new HashMap<>());

        kompletthetssjekkerSøknadFP = new KompletthetssjekkerSøknadFørstegangsbehandling(dokumentArkivTjeneste, repositoryProvider, skjæringstidspunktTjeneste, 4);
        kompletthetssjekkerInntektsmelding = new KompletthetssjekkerInntektsmeldingImpl(inntektArbeidYtelseTjenesteMock);
        kompletthetsjekkerFPFelles = new KompletthetsjekkerFelles(repositoryProvider, mock(SendVarselTjeneste.class));
        kompletthetsjekker = new KompletthetsjekkerFørstegangsbehandling(kompletthetssjekkerSøknadFP, kompletthetssjekkerInntektsmelding, inntektArbeidYtelseTjenesteMock, kompletthetsjekkerFPFelles, skjæringstidspunktTjeneste, søknadRepository);
    }

    @Test
    public void skal_finne_at_kompletthet_er_oppfylt() {
        // Arrange
        Behandling behandling = ScenarioMorSøkerForeldrepenger.forDefaultAktør().lagre(repositoryProvider, resultatRepositoryProvider);

        // Act
        KompletthetResultat kompletthetResultat = kompletthetsjekker.vurderForsendelseKomplett(behandling);

        // Assert
        assertThat(kompletthetResultat.erOppfylt()).isTrue();
        assertThat(kompletthetResultat.getVentefrist()).isNull();
    }

    @Test
    public void skal_finne_at_kompletthet_ikke_er_oppfylt_når_inntektsmelding_mangler() {
        // Arrange
        Behandling behandling = ScenarioMorSøkerForeldrepenger.forDefaultAktør().lagre(repositoryProvider, resultatRepositoryProvider);
        mockManglendeInntektsmelding();
        testUtil.lagreSøknad(behandling);

        // Act
        KompletthetResultat kompletthetResultat = kompletthetsjekker.vurderForsendelseKomplett(behandling);

        // Assert
        assertThat(kompletthetResultat.erOppfylt()).isFalse();
        assertThat(kompletthetResultat.getVentefrist().toLocalDate()).isEqualTo(søknadRepository.hentSøknad(behandling).getMottattDato().plusWeeks(1));
    }

    private void mockManglendeInntektsmelding() {
        HashMap<String, Set<String>> manglendeInntektsmeldinger = new HashMap<>();
        manglendeInntektsmeldinger.put("1", new HashSet<>());
        when(inntektArbeidYtelseTjenesteMock.utledManglendeInntektsmeldingerFraArkiv(any(Behandling.class))).thenReturn(manglendeInntektsmeldinger);
    }

    private void mockManglendeInntektsmeldingGrunnlag() {
        HashMap<String, Set<String>> manglendeInntektsmeldinger = new HashMap<>();
        manglendeInntektsmeldinger.put("1", new HashSet<>());
        when(inntektArbeidYtelseTjenesteMock.utledManglendeInntektsmeldingerFraGrunnlag(any(Behandling.class))).thenReturn(manglendeInntektsmeldinger);
    }
}
