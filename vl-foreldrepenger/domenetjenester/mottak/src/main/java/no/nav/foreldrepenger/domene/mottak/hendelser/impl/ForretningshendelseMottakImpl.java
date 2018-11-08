package no.nav.foreldrepenger.domene.mottak.hendelser.impl;

import static no.nav.foreldrepenger.domene.mottak.hendelser.impl.ForretningshendelseMottakFeil.FEILFACTORY;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRevurderingRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.hendelser.Forretningshendelse;
import no.nav.foreldrepenger.behandlingslager.hendelser.ForretningshendelseType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.mottak.ForretningshendelseHåndterer;
import no.nav.foreldrepenger.domene.mottak.ForretningshendelseHåndtererProvider;
import no.nav.foreldrepenger.domene.mottak.ForretningshendelsestypeRef;
import no.nav.foreldrepenger.domene.mottak.hendelser.ForretningshendelseMottak;
import no.nav.foreldrepenger.domene.mottak.hendelser.ForretningshendelseOversetter;
import no.nav.foreldrepenger.domene.mottak.hendelser.kontrakt.ForretningshendelseDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
public class ForretningshendelseMottakImpl implements ForretningshendelseMottak {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Instance<ForretningshendelseOversetter<? extends Forretningshendelse>> oversettere;
    private FagsakRepository fagsakRepository;
    private ForretningshendelseHåndtererProvider forretningshendelseHåndtererProvider;
    private KodeverkRepository kodeverkRepository;
    private BehandlingRepository behandlingRepository;
    private BehandlingRevurderingRepository revurderingRepository;
    private ProsessTaskRepository prosessTaskRepository;

    ForretningshendelseMottakImpl() {
        //for CDI proxy
    }

    @Inject
    public ForretningshendelseMottakImpl(@Any Instance<ForretningshendelseOversetter<? extends Forretningshendelse>> oversettere,
                                         ForretningshendelseHåndtererProvider forretningshendelseHåndtererProvider,
                                         KodeverkRepository kodeverkRepository,
                                         BehandlingRepositoryProvider repositoryProvider,
                                         ProsessTaskRepository prosessTaskRepository) {
        this.oversettere = oversettere;
        this.fagsakRepository = repositoryProvider.getFagsakRepository();
        this.forretningshendelseHåndtererProvider = forretningshendelseHåndtererProvider;
        this.kodeverkRepository = kodeverkRepository;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.revurderingRepository = repositoryProvider.getBehandlingRevurderingRepository();
        this.prosessTaskRepository = prosessTaskRepository;
    }


    /**
    * 1. steg av håndtering av mottatt forretningshendelse. Identifiserer fagsaker som er kandidat for revurdering.
    */
    @Override
    public void mottaForretningshendelse(ForretningshendelseDto forretningshendelseDto) {
        String hendelseKode = forretningshendelseDto.getForretningshendelseType();
        ForretningshendelseType forretningshendelseType = kodeverkRepository.finn(ForretningshendelseType.class, hendelseKode);
        if (forretningshendelseType == null) {
            FEILFACTORY.ukjentForretningshendelse(hendelseKode).log(logger);
            return;
        }
        Forretningshendelse forretningshendelse = finnOversetter(forretningshendelseType).oversett(forretningshendelseDto);
        ForretningshendelseHåndterer<Forretningshendelse> håndterer = forretningshendelseHåndtererProvider.finnHåndterer(forretningshendelseType);

        håndterer.finnRelaterteFagsaker(forretningshendelse)
            .forEach(fagsak -> opprettProsesstaskForFagsak(fagsak, hendelseKode));
    }

    /**
     * 2. steg av håndtering av mottatt forretningshendelse. Hendelsen på fagsaken brukes som TRIGGER ift. protokoll
     * for mottak av hendelser på fagsak/behandling
     */
    @Override
    public void håndterHendelsePåFagsak(Long fagsakId, String hendelseTypeKode) {
        Objects.requireNonNull(hendelseTypeKode);
        Objects.requireNonNull(fagsakId);

        ForretningshendelseType hendelseType = kodeverkRepository.finn(ForretningshendelseType.class, hendelseTypeKode);
        ForretningshendelseHåndterer<Forretningshendelse> håndterer = forretningshendelseHåndtererProvider.finnHåndterer(hendelseType);
        Fagsak fagsak = fagsakRepository.finnEksaktFagsak(fagsakId);

        // Hent siste ytelsebehandling
        Behandling sisteYtelsebehandling = revurderingRepository.hentSisteYtelsesbehandling(fagsak.getId())
            .orElse(null);

        // Case 1: Ingen ytelsesbehandling er opprettet på fagsak - hendelse skal ikke opprette noen behandling
        if(sisteYtelsebehandling == null) {
            return;
        }

        // Case 2: Berørt (køet) behandling
        if (finnesÅpenBehandlingPåMedforelder(fagsak)) {
            håndterer.håndterKøetBehandling(fagsak, hendelseType);
            return;
        }

        // Case 3: Åpen ytelsesbehandling
        if (!sisteYtelsebehandling.erAvsluttet() && !sisteYtelsebehandling.erUnderIverksettelse()) {
            håndterer.håndterÅpenBehandling(sisteYtelsebehandling, hendelseType);
            return;
        }

        Optional<Behandling> sisteInnvilgedeYtelsesbehandling = behandlingRepository.finnSisteAvsluttedeIkkeHenlagteBehandling(fagsak.getId());
        // Case 4: Ytelsesbehandling finnes, men verken åpen eller innvilget. Antas å inntreffe sjelden
        if (!sisteInnvilgedeYtelsesbehandling.isPresent()) {
            FEILFACTORY.finnesYtelsebehandlingSomVerkenErÅpenEllerInnvilget(hendelseType.getKode()).log(logger);
            return;
        }
        Behandling innvilgetBehandling = sisteInnvilgedeYtelsesbehandling.get();

        // Case 5: Avsluttet eller iverksatt ytelsesbehandling
        håndterer.håndterAvsluttetBehandling(innvilgetBehandling, hendelseType);
    }

    private void opprettProsesstaskForFagsak(Fagsak fagsak, String hendelseType) {
        ProsessTaskData taskData = new ProsessTaskData(MottaHendelseFagsakTask.TASKTYPE);
        taskData.setProperty(MottaHendelseFagsakTask.PROPERTY_HENDELSE_TYPE, hendelseType);
        taskData.setFagsakId(fagsak.getId());
        taskData.setCallIdFraEksisterende();
        prosessTaskRepository.lagre(taskData);
    }

    private boolean finnesÅpenBehandlingPåMedforelder(Fagsak fagsak) {
        return revurderingRepository.finnÅpenBehandlingMedforelder(fagsak).isPresent();
    }

    @SuppressWarnings("unchecked")
    private <T extends Forretningshendelse> ForretningshendelseOversetter<T> finnOversetter(ForretningshendelseType forretningshendelseType) {
        Instance<ForretningshendelseOversetter<? extends Forretningshendelse>> selected = oversettere.select(new ForretningshendelsestypeRef.ForretningshendelsestypeRefLiteral(forretningshendelseType));
        if (selected.isAmbiguous()) {
            throw new IllegalArgumentException("Mer enn en implementasjon funnet for forretningshendelsetype:" + forretningshendelseType);
        } else if (selected.isUnsatisfied()) {
            throw new IllegalArgumentException("Ingen implementasjoner funnet for forretningshendelsetype:" + forretningshendelseType);
        }
        ForretningshendelseOversetter<? extends Forretningshendelse> minInstans = selected.get();
        if (minInstans.getClass().isAnnotationPresent(Dependent.class)) {
            throw new IllegalStateException("Kan ikke ha @Dependent scope bean ved Instance lookup dersom en ikke også håndtere lifecycle selv: " + minInstans.getClass());
        }
        return (ForretningshendelseOversetter<T>) minInstans;
    }
}
