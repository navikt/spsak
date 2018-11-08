package no.nav.foreldrepenger.domene.kontrollerfakta.adopsjon;

import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_ADOPSJONSDOKUMENTAJON;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlagBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;

public class AksjonspunktUtlederForForeldrepengerAdopsjonTest {

    @Mock
    private Behandling behandlingMock;
    @Mock
    private Fagsak fagsakMock;
    @Mock
    private BehandlingRepositoryProvider behandlingRepositoryProvider;
    @Mock
    private FamilieHendelseRepository familieHendelseRepositoryMock;

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private AksjonspunktUtlederForForeldrepengerAdopsjon utleder;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ScenarioMorSøkerForeldrepenger morSøkerAdopsjonScenario = ScenarioMorSøkerForeldrepenger.forAdopsjon();

        Mockito.when(behandlingRepositoryProvider.getFamilieGrunnlagRepository()).thenReturn(familieHendelseRepositoryMock);
        utleder = new AksjonspunktUtlederForForeldrepengerAdopsjon(behandlingRepositoryProvider);
        Mockito.when(behandlingMock.getFagsak()).thenReturn(fagsakMock);

        FamilieHendelseBuilder familieHendelseBuilder = morSøkerAdopsjonScenario.medSøknadHendelse();

        morSøkerAdopsjonScenario.medSøknadHendelse().medAdopsjon(morSøkerAdopsjonScenario.medSøknadHendelse().getAdopsjonBuilder());
        FamilieHendelseGrunnlag familieHendelseAggregat = FamilieHendelseGrunnlagBuilder.oppdatere(Optional.empty())
            .medSøknadVersjon(familieHendelseBuilder).build();
        Mockito.when(familieHendelseRepositoryMock.hentAggregat(any())).thenReturn(familieHendelseAggregat);
    }

    @Test
    public void skal_utlede_aksjonspunkt_basert_på_fakta_om_fp_til_mor() {
        List<AksjonspunktResultat> aksjonspunkter = aksjonspunktForFaktaForMor();

        assertThat(aksjonspunkter).hasSize(2);
        assertThat(aksjonspunkter.stream().map(e -> e.getAksjonspunktDefinisjon()).collect(Collectors.toList()))
            .containsExactlyInAnyOrder(AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN, AVKLAR_ADOPSJONSDOKUMENTAJON);
    }

    @Test
    public void skal_utlede_aksjonspunkt_basert_på_fakta_om_fp_til_far() {
        List<AksjonspunktResultat> aksjonspunkter = aksjonspunktForFaktaForFar();

        assertThat(aksjonspunkter).hasSize(2);
        assertThat(aksjonspunkter.stream().map(e -> e.getAksjonspunktDefinisjon()).collect(Collectors.toList()))
            .containsExactlyInAnyOrder(AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN, AVKLAR_ADOPSJONSDOKUMENTAJON);
    }

    @Test
    public void skal_utlede_aksjonspunkt_basert_på_fakta_om_fp_til_medmor() {
        List<AksjonspunktResultat> aksjonspunkter = aksjonspunktForFaktaForMedMor();

        assertThat(aksjonspunkter).hasSize(2);
        assertThat(aksjonspunkter.stream().map(e -> e.getAksjonspunktDefinisjon()).collect(Collectors.toList()))
            .containsExactlyInAnyOrder(AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN, AVKLAR_ADOPSJONSDOKUMENTAJON);
    }

    private List<AksjonspunktResultat> aksjonspunktForFaktaForMor() {
        Mockito.when(fagsakMock.getRelasjonsRolleType()).thenReturn(RelasjonsRolleType.MORA);
        return utleder.utledAksjonspunkterFor(behandlingMock);
    }

    private List<AksjonspunktResultat> aksjonspunktForFaktaForFar() {
        Mockito.when(fagsakMock.getRelasjonsRolleType()).thenReturn(RelasjonsRolleType.FARA);
        return utleder.utledAksjonspunkterFor(behandlingMock);
    }

    private List<AksjonspunktResultat> aksjonspunktForFaktaForMedMor() {
        Mockito.when(fagsakMock.getRelasjonsRolleType()).thenReturn(RelasjonsRolleType.MEDMOR);
        return utleder.utledAksjonspunkterFor(behandlingMock);
    }

}
