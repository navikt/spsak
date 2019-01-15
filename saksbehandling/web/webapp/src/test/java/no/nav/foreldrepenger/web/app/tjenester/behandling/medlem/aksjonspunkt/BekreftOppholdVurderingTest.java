package no.nav.foreldrepenger.web.app.tjenester.behandling.medlem.aksjonspunkt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.historikk.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskap;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.medlem.api.MedlemTjeneste;
import no.nav.foreldrepenger.domene.medlem.api.UtledVurderingsdatoerForMedlemskapTjeneste;
import no.nav.foreldrepenger.domene.medlem.api.VurderMedlemskapTjeneste;
import no.nav.foreldrepenger.domene.medlem.impl.HentMedlemskapFraRegister;
import no.nav.foreldrepenger.domene.medlem.impl.MedlemEndringssjekkerProvider;
import no.nav.foreldrepenger.domene.medlem.impl.MedlemskapTjenesteImpl;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.medlem.aksjonspunkt.BekreftOppholdOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.medlem.aksjonspunkt.BekreftOppholdVurderingDto.BekreftLovligOppholdVurderingDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.medlem.aksjonspunkt.BekreftOppholdVurderingDto.BekreftOppholdsrettVurderingDto;
import no.nav.vedtak.util.Tuple;

public class BekreftOppholdVurderingTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repositoryRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repositoryRule.getEntityManager());
    private final HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder();
    private PersonopplysningTjeneste personopplysningTjeneste = mock(PersonopplysningTjeneste.class);
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider, resultatRepositoryProvider);

    private LocalDate now = LocalDate.now();

    @Test
    public void bekreft_oppholdsrett_vurdering() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        scenario.medSøknad()
            .medSøknadsdato(now);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_OPPHOLDSRETT, BehandlingStegType.VURDER_MEDLEMSKAPVILKÅR);
        Behandling behandling = scenario.lagMocked();

        BekreftOppholdsrettVurderingDto dto = new BekreftOppholdsrettVurderingDto("test", true, true, true);

        // Act
        Tuple<GrunnlagRepositoryProvider, ResultatRepositoryProvider> providerTuple = scenario.mockBehandlingRepositoryProvider();
        GrunnlagRepositoryProvider repositoryProvider = providerTuple.getElement1();
        final MedlemTjeneste medlemskapTjeneste = new MedlemskapTjenesteImpl(mock(MedlemEndringssjekkerProvider.class), repositoryProvider,
            mock(HentMedlemskapFraRegister.class), providerTuple.getElement2().getMedlemskapVilkårPeriodeRepository(), skjæringstidspunktTjeneste,
            personopplysningTjeneste, mock(UtledVurderingsdatoerForMedlemskapTjeneste.class), mock(VurderMedlemskapTjeneste.class));
        new BekreftOppholdOppdaterer(repositoryProvider, lagMockHistory(), medlemskapTjeneste) {
        }
            .oppdater(dto, behandling, null);

        // Assert
        VurdertMedlemskap vurdertMedlemskap = getVurdertMedlemskap(behandling, repositoryProvider);
        assertThat(vurdertMedlemskap.getOppholdsrettVurdering()).isTrue();
    }

    private VurdertMedlemskap getVurdertMedlemskap(Behandling behandling, GrunnlagRepositoryProvider repositoryProvier) {
        MedlemskapRepository medlemskapRepository = repositoryProvier.getMedlemskapRepository();
        Optional<VurdertMedlemskap> vurdertMedlemskap = medlemskapRepository.hentVurdertMedlemskap(behandling);
        return vurdertMedlemskap.orElse(null);
    }

    @Test
    public void bekreft_lovlig_opphold_vurdering() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        scenario.medSøknad()
            .medSøknadsdato(now);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_LOVLIG_OPPHOLD, BehandlingStegType.VURDER_MEDLEMSKAPVILKÅR);

        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);

        BekreftLovligOppholdVurderingDto dto = new BekreftLovligOppholdVurderingDto("test", true, true, true);

        // Act
        final MedlemTjeneste medlemskapTjeneste = new MedlemskapTjenesteImpl(mock(MedlemEndringssjekkerProvider.class), repositoryProvider,
            mock(HentMedlemskapFraRegister.class), resultatRepositoryProvider.getMedlemskapVilkårPeriodeRepository(), skjæringstidspunktTjeneste,
            personopplysningTjeneste, mock(UtledVurderingsdatoerForMedlemskapTjeneste.class), mock(VurderMedlemskapTjeneste.class));
        new BekreftOppholdOppdaterer(repositoryProvider, lagMockHistory(), medlemskapTjeneste) {
        }
            .oppdater(dto, behandling, null);

        // Assert
        VurdertMedlemskap vurdertMedlemskap = getVurdertMedlemskap(behandling, repositoryProvider);
        assertThat(vurdertMedlemskap.getLovligOppholdVurdering()).isTrue();
    }

    private HistorikkTjenesteAdapter lagMockHistory() {
        HistorikkTjenesteAdapter mockHistory = Mockito.mock(HistorikkTjenesteAdapter.class);
        Mockito.when(mockHistory.tekstBuilder()).thenReturn(tekstBuilder);
        return mockHistory;
    }
}
