package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.RegisterInnhentingIntervallEndringTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Terminbekreftelse;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagFelt;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.familiehendelse.impl.FamilieHendelseTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.BeregnMorsMaksdatoTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.RelatertBehandlingTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse.BekreftTerminbekreftelseAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse.BekreftTerminbekreftelseValidator;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

public class BekreftTerminbekreftelseOppdatererTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private DateTimeFormatter formatterer = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private final HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder();
    private final FamilieHendelseTjenesteImpl familieHendelseTjeneste = new FamilieHendelseTjenesteImpl(null, 16, 4, repositoryProvider);
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;

    private final LocalDate now = LocalDate.now();
    @Before
    public void oppsett() {
        this.skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider,
            new BeregnMorsMaksdatoTjenesteImpl(repositoryProvider, new RelatertBehandlingTjenesteImpl(repositoryProvider)),
            new RegisterInnhentingIntervallEndringTjeneste(Period.of(1, 0, 0), Period.of(0, 4, 0)),
            Period.of(0, 3, 0),
            Period.of(0, 10, 0));
    }

    @Test
    public void skal_generere_historikkinnslag_ved_avklaring_av_terminbekreftelse() {
        // Arrange
        LocalDate opprinneligTermindato = LocalDate.now();
        LocalDate avklartTermindato = opprinneligTermindato.plusDays(1);
        LocalDate opprinneligUtstedtDato = LocalDate.now().minusDays(20);
        LocalDate avklartUtstedtDato = opprinneligUtstedtDato.plusDays(1);
        int opprinneligAntallBarn = 1;
        int avklartAntallBarn = 2;

        // Behandling
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medSøknad();
        scenario.medSøknadHendelse()
                .medTerminbekreftelse(scenario.medSøknadHendelse().getTerminbekreftelseBuilder()
                        .medTermindato(opprinneligTermindato)
                        .medUtstedtDato(opprinneligUtstedtDato)
                        .medNavnPå("LEGEN MIN"))
                .medAntallBarn(opprinneligAntallBarn);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_TERMINBEKREFTELSE, BehandlingStegType.SØKERS_RELASJON_TIL_BARN);
        Behandling behandling = scenario.lagre(repositoryProvider);

        // Dto
        BekreftTerminbekreftelseAksjonspunktDto dto = new BekreftTerminbekreftelseAksjonspunktDto("begrunnelse",
                avklartTermindato, avklartUtstedtDato, avklartAntallBarn);

        // Act
        BekreftTerminbekreftelseOppdaterer oppdaterer = new BekreftTerminbekreftelseOppdaterer(repositoryProvider,
                lagMockHistory(),
                Period.parse("P25D"),
                familieHendelseTjeneste,
            skjæringstidspunktTjeneste,
                new BekreftTerminbekreftelseValidator(Period.parse("P25D")));

        oppdaterer.oppdater(dto, behandling);
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setType(HistorikkinnslagType.FAKTA_ENDRET);
        List<HistorikkinnslagDel> historikkInnslag = tekstBuilder.build(historikkinnslag);

        // Assert

        assertThat(historikkInnslag).hasSize(1);

        HistorikkinnslagDel del = historikkInnslag.get(0);
        List<HistorikkinnslagFelt> feltList = del.getEndredeFelt();
        assertThat(feltList).hasSize(3);
        assertFelt(del, HistorikkEndretFeltType.TERMINDATO, opprinneligTermindato.format(formatterer), avklartTermindato.format(formatterer));
        assertFelt(del, HistorikkEndretFeltType.UTSTEDTDATO, opprinneligUtstedtDato.format(formatterer), avklartUtstedtDato.format(formatterer));
        assertFelt(del, HistorikkEndretFeltType.ANTALL_BARN, Integer.toString(opprinneligAntallBarn), Integer.toString(avklartAntallBarn));
    }

    private void assertFelt(HistorikkinnslagDel del, HistorikkEndretFeltType historikkEndretFeltType, String fraVerdi, String tilVerdi) {
        Optional<HistorikkinnslagFelt> feltOpt = del.getEndretFelt(historikkEndretFeltType);
        String feltNavn = historikkEndretFeltType.getKode();
        assertThat(feltOpt).hasValueSatisfying(felt -> {
            assertThat(felt.getNavn()).as(feltNavn + ".navn").isEqualTo(feltNavn);
            assertThat(felt.getFraVerdi()).as(feltNavn + ".fraVerdi").isEqualTo(fraVerdi);
            assertThat(felt.getTilVerdi()).as(feltNavn + ".tilVerdi").isEqualTo(tilVerdi);
        });
    }

    @Test
    public void skal_oppdatere_terminbekreftelse() {
        // Arrange
        ScenarioFarSøkerEngangsstønad scenario = ScenarioFarSøkerEngangsstønad.forFødsel();
        scenario.medSøknad()
                .medSøknadsdato(now);
        scenario.medSøknadHendelse().medTerminbekreftelse(scenario.medSøknadHendelse().getTerminbekreftelseBuilder()
                .medNavnPå("LEGEN MIN")
                .medTermindato(now.plusDays(30))
                .medUtstedtDato(now.minusDays(3)))
                .medAntallBarn(1);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_TERMINBEKREFTELSE, BehandlingStegType.SØKERS_RELASJON_TIL_BARN);

        Behandling behandling = scenario.lagre(repositoryProvider);

        BekreftTerminbekreftelseAksjonspunktDto dto = new BekreftTerminbekreftelseAksjonspunktDto(
                "Begrunnelse", now.plusDays(30), now.minusDays(3), 1);

        // Act
        final FamilieHendelseTjenesteImpl familieHendelseTjeneste = new FamilieHendelseTjenesteImpl(null, 16, 4, repositoryProvider);
        BekreftTerminbekreftelseOppdaterer oppdaterer = new BekreftTerminbekreftelseOppdaterer(repositoryProvider,
                lagMockHistory(),
                Period.parse("P25D"),
                familieHendelseTjeneste,
            skjæringstidspunktTjeneste,
                new BekreftTerminbekreftelseValidator(Period.parse("P25D")));

        oppdaterer.oppdater(dto, behandling);


        // Assert
        final FamilieHendelseGrunnlag familieHendelseGrunnlag = repositoryProvider.getFamilieGrunnlagRepository()
                .hentAggregat(behandling);

        final Optional<Terminbekreftelse> terminbekreftelse = familieHendelseGrunnlag.getGjeldendeVersjon().getTerminbekreftelse();

        assertThat(terminbekreftelse).isPresent();
        assertThat(familieHendelseGrunnlag.getGjeldendeVersjon().getAntallBarn()).isEqualTo(1);
        assertThat(terminbekreftelse.get().getTermindato()).isEqualTo(now.plusDays(30));
        assertThat(terminbekreftelse.get().getUtstedtdato()).isEqualTo(now.minusDays(3));
    }


    private HistorikkTjenesteAdapter lagMockHistory() {
        HistorikkTjenesteAdapter mockHistory = Mockito.mock(HistorikkTjenesteAdapter.class);
        Mockito.when(mockHistory.tekstBuilder()).thenReturn(tekstBuilder);
        return mockHistory;
    }

}
