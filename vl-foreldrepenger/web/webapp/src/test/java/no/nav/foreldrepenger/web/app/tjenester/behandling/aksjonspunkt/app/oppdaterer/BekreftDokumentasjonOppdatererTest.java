package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.RegisterInnhentingIntervallEndringTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.UidentifisertBarnEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagFelt;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.FarSøkerType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.familiehendelse.FamilieHendelseTjeneste;
import no.nav.foreldrepenger.domene.familiehendelse.impl.FamilieHendelseTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.BeregnMorsMaksdatoTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.RelatertBehandlingTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse.BekreftDokumentertDatoAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

public class BekreftDokumentasjonOppdatererTest {
    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private DateTimeFormatter formatterer = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private final HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder();
    private final SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider,
        new BeregnMorsMaksdatoTjenesteImpl(repositoryProvider, new RelatertBehandlingTjenesteImpl(repositoryProvider)),
        new RegisterInnhentingIntervallEndringTjeneste(Period.of(1, 0, 0), Period.of(0, 4, 0)),
        Period.of(0, 3, 0),
        Period.of(0, 10, 0));
    private final FamilieHendelseTjeneste hendelseTjeneste = new FamilieHendelseTjenesteImpl(null, 15, 6, repositoryProvider);

    @Test
    public void skal_generere_historikkinnslag_ved_avklaring_av_dokumentert_adopsjonsdato() {
        // Arrange
        LocalDate opprinneligOvertakelsesdato = LocalDate.now();
        LocalDate bekreftetOvertakelsesdato = opprinneligOvertakelsesdato.plusDays(1);
        LocalDate opprinneligFødselsdato = LocalDate.now().plusDays(30);
        LocalDate bekreftetFødselsdato = opprinneligFødselsdato.plusDays(1);

        // Behandling
        ScenarioFarSøkerEngangsstønad scenario = ScenarioFarSøkerEngangsstønad.forAdopsjon();
        scenario.medSøknad().medFarSøkerType(FarSøkerType.ADOPTERER_ALENE);
        scenario.medSøknadHendelse()
            .medAdopsjon(scenario.medSøknadHendelse().getAdopsjonBuilder()
                .medOmsorgsovertakelseDato(opprinneligOvertakelsesdato))
            .leggTilBarn(new UidentifisertBarnEntitet(opprinneligFødselsdato));
        scenario.medBekreftetHendelse().medAdopsjon(scenario.medBekreftetHendelse().getAdopsjonBuilder()
            .medOmsorgsovertakelseDato(opprinneligOvertakelsesdato)).leggTilBarn(opprinneligFødselsdato);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_ADOPSJONSDOKUMENTAJON, BehandlingStegType.SØKERS_RELASJON_TIL_BARN);
        scenario.lagre(repositoryProvider);

        Behandling behandling = scenario.getBehandling();

        // Dto
        Map<Integer, LocalDate> bekreftedeFødselsdatoer = new HashMap<>();
        bekreftedeFødselsdatoer.put(1, bekreftetFødselsdato);
        BekreftDokumentertDatoAksjonspunktDto dto = new BekreftDokumentertDatoAksjonspunktDto("begrunnelse", bekreftetOvertakelsesdato,
            bekreftedeFødselsdatoer);

        // Act
        new BekreftDokumentasjonOppdaterer(repositoryProvider, lagMockHistory(), skjæringstidspunktTjeneste, hendelseTjeneste).oppdater(dto, behandling);
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setType(HistorikkinnslagType.FAKTA_ENDRET);
        List<HistorikkinnslagDel> historikkInnslag = tekstBuilder.build(historikkinnslag);

        // Assert
        assertThat(historikkInnslag).hasSize(1);
        HistorikkinnslagDel del = historikkInnslag.get(0);
        List<HistorikkinnslagFelt> feltList = del.getEndredeFelt();
        assertThat(feltList).hasSize(2);
        assertFelt(del, HistorikkEndretFeltType.OMSORGSOVERTAKELSESDATO, opprinneligOvertakelsesdato.format(formatterer), bekreftetOvertakelsesdato.format(formatterer));
        assertFelt(del, HistorikkEndretFeltType.FODSELSDATO, opprinneligFødselsdato.format(formatterer), bekreftetFødselsdato.format(formatterer));
    }

    private void assertFelt(HistorikkinnslagDel historikkinnslagDel, HistorikkEndretFeltType historikkEndretFeltType, String fraVerdi, String tilVerdi) {
        Optional<HistorikkinnslagFelt> feltOpt = historikkinnslagDel.getEndretFelt(historikkEndretFeltType);
        String feltNavn = historikkEndretFeltType.getKode();
        assertThat(feltOpt).as("endretFelt[" + feltNavn + "]").hasValueSatisfying(felt -> {
            assertThat(felt.getNavn()).as(feltNavn + ".navn").isEqualTo(feltNavn);
            assertThat(felt.getFraVerdi()).as(feltNavn + ".fraVerdi").isEqualTo(fraVerdi);
            assertThat(felt.getTilVerdi()).as(feltNavn + ".tilVerdi").isEqualTo(tilVerdi);
        });
    }

    private HistorikkTjenesteAdapter lagMockHistory() {
        HistorikkTjenesteAdapter mockHistory = mock(HistorikkTjenesteAdapter.class);
        when(mockHistory.tekstBuilder()).thenReturn(tekstBuilder);
        return mockHistory;
    }


}
