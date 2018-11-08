package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.RegisterInnhentingIntervallEndringTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.aktør.FødtBarnInfo;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltVerdiType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkOpplysningType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagFelt;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.FarSøkerType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.familiehendelse.FamilieHendelseTjeneste;
import no.nav.foreldrepenger.domene.familiehendelse.impl.FamilieHendelseTjenesteImpl;
import no.nav.foreldrepenger.domene.person.TpsFamilieTjeneste;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.BeregnMorsMaksdatoTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.RelatertBehandlingTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftEktefelleAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.SjekkManglendeFodselDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;
import no.nav.vedtak.felles.jpa.TomtResultatException;

public class SjekkManglendeFødselOppdatererTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private final LocalDate now = LocalDate.now();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private final HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder();
    private final FamilieHendelseTjeneste hendelseTjeneste = new FamilieHendelseTjenesteImpl(null, 15, 6, repositoryProvider);

    private DateTimeFormatter formatterer = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider,
        new BeregnMorsMaksdatoTjenesteImpl(repositoryProvider, new RelatertBehandlingTjenesteImpl(repositoryProvider)),
        new RegisterInnhentingIntervallEndringTjeneste(Period.of(1, 0, 0), Period.of(0, 4, 0), Period.of(0, 6, 0), Period.of(1, 0, 0)),
        Period.of(0, 3, 0),
        Period.of(0, 10, 0));


    private TpsFamilieTjeneste tpsFamilieTjeneste = Mockito.mock(TpsFamilieTjeneste.class);

    @Test
    public void skal_generere_historikkinnslag_ved_avklaring_av_ektefelle() {
        // Arrange
        boolean oppdatertEktefellesBarn = true;

        // Behandling
        ScenarioFarSøkerEngangsstønad scenario = ScenarioFarSøkerEngangsstønad.forAdopsjon();
        scenario.medSøknadHendelse()
            .medAdopsjon(scenario.medSøknadHendelse().getAdopsjonBuilder()
                .medOmsorgsovertakelseDato(LocalDate.now()));

        scenario.medSøknad()
            .medFarSøkerType(FarSøkerType.ADOPTERER_ALENE);
        scenario.medBekreftetHendelse().medAdopsjon(scenario.medBekreftetHendelse().getAdopsjonBuilder()
            .medOmsorgsovertakelseDato(LocalDate.now())
            .medAdoptererAlene(true));
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN,
            BehandlingStegType.SØKERS_RELASJON_TIL_BARN);
        scenario.lagre(repositoryProvider);

        Behandling behandling = scenario.getBehandling();

        // Dto
        BekreftEktefelleAksjonspunktDto dto = new BekreftEktefelleAksjonspunktDto("begrunnelse", oppdatertEktefellesBarn);

        // Act
        new BekreftEktefelleOppdaterer(repositoryProvider, lagMockHistory(), hendelseTjeneste)
            .oppdater(dto, behandling);
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setType(HistorikkinnslagType.FAKTA_ENDRET);
        List<HistorikkinnslagDel> historikkInnslagDeler = this.tekstBuilder.build(historikkinnslag);

        // Assert
        Optional<HistorikkinnslagFelt> feltOpt = historikkInnslagDeler.get(0).getEndretFelt(HistorikkEndretFeltType.EKTEFELLES_BARN);
        assertThat(feltOpt).hasValueSatisfying(felt -> {
            assertThat(felt.getNavn()).isEqualTo(HistorikkEndretFeltType.EKTEFELLES_BARN.getKode());
            assertThat(felt.getFraVerdi()).isNull();
            assertThat(felt.getTilVerdi()).isEqualTo(HistorikkEndretFeltVerdiType.EKTEFELLES_BARN.getKode());
        });
    }

    @Test
    public void skal_generere_historikkinnslag_ved_avklaring_av_antall_barn() {
        // Arrange
        int antallBarnOpprinnelig = 2;
        final boolean antallBarnTpsGjelderBekreftet = false;

        LocalDate fødselsdatoFraTps = LocalDate.now().minusDays(1);
        LocalDate fødselsDatoFraSøknad = fødselsdatoFraTps.minusDays(10);

        // Behandling
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medSøknadHendelse()
            .medFødselsDato(fødselsDatoFraSøknad)
            .medAntallBarn(2);
        scenario.medBekreftetHendelse()
            .leggTilBarn(fødselsdatoFraTps)
            .leggTilBarn(fødselsdatoFraTps)
            .leggTilBarn(fødselsdatoFraTps)
            .medAntallBarn(antallBarnOpprinnelig);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL, BehandlingStegType.SØKERS_RELASJON_TIL_BARN);
        final Behandling behandling = scenario.lagre(repositoryProvider);

        // Dto
        SjekkManglendeFodselDto dto = new SjekkManglendeFodselDto("begrunnelse",
            true, antallBarnTpsGjelderBekreftet, fødselsDatoFraSøknad, 2);
        // Act

        new SjekkManglendeFødselOppdaterer(repositoryProvider, lagMockHistory(), skjæringstidspunktTjeneste,
            hendelseTjeneste)
            .oppdater(dto, behandling);
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setType(HistorikkinnslagType.FAKTA_ENDRET);
        List<HistorikkinnslagDel> historikkInnslagDeler = this.tekstBuilder.build(historikkinnslag);

        // Assert
        assertFelt(historikkInnslagDeler.get(0), HistorikkEndretFeltType.BRUK_ANTALL_I_SOKNAD, null, true);
    }

    @Test
    public void skal_generere_historikkinnslag_ved_avklaring_av_fødsel() {
        // Arrange
        LocalDate opprinneligFødseldato = LocalDate.now();
        LocalDate avklartFødseldato = opprinneligFødseldato.plusDays(1);
        int opprinneligAntallBarn = 1;
        int avklartAntallBarn = 2;

        // Behandling
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medSøknadHendelse()
            .medFødselsDato(opprinneligFødseldato)
            .medAntallBarn(opprinneligAntallBarn);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL, BehandlingStegType.SØKERS_RELASJON_TIL_BARN);
        scenario.lagre(repositoryProvider);

        Behandling behandling = scenario.getBehandling();

        when(tpsFamilieTjeneste.getFødslerRelatertTilBehandling(any(), any())).thenReturn(new ArrayList<>());

        // Dto
        SjekkManglendeFodselDto dto = new SjekkManglendeFodselDto("begrunnelse", true, false, avklartFødseldato, avklartAntallBarn);

        // Act
        new SjekkManglendeFødselOppdaterer(repositoryProvider, lagMockHistory(), skjæringstidspunktTjeneste,
            hendelseTjeneste)
            .oppdater(dto, behandling);
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setType(HistorikkinnslagType.FAKTA_ENDRET);
        List<HistorikkinnslagDel> historikkInnslagDeler = this.tekstBuilder.build(historikkinnslag);

        // Assert
        HistorikkinnslagDel del = historikkInnslagDeler.get(0);

        assertFelt(del, HistorikkEndretFeltType.FODSELSDATO, formatterer.format(opprinneligFødseldato), formatterer.format(avklartFødseldato));
        assertFelt(del, HistorikkEndretFeltType.ANTALL_BARN, opprinneligAntallBarn, avklartAntallBarn);
        assertFelt(del, HistorikkEndretFeltType.FODSELSDATO, formatterer.format(opprinneligFødseldato), formatterer.format(avklartFødseldato));

        Optional<HistorikkinnslagFelt> opplysningOpt = del.getOpplysning(HistorikkOpplysningType.ANTALL_BARN);
        assertThat(opplysningOpt).as("opplysningOpt").hasValueSatisfying(opplysning -> {
            assertThat(opplysning.getNavn()).isEqualTo(HistorikkOpplysningType.ANTALL_BARN.getKode());
            assertThat(opplysning.getTilVerdi()).isEqualTo(Integer.toString(avklartAntallBarn));
        });
    }

    @Test
    public void skal_oppdatere_fødsel() {
        // Arrange
        ScenarioFarSøkerEngangsstønad scenario = ScenarioFarSøkerEngangsstønad.forFødsel();
        scenario.medSøknad()
            .medSøknadsdato(now);
        scenario.medSøknadHendelse()
            .medFødselsDato(now.minusDays(3))
            .medAntallBarn(1);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL, BehandlingStegType.SØKERS_RELASJON_TIL_BARN);

        Behandling behandling = scenario.lagre(repositoryProvider);

        SjekkManglendeFodselDto dto = new SjekkManglendeFodselDto("Begrunnelse",
            true, false, now.minusDays(6), 1);

        when(tpsFamilieTjeneste.getFødslerRelatertTilBehandling(any(), any())).thenReturn(new ArrayList<>());

        // Act
        final FamilieHendelseTjeneste hendelseTjeneste = new FamilieHendelseTjenesteImpl(null, 14, 6, repositoryProvider);
        new SjekkManglendeFødselOppdaterer(repositoryProvider, lagMockHistory(), skjæringstidspunktTjeneste,
            hendelseTjeneste)
            .oppdater(dto, behandling);

        // Assert
        final FamilieHendelse hendelse = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling).getGjeldendeVersjon();
        assertThat(hendelse).isNotNull();
        assertThat(hendelse.getAntallBarn()).isEqualTo(1);
        Optional<LocalDate> fodselsdatoOpt = hendelse.getBarna().stream().map(UidentifisertBarn::getFødselsdato).findFirst();
        assertThat(fodselsdatoOpt).as("fodselsdatoOpt").hasValueSatisfying(fodselsdato -> assertThat(fodselsdato).as("fodselsdato").isEqualTo(now.minusDays(6)));
    }

    @Test
    public void skal_oppdatere_antall_barn_basert_på_saksbehandlers_oppgitte_antall() {
        // Arrange
        ScenarioFarSøkerEngangsstønad scenario = ScenarioFarSøkerEngangsstønad.forFødsel();
        scenario.medSøknad()
            .medSøknadsdato(now);
        scenario.medSøknadHendelse()
            .medFødselsDato(now.minusDays(3))
            .medAntallBarn(2);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL, BehandlingStegType.SØKERS_RELASJON_TIL_BARN);

        Behandling behandling = scenario.lagre(repositoryProvider);

        SjekkManglendeFodselDto dto = new SjekkManglendeFodselDto("Begrunnelse",
            true, false, now.minusDays(3), 1);
        when(tpsFamilieTjeneste.getFødslerRelatertTilBehandling(any(), any())).thenReturn(new ArrayList<>());

        // Act
        final FamilieHendelseTjeneste hendelseTjeneste = new FamilieHendelseTjenesteImpl(null, 14, 6, repositoryProvider);
        new SjekkManglendeFødselOppdaterer(repositoryProvider, lagMockHistory(), skjæringstidspunktTjeneste,
            hendelseTjeneste)
            .oppdater(dto, behandling);

        // Assert
        final FamilieHendelse hendelse = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling).getGjeldendeVersjon();
        assertThat(hendelse).isNotNull();
        assertThat(hendelse.getAntallBarn()).isEqualTo(1);
        Optional<LocalDate> fodselsdatoOpt = hendelse.getBarna().stream().map(UidentifisertBarn::getFødselsdato).findFirst();
        assertThat(fodselsdatoOpt).as("fodselsdatoOpt").hasValueSatisfying(fodselsdato ->
            assertThat(fodselsdato).as("fodselsdato").isEqualTo(now.minusDays(3)));
    }

    @Test
    public void skal_oppdatere_antall_barn_basert_på_tps_dersom_flagg_satt() {
        // Arrange
        LocalDate fødselsdatoFraSøknad = now;
        LocalDate fødselsdatoFraTps = now.minusDays(1);

        ScenarioFarSøkerEngangsstønad scenario = ScenarioFarSøkerEngangsstønad.forFødsel();
        scenario.medSøknad()
            .medSøknadsdato(now);
        scenario.medSøknadHendelse()
            .medAntallBarn(2)
            .medFødselsDato(fødselsdatoFraSøknad);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL, BehandlingStegType.SØKERS_RELASJON_TIL_BARN);

        scenario.medBekreftetHendelse()
            .medAntallBarn(3)
            .medFødselsDato(fødselsdatoFraTps);
        Behandling behandling = scenario.lagre(repositoryProvider);

        SjekkManglendeFodselDto dto = new SjekkManglendeFodselDto("Begrunnelse",
            true, true, null, 0);

        // Act
        final FamilieHendelseTjeneste hendelseTjeneste = new FamilieHendelseTjenesteImpl(null, 14, 6, repositoryProvider);
        new SjekkManglendeFødselOppdaterer(repositoryProvider, lagMockHistory(), skjæringstidspunktTjeneste,
            hendelseTjeneste)
            .oppdater(dto, behandling);

        // Assert
        final FamilieHendelse hendelse = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling).getGjeldendeVersjon();
        assertThat(hendelse).isNotNull();
        assertThat(hendelse.getAntallBarn()).isEqualTo(3);
        Optional<LocalDate> fodselsdatoOpt = hendelse.getBarna().stream().map(UidentifisertBarn::getFødselsdato).findFirst();
        assertThat(fodselsdatoOpt).as("fodselsdatoOpt").hasValueSatisfying(fodselsdato ->
            assertThat(fodselsdato).as("fodselsdato").isEqualTo(fødselsdatoFraTps));
    }

    @Test
    public void skal_hive_exception_når_dokumentasjon_foreligger_og_fødselsdato_er_tom() {
        // Arrange
        LocalDate fødselsdatoFraSøknad = now;

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medSøknad()
            .medSøknadsdato(now);
        scenario.medSøknadHendelse()
            .medAntallBarn(1)
            .medFødselsDato(fødselsdatoFraSøknad);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL, BehandlingStegType.SØKERS_RELASJON_TIL_BARN);

        Behandling behandling = scenario.lagre(repositoryProvider);

        SjekkManglendeFodselDto dto = new SjekkManglendeFodselDto("Begrunnelse",
            true, false, null, 1);

        try {
            new SjekkManglendeFødselOppdaterer(repositoryProvider, lagMockHistory(), skjæringstidspunktTjeneste,
                hendelseTjeneste)
                .oppdater(dto, behandling);
            fail("expected exception to be thrown");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(TomtResultatException.class);
        }
    }

    private void assertFelt(HistorikkinnslagDel historikkinnslagDel, HistorikkEndretFeltType historikkEndretFeltType, Object fraVerdi, Object tilVerdi) {
        Optional<HistorikkinnslagFelt> feltOpt = historikkinnslagDel.getEndretFelt(historikkEndretFeltType);
        assertThat(feltOpt).hasValueSatisfying(felt -> {
            assertThat(felt.getNavn()).as(historikkEndretFeltType + ".navn").isEqualTo(historikkEndretFeltType.getKode());
            assertThat(felt.getFraVerdi()).as(historikkEndretFeltType + ".fraVerdi").isEqualTo(fraVerdi != null ? fraVerdi.toString() : null);
            assertThat(felt.getTilVerdi()).as(historikkEndretFeltType + ".tilVerdi").isEqualTo(tilVerdi.toString());
        });
    }

    private void settOppBarnRepositoryMed3Barn(LocalDate fødselsdato) {
        List<FødtBarnInfo> listeAvBarn = new ArrayList<>();

        listeAvBarn.add(byggBaby(fødselsdato));
        listeAvBarn.add(byggBaby(fødselsdato));
        listeAvBarn.add(byggBaby(fødselsdato));
        when(tpsFamilieTjeneste.getFødslerRelatertTilBehandling(any(), any())).thenReturn(listeAvBarn);
    }

    private FødtBarnInfo byggBaby(LocalDate fødselsdato) {
        return new FødtBarnInfo.Builder()
            .medFødselsdato(fødselsdato)
            .medIdent(PersonIdent.fra("19010100000"))
            .medNavn("barn")
            .medNavBrukerKjønn(NavBrukerKjønn.MANN)
            .build();
    }

    private HistorikkTjenesteAdapter lagMockHistory() {
        HistorikkTjenesteAdapter mockHistory = Mockito.mock(HistorikkTjenesteAdapter.class);
        Mockito.when(mockHistory.tekstBuilder()).thenReturn(tekstBuilder);
        return mockHistory;
    }

    protected Aksjonspunkt leggTilAksjonspunkt(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        AksjonspunktRepository aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        return aksjonspunktRepository.leggTilAksjonspunkt(behandling, aksjonspunktDefinisjon,
            BehandlingStegType.KONTROLLER_FAKTA);
    }

}
