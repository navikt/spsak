package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
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
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.AvklarFortsattMedlemskapDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

public class AvklarFortsattMedlemskapOppdatererTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repositoryRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repositoryRule.getEntityManager());
    private LocalDate now = LocalDate.now();
    private HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder();
    private PersonopplysningTjeneste personopplysningTjeneste = mock(PersonopplysningTjeneste.class);

    @Test
    public void avklar_fortsatt_medlemskap() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        scenario.medSøknad()
            .medSøknadsdato(now);

        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_FORTSATT_MEDLEMSKAP, BehandlingStegType.VURDER_MEDLEMSKAPVILKÅR);

        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);

        AvklarFortsattMedlemskapDto dto = new AvklarFortsattMedlemskapDto("test", now);

        // Act
        final MedlemTjeneste medlemskapTjeneste = new MedlemskapTjenesteImpl(mock(MedlemEndringssjekkerProvider.class),
            repositoryProvider, mock(HentMedlemskapFraRegister.class), resultatRepositoryProvider.getMedlemskapVilkårPeriodeRepository(), mock(SkjæringstidspunktTjeneste.class), personopplysningTjeneste
            , mock(UtledVurderingsdatoerForMedlemskapTjeneste.class), mock(VurderMedlemskapTjeneste.class));
        new AvklarFortsattMedlemskapOppdaterer(medlemskapTjeneste , repositoryProvider.getAksjonspunktRepository(), lagMockHistory(), lagMockYtelseSkjæringstidspunktTjeneste(now))
            .oppdater(dto, behandling, null);

        getVurdertMedlemskap(behandling, repositoryProvider);
    }

    @Test
    public void avklar_fortsatt_medlemskap_endret_dato() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        scenario.medSøknad()
            .medSøknadsdato(now);

        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_FORTSATT_MEDLEMSKAP, BehandlingStegType.VURDER_MEDLEMSKAPVILKÅR);

        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);

        final MedlemTjeneste medlemskapTjeneste = new MedlemskapTjenesteImpl(mock(MedlemEndringssjekkerProvider.class),
            repositoryProvider, mock(HentMedlemskapFraRegister.class), resultatRepositoryProvider.getMedlemskapVilkårPeriodeRepository(), mock(SkjæringstidspunktTjeneste.class), personopplysningTjeneste
            , mock(UtledVurderingsdatoerForMedlemskapTjeneste.class), mock(VurderMedlemskapTjeneste.class));

        AvklarFortsattMedlemskapDto dto = new AvklarFortsattMedlemskapDto("test", now);
        new AvklarFortsattMedlemskapOppdaterer(medlemskapTjeneste , repositoryProvider.getAksjonspunktRepository(), lagMockHistory(), lagMockYtelseSkjæringstidspunktTjeneste(now))
            .oppdater(dto, behandling, null);

        getVurdertMedlemskap(behandling, repositoryProvider);

        // Act
        dto = new AvklarFortsattMedlemskapDto("test", now.minusDays(8));
        new AvklarFortsattMedlemskapOppdaterer(medlemskapTjeneste , repositoryProvider.getAksjonspunktRepository(), lagMockHistory(), lagMockYtelseSkjæringstidspunktTjeneste(now.minusDays(8)))
            .oppdater(dto, behandling, null);

        getVurdertMedlemskap(behandling, repositoryProvider);
    }

    private VurdertMedlemskap getVurdertMedlemskap(Behandling behandling, GrunnlagRepositoryProvider repositoryProvider) {
        MedlemskapRepository medlemskapRepository = repositoryProvider.getMedlemskapRepository();
        Optional<VurdertMedlemskap> vurdertMedlemskap = medlemskapRepository.hentVurdertMedlemskap(behandling);
        return vurdertMedlemskap.orElse(null);
    }

    private HistorikkTjenesteAdapter lagMockHistory() {
        HistorikkTjenesteAdapter mockHistory = Mockito.mock(HistorikkTjenesteAdapter.class);
        Mockito.when(mockHistory.tekstBuilder()).thenReturn(tekstBuilder);
        return mockHistory;
    }

    private SkjæringstidspunktTjeneste lagMockYtelseSkjæringstidspunktTjeneste(LocalDate fom){
        SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = Mockito.mock(SkjæringstidspunktTjeneste.class);
        Mockito.when(skjæringstidspunktTjeneste.utledSkjæringstidspunktForForeldrepenger(Mockito.any())).thenReturn(fom);
        return skjæringstidspunktTjeneste;
    }
}
