package no.nav.foreldrepenger.domene.mottak.hendelser;

import static no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType.RE_HENDELSE_FØDSEL;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRevurderingRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.hendelser.ForretningshendelseType;
import no.nav.foreldrepenger.domene.familiehendelse.fødsel.FødselForretningshendelse;
import no.nav.foreldrepenger.domene.mottak.Behandlingsoppretter;
import no.nav.foreldrepenger.domene.mottak.ForretningshendelseHåndterer;
import no.nav.foreldrepenger.domene.mottak.ForretningshendelsestypeRef;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.HistorikkinnslagTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl.Kompletthetskontroller;

@ApplicationScoped
@ForretningshendelsestypeRef(FødselForretningshendelseHåndterer.DISKRIMINATOR)
public class FødselForretningshendelseHåndterer implements ForretningshendelseHåndterer<FødselForretningshendelse> {

    public static final String DISKRIMINATOR = "FØDSEL";

    private FagsakRepository fagsakRepository;
    private Behandlingsoppretter behandlingsoppretter;
    private Kompletthetskontroller kompletthetskontroller;
    private BehandlingRevurderingRepository behandlingRevurderingRepository;
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;


    @Inject
    public FødselForretningshendelseHåndterer(BehandlingRepositoryProvider repositoryProvider,
                                              Behandlingsoppretter behandlingsoppretter,
                                              Kompletthetskontroller kompletthetskontroller,
                                              BehandlingRevurderingRepository behandlingRevurderingRepository, HistorikkinnslagTjeneste historikkinnslagTjeneste) {
        this.fagsakRepository = repositoryProvider.getFagsakRepository();
        this.behandlingsoppretter = behandlingsoppretter;
        this.kompletthetskontroller = kompletthetskontroller;
        this.behandlingRevurderingRepository = behandlingRevurderingRepository;
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
    }

    @Override
    public List<Fagsak> finnRelaterteFagsaker(FødselForretningshendelse forretningshendelse) {
        return forretningshendelse.getAktørIdListe().stream()
            .flatMap(aktørId -> fagsakRepository.hentForBruker(aktørId).stream())
            .filter(fagsak -> !FagsakStatus.AVSLUTTET.equals(fagsak.getStatus()))
            .filter(fagsak -> fagsak.getYtelseType().equals(FagsakYtelseType.FORELDREPENGER))
            .collect(Collectors.toList());
    }

    @Override
    public void håndterÅpenBehandling(Behandling åpenBehandling, ForretningshendelseType forretningshendelseType) {
        historikkinnslagTjeneste.opprettHistorikkinnslagForBehandlingOppdatertMedNyeOpplysninger(åpenBehandling, BehandlingÅrsakType.RE_HENDELSE_FØDSEL);
        kompletthetskontroller.vurderNyForretningshendelse(åpenBehandling);
    }

    @Override
    public void håndterAvsluttetBehandling(Behandling avsluttetBehandling, ForretningshendelseType forretningshendelseType) {
        behandlingsoppretter.opprettRevurdering(avsluttetBehandling.getFagsak(), RE_HENDELSE_FØDSEL);
    }

    @Override
    public void håndterKøetBehandling(Fagsak fagsak, ForretningshendelseType forretningshendelseType) {
        Optional<Behandling> køetBehandlingOpt = behandlingRevurderingRepository.finnKøetYtelsesbehandling(fagsak.getId());
        if (køetBehandlingOpt.isPresent()) {
            // Oppdateringer fanges opp etter at behandling tas av kø, ettersom den vil passere steg innhentregisteropplysninger
            return;
        }
        Behandling køetBehandling = behandlingsoppretter.opprettKøetBehandling(fagsak, RE_HENDELSE_FØDSEL);
        historikkinnslagTjeneste.opprettHistorikkinnslagForVenteFristRelaterteInnslag(køetBehandling, HistorikkinnslagType.BEH_KØET, null, Venteårsak.VENT_ÅPEN_BEHANDLING);
        kompletthetskontroller.oppdaterKompletthetForKøetBehandling(køetBehandling);
    }
}

