package no.nav.foreldrepenger.økonomistøtte;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdrag110;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragskontroll;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

@ApplicationScoped
public class OppdragskontrollTjenesteImpl implements OppdragskontrollTjeneste {

    private ØkonomioppdragRepository økonomioppdragRepository;
    private BehandlingRepository behandlingRepository;
    private FagsakRepository fagsakRepository;
    private OppdragskontrollManagerFactory oppdragskontrollManagerFactory;

    OppdragskontrollTjenesteImpl() {
        // For CDI
    }

    @Inject
    public OppdragskontrollTjenesteImpl(BehandlingRepositoryProvider repositoryProvider, OppdragskontrollManagerFactory oppdragskontrollManagerFactory, ØkonomioppdragRepository økonomioppdragRepository) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.fagsakRepository = repositoryProvider.getFagsakRepository();
        this.oppdragskontrollManagerFactory = oppdragskontrollManagerFactory;
        this.økonomioppdragRepository = økonomioppdragRepository;
    }

    @Override
    public Long opprettOppdrag(Long behandlingId, Long prosessTaskId) {
        return opprettOppdrag(behandlingId, prosessTaskId, false);
    }

    @Override
    public Long opprettOppdragSimulering(Long behandlingId, Long prosessTaskId) {
        return opprettOppdrag(behandlingId, prosessTaskId, true);
    }

    private Long opprettOppdrag(Long behandlingId, Long prosessTaskId, boolean simulering) {

        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Fagsak fagsak = fagsakRepository.finnEksaktFagsak(behandling.getFagsakId());

        Optional<Oppdragskontroll> forrigeOppdrag = finnForrigeOppddragForSak(fagsak);
        Oppdragskontroll oppdragskontroll = opprettOppdragskontroll(prosessTaskId, fagsak.getSaksnummer(), behandlingId, simulering);

        boolean eksistererForrigeOppdrag = forrigeOppdrag.isPresent();
        OppdragskontrollManager manager = oppdragskontrollManagerFactory.getManager(behandling, eksistererForrigeOppdrag);

        manager.opprettØkonomiOppdrag(behandling, forrigeOppdrag, oppdragskontroll);

        økonomioppdragRepository.lagre(oppdragskontroll);

        return oppdragskontroll.getId();
    }

    private Oppdragskontroll opprettOppdragskontroll(Long prosessTaskId, Saksnummer saksnummer, Long behandlingId, boolean simulering) {

        return Oppdragskontroll.builder()
            .medSaksnummer(saksnummer)
            .medBehandlingId(behandlingId)
            .medVenterKvittering(Boolean.TRUE)
            .medProsessTaskId(prosessTaskId)
            .medSimulering(simulering)
            .build();
    }

    @Override
    public Oppdragskontroll hentOppdragskontroll(Long oppdragskontrollId) {
        return økonomioppdragRepository.hentOppdragskontroll(oppdragskontrollId);
    }

    private Optional<Oppdragskontroll> finnForrigeOppddragForSak(Fagsak fagsak) {
        Optional<Oppdragskontroll> oppdragOpt = økonomioppdragRepository.finnNyesteOppdragForSak(fagsak.getSaksnummer());
        if (oppdragOpt.isPresent()) {
            List<Oppdrag110> oppdrag110Liste = oppdragOpt.get().getOppdrag110Liste();
            if (!erTomListe(oppdrag110Liste)) {
                return oppdragOpt;
            }
        }
        return Optional.empty();
    }

    private boolean erTomListe(List<?> liste) {
        return liste == null || liste.isEmpty();
    }

    @Override
    public List<Oppdrag110> hentOppdragForPeriodeOgFagområde(LocalDate fomDato, LocalDate tomDato, String fagområde) {
        return økonomioppdragRepository.hentOppdrag110ForPeriodeOgFagområde(fomDato, tomDato, fagområde);
    }

}
