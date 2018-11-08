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
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.datavarehus.tjeneste.DatavarehusTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.BehandlendeEnhetTjeneste;

@BehandlingStegRef(kode = "INSØK")
@BehandlingTypeRef
@FagsakYtelseTypeRef("ES")
@ApplicationScoped
public class TilknyttFagsakESStegImpl implements TilknyttFagsakSteg {

    private SøknadRepository søknadRepository;
    private FagsakRepository fagsakRepository;
    private BehandlingRepository behandlingRepository;
    private BehandlendeEnhetTjeneste behandlendeEnhetTjeneste;
    private DatavarehusTjeneste datavarehusTjeneste;

    TilknyttFagsakESStegImpl() {
        // for CDI proxy
    }

    @Inject
    public TilknyttFagsakESStegImpl(BehandlingRepositoryProvider provider, BehandlendeEnhetTjeneste behandlendeEnhetTjeneste, DatavarehusTjeneste datavarehusTjeneste) {
        this.søknadRepository = provider.getSøknadRepository();
        this.fagsakRepository = provider.getFagsakRepository();
        this.behandlingRepository = provider.getBehandlingRepository();
        this.behandlendeEnhetTjeneste = behandlendeEnhetTjeneste;
        this.datavarehusTjeneste = datavarehusTjeneste;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());
        oppdaterFagsakMedRelasjonsRolle(behandling);
        oppdaterEnhetMedAnnenPart(behandling, kontekst.getSkriveLås());
        return BehandleStegResultat.utførtUtenAksjonspunkter();
    }

    private void oppdaterFagsakMedRelasjonsRolle(Behandling behandling) {
        Optional<Søknad> søknad = søknadRepository.hentSøknadHvisEksisterer(behandling);
        if (søknad.isPresent()) {
            fagsakRepository.oppdaterRelasjonsRolle(behandling.getFagsak().getId(), søknad.get().getRelasjonsRolleType());
        }
    }

    private void oppdaterEnhetMedAnnenPart(Behandling behandling, BehandlingLås lås) {
        behandlendeEnhetTjeneste.endretBehandlendeEnhetFraOppgittAnnenPart(behandling).ifPresent(organisasjonsEnhet -> {
            behandling.setBehandlendeEnhet(organisasjonsEnhet);
            behandlingRepository.lagre(behandling, lås);
            varsleDatavarehus(behandling);
        });
    }

    private void varsleDatavarehus(Behandling behandling) {
        //Lagre den oppdaterte behandlingen i datavarehus datavarehus.
        //Dette er et spesialtilfelle, DVH skal normalt oppdateres via DatavarehusEventObserver
        datavarehusTjeneste.lagreNedBehandling(behandling);
    }
}
