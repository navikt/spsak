package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapManuellVurderingType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskap;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.medlem.api.MedlemTjeneste;
import no.nav.foreldrepenger.domene.medlem.api.UtledVurderingsdatoerForMedlemskapTjeneste;
import no.nav.foreldrepenger.domene.medlem.api.VurderMedlemskapTjeneste;
import no.nav.foreldrepenger.domene.medlem.impl.HentMedlemskapFraRegister;
import no.nav.foreldrepenger.domene.medlem.impl.MedlemEndringssjekkerProvider;
import no.nav.foreldrepenger.domene.medlem.impl.MedlemskapTjenesteImpl;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftErMedlemVurderingDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

public class BekreftErMedlemOppdatererTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private final HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder();
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider, Period.of(0, 10, 0));
    private LocalDate now = LocalDate.now();

    @Test
    public void bekreft_er_medlem_vurdering() {
        // Arrange
        ScenarioFarSøkerEngangsstønad scenario = ScenarioFarSøkerEngangsstønad.forFødsel();
        scenario.medSøknad()
            .medSøknadsdato(now);

        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_GYLDIG_MEDLEMSKAPSPERIODE, BehandlingStegType.VURDER_MEDLEMSKAPVILKÅR);

        Behandling behandling = scenario.lagre(repositoryProvider);

        BekreftErMedlemVurderingDto dto = new BekreftErMedlemVurderingDto("test", MedlemskapManuellVurderingType.MEDLEM);

        // Act
        final MedlemTjeneste medlemskapTjeneste = new MedlemskapTjenesteImpl(mock(MedlemEndringssjekkerProvider.class),
            repositoryProvider, mock(HentMedlemskapFraRegister.class), repositoryProvider.getMedlemskapVilkårPeriodeRepository(), skjæringstidspunktTjeneste, mock(PersonopplysningTjeneste.class), mock(UtledVurderingsdatoerForMedlemskapTjeneste.class), mock(VurderMedlemskapTjeneste.class));
        new BekreftErMedlemVurderingOppdaterer(repositoryProvider, lagMockHistory(), medlemskapTjeneste)
        .oppdater(dto, behandling, null);

        // Assert
        VurdertMedlemskap vurdertMedlemskap = getVurdertMedlemskap(behandling, repositoryProvider);
        assertThat(vurdertMedlemskap.getMedlemsperiodeManuellVurdering())
            .isEqualTo(MedlemskapManuellVurderingType.MEDLEM);
    }


    private HistorikkTjenesteAdapter lagMockHistory() {
        HistorikkTjenesteAdapter mockHistory = Mockito.mock(HistorikkTjenesteAdapter.class);
        Mockito.when(mockHistory.tekstBuilder()).thenReturn(tekstBuilder);
        return mockHistory;
    }

    private VurdertMedlemskap getVurdertMedlemskap(Behandling behandling, BehandlingRepositoryProvider repositoryProvider) {
        MedlemskapRepository medlemskapRepository = repositoryProvider.getMedlemskapRepository();
        Optional<VurdertMedlemskap> vurdertMedlemskap = medlemskapRepository.hentVurdertMedlemskap(behandling);
        return vurdertMedlemskap.orElse(null);
    }
}
