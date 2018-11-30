package no.nav.foreldrepenger.domene.inngangsvilkaar.søknad;

import static java.util.Collections.singletonList;

import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.domene.inngangsvilkaar.Inngangsvilkår;
import no.nav.foreldrepenger.domene.inngangsvilkaar.VilkårData;
import no.nav.foreldrepenger.domene.inngangsvilkaar.VilkårTypeRef;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetsjekkerProvider;

@ApplicationScoped
@VilkårTypeRef(VilkårType.FP_VK_34)
public class InngangsvilkårSøkersOpplysningsplikt implements Inngangsvilkår {

    private KompletthetsjekkerProvider kompletthetsjekkerProvider;

    public InngangsvilkårSøkersOpplysningsplikt() {
        // for CDI proxy
    }

    @Inject
    public InngangsvilkårSøkersOpplysningsplikt(KompletthetsjekkerProvider kompletthetsjekkerProvider) {
        this.kompletthetsjekkerProvider = kompletthetsjekkerProvider;
    }

    @Override
    public VilkårData vurderVilkår(Behandling behandling) {
        return vurderOpplysningspliktOppfyltAutomatisk(behandling);
    }

    private VilkårData vurderOpplysningspliktOppfyltAutomatisk(Behandling behandling) {
        VilkårData oppfylt = new VilkårData(VilkårType.SØKERSOPPLYSNINGSPLIKT, VilkårUtfallType.OPPFYLT, Collections.emptyList());

        VilkårData manuellVurdering = new VilkårData(VilkårType.SØKERSOPPLYSNINGSPLIKT, VilkårUtfallType.IKKE_VURDERT,
            singletonList(AksjonspunktDefinisjon.SØKERS_OPPLYSNINGSPLIKT_MANU));

        if (behandling.getFagsak().getYtelseType().equals(FagsakYtelseType.FORELDREPENGER) &&
            behandling.getType().equals(BehandlingType.REVURDERING)) {
            // For revurdering FP skal det ikke utføres vilkårskontroll om opplysningsplikt (NOOP)
            return oppfylt;
        }

        boolean søknadKomplett = this.kompletthetsjekkerProvider.finnKompletthetsjekkerFor(behandling).erForsendelsesgrunnlagKomplett(behandling);
        if (søknadKomplett) {
            return oppfylt;
        }

        return manuellVurdering;
    }
}

