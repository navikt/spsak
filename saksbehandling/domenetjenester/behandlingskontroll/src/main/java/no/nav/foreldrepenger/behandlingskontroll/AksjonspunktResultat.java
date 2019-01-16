package no.nav.foreldrepenger.behandlingskontroll;

import static java.util.Collections.singletonList;

import java.util.List;
import java.util.function.Consumer;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;

/**
 * Knytter {@link AksjonspunktDefinisjon} med en callback for å modifisere aksjonpunktet som blir opprettet.
 */
public class AksjonspunktResultat {
    public static final Consumer<Aksjonspunkt> DUMMY_CONSUMER = new Consumer<Aksjonspunkt>() {

        @Override
        public void accept(Aksjonspunkt t) {
            // dummy
        }};

    private AksjonspunktDefinisjon aksjonspunktDefinisjon;
    private Consumer<Aksjonspunkt> aksjonspunktModifiserer;

    private AksjonspunktResultat(AksjonspunktDefinisjon aksjonspunktDefinisjon,
            Consumer<Aksjonspunkt> aksjonspunktModifiserer) {
        this.aksjonspunktDefinisjon = aksjonspunktDefinisjon;
        this.aksjonspunktModifiserer = aksjonspunktModifiserer;
    }

    /**
     * Factory-metode direkte basert på {@link AksjonspunktDefinisjon}. Ingen callback for consumer.
     */
    public static AksjonspunktResultat opprettForAksjonspunkt(AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        return new AksjonspunktResultat(aksjonspunktDefinisjon, DUMMY_CONSUMER);
    }

    /**
     * Factory-metode direkte basert på {@link AksjonspunktDefinisjon}, returnerer liste. Ingen callback for consumer.
     */
    public static List<AksjonspunktResultat> opprettListeForAksjonspunkt(AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        return singletonList(new AksjonspunktResultat(aksjonspunktDefinisjon, DUMMY_CONSUMER));
    }

    /**
     * Factory-metode som linker {@link AksjonspunktDefinisjon} sammen med callback for consumer-operasjon.
     */
    public static AksjonspunktResultat opprettForAksjonspunktMedCallback(AksjonspunktDefinisjon aksjonspunktDefinisjon,
            Consumer<Aksjonspunkt> consumer) {
        return new AksjonspunktResultat(aksjonspunktDefinisjon, consumer);
    }

    public AksjonspunktDefinisjon getAksjonspunktDefinisjon() {
        return aksjonspunktDefinisjon;
    }

    public Consumer<Aksjonspunkt> getAksjonspunktModifiserer() {
        return aksjonspunktModifiserer;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + aksjonspunktDefinisjon.getKode() + ", modifiserer=" + getAksjonspunktModifiserer() + ">";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AksjonspunktResultat)) return false;

        AksjonspunktResultat that = (AksjonspunktResultat) o;

        return aksjonspunktDefinisjon.getKode().equals(that.aksjonspunktDefinisjon.getKode());
    }

    @Override
    public int hashCode() {
        return aksjonspunktDefinisjon.getKode().hashCode();
    }
}
