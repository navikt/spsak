package no.nav.foreldrepenger.behandling.revurdering.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.behandling.revurdering.RevurderingTjeneste;
import no.nav.foreldrepenger.behandling.revurdering.fp.impl.RevurderingFPTjenesteImpl;
import no.nav.foreldrepenger.behandling.revurdering.testutil.BeregningRevurderingTestUtil;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellRepository;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellRepositoryImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.aktør.FødtBarnInfo;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkOpplysningType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagFelt;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

@RunWith(CdiRunner.class)
public class RevurderingFPTjenesteImplTest {

    @Rule
    public final RepositoryRule repoRule = new UnittestRepositoryRule();

    @Inject
    private BeregningRevurderingTestUtil revurderingTestUtil;

    private final BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private BehandlingModellRepository behandlingModellRepository = new BehandlingModellRepositoryImpl(repoRule.getEntityManager());
    private HistorikkRepository historikkRepository = spy(repositoryProvider.getHistorikkRepository());
    private RevurderingTjeneste revurderingTjeneste;
    private Behandling behandling;
    @Inject
    @FagsakYtelseTypeRef("FP")
    private RevurderingEndring revurderingEndringFP;

    @Before
    public void setup() {
        opprettRevurderingsKandidat();
    }

    @Test
    public void skal_opprette_historikkinnslag_for_registrert_fødsel() {
        LocalDate fødselsdato = LocalDate.parse("2017-09-04");
        List<FødtBarnInfo> barn = Collections.singletonList(byggBaby(fødselsdato));
        final BehandlingskontrollTjenesteImpl behandlingskontrollTjeneste = new BehandlingskontrollTjenesteImpl(repositoryProvider,
            behandlingModellRepository, null);
        revurderingTjeneste = new RevurderingFPTjenesteImpl(repositoryProvider, behandlingskontrollTjeneste, historikkRepository, revurderingEndringFP);
        revurderingTjeneste.opprettHistorikkinnslagForFødsler(behandling, barn);
        ArgumentCaptor<Historikkinnslag> captor = ArgumentCaptor.forClass(Historikkinnslag.class);

        verify(historikkRepository).lagre(captor.capture());
        Historikkinnslag historikkinnslag = captor.getValue();

        assertThat(historikkinnslag.getType()).isEqualTo(HistorikkinnslagType.NY_INFO_FRA_TPS);
        assertThat(historikkinnslag.getAktør()).isEqualTo(HistorikkAktør.VEDTAKSLØSNINGEN);
        HistorikkinnslagDel del = historikkinnslag.getHistorikkinnslagDeler().get(0);
        Optional<HistorikkinnslagFelt> fodsel = del.getOpplysning(HistorikkOpplysningType.FODSELSDATO);
        Optional<HistorikkinnslagFelt> antallBarn = del.getOpplysning(HistorikkOpplysningType.TPS_ANTALL_BARN);
        assertThat(fodsel).hasValueSatisfying(v -> assertThat(v.getTilVerdi()).isEqualTo("04.09.2017"));
        assertThat(antallBarn).as("antallBarn").hasValueSatisfying(v -> assertThat(v.getTilVerdi()).as("antallBarn.tilVerdi").isEqualTo(Integer.toString(1)));
    }

    @Test
    public void skal_opprette_korrekt_historikkinnslag_for_trillingfødsel_over_2_dager() {
        LocalDate fødselsdato1 = LocalDate.parse("2017-09-04");
        LocalDate fødselsdato2 = LocalDate.parse("2017-09-05");
        List<FødtBarnInfo> barn = new ArrayList<>();
        barn.add(byggBaby(fødselsdato1));
        barn.add(byggBaby(fødselsdato1));
        barn.add(byggBaby(fødselsdato2));

        final BehandlingskontrollTjenesteImpl behandlingskontrollTjeneste = new BehandlingskontrollTjenesteImpl(repositoryProvider,
            behandlingModellRepository, null);
        revurderingTjeneste = new RevurderingFPTjenesteImpl(repositoryProvider, behandlingskontrollTjeneste, historikkRepository, revurderingEndringFP);
        revurderingTjeneste.opprettHistorikkinnslagForFødsler(behandling, barn);
        ArgumentCaptor<Historikkinnslag> captor = ArgumentCaptor.forClass(Historikkinnslag.class);

        verify(historikkRepository).lagre(captor.capture());
        Historikkinnslag historikkinnslag = captor.getValue();

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        assertThat(historikkinnslag.getType()).isEqualTo(HistorikkinnslagType.NY_INFO_FRA_TPS);
        assertThat(historikkinnslag.getAktør()).isEqualTo(HistorikkAktør.VEDTAKSLØSNINGEN);
        HistorikkinnslagDel del = historikkinnslag.getHistorikkinnslagDeler().get(0);
        Optional<HistorikkinnslagFelt> fodsel = del.getOpplysning(HistorikkOpplysningType.FODSELSDATO);
        Optional<HistorikkinnslagFelt> antallBarn = del.getOpplysning(HistorikkOpplysningType.TPS_ANTALL_BARN);
        String dateString = dateFormat.format(fødselsdato1) + ", " + dateFormat.format(fødselsdato2);
        assertThat(fodsel).as("fodsel").hasValueSatisfying(v -> assertThat(v.getTilVerdi()).as("fodsel.tilVerdi").isEqualTo(dateString));
        assertThat(antallBarn).as("antallBarn").hasValueSatisfying(v -> assertThat(v.getTilVerdi()).as("antallBarn.tilVerdi").isEqualTo(Integer.toString(3)));
    }

    @Test
    public void skal_opprette_revurdering_for_foreldrepenger() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_TERMINBEKREFTELSE, BehandlingStegType.KONTROLLER_FAKTA);
        Behandling behandlingSomSkalRevurderes = scenario.lagre(repositoryProvider);
        revurderingTestUtil.avsluttBehandling(behandlingSomSkalRevurderes);

        final BehandlingskontrollTjenesteImpl behandlingskontrollTjeneste = new BehandlingskontrollTjenesteImpl(repositoryProvider,
            behandlingModellRepository, null);
        revurderingTjeneste = new RevurderingFPTjenesteImpl(repositoryProvider, behandlingskontrollTjeneste, historikkRepository, revurderingEndringFP);

        // Act
        Behandling revurdering = revurderingTjeneste
            .opprettAutomatiskRevurdering(behandlingSomSkalRevurderes.getFagsak(), BehandlingÅrsakType.RE_HENDELSE_FØDSEL);

        // Assert
        assertThat(revurdering.getFagsak()).isEqualTo(behandlingSomSkalRevurderes.getFagsak());
        assertThat(revurdering.getBehandlingÅrsaker().get(0).getBehandlingÅrsakType()).isEqualTo(BehandlingÅrsakType.RE_HENDELSE_FØDSEL);
        assertThat(revurdering.getType()).isEqualTo(BehandlingType.REVURDERING);
        assertThat(revurdering.getAlleAksjonspunkterInklInaktive()).hasSize(1);
        Aksjonspunkt aksjonspunkt = revurdering.getAlleAksjonspunkterInklInaktive().iterator().next();
        assertThat(aksjonspunkt.getAksjonspunktDefinisjon()).isEqualTo(AksjonspunktDefinisjon.AVKLAR_TERMINBEKREFTELSE);
        assertThat(aksjonspunkt.erAktivt()).isFalse();
    }

    private void opprettRevurderingsKandidat() {

        LocalDate terminDato = LocalDate.now().minusDays(70);
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        scenario.medSøknadHendelse()
            .medTerminbekreftelse(scenario.medSøknadHendelse().getTerminbekreftelseBuilder()
                .medTermindato(terminDato).medUtstedtDato(terminDato))
            .medAntallBarn(1);
        scenario.medBekreftetHendelse()
            .medTerminbekreftelse(scenario.medBekreftetHendelse().getTerminbekreftelseBuilder()
                .medTermindato(terminDato).medUtstedtDato(terminDato.minusDays(40)))
            .medAntallBarn(1);
        behandling = scenario.lagre(repositoryProvider);
    }

    private FødtBarnInfo byggBaby(LocalDate fødselsdato) {
        return new FødtBarnInfo.Builder()
            .medFødselsdato(fødselsdato)
            .medIdent(PersonIdent.fra("19010100000"))
            .medNavn("barn")
            .medNavBrukerKjønn(NavBrukerKjønn.MANN)
            .build();
    }
}
