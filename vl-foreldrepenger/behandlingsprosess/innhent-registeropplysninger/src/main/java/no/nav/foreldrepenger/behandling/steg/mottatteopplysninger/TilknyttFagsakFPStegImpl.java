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
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRevurderingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.datavarehus.tjeneste.DatavarehusTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl.Kompletthetskontroller;

@BehandlingStegRef(kode = "INSØK")
@BehandlingTypeRef
@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class TilknyttFagsakFPStegImpl implements TilknyttFagsakSteg {

    private SøknadRepository søknadRepository;
    private DatavarehusTjeneste datavarehusTjeneste;
    private BehandlingRepository behandlingRepository;
    private FagsakRepository fagsakRepository;
    private BehandlingRevurderingRepository revurderingRepository;
    private Kompletthetskontroller kompletthetskontroller;

    TilknyttFagsakFPStegImpl() {
        // for CDI proxy
    }

    @Inject
    public TilknyttFagsakFPStegImpl(DatavarehusTjeneste datavarehusTjeneste,
                                    BehandlingRepositoryProvider provider,
                                    BehandlingRevurderingRepository revurderingRepository,
                                    Kompletthetskontroller kompletthetskontroller) {
        this.søknadRepository = provider.getSøknadRepository();
        this.datavarehusTjeneste = datavarehusTjeneste;
        this.behandlingRepository = provider.getBehandlingRepository();
        this.fagsakRepository = provider.getFagsakRepository();
        this.revurderingRepository = revurderingRepository;
        this.kompletthetskontroller = kompletthetskontroller;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());

        varsleDatavarehus(behandling);

        oppdaterFagsakMedRelasjonsRolle(behandling);
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

}
