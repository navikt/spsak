package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util;

import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallMerknad;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;

public class VilkårTestutfall {
    private VilkårType vilkårType;
    private VilkårUtfallType utfallType;
    private VilkårUtfallMerknad utfallMerknad;
    private Avslagsårsak avslagsårsak;
    private boolean overstyrt;

    private VilkårTestutfall(VilkårType vilkårType, VilkårUtfallType utfallType, VilkårUtfallMerknad utfallMerknad, Avslagsårsak avslagsårsak, boolean overstyrt) {
        this.vilkårType = vilkårType;
        this.utfallType = utfallType;
        this.utfallMerknad = utfallMerknad;
        this.avslagsårsak = avslagsårsak;
        this.overstyrt = overstyrt;
    }

    public static VilkårTestutfall resultat(VilkårType vilkårType, VilkårUtfallType vilkårUtfallType, VilkårUtfallMerknad vilkårUtfallMerknad) {
        return new VilkårTestutfall(vilkårType, vilkårUtfallType, vilkårUtfallMerknad, null, false);
    }

    public static VilkårTestutfall resultat(VilkårType vilkårType, VilkårUtfallType vilkårUtfallType) {
        return new VilkårTestutfall(vilkårType, vilkårUtfallType, null, null, false);
    }

    public static VilkårTestutfall resultat(VilkårType vilkårType, VilkårUtfallType vilkårUtfallType, boolean overstyrt) {
        return new VilkårTestutfall(vilkårType, vilkårUtfallType, null, null, overstyrt);
    }

    public static VilkårTestutfall resultat(VilkårType vilkårType, VilkårUtfallType vilkårUtfallType, Avslagsårsak avslagsårsak) {
        return new VilkårTestutfall(vilkårType, vilkårUtfallType, null, avslagsårsak, false);
    }

    public static VilkårTestutfall resultat(VilkårType vilkårType, VilkårUtfallType vilkårUtfallType, Avslagsårsak avslagsårsak, boolean overstyrt) {
        return new VilkårTestutfall(vilkårType, vilkårUtfallType, null, avslagsårsak, overstyrt);
    }

    public static VilkårTestutfall resultat(VilkårType vilkårType, VilkårUtfallType vilkårUtfallType, VilkårUtfallMerknad merknad, boolean overstyrt) {
        return new VilkårTestutfall(vilkårType, vilkårUtfallType, merknad, null, overstyrt);
    }

    public static VilkårTestutfall resultat(VilkårType vilkårType, VilkårUtfallType vilkårUtfallType, VilkårUtfallMerknad vilkårUtfallMerknad, Avslagsårsak avslagsårsak) {
        return new VilkårTestutfall(vilkårType, vilkårUtfallType, vilkårUtfallMerknad, avslagsårsak, false);
    }

    VilkårType getVilkårType() {
        return vilkårType;
    }

    VilkårUtfallType getUtfallType() {
        return utfallType;
    }

    VilkårUtfallMerknad getUtfallMerknad() {
        return utfallMerknad;
    }

    Avslagsårsak getAvslagsårsak() {
        return avslagsårsak;
    }

    boolean erOverstyrt() {
        return overstyrt;
    }
}
