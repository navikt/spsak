package no.nav.foreldrepenger.domene.vedtak;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;

public class VedtakAksjonspunktData {

    private boolean godkjent;
    private String begrunnelse;
    private Set<String> vurderÅrsakskoder = Collections.emptySet();
    private AksjonspunktDefinisjon aksjonspunktDefinisjon;

    public VedtakAksjonspunktData(AksjonspunktDefinisjon aksjonspunktDefinisjon, boolean godkjent, String begrunnelse, Collection<String> vurderÅrsakskoder) {
        this.aksjonspunktDefinisjon = aksjonspunktDefinisjon;
        this.godkjent = godkjent;
        this.begrunnelse = begrunnelse;

        if (vurderÅrsakskoder != null) {
            this.vurderÅrsakskoder = new HashSet<>(vurderÅrsakskoder);
        }
    }

    public boolean isGodkjent() {
        return godkjent;
    }

    public AksjonspunktDefinisjon getAksjonspunktDefinisjon() {
        return aksjonspunktDefinisjon;
    }
    
    public String getBegrunnelse() {
        return begrunnelse;
    }

    public Set<String> getVurderÅrsakskoder() {
        return vurderÅrsakskoder;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !Objects.equals(obj.getClass(), this.getClass())) {
            return false;
        }
        VedtakAksjonspunktData other = (VedtakAksjonspunktData) obj;
        return Objects.equals(aksjonspunktDefinisjon, other.aksjonspunktDefinisjon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aksjonspunktDefinisjon);
    }

}
