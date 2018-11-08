package no.nav.foreldrepenger.behandling.steg.mottatteopplysninger;

import static java.util.Collections.singletonList;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.steg.mottatteopplysninger.api.TilknyttFagsakSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRevurderingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjon;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.datavarehus.tjeneste.DatavarehusTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl.Kompletthetskontroller;
import no.nav.foreldrepenger.domene.mottak.sakogenhet.KobleSakTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.BehandlendeEnhetTjeneste;

@BehandlingStegRef(kode = "INSØK")
@BehandlingTypeRef
@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class TilknyttFagsakFPStegImpl implements TilknyttFagsakSteg {

    private SøknadRepository søknadRepository;
    private DatavarehusTjeneste datavarehusTjeneste;
    private BehandlingRepository behandlingRepository;
    private FagsakRepository fagsakRepository;
    private KobleSakTjeneste kobleSakTjeneste;
    private BehandlendeEnhetTjeneste behandlendeEnhetTjeneste;
    private BehandlingRevurderingRepository revurderingRepository;
    private Kompletthetskontroller kompletthetskontroller;

    TilknyttFagsakFPStegImpl() {
        // for CDI proxy
    }

    @Inject
    public TilknyttFagsakFPStegImpl(DatavarehusTjeneste datavarehusTjeneste,
                                    BehandlingRepositoryProvider provider,
                                    KobleSakTjeneste kobleSakTjeneste,
                                    BehandlendeEnhetTjeneste behandlendeEnhetTjeneste,
                                    BehandlingRevurderingRepository revurderingRepository,
                                    Kompletthetskontroller kompletthetskontroller) {
        this.søknadRepository = provider.getSøknadRepository();
        this.datavarehusTjeneste = datavarehusTjeneste;
        this.behandlingRepository = provider.getBehandlingRepository();
        this.fagsakRepository = provider.getFagsakRepository();
        this.kobleSakTjeneste = kobleSakTjeneste;
        this.behandlendeEnhetTjeneste = behandlendeEnhetTjeneste;
        this.revurderingRepository = revurderingRepository;
        this.kompletthetskontroller = kompletthetskontroller;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());

        varsleDatavarehus(behandling);

        oppdaterFagsakMedRelasjonsRolle(behandling);
        // Prøve å koble fagsaker
        kobleSakerOppdaterEnhetVedBehov(behandling, kontekst.getSkriveLås());
        // Sjekke om koblet medforelder har åpen behandling
        Fagsak fagsak = fagsakRepository.finnEksaktFagsak(kontekst.getFagsakId());
        if (finnesÅpenBehandlingPåMedforelder(fagsak)) {
            kompletthetskontroller.oppdaterKompletthetForKøetBehandling(behandling);
            return BehandleStegResultat.utførtMedAksjonspunkter(singletonList(AksjonspunktDefinisjon.AUTO_KØET_BEHANDLING));
        }

        return BehandleStegResultat.utførtUtenAksjonspunkter();
    }

    private void varsleDatavarehus(Behandling behandling) {
        //Lagre den oppdaterte behandlingen i datavarehus datavarehus.
        //Dette er et spesialtilfelle, DVH skal normalt oppdateres via DatavarehusEventObserver
        datavarehusTjeneste.lagreNedBehandling(behandling);
    }

    private void oppdaterFagsakMedRelasjonsRolle(Behandling behandling) {
        Optional<Søknad> søknad = søknadRepository.hentSøknadHvisEksisterer(behandling);
        if (søknad.isPresent()) {
            fagsakRepository.oppdaterRelasjonsRolle(behandling.getFagsak().getId(), søknad.get().getRelasjonsRolleType());
        }
    }

    private boolean finnesÅpenBehandlingPåMedforelder(Fagsak fagsak) {
        return revurderingRepository.finnÅpenBehandlingMedforelder(fagsak).isPresent();
    }

    private void oppdaterEnhetMedAnnenPart(Behandling behandling, BehandlingLås lås) {
        behandlendeEnhetTjeneste.endretBehandlendeEnhetFraOppgittAnnenPart(behandling).ifPresent(organisasjonsEnhet -> {
            behandling.setBehandlendeEnhet(organisasjonsEnhet);
            behandlingRepository.lagre(behandling, lås);
        });
    }

    private void kobleSakerOppdaterEnhetVedBehov(Behandling behandling, BehandlingLås lås) {
        FagsakRelasjon kobling = kobleSakTjeneste.finnFagsakRelasjonDersomOpprettet(behandling).orElse(null);
        if (kobling != null && kobling.getFagsakNrTo().isPresent()) {
            // Allerede koblet. Da er vi på gjenvisitt og vi ser heller ikke på annen part fra søknad.
            return;
        }

        kobleSakTjeneste.kobleRelatertFagsakHvisDetFinnesEn(behandling);
        kobling = kobleSakTjeneste.finnFagsakRelasjonDersomOpprettet(behandling).orElse(null);

        if (kobling == null || !kobling.getFagsakNrTo().isPresent()) {
            // Ingen kobling foretatt
            oppdaterEnhetMedAnnenPart(behandling, lås);
            return;
        }
        behandlendeEnhetTjeneste.endretBehandlendeEnhetEtterFagsakKobling(behandling, kobling).ifPresent(organisasjonsEnhet -> {
            behandling.setBehandlendeEnhet(organisasjonsEnhet);
            behandlingRepository.lagre(behandling, lås);
        });
    }
}
