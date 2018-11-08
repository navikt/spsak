package no.nav.foreldrepenger.behandlingskontroll;

import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellImpl.TriFunction;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.VurderingspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.VurderingspunktDefinisjon.Type;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;

/** Modell for testing som lar oss endre på referansedata uten å eksponere vanlig api. */
public class ModifiserbarBehandlingModell {

    public static ModifiserbarBehandlingStegType fra(BehandlingStegType original) {
        return new ModifiserbarBehandlingStegType(original);
    }

    public static ModifiserbarVurderingspunktDefinisjon fra(ModifiserbarBehandlingStegType stegType, VurderingspunktDefinisjon.Type type) {
        return new ModifiserbarVurderingspunktDefinisjon(stegType.getKode() + (type == Type.INNGANG ? "INN" : "UT"), type);
    }

    public static class ModifiserbarBehandlingStegType extends BehandlingStegType {
        ModifiserbarBehandlingStegType(BehandlingStegType stegType) {
            super(stegType.getKode());
        }

        public void leggTilVurderingspunkt(VurderingspunktDefinisjon vurderingspunkt) {
            super.vurderingspunkter.add(vurderingspunkt);
        }
    }

    public static class ModifiserbarVurderingspunktDefinisjon extends VurderingspunktDefinisjon {
        ModifiserbarVurderingspunktDefinisjon(String kode, VurderingspunktDefinisjon.Type type) {
            super(kode, type);
        }

        /** KUN FOR TEST! */
        public void leggTil(List<AksjonspunktDefinisjon> aksjonspunktDefinisjoner) {
            super.aksjonspunktDefinisjoner.addAll(aksjonspunktDefinisjoner);
        }

    }


    public static BehandlingModellImpl setupModell(BehandlingType behandlingType, FagsakYtelseType fagsakYtelseType, List<TestStegKonfig> resolve) {
        TriFunction<BehandlingStegType, BehandlingType, FagsakYtelseType, BehandlingSteg> finnSteg = DummySteg.map(resolve);

        BehandlingModellImpl modell = new BehandlingModellImpl(behandlingType, fagsakYtelseType, finnSteg);
        for (TestStegKonfig konfig : resolve) {
            BehandlingStegType stegType = konfig.getBehandlingStegType();

            // fake legg til behandlingSteg og vureringspunkter
            ModifiserbarBehandlingStegType modStegType = ModifiserbarBehandlingModell.fra(stegType);
            modell.leggTil(modStegType, behandlingType, fagsakYtelseType);

            ModifiserbarVurderingspunktDefinisjon modVurderingspunktInngang = ModifiserbarBehandlingModell.fra(modStegType,
                    VurderingspunktDefinisjon.Type.INNGANG);
            modStegType.leggTilVurderingspunkt(modVurderingspunktInngang);
            modVurderingspunktInngang.leggTil(konfig.getInngangAksjonspunkter());

            ModifiserbarVurderingspunktDefinisjon modVurderingspunktUtgang = ModifiserbarBehandlingModell.fra(modStegType,
                    VurderingspunktDefinisjon.Type.UTGANG);
            modStegType.leggTilVurderingspunkt(modVurderingspunktUtgang);
            modVurderingspunktUtgang.leggTil(konfig.getUtgangAksjonspunkter());

            modell.internFinnSteg(stegType).leggTilVurderingspunktInngang(Optional.of(modVurderingspunktInngang));
            modell.internFinnSteg(stegType).leggTilVurderingspunktUtgang(Optional.of(modVurderingspunktUtgang));

        }
        return modell;

    }

}
