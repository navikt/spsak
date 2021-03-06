package no.nav.foreldrepenger.behandling.steg.avklarfakta.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandling.steg.avklarfakta.api.KontrollerFaktaSteg;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsResultat;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon.Builder;
import no.nav.foreldrepenger.domene.kontrollerfakta.KontrollerFaktaTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class KontrollerFaktaStegImplTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private Behandling behandling;
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repositoryRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repositoryRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    @Inject
    @FagsakYtelseTypeRef("FP")
    @BehandlingTypeRef
    @StartpunktRef
    private KontrollerFaktaTjeneste kontrollerFaktaTjeneste;

    @Inject
    @FagsakYtelseTypeRef("FP")
    @BehandlingTypeRef
    @StartpunktRef
    private KontrollerFaktaSteg steg;

    private ScenarioMorSøkerForeldrepenger byggBehandlingMedFarSøkerType() {
        AktørId aktørId = new AktørId("1");
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        scenario.medBruker(aktørId, NavBrukerKjønn.MANN);
        leggTilSøker(scenario, NavBrukerKjønn.MANN);

        return scenario;
    }

    @Before
    public void oppsett() {
        ScenarioMorSøkerForeldrepenger scenario = byggBehandlingMedFarSøkerType();
        scenario.medBruker(new AktørId("123"), NavBrukerKjønn.MANN);
        behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
    }

    @Test
    public void skal_ved_overhopp_bakover_rydde_avklarte_fakta() {
        Fagsak fagsak = behandling.getFagsak();
        // Arrange
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(fagsak.getId(),fagsak.getAktørId(), lås);

        BehandlingStegModell stegModellMock = mock(BehandlingStegModell.class);
        BehandlingModellImpl modellmock = mock(BehandlingModellImpl.class);
        when(stegModellMock.getBehandlingModell()).thenReturn(modellmock);

        // Act
        steg.vedTransisjon(kontekst, behandling, stegModellMock, BehandlingSteg.TransisjonType.HOPP_OVER_BAKOVER, null, null, BehandlingSteg.TransisjonType.FØR_INNGANG);

        // Assert
        final Optional<MedlemskapAggregat> medlemskapAggregat = repositoryProvider.getMedlemskapRepository().hentMedlemskap(behandling);
        assertThat(medlemskapAggregat).isPresent();
        assertThat(medlemskapAggregat.flatMap(MedlemskapAggregat::getVurdertMedlemskap)).isNotPresent();
        behandling = behandlingRepository.hentBehandling(behandling.getId());
        Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(behandling.getId());
        BeregningsresultatRepository beregningsresultatRepository = resultatRepositoryProvider.getBeregningsresultatRepository();
        Optional<BeregningsResultat> beregningsResultat = beregningsresultatRepository.hentHvisEksistererFor(behandlingsresultat);

        assertThat(beregningsResultat).isNotPresent();
    }

    private void leggTilSøker(AbstractTestScenario<?> scenario, NavBrukerKjønn kjønn) {
        Builder builderForRegisteropplysninger = scenario.opprettBuilderForRegisteropplysninger();
        AktørId søkerAktørId = scenario.getDefaultBrukerAktørId();
        PersonInformasjon søker = builderForRegisteropplysninger
            .medPersonas()
            .voksenPerson(søkerAktørId, SivilstandType.UOPPGITT, kjønn, Region.UDEFINERT)
            .build();
        scenario.medRegisterOpplysninger(søker);
    }
}
