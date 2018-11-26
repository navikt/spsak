package no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.impl.fp;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.brev.SendVarselTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadVedleggEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.dokumentarkiv.DokumentArkivTjeneste;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetResultat;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetssjekkerInntektsmelding;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.ManglendeVedlegg;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.impl.KompletthetssjekkerInntektsmeldingImpl;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.impl.KompletthetssjekkerTestUtil;

public class KompletthetsjekkerFPTest {

    private static final LocalDate STARTDATO_PERMISJON = LocalDate.now().plusWeeks(1);
    private static final String KODE_INNLEGGELSE = "I000037";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private SøknadRepository søknadRepository = repositoryProvider.getSøknadRepository();

    private KompletthetssjekkerTestUtil testUtil = new KompletthetssjekkerTestUtil(repositoryProvider);

    @Mock
    private DokumentArkivTjeneste dokumentArkivTjeneste;
    @Mock
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    @Mock
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjenesteMock;

    private KompletthetssjekkerSøknadFP kompletthetssjekkerSøknadFP;
    private KompletthetssjekkerInntektsmelding kompletthetssjekkerInntektsmelding;
    private KompletthetsjekkerFPFelles kompletthetsjekkerFPFelles;
    private KompletthetsjekkerFP kompletthetsjekkerFP;

    @Before
    public void before() {
        when(skjæringstidspunktTjeneste.utledSkjæringstidspunktForForeldrepenger(any(Behandling.class))).thenReturn(STARTDATO_PERMISJON);
        when(inntektArbeidYtelseTjenesteMock.utledManglendeInntektsmeldingerFraArkiv(any(Behandling.class))).thenReturn(new HashMap<>());

        kompletthetssjekkerSøknadFP = new KompletthetssjekkerSøknadFPFørstegangsbehandling(dokumentArkivTjeneste, repositoryProvider, skjæringstidspunktTjeneste, 4);
        kompletthetssjekkerInntektsmelding = new KompletthetssjekkerInntektsmeldingImpl(inntektArbeidYtelseTjenesteMock);
        kompletthetsjekkerFPFelles = new KompletthetsjekkerFPFelles(repositoryProvider, mock(SendVarselTjeneste.class));
        kompletthetsjekkerFP = new KompletthetsjekkerFP(kompletthetssjekkerSøknadFP, kompletthetssjekkerInntektsmelding, inntektArbeidYtelseTjenesteMock, kompletthetsjekkerFPFelles, skjæringstidspunktTjeneste, søknadRepository);
    }

    @Test
    public void skal_finne_at_kompletthet_er_oppfylt() {
        // Arrange
        Behandling behandling = ScenarioMorSøkerForeldrepenger.forDefaultAktør().lagre(repositoryProvider);

        // Act
        KompletthetResultat kompletthetResultat = kompletthetsjekkerFP.vurderForsendelseKomplett(behandling);

        // Assert
        assertThat(kompletthetResultat.erOppfylt()).isTrue();
        assertThat(kompletthetResultat.getVentefrist()).isNull();
    }

    @Test
    public void skal_finne_at_kompletthet_ikke_er_oppfylt_når_inntektsmelding_mangler() {
        // Arrange
        Behandling behandling = ScenarioMorSøkerForeldrepenger.forDefaultAktør().lagre(repositoryProvider);
        mockManglendeInntektsmelding();
        testUtil.lagreSøknad(behandling);

        // Act
        KompletthetResultat kompletthetResultat = kompletthetsjekkerFP.vurderForsendelseKomplett(behandling);

        // Assert
        assertThat(kompletthetResultat.erOppfylt()).isFalse();
        assertThat(kompletthetResultat.getVentefrist().toLocalDate()).isEqualTo(søknadRepository.hentSøknad(behandling).getMottattDato().plusWeeks(1));
    }

    @Test
    public void skal_finne_at_kompletthet_ikke_er_oppfylt_når_vedlegg_til_søknad_mangler() {
        // Arrange
        Behandling behandling = ScenarioMorSøkerForeldrepenger.forDefaultAktør().lagre(repositoryProvider);
        opprettSøknadMedPåkrevdVedlegg(behandling);

        // Act
        KompletthetResultat kompletthetResultat = kompletthetsjekkerFP.vurderForsendelseKomplett(behandling);

        // Assert
        assertThat(kompletthetResultat.erOppfylt()).isFalse();
        assertThat(kompletthetResultat.getVentefrist().toLocalDate()).isEqualTo(søknadRepository.hentSøknad(behandling).getMottattDato().plusWeeks(3));
    }

    @Test
    public void skal_finne_at_kompletthet_er_oppfylt_når_vedlegg_til_søknad_finnes_i_joark() {
        // Arrange
        Behandling behandling = ScenarioMorSøkerForeldrepenger.forDefaultAktør().lagre(repositoryProvider);

        DokumentTypeId dokumentType = repositoryProvider.getKodeverkRepository().finnForKodeverkEiersKode(DokumentTypeId.class, KODE_INNLEGGELSE);
        Set<DokumentTypeId> dokumentTypeIds = singleton(dokumentType);
        when(dokumentArkivTjeneste.hentDokumentTypeIdForSak(any(), any(), any())).thenReturn(dokumentTypeIds);

        opprettSøknadMedPåkrevdVedlegg(behandling);

        // Act
        KompletthetResultat kompletthetResultat = kompletthetsjekkerFP.vurderForsendelseKomplett(behandling);

        // Assert
        assertThat(kompletthetResultat.erOppfylt()).isTrue();
        assertThat(kompletthetResultat.getVentefrist()).isNull();
    }

    @Test
    public void skal_returnere_hvilke_vedlegg_som_mangler() {
        // Arrange
        Behandling behandling = ScenarioMorSøkerForeldrepenger.forDefaultAktør().lagre(repositoryProvider);
        mockManglendeInntektsmeldingGrunnlag();
        opprettSøknadMedPåkrevdVedlegg(behandling);

        // Act
        List<ManglendeVedlegg> manglendeVedlegg = kompletthetsjekkerFP.utledAlleManglendeVedleggForForsendelse(behandling);

        // Assert
        assertThat(manglendeVedlegg).hasSize(2);
        List<DokumentTypeId> koder = manglendeVedlegg.stream().map(ManglendeVedlegg::getDokumentType).collect(Collectors.toList());
        assertThat(koder).containsExactlyInAnyOrder(DokumentTypeId.DOK_INNLEGGELSE, DokumentTypeId.INNTEKTSMELDING);
    }

    private void opprettSøknadMedPåkrevdVedlegg(Behandling behandling) {
        testUtil.lagreSøknad(behandling);
        Søknad søknad = new SøknadEntitet.Builder(søknadRepository.hentSøknad(behandling)).leggTilVedlegg(
            new SøknadVedleggEntitet.Builder()
                .medSkjemanummer(KODE_INNLEGGELSE)
                .medErPåkrevdISøknadsdialog(true)
                .build()).build();
        søknadRepository.lagreOgFlush(behandling, søknad);
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
