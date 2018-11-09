package no.nav.foreldrepenger.behandling.revurdering.etterkontroll.fp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandling.revurdering.RevurderingTjeneste;
import no.nav.foreldrepenger.behandling.revurdering.etterkontroll.task.AutomatiskEtterkontrollTask;
import no.nav.foreldrepenger.behandling.revurdering.impl.RevurderingTjenesteProvider;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.aktør.FødtBarnInfo;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.person.TpsFamilieTjeneste;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.Whitebox;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@SuppressWarnings("deprecation")
@RunWith(CdiRunner.class)
public class AutomatiskEtterkontrollTaskFPTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private TpsFamilieTjeneste tpsFamilieTjenesteMock;

    @Inject
    private BehandlingRepository behandlingRepositoryMock;

    @Inject
    @FagsakYtelseTypeRef("FP")
    private RevurderingTjeneste revurderingTjenesteMock;

    @Inject
    private RevurderingTjenesteProvider revurderingTjenesteProviderMock;

    @Inject
    private ProsessTaskRepository prosessTaskRepositoryMock;

    private AutomatiskEtterkontrollTask task;

    @Inject
    private BehandlingRepositoryProvider repositoryProvider;

    @Before
    public void setUp() {
        tpsFamilieTjenesteMock = mock(TpsFamilieTjeneste.class);
    }

    @Test
    public void skal_opprette_revurderingsbehandling_med_årsak_fødsel_mangler_dersom_fødsel_mangler_i_tps() {

        Behandling behandling = opprettRevurderingsKandidat(4, 1);
        when(tpsFamilieTjenesteMock.getFødslerRelatertTilBehandling(any(), any())).thenReturn(Collections.emptyList());

        ProsessTaskData prosessTaskData = new ProsessTaskData(AutomatiskEtterkontrollTask.TASKNAME);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setSekvens("1");

        createTask();
        task.doTask(prosessTaskData);

        assertRevurdering(behandling, BehandlingÅrsakType.RE_MANGLER_FØDSEL);

    }

    private void assertRevurdering(Behandling behandling, BehandlingÅrsakType behandlingÅrsakType) {
        Optional<Behandling> revurdering = behandlingRepositoryMock.hentSisteBehandlingForFagsakId(behandling.getFagsakId(), BehandlingType.REVURDERING);
        assertThat(revurdering).as("Ingen revurdering").isPresent();
        List<BehandlingÅrsak> behandlingÅrsaker = revurdering.get().getBehandlingÅrsaker();
        assertThat(behandlingÅrsaker).isNotEmpty();
        List<BehandlingÅrsakType> årsaker = behandlingÅrsaker.stream().map(bå -> bå.getBehandlingÅrsakType()).collect(Collectors.toList());
        assertThat(årsaker).contains(behandlingÅrsakType);
    }

    private void assertIngenRevurdering(Behandling behandling) {
        Optional<Behandling> revurdering = behandlingRepositoryMock.hentSisteBehandlingForFagsakId(behandling.getFagsakId(), BehandlingType.REVURDERING);
        assertThat(revurdering).as("Har revurdering: " + behandling).isNotPresent();
    }

    private void createTask() {
        Period etterkontrollTpsRegistreringPeriode = Period.parse("P11W");

        task = new AutomatiskEtterkontrollTask(tpsFamilieTjenesteMock, repositoryProvider, revurderingTjenesteProviderMock,
            etterkontrollTpsRegistreringPeriode, prosessTaskRepositoryMock);
    }

    @Test
    public void skal_opprette_revurderingsbehandling_med_årsak_fødsel_mangler_i_periode_dersom_fødsel_mangler_i_tps_og_vedtaksdato_er_før_uke29() {

        Behandling behandling = opprettRevurderingsKandidat(12, 1);
        when(tpsFamilieTjenesteMock.getFødslerRelatertTilBehandling(any(), any())).thenReturn(Collections.emptyList());

        ProsessTaskData prosessTaskData = new ProsessTaskData(AutomatiskEtterkontrollTask.TASKNAME);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setSekvens("1");

        createTask();
        task.doTask(prosessTaskData);

        assertRevurdering(behandling, BehandlingÅrsakType.RE_MANGLER_FØDSEL_I_PERIODE);

    }

    @Test
    public void skal_opprette_revurderingsbehandling_med_årsak_avvik_antall_barn_dersom_TPS_returnere_ulikt_antall_barn() {

        List<FødtBarnInfo> barn = Collections.singletonList(byggBaby(LocalDate.now().minusDays(70)));
        Behandling behandling = opprettRevurderingsKandidat(0, 2);
        when(tpsFamilieTjenesteMock.getFødslerRelatertTilBehandling(any(), any())).thenReturn(barn);

        ProsessTaskData prosessTaskData = new ProsessTaskData(AutomatiskEtterkontrollTask.TASKNAME);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setSekvens("1");

        createTask();
        task.doTask(prosessTaskData);

        assertRevurdering(behandling, BehandlingÅrsakType.RE_AVVIK_ANTALL_BARN);
    }

    @Test
    public void skal_registrere_fødsler_dersom_de_oppdages_i_tps() {
        List<FødtBarnInfo> barn = Collections.singletonList(byggBaby(LocalDate.now().minusDays(70)));
        Behandling behandling = opprettRevurderingsKandidat(0, 2);
        when(tpsFamilieTjenesteMock.getFødslerRelatertTilBehandling(any(), any())).thenReturn(barn);

        ProsessTaskData prosessTaskData = new ProsessTaskData(AutomatiskEtterkontrollTask.TASKNAME);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setSekvens("1");

        createTask();
        task.doTask(prosessTaskData);

        assertRevurdering(behandling, BehandlingÅrsakType.RE_AVVIK_ANTALL_BARN);

    }

    @Test
    public void skal_ikke_opprette_revurdering_dersom_barn_i_tps_matcher_søknad() {
        List<FødtBarnInfo> barn = Collections.singletonList(byggBaby(LocalDate.now().minusDays(70)));
        Behandling behandling = opprettRevurderingsKandidat(0, 1);
        when(tpsFamilieTjenesteMock.getFødslerRelatertTilBehandling(any(), any())).thenReturn(barn);

        ProsessTaskData prosessTaskData = new ProsessTaskData(AutomatiskEtterkontrollTask.TASKNAME);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setSekvens("1");

        createTask();
        task.doTask(prosessTaskData);

        assertIngenRevurdering(behandling);

    }

    @Test
    public void skal_opprette_vurder_konsekvens_oppgave_hvis_det_finnes_åpen_førstegangs_behandling() {
        Behandling behandling = opprettRevurderingsKandidat(0, 2);

        ProsessTaskData prosessTaskData = new ProsessTaskData(AutomatiskEtterkontrollTask.TASKNAME);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setSekvens("1");

        createTask();
        task.doTask(prosessTaskData);

    }

    private Behandling opprettRevurderingsKandidat(int fødselUkerFørTermin, int antallBarn) {
        LocalDate terminDato = LocalDate.now().minusDays(70);

        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medSøknadDato(terminDato.minusDays(20));

        scenario.medSøknadHendelse()
            .medTerminbekreftelse(scenario.medSøknadHendelse().getTerminbekreftelseBuilder()
                .medNavnPå("Lege Legesen")
                .medTermindato(terminDato)
                .medUtstedtDato(terminDato.minusDays(40)))
            .medAntallBarn(1);

        scenario.medBekreftetHendelse()
            .medTerminbekreftelse(scenario.medBekreftetHendelse().getTerminbekreftelseBuilder()
                .medNavnPå("Lege Legesen")
                .medTermindato(terminDato)
                .medUtstedtDato(terminDato.minusDays(40)))
            .medAntallBarn(antallBarn);

        scenario.leggTilVilkår(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.OPPFYLT);
        scenario.medVilkårResultatType(VilkårResultatType.INNVILGET);

        scenario.medBehandlingVedtak()
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medVedtaksdato(terminDato.minusWeeks(fødselUkerFørTermin))
            .medAnsvarligSaksbehandler("Severin Saksbehandler")
            .build();

        scenario.medBehandlingsresultat(Behandlingsresultat.builderForInngangsvilkår().medBehandlingResultatType(BehandlingResultatType.INNVILGET));
        
        Behandling behandling = scenario.lagre(repositoryProvider);

        Whitebox.setInternalState(behandling, "status", BehandlingStatus.AVSLUTTET);

        BehandlingLås lås = behandlingRepositoryMock.taSkriveLås(behandling);
        behandlingRepositoryMock.lagre(behandling, lås);

        repoRule.getRepository().flushAndClear();

        return repoRule.getEntityManager().find(Behandling.class, behandling.getId());
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
