package no.nav.foreldrepenger.behandling.steg.uttak.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.steg.uttak.UttakSteg;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjon;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjonRepository;
import no.nav.foreldrepenger.domene.uttak.beregnkontoer.BeregnStønadskontoerTjeneste;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.FastsettePerioderTjeneste;

@BehandlingStegRef(kode = "VURDER_UTTAK")
@BehandlingTypeRef
@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class UttakStegImpl implements UttakSteg {

    private BeregnStønadskontoerTjeneste beregnStønadskontoerTjeneste;
    private FastsettePerioderTjeneste fastsettePerioderTjeneste;
    private BehandlingRepository behandlingRepository;
    private FastsettUttakManueltAksjonspunktUtleder fastsettUttakManueltAksjonspunktUtleder;
    private FagsakRelasjonRepository fagsakRelasjonRepository;
    private BehandlingRepositoryProvider repositoryProvider;

    @Inject
    public UttakStegImpl(BeregnStønadskontoerTjeneste beregnStønadskontoerTjeneste,
                         BehandlingRepositoryProvider behandlingRepositoryProvider,
                         FastsettePerioderTjeneste fastsettePerioderTjeneste,
                         FastsettUttakManueltAksjonspunktUtleder fastsettUttakManueltAksjonspunktUtleder) {
        this.beregnStønadskontoerTjeneste = beregnStønadskontoerTjeneste;
        this.behandlingRepository = behandlingRepositoryProvider.getBehandlingRepository();
        this.fastsettUttakManueltAksjonspunktUtleder = fastsettUttakManueltAksjonspunktUtleder;
        this.fastsettePerioderTjeneste = fastsettePerioderTjeneste;
        this.fagsakRelasjonRepository = behandlingRepositoryProvider.getFagsakRelasjonRepository();
        this.repositoryProvider = behandlingRepositoryProvider;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());

        beregnStønadskontoer(behandling);

        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        fastsettePerioderTjeneste.fastsettePerioder(behandling);
        behandlingRepository.lagre(behandling, lås);

        List<AksjonspunktResultat> aksjonspunkter = fastsettUttakManueltAksjonspunktUtleder.utledAksjonspunkterFor(behandling);
        return BehandleStegResultat.utførtMedAksjonspunktResultater(aksjonspunkter);
    }

    private void beregnStønadskontoer(Behandling behandling) {
        if (skalBeregneStønadskontoer(behandling)) {
            beregnStønadskontoerTjeneste.beregnStønadskontoer(behandling); //Trenger ikke behandlingslås siden stønadskontoer lagres på fagsakrelasjon.
        }
    }

    private boolean skalBeregneStønadskontoer(Behandling behandling) {
        Optional<FagsakRelasjon> fagsakRelasjonOptional = fagsakRelasjonRepository.finnRelasjonForHvisEksisterer(behandling.getFagsak());
        if (!fagsakRelasjonOptional.isPresent()) {
            return true;
        }

        FagsakRelasjon fagsakRelasjon = fagsakRelasjonOptional.get();
        return !fagsakRelasjon.getStønadskontoberegning().isPresent() || !finnesTidligereAvsluttedeBehandlinger(fagsakRelasjon);
    }


    private boolean finnesTidligereAvsluttedeBehandlinger(FagsakRelasjon fagsakRelasjon) {
        Optional<Behandling> tidligereBehandling = behandlingRepository.finnSisteAvsluttedeIkkeHenlagteBehandling(fagsakRelasjon.getFagsakNrEn().getId());
        if (tidligereBehandling.isPresent()) {
            return true;
        }
        Optional<Fagsak> fagsakNrTo = fagsakRelasjon.getFagsakNrTo();
        if (fagsakNrTo.isPresent()) {
            return behandlingRepository.finnSisteAvsluttedeIkkeHenlagteBehandling(fagsakNrTo.get().getId()).isPresent();
        }
        return false;
    }

    @Override
    public void vedTransisjon(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingStegModell modell,
                              TransisjonType transisjonType, BehandlingStegType førsteSteg, BehandlingStegType sisteSteg, TransisjonType skalTil) {
        if (Objects.equals(TransisjonType.HOPP_OVER_BAKOVER, transisjonType)) {
            if (!Objects.equals(BehandlingStegType.VURDER_UTTAK, førsteSteg)){
                ryddUttak(behandling);
            }
        } else if (Objects.equals(TransisjonType.HOPP_OVER_FRAMOVER, transisjonType)) {
            ryddUttak(behandling);
        }
    }

    private void ryddUttak(Behandling behandling) {
        new RyddUttakTjeneste(behandling, repositoryProvider).ryddUttaksresultat();
    }
}
