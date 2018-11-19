package no.nav.foreldrepenger.behandling.steg.avklarfakta.impl;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.steg.avklarfakta.api.KontrollerFaktaSteg;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.StartpunktRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.domene.kontrollerfakta.KontrollerFaktaTjeneste;
import no.nav.foreldrepenger.inngangsvilkaar.impl.ForeldrepengerVilkårUtleder;
import no.nav.foreldrepenger.inngangsvilkaar.impl.UtledeteVilkår;

@BehandlingStegRef(kode = "KOFAK")
@BehandlingTypeRef
@FagsakYtelseTypeRef("FP")
@StartpunktRef
@ApplicationScoped
public class KontrollerFaktaStegForeldrepengerImpl implements KontrollerFaktaSteg {

    private KontrollerFaktaTjeneste tjeneste;
    private BehandlingRepositoryProvider repositoryProvider;
    private BehandlingRepository behandlingRepository;

    KontrollerFaktaStegForeldrepengerImpl() {
        // for CDI proxy
    }

    @Inject
    KontrollerFaktaStegForeldrepengerImpl(BehandlingRepositoryProvider repositoryProvider,
                                          @FagsakYtelseTypeRef("FP") @BehandlingTypeRef @StartpunktRef KontrollerFaktaTjeneste tjeneste) {
        this.repositoryProvider = repositoryProvider;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.tjeneste = tjeneste;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        List<AksjonspunktResultat> aksjonspunktResultater = tjeneste.utledAksjonspunkter(kontekst.getBehandlingId());
        utledVilkår(kontekst);
        return BehandleStegResultat.utførtMedAksjonspunktResultater(aksjonspunktResultater);
    }

    private void utledVilkår(BehandlingskontrollKontekst kontekst) {
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());
        UtledeteVilkår utledeteVilkår = new ForeldrepengerVilkårUtleder().utledVilkår(behandling);
        opprettVilkår(utledeteVilkår, behandling, kontekst.getSkriveLås());
    }

    @Override
    public void vedHoppOverBakover(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingStegModell modell, BehandlingStegType tilSteg, BehandlingStegType fraSteg) {
        if (!BehandlingStegType.KONTROLLER_FAKTA.equals(fraSteg)) {
            RyddRegisterData rydder = new RyddRegisterData(modell, repositoryProvider, behandling, kontekst);
            rydder.ryddRegisterdataForeldrepenger();
        }
    }

    private void opprettVilkår(UtledeteVilkår utledeteVilkår, Behandling behandling, BehandlingLås skriveLås) {
        // Opprett Vilkårsresultat med vilkårne som som skal vurderes, og sett dem som ikke vurdert
        VilkårResultat.Builder vilkårBuilder = behandling.getBehandlingsresultat() != null
            ? VilkårResultat.builderFraEksisterende(behandling.getBehandlingsresultat().getVilkårResultat())
            : VilkårResultat.builder();
        utledeteVilkår.getAlleAvklarte()
            .forEach(vilkårType -> vilkårBuilder.leggTilVilkår(vilkårType, VilkårUtfallType.IKKE_VURDERT));
        vilkårBuilder.buildFor(behandling);
        behandlingRepository.lagre(behandling.getBehandlingsresultat().getVilkårResultat(), skriveLås);
    }

}
