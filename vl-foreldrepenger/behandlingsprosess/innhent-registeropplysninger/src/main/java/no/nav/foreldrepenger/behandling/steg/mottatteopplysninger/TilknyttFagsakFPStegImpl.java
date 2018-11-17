package no.nav.foreldrepenger.behandling.steg.mottatteopplysninger;

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
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.datavarehus.tjeneste.DatavarehusTjeneste;

@BehandlingStegRef(kode = "INSØK")
@BehandlingTypeRef
@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class TilknyttFagsakFPStegImpl implements TilknyttFagsakSteg {

    private SøknadRepository søknadRepository;
    private DatavarehusTjeneste datavarehusTjeneste;
    private BehandlingRepository behandlingRepository;
    private FagsakRepository fagsakRepository;

    TilknyttFagsakFPStegImpl() {
        // for CDI proxy
    }

    @Inject
    public TilknyttFagsakFPStegImpl(DatavarehusTjeneste datavarehusTjeneste,
                                    BehandlingRepositoryProvider provider) {
        this.søknadRepository = provider.getSøknadRepository();
        this.datavarehusTjeneste = datavarehusTjeneste;
        this.behandlingRepository = provider.getBehandlingRepository();
        this.fagsakRepository = provider.getFagsakRepository();
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());

        varsleDatavarehus(behandling);

        oppdaterFagsakMedRelasjonsRolle(behandling);
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

}
