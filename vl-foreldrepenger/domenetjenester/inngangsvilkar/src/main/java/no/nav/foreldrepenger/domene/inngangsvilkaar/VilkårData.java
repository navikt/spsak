package no.nav.foreldrepenger.domene.inngangsvilkaar;

import java.util.List;
import java.util.Properties;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallMerknad;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;

public class VilkårData {
    private final VilkårType vilkårType;
    private VilkårUtfallType utfallType;
    private Properties merknadParametere;
    private final List<AksjonspunktDefinisjon> apDefinisjoner;
    private VilkårUtfallMerknad vilkårUtfallMerknad;
    private Avslagsårsak avslagsårsak;
    private String regelEvaluering;
    private String regelInput;
    private boolean erOverstyrt;
    private Object data;

    /** Ctor som tar alle parametere inkl. regel input og evaluering. */
    public VilkårData(VilkårType vilkårType, VilkårUtfallType utfallType, Properties merknadParametere,
            List<AksjonspunktDefinisjon> apDefinisjoner, VilkårUtfallMerknad vilkårUtfallMerknad,
            Avslagsårsak avslagsårsak, String regelEvaluering, String regelInput, boolean erOverstyrt) {

        this.vilkårType = vilkårType;
        this.utfallType = utfallType;
        this.merknadParametere = merknadParametere;
        this.apDefinisjoner = apDefinisjoner;
        this.vilkårUtfallMerknad = vilkårUtfallMerknad;
        this.avslagsårsak = avslagsårsak;
        this.regelEvaluering = regelEvaluering;
        this.regelInput = regelInput;
        this.erOverstyrt = erOverstyrt;
    }

    /** Ctor som tar minimum av parametere, og ingen regel evaluering og input data.  Vil heller aldri være overstyrt. */
    public VilkårData(VilkårType vilkårType, VilkårUtfallType utfallType, List<AksjonspunktDefinisjon> apDefinisjoner) {
        this(vilkårType, utfallType, new Properties(), apDefinisjoner, null, null, null, null, false);
    }

    /** (Optional) ekstra resultat data. */
    public void setEkstraVilkårresultat(Object data) {
        this.data = data;
    }

    public Object getEkstraVilkårresultat() {
        return data;
    }

    public VilkårType getVilkårType() {
        return vilkårType;
    }

    public VilkårUtfallType getUtfallType() {
        return utfallType;
    }

    public Properties getMerknadParametere() {
        return merknadParametere;
    }

    public List<AksjonspunktDefinisjon> getApDefinisjoner() {
        return apDefinisjoner;
    }

    public VilkårUtfallMerknad getVilkårUtfallMerknad() {
        return vilkårUtfallMerknad;
    }

    public Avslagsårsak getAvslagsårsak() {
        return avslagsårsak;
    }

    public String getRegelEvaluering() {
        return regelEvaluering;
    }

    public String getRegelInput() {
        return regelInput;
    }

    public boolean erOverstyrt() {
        return erOverstyrt;
    }
}
