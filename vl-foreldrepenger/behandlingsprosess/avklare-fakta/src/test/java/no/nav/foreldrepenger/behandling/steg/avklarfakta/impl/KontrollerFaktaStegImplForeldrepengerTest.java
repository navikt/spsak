package no.nav.foreldrepenger.behandling.steg.avklarfakta.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandling.steg.avklarfakta.api.KontrollerFaktaSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingModell;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.StartpunktRef;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.FarSøkerType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon.Builder;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.kontrollerfakta.KontrollerFaktaTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class KontrollerFaktaStegImplForeldrepengerTest {

    private static final LocalDate FØDSELSDATO_BARN = LocalDate.of(2017, Month.JANUARY, 1);

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private Behandling behandling;
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
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

    private ScenarioFarSøkerEngangsstønad byggBehandlingMedFarSøkerType(FarSøkerType farSøkerType) {
        AktørId aktørId = new AktørId("1");
        ScenarioFarSøkerEngangsstønad scenario = ScenarioFarSøkerEngangsstønad
            .forAdopsjon();
        scenario.medBruker(aktørId, NavBrukerKjønn.MANN);
        scenario.medSøknad()
            .medFarSøkerType(farSøkerType);
        scenario.medSøknadHendelse()
            .medFødselsDato(FØDSELSDATO_BARN);

        leggTilSøker(scenario, NavBrukerKjønn.MANN);

        return scenario;
    }

    @Before
    public void oppsett() {
        ScenarioFarSøkerEngangsstønad scenario = byggBehandlingMedFarSøkerType(FarSøkerType.ADOPTERER_ALENE);
        scenario.medBruker(new AktørId("123"), NavBrukerKjønn.MANN);
        behandling = scenario.lagre(repositoryProvider);
    }

    @Test
    public void skal_ved_overhopp_bakover_rydde_avklarte_fakta() {
        Fagsak fagsak = behandling.getFagsak();
        // Arrange
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(fagsak.getId(),fagsak.getAktørId(), lås);

        BehandlingStegModell stegModellMock = mock(BehandlingStegModell.class);
        BehandlingModell modellmock = mock(BehandlingModell.class);
        when(stegModellMock.getBehandlingModell()).thenReturn(modellmock);

        // Act
        steg.vedTransisjon(kontekst, behandling, stegModellMock, BehandlingSteg.TransisjonType.HOPP_OVER_BAKOVER, null, null, BehandlingSteg.TransisjonType.FØR_INNGANG);

        // Assert
        final Optional<MedlemskapAggregat> medlemskapAggregat = repositoryProvider.getMedlemskapRepository().hentMedlemskap(behandling);
        assertThat(medlemskapAggregat).isPresent();
        assertThat(medlemskapAggregat.flatMap(MedlemskapAggregat::getVurdertMedlemskap)).isNotPresent();
        behandling = behandlingRepository.hentBehandling(behandling.getId());

        assertThat(behandling.getBehandlingsresultat().getBeregningResultat()).isNull();
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
