package no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.impl;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtleder;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.omsorg.AksjonspunktUtlederForAleneomsorg;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.omsorg.AksjonspunktUtlederForOmsorg;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.KontrollerFaktaUttakUtledereTjeneste;

@ApplicationScoped
@FagsakYtelseTypeRef("FP")
public class KontrollerFaktaUttakUtledereTjenesteImpl implements KontrollerFaktaUttakUtledereTjeneste {

    KontrollerFaktaUttakUtledereTjenesteImpl() {
        //CDI
    }

    @Override
    public List<AksjonspunktUtleder> utledUtledereFor(Behandling behandling) {
        final AksjonspunktUtlederHolder utlederHolder = new AksjonspunktUtlederHolder();
        utlederHolder.leggTil(AksjonspunktUtlederForOmsorg.class);
        utlederHolder.leggTil(AksjonspunktUtlederForAleneomsorg.class);
        utlederHolder.leggTil(AksjonspunktUtlederForAvklarFørsteUttaksdato.class);
        utlederHolder.leggTil(AksjonspunktUtlederForAvklareFakta.class);
        utlederHolder.leggTil(AksjonspunktUtlederForAvklarHendelse.class);
        return utlederHolder.getUtledere();
    }

}
