package no.nav.foreldrepenger.behandlingslager.behandling.vilkår;

import java.util.Objects;
import java.util.Properties;

class VilkårBuilder {
    private VilkårType vilkårType;
    private Avslagsårsak avslagsårsak;
    private VilkårUtfallType vilkårUtfall;
    private VilkårUtfallMerknad vilkårUtfallMerknad;
    private Properties merknadParametere;
    private VilkårUtfallType vilkårUtfallManuell;
    private VilkårUtfallType vilkårUtfallOverstyrt;
    private String regelEvaluering;
    private String regelInput;

    VilkårBuilder medVilkårType(VilkårType vilkårType) {
        this.vilkårType = vilkårType;
        return this;
    }

    VilkårBuilder medAvslagsårsak(Avslagsårsak avslagsårsak) {
        this.avslagsårsak = avslagsårsak;
        return this;
    }

    VilkårBuilder medVilkårUtfall(VilkårUtfallType vilkårUtfall) {
        this.vilkårUtfall = vilkårUtfall;
        return this;
    }

    VilkårBuilder medVilkårUtfallMerknad(VilkårUtfallMerknad vilkårUtfallMerknad) {
        this.vilkårUtfallMerknad = vilkårUtfallMerknad;
        return this;
    }

    VilkårBuilder medMerknadParametere(Properties merknadParametere) {
        this.merknadParametere = merknadParametere;
        return this;
    }

    VilkårBuilder medUtfallManuell(VilkårUtfallType vilkårUtfallManuell) {
        this.vilkårUtfallManuell = vilkårUtfallManuell;
        return this;
    }

    VilkårBuilder medUtfallOverstyrt(VilkårUtfallType vilkårUtfallOverstyrt) {
        this.vilkårUtfallOverstyrt = vilkårUtfallOverstyrt;
        return this;
    }

    VilkårBuilder medRegelEvaluering(String regelEvaluering) {
        this.regelEvaluering = regelEvaluering;
        return this;
    }

    VilkårBuilder medRegelInput(String regelInput) {
        this.regelInput = regelInput;
        return this;
    }

    Vilkår build() {
        Vilkår vilkår = new Vilkår();
        Objects.requireNonNull(vilkårType, "vilkårType");
        vilkår.setVilkårType(vilkårType);
        vilkår.setVilkårUtfall(vilkårUtfall);
        vilkår.setVilkårUtfallMerknad(vilkårUtfallMerknad);
        vilkår.setAvslagsårsak(avslagsårsak);
        vilkår.setMerknadParametere(merknadParametere);
        vilkår.setVilkårUtfallManuelt(vilkårUtfallManuell);
        vilkår.setVilkårUtfallOverstyrt(vilkårUtfallOverstyrt);
        vilkår.setRegelEvaluering(regelEvaluering);
        vilkår.setRegelInput(regelInput);
        return vilkår;
    }
}
