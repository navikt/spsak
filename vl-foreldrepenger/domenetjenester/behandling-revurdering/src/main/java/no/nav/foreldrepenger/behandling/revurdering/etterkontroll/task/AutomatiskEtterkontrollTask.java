package no.nav.foreldrepenger.behandling.revurdering.etterkontroll.task;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.revurdering.RevurderingTjeneste;
import no.nav.foreldrepenger.behandling.revurdering.impl.RevurderingTjenesteProvider;
import no.nav.foreldrepenger.behandlingskontroll.task.FagsakProsessTask;
import no.nav.foreldrepenger.behandlingskontroll.task.FortsettBehandlingTaskProperties;
import no.nav.foreldrepenger.behandlingslager.aktør.FødtBarnInfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Terminbekreftelse;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.domene.person.TpsFamilieTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl.OpprettOppgaveVurderKonsekvensTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.konfig.KonfigVerdi;

/**
 * @Dependent scope for å hente konfig ved hver kjøring.
 */
@Dependent
@ProsessTask(AutomatiskEtterkontrollTask.TASKNAME)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class AutomatiskEtterkontrollTask extends FagsakProsessTask {
    public static final String TASKNAME = "behandlingsprosess.etterkontroll";

    private TpsFamilieTjeneste tpsFamilieTjeneste;
    private BehandlingRepository behandlingRepository;
    private RevurderingTjenesteProvider revurderingTjenesteProvider;
    private Period tpsRegistreringsTidsrom;
    private ProsessTaskRepository prosessTaskRepository;
    private FamilieHendelseRepository familieHendelseRepository;

    AutomatiskEtterkontrollTask() {
        // for CDI proxy
    }

    @Inject
    public AutomatiskEtterkontrollTask(TpsFamilieTjeneste tpsFamilieTjeneste, BehandlingRepositoryProvider repositoryProvider,
                                       RevurderingTjenesteProvider revurderingTjenesteProvider,
                                       @KonfigVerdi("etterkontroll.tpsregistrering.periode") Period tpsRegistreringsTidsrom,
                                       ProsessTaskRepository prosessTaskRepository) {
        super(repositoryProvider);
        this.tpsFamilieTjeneste = tpsFamilieTjeneste;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.revurderingTjenesteProvider = revurderingTjenesteProvider;
        this.tpsRegistreringsTidsrom = tpsRegistreringsTidsrom;
        this.prosessTaskRepository = prosessTaskRepository;
        this.familieHendelseRepository = repositoryProvider.getFamilieGrunnlagRepository();
    }

    @Override
    protected void prosesser(ProsessTaskData prosessTaskData) {
        final Long fagsakId = prosessTaskData.getFagsakId();
        final Long behandlingId = prosessTaskData.getBehandlingId();

        List<Behandling> åpneBehandlinger = behandlingRepository.hentBehandlingerSomIkkeErAvsluttetForFagsakId(fagsakId);
        Behandling behandlingForRevurdering = behandlingRepository.hentBehandling(behandlingId);
        RevurderingTjeneste revurderingTjeneste = revurderingTjenesteProvider.finnRevurderingTjenesteFor(behandlingForRevurdering.getFagsak());

        List<Behandling> åpenRevurdering = åpneBehandlinger.stream()
            .filter(behandling -> behandling.getType().equals(BehandlingType.REVURDERING)).collect(Collectors.toList());
        if (!åpenRevurdering.isEmpty()) {
            return;
        }
        List<Behandling> åpenForstGangs = åpneBehandlinger.stream()
            .filter(behandling -> behandling.getType().equals(BehandlingType.FØRSTEGANGSSØKNAD)).collect(Collectors.toList());
        if (!åpenForstGangs.isEmpty()) {
            opprettTaskForÅVurdereKonsekvens(fagsakId, behandlingForRevurdering.getBehandlendeEnhet());
            return;
        }

        final FamilieHendelseGrunnlag familieHendelseGrunnlag = familieHendelseRepository.hentAggregat(behandlingForRevurdering);
        List<FødtBarnInfo> barnFødtIPeriode = tpsFamilieTjeneste.getFødslerRelatertTilBehandling(behandlingForRevurdering,
            familieHendelseGrunnlag);

        Fagsak fagsak = behandlingForRevurdering.getFagsak();
        if (!barnFødtIPeriode.isEmpty()) {
            revurderingTjeneste.opprettHistorikkinnslagForFødsler(behandlingForRevurdering, barnFødtIPeriode);
        }

        final FamilieHendelse bekreftedeData = familieHendelseGrunnlag.getGjeldendeBekreftetVersjon().get();
        if (barnFødtIPeriode.size() == bekreftedeData.getAntallBarn()) {
            // alle forventede barn funnet i TPS, etterkontroll er ferdig
            return;
        }

        opprettRevurdering(prosessTaskData, behandlingForRevurdering, barnFødtIPeriode, fagsak, revurderingTjeneste, bekreftedeData);
    }

    private void opprettRevurdering(ProsessTaskData prosessTaskData, Behandling behandlingForRevurdering, List<FødtBarnInfo> barnFødtIPeriode, Fagsak fagsak,
                                    RevurderingTjeneste revurderingTjeneste, final FamilieHendelse bekreftedeData) {
        Behandling revurdering;
        if (!barnFødtIPeriode.isEmpty()) {
            revurdering = revurderingTjeneste.opprettAutomatiskRevurdering(fagsak, BehandlingÅrsakType.RE_AVVIK_ANTALL_BARN);
        } else {
            Terminbekreftelse terminbekreftelse = bekreftedeData.getTerminbekreftelse().get();
            LocalDate tidligsteTpsRegistreringsDato = terminbekreftelse.getTermindato().minus(tpsRegistreringsTidsrom);
            Behandlingsresultat behandlingsresultat = behandlingForRevurdering.getBehandlingsresultat();
            BehandlingVedtak vedtak = behandlingsresultat.getBehandlingVedtak();
            LocalDate vedtaksDato = vedtak.getVedtaksdato();
            if (vedtaksDato.isBefore(tidligsteTpsRegistreringsDato)) {
                revurdering = revurderingTjeneste.opprettAutomatiskRevurdering(fagsak, BehandlingÅrsakType.RE_MANGLER_FØDSEL_I_PERIODE);
            } else {
                revurdering = revurderingTjeneste.opprettAutomatiskRevurdering(fagsak, BehandlingÅrsakType.RE_MANGLER_FØDSEL);
            }
        }

        if (revurdering != null) {
            opprettTaskForProsesserBehandling(prosessTaskData, revurdering);
        }
    }

    private void opprettTaskForProsesserBehandling(ProsessTaskData denneTask, Behandling behandling) {
        ProsessTaskData prosessTaskData = new ProsessTaskData(FortsettBehandlingTaskProperties.TASKTYPE);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        // legger i samme gruppe og sekvens
        prosessTaskData.setGruppe(denneTask.getGruppe());
        prosessTaskData.setSekvens(String.valueOf(Integer.parseInt(denneTask.getSekvens()) + 1));  // increment 1
        prosessTaskData.setCallIdFraEksisterende();
        prosessTaskRepository.lagre(prosessTaskData);
    }

    private void opprettTaskForÅVurdereKonsekvens(Long fagsakId, String behandlendeEnhetsId) {
        ProsessTaskData prosessTaskData = new ProsessTaskData(OpprettOppgaveVurderKonsekvensTask.TASKTYPE);
        prosessTaskData.setProperty(OpprettOppgaveVurderKonsekvensTask.KEY_BEHANDLENDE_ENHET, behandlendeEnhetsId);
        prosessTaskData.setProperty(OpprettOppgaveVurderKonsekvensTask.KEY_BESKRIVELSE, OpprettOppgaveVurderKonsekvensTask.STANDARD_BESKRIVELSE);
        prosessTaskData.setProperty(OpprettOppgaveVurderKonsekvensTask.KEY_PRIORITET, OpprettOppgaveVurderKonsekvensTask.PRIORITET_NORM);
        prosessTaskData.setFagsakId(fagsakId);
        prosessTaskData.setCallIdFraEksisterende();
        prosessTaskRepository.lagre(prosessTaskData);
    }
}
