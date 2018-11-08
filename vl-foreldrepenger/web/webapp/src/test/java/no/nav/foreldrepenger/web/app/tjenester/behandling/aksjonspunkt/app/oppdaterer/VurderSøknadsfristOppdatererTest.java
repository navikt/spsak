package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.behandling.søknadsfrist.VurderSøknadsfristAksjonspunktDto;
import no.nav.foreldrepenger.behandling.søknadsfrist.SøknadsfristForeldrepengerTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.Uttaksperiodegrense;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VurderSøknadsfristDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

public class VurderSøknadsfristOppdatererTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private SøknadsfristForeldrepengerTjeneste tjeneste;
    private HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder();
    private ScenarioMorSøkerForeldrepenger scenario;
    private BehandlingRepositoryProvider behandlingRepositoryProvider;
    private UttakRepository uttakRepository;
    private VurderSøknadsfristOppdaterer oppdaterer;


    private HistorikkTjenesteAdapter lagMockHistory() {
        HistorikkTjenesteAdapter mockHistory = mock(HistorikkTjenesteAdapter.class);
        when(mockHistory.tekstBuilder()).thenReturn(tekstBuilder);
        return mockHistory;
    }


    @Before
    public void oppsett() {
        scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.MANUELL_VURDERING_AV_SØKNADSFRIST_FORELDREPENGER,
            BehandlingStegType.SØKNADSFRIST_FORELDREPENGER);

        behandlingRepositoryProvider = scenario.mockBehandlingRepositoryProvider();
        uttakRepository = mock(UttakRepository.class);
        when(behandlingRepositoryProvider.getUttakRepository()).thenReturn(uttakRepository);

        tjeneste = mock(SøknadsfristForeldrepengerTjeneste.class);

        oppdaterer = new VurderSøknadsfristOppdaterer(tjeneste, lagMockHistory(), behandlingRepositoryProvider);
    }

    @Test
    public void oppdatererMottattDatoVedGyldigGrunn() {
        LocalDate mottattDato = LocalDate.now();
        String begrunnelse = "Begrunnelsen er god";

        Behandling behandling = lagBehandlingMedMottattDato(mottattDato);

        VurderSøknadsfristDto dto = new VurderSøknadsfristDto(begrunnelse, true);
        LocalDate nyMottatDato = mottattDato.minusWeeks(3);
        dto.setAnsesMottattDato(nyMottatDato);

        ArgumentCaptor<VurderSøknadsfristAksjonspunktDto> argumentCaptor = ArgumentCaptor.forClass(VurderSøknadsfristAksjonspunktDto.class);

        // Act
        oppdaterer.oppdater(dto, behandling);

        // Assert
        verify(tjeneste).lagreVurderSøknadsfristResultat(any(), argumentCaptor.capture());
        VurderSøknadsfristAksjonspunktDto aksjonspunktDto = argumentCaptor.getValue();
        assertThat(aksjonspunktDto.getMottattDato()).isEqualTo(nyMottatDato);
        assertThat(aksjonspunktDto.getBegrunnelse()).isEqualTo(begrunnelse);

        // Historikkinnslag skal ha to endrede felter
        assertThat(tekstBuilder.antallEndredeFelter()).isEqualTo(2);
    }


    @Test
    public void resetterMottattDatoVedEndringFraGyldigGrunnTilIkkeGyldigGrunn() {
        LocalDate mottattDato = LocalDate.now();
        String begrunnelse = "Begrunnelsen er god";

        Behandling behandling = lagBehandlingMedMottattDato(mottattDato);

        VurderSøknadsfristDto dto = new VurderSøknadsfristDto(begrunnelse, false);

        ArgumentCaptor<VurderSøknadsfristAksjonspunktDto> argumentCaptor = ArgumentCaptor.forClass(VurderSøknadsfristAksjonspunktDto.class);

        // Act
        oppdaterer.oppdater(dto, behandling);

        // Assert
        verify(tjeneste).lagreVurderSøknadsfristResultat(any(), argumentCaptor.capture());
        VurderSøknadsfristAksjonspunktDto aksjonspunktDto = argumentCaptor.getValue();
        assertThat(aksjonspunktDto.getMottattDato()).isEqualTo(mottattDato);
        assertThat(aksjonspunktDto.getBegrunnelse()).isEqualTo(begrunnelse);

        // Historikkinnslag skal ha ett endrede felter
        assertThat(tekstBuilder.antallEndredeFelter()).isEqualTo(1);
    }

    private Behandling lagBehandlingMedMottattDato(LocalDate mottattDato) {
        scenario.medSøknad().medMottattDato(mottattDato);
        Behandling behandling = scenario.lagMocked();

        Uttaksperiodegrense uttaksperiodegrense = new Uttaksperiodegrense.Builder(behandling)
            .medFørsteLovligeUttaksdag(mottattDato.minusMonths(3).withDayOfMonth(1))
            .medMottattDato(mottattDato).build();
        when(uttakRepository.hentUttaksperiodegrense(anyLong())).thenReturn(uttaksperiodegrense);
        return behandling;
    }
}
