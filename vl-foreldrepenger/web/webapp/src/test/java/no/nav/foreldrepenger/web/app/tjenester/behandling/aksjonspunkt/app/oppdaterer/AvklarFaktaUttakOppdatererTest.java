package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.Collections;

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
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.InntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.UttakArbeidTjeneste;
import no.nav.foreldrepenger.domene.uttak.UttakArbeidTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakBeregningsandelTjeneste;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl.UttakBeregningsandelTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.KontrollerFaktaPeriode;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.KontrollerFaktaUttakTjeneste;
import no.nav.foreldrepenger.domene.ytelsefordeling.YtelseFordelingTjeneste;
import no.nav.foreldrepenger.domene.ytelsefordeling.impl.YtelseFordelingTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app.AvklarFaktaTestUtil;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app.FaktaUttakToTrinnsTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app.FaktaUttakToTrinnsTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app.FaktaUttakHistorikkTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app.FaktaUttakHistorikkTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app.KontrollerOppgittFordelingTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app.KontrollerOppgittFordelingTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.AvklarFaktaUttakDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.BekreftetUttakPeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.KontrollerFaktaPeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.SlettetUttakPeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

public class AvklarFaktaUttakOppdatererTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider behandlingRepositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private YtelseFordelingTjeneste ytelseFordelingTjeneste = new YtelseFordelingTjenesteImpl(behandlingRepositoryProvider);
    private KontrollerFaktaUttakTjeneste kontrollerFaktaUttakTjeneste = Mockito.mock(KontrollerFaktaUttakTjeneste.class);
    private HistorikkTjenesteAdapter historikkApplikasjonTjeneste = Mockito.mock(HistorikkTjenesteAdapter.class);
    private VirksomhetRepository virksomhetRepository = new VirksomhetRepositoryImpl(repositoryRule.getEntityManager());
    private InntektArbeidYtelseTjeneste iyaTjeneste = new InntektArbeidYtelseTjenesteImpl(behandlingRepositoryProvider,
        null, null, null, null, null);
    private UttakBeregningsandelTjeneste uttakBeregningsandelTjeneste = new UttakBeregningsandelTjenesteImpl(behandlingRepositoryProvider.getBeregningsgrunnlagRepository());
    private UttakArbeidTjeneste uttakArbeidTjeneste = new UttakArbeidTjenesteImpl(iyaTjeneste, uttakBeregningsandelTjeneste);
    private FaktaUttakHistorikkTjeneste faktaUttakHistorikkTjeneste = new FaktaUttakHistorikkTjenesteImpl(historikkApplikasjonTjeneste, behandlingRepositoryProvider, uttakArbeidTjeneste);
    private FaktaUttakToTrinnsTjeneste faktaUttakToTrinnsTjeneste = new FaktaUttakToTrinnsTjenesteImpl(behandlingRepositoryProvider);
    private KontrollerOppgittFordelingTjeneste kontrollerOppgittFordelingTjeneste = new KontrollerOppgittFordelingTjenesteImpl(ytelseFordelingTjeneste, behandlingRepositoryProvider,virksomhetRepository);

    @Test
    public void skal_generere_historikkinnslag_ved_slettet_søknadsperiode() {

        //Scenario med avklar fakta uttak
        ScenarioMorSøkerForeldrepenger scenario = AvklarFaktaTestUtil.opprettScenarioMorSøkerForeldrepenger();
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_FAKTA_UTTAK,
            BehandlingStegType.VURDER_UTTAK);
        scenario.lagre(behandlingRepositoryProvider);
        // Behandling
        Behandling behandling = AvklarFaktaTestUtil.opprettBehandling(scenario);

        // dto
        AvklarFaktaUttakDto dto = opprettDtoAvklarFaktaUttakDto();

        new AvklarFaktaUttakOppdaterer(behandlingRepositoryProvider, kontrollerFaktaUttakTjeneste, kontrollerOppgittFordelingTjeneste, faktaUttakHistorikkTjeneste, faktaUttakToTrinnsTjeneste).oppdater(dto, behandling, null);

        // Verifiserer HistorikkinnslagDto
        ArgumentCaptor<Historikkinnslag> historikkCapture = ArgumentCaptor.forClass(Historikkinnslag.class);
        verify(historikkApplikasjonTjeneste).lagInnslag(historikkCapture.capture());
        Historikkinnslag historikkinnslag = historikkCapture.getValue();
        assertThat(historikkinnslag.getType()).isEqualTo(HistorikkinnslagType.UTTAK);
        assertThat(historikkinnslag.getAktør()).isEqualTo(HistorikkAktør.SAKSBEHANDLER);
        HistorikkinnslagDel del = historikkinnslag.getHistorikkinnslagDeler().get(0);
        assertThat(del.getSkjermlenke()).as("skjermlenke").hasValueSatisfying(skjermlenke -> assertThat(skjermlenke).isEqualTo(SkjermlenkeType.FAKTA_OM_UTTAK.getKode()));
        assertThat(del.getAvklartSoeknadsperiode()).as("soeknadsperiode").hasValueSatisfying(soeknadsperiode -> assertThat(soeknadsperiode.getNavn()).as("navn").isEqualTo(HistorikkAvklartSoeknadsperiodeType.SLETTET_SOEKNASPERIODE.getKode()));
    }

    @Test
    public void skal_fjerne_overstyrings_aksjonspunkt_hvis_finnes() {

        //Scenario med både avklar fakta uttak og manuell avklar fakta uttak
        ScenarioMorSøkerForeldrepenger scenario = AvklarFaktaTestUtil.opprettScenarioMorSøkerForeldrepenger();
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.MANUELL_AVKLAR_FAKTA_UTTAK,
            BehandlingStegType.VURDER_UTTAK);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_FAKTA_UTTAK,
            BehandlingStegType.VURDER_UTTAK);
        scenario.lagre(behandlingRepositoryProvider);
        // Behandling
        Behandling behandling = AvklarFaktaTestUtil.opprettBehandling(scenario);

        // dto
        AvklarFaktaUttakDto dto = opprettDtoAvklarFaktaUttakDto();

        new AvklarFaktaUttakOppdaterer(behandlingRepositoryProvider, kontrollerFaktaUttakTjeneste, kontrollerOppgittFordelingTjeneste, faktaUttakHistorikkTjeneste, faktaUttakToTrinnsTjeneste).oppdater(dto, behandling, null);

        boolean ikkeFinnes = behandling.getAksjonspunkter().stream()
            .noneMatch(ap -> ap.getAksjonspunktDefinisjon().equals(AksjonspunktDefinisjon.MANUELL_AVKLAR_FAKTA_UTTAK));

        assertThat(ikkeFinnes).isTrue();

    }

    private AvklarFaktaUttakDto opprettDtoAvklarFaktaUttakDto() {
        AvklarFaktaUttakDto dto = new AvklarFaktaUttakDto.AvklarFaktaUttakPerioderDto();
        BekreftetUttakPeriodeDto bekreftetUttakPeriodeDto = getBekreftetUttakPeriodeDto(LocalDate.now().minusDays(20), LocalDate.now().minusDays(11));
        SlettetUttakPeriodeDto slettetPeriodeDto = new SlettetUttakPeriodeDto();
        slettetPeriodeDto.setBegrunnelse("ugyldig søknadsperiode");
        slettetPeriodeDto.setUttakPeriodeType(UttakPeriodeType.FORELDREPENGER);
        slettetPeriodeDto.setFom(LocalDate.now().minusDays(10));
        slettetPeriodeDto.setTom(LocalDate.now());
        dto.setSlettedePerioder(Collections.singletonList(slettetPeriodeDto));
        dto.setBekreftedePerioder(Collections.singletonList(bekreftetUttakPeriodeDto));
        return dto;
    }

    private BekreftetUttakPeriodeDto getBekreftetUttakPeriodeDto(LocalDate fom, LocalDate tom) {
        BekreftetUttakPeriodeDto bekreftetUttakPeriodeDto = new BekreftetUttakPeriodeDto();
        OppgittPeriode bekreftetperiode = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .build();
        bekreftetUttakPeriodeDto.setBekreftetPeriode(new KontrollerFaktaPeriodeDto(KontrollerFaktaPeriode.ubekreftet(bekreftetperiode)));
        bekreftetUttakPeriodeDto.setOrginalFom(fom);
        bekreftetUttakPeriodeDto.setOrginalTom(tom);
        return bekreftetUttakPeriodeDto;
    }
}
