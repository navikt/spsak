package no.nav.foreldrepenger.behandling.steg.avklarfakta.impl;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.steg.avklarfakta.api.KontrollerFaktaSteg;
import no.nav.foreldrepenger.behandlingskontroll.*;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.domene.inngangsvilkaar.impl.ForeldrepengerVilkårUtleder;
import no.nav.foreldrepenger.domene.inngangsvilkaar.impl.UtledeteVilkår;
import no.nav.foreldrepenger.domene.kontrollerfakta.KontrollerFaktaTjeneste;

@BehandlingStegRef(kode = "KOFAK")
@BehandlingTypeRef
@FagsakYtelseTypeRef("FP")
@StartpunktRef
@ApplicationScoped
public class KontrollerFaktaStegForeldrepengerImpl implements KontrollerFaktaSteg {

    private KontrollerFaktaTjeneste tjeneste;
    private GrunnlagRepositoryProvider repositoryProvider;
    private BehandlingRepository behandlingRepository;

    KontrollerFaktaStegForeldrepengerImpl() {
        // for CDI proxy
    }

    @Inject
    KontrollerFaktaStegForeldrepengerImpl(GrunnlagRepositoryProvider repositoryProvider,
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
        Behandlingsresultat resultat = behandlingRepository.hentResultatHvisEksisterer(behandling.getId())
            .orElse(Behandlingsresultat.opprettFor(behandling));
        // Opprett Vilkårsresultat med vilkårne som som skal vurderes, og sett dem som ikke vurdert
        VilkårResultat.Builder vilkårBuilder = resultat.getVilkårResultat() != null
            ? VilkårResultat.builderFraEksisterende(resultat.getVilkårResultat())
            : VilkårResultat.builder();
        utledeteVilkår.getAlleAvklarte()
            .forEach(vilkårType -> vilkårBuilder.leggTilVilkår(vilkårType, VilkårUtfallType.IKKE_VURDERT));
        vilkårBuilder.buildFor(resultat);
        behandlingRepository.lagre(resultat.getVilkårResultat(), skriveLås);
        behandlingRepository.lagre(resultat, skriveLås);
    }

}
