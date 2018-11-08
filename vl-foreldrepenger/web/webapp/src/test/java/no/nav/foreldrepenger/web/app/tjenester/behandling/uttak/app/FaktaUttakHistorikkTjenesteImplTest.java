package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAvklartSoeknadsperiodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.InntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.UttakArbeidTjeneste;
import no.nav.foreldrepenger.domene.uttak.UttakArbeidTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakBeregningsandelTjeneste;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl.UttakBeregningsandelTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.AvklarFaktaUttakDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.ManuellAvklarFaktaUttakDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

public class FaktaUttakHistorikkTjenesteImplTest {
    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider behandlingRepositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private HistorikkTjenesteAdapter historikkApplikasjonTjeneste = Mockito.mock(HistorikkTjenesteAdapter.class);
    private InntektArbeidYtelseTjeneste iyaTjeneste = new InntektArbeidYtelseTjenesteImpl(behandlingRepositoryProvider,
        null, null, null, null, null);
    private UttakBeregningsandelTjeneste uttakBeregningsandelTjeneste = new UttakBeregningsandelTjenesteImpl(behandlingRepositoryProvider.getBeregningsgrunnlagRepository());
    private UttakArbeidTjeneste uttakArbeidTjeneste = new UttakArbeidTjenesteImpl(iyaTjeneste, uttakBeregningsandelTjeneste);

    @Test
    public void skal_generere_historikkinnslag_ved_ny_søknadsperiode_avklar_fakta() {

        //Scenario med avklar fakta uttak
        ScenarioMorSøkerForeldrepenger scenario = AvklarFaktaTestUtil.opprettScenarioMorSøkerForeldrepenger();
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_FAKTA_UTTAK,
            BehandlingStegType.VURDER_UTTAK);
        scenario.lagre(behandlingRepositoryProvider);
        // Behandling
        Behandling behandling = AvklarFaktaTestUtil.opprettBehandling(scenario);

        // dto
        AvklarFaktaUttakDto dto = AvklarFaktaTestUtil.opprettDtoAvklarFaktaUttakDto();

        new FaktaUttakHistorikkTjenesteImpl(historikkApplikasjonTjeneste, behandlingRepositoryProvider, uttakArbeidTjeneste).byggHistorikkinnslagForAvklarFakta(dto, behandling);

        // Verifiserer HistorikkinnslagDto
        ArgumentCaptor<Historikkinnslag> historikkCapture = ArgumentCaptor.forClass(Historikkinnslag.class);
        verify(historikkApplikasjonTjeneste).lagInnslag(historikkCapture.capture());
        Historikkinnslag historikkinnslag = historikkCapture.getValue();
        assertThat(historikkinnslag.getType()).isEqualTo(HistorikkinnslagType.UTTAK);
        assertThat(historikkinnslag.getAktør()).isEqualTo(HistorikkAktør.SAKSBEHANDLER);
        HistorikkinnslagDel del = historikkinnslag.getHistorikkinnslagDeler().get(0);
        assertThat(del.getSkjermlenke()).as("skjermlenke").hasValueSatisfying(skjermlenke -> assertThat(skjermlenke).isEqualTo(SkjermlenkeType.FAKTA_OM_UTTAK.getKode()));
        assertThat(del.getAvklartSoeknadsperiode()).as("soeknadsperiode").hasValueSatisfying(soeknadsperiode -> assertThat(soeknadsperiode.getNavn()).as("navn").isEqualTo(HistorikkAvklartSoeknadsperiodeType.NY_SOEKNADSPERIODE.getKode()));
    }

    @Test
    public void skal_generere_historikkinnslag_ved_ny_søknadsperiode_manuell_avklar_fakta() {

        //Scenario med manuell avklar fakta uttak
        ScenarioMorSøkerForeldrepenger scenario = AvklarFaktaTestUtil.opprettScenarioMorSøkerForeldrepenger();
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.MANUELL_AVKLAR_FAKTA_UTTAK,
            BehandlingStegType.VURDER_UTTAK);
        scenario.lagre(behandlingRepositoryProvider);
        // Behandling
        Behandling behandling = AvklarFaktaTestUtil.opprettBehandling(scenario);

        // dto
        ManuellAvklarFaktaUttakDto dto = AvklarFaktaTestUtil.opprettDtoManuellAvklarFaktaUttakDto();

        new FaktaUttakHistorikkTjenesteImpl(historikkApplikasjonTjeneste, behandlingRepositoryProvider, uttakArbeidTjeneste).byggHistorikkinnslagForManuellAvklarFakta(dto, behandling);

        // Verifiserer HistorikkinnslagDto
        ArgumentCaptor<Historikkinnslag> historikkCapture = ArgumentCaptor.forClass(Historikkinnslag.class);
        verify(historikkApplikasjonTjeneste).lagInnslag(historikkCapture.capture());
        Historikkinnslag historikkinnslag = historikkCapture.getValue();
        assertThat(historikkinnslag.getType()).isEqualTo(HistorikkinnslagType.UTTAK);
        assertThat(historikkinnslag.getAktør()).isEqualTo(HistorikkAktør.SAKSBEHANDLER);
        HistorikkinnslagDel del = historikkinnslag.getHistorikkinnslagDeler().get(0);
        assertThat(del.getSkjermlenke()).as("skjermlenke").hasValueSatisfying(skjermlenke -> assertThat(skjermlenke).isEqualTo(SkjermlenkeType.FAKTA_OM_UTTAK.getKode()));
        assertThat(del.getAvklartSoeknadsperiode()).as("soeknadsperiode").hasValueSatisfying(soeknadsperiode -> assertThat(soeknadsperiode.getNavn()).as("navn").isEqualTo(HistorikkAvklartSoeknadsperiodeType.NY_SOEKNADSPERIODE.getKode()));
    }

}
