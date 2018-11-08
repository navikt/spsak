package no.nav.foreldrepenger.inngangsvilkaar.impl;

import static java.util.Arrays.asList;
import static no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType.ADOPSJON;
import static no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType.FØDSEL;
import static no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType.OMSORG;
import static no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType.TERMIN;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.ADOPSJONSVILKARET_FORELDREPENGER;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.BEREGNINGSGRUNNLAGVILKÅR;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.FORELDREANSVARSVILKÅRET_2_LEDD;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.FØDSELSVILKÅRET_FAR_MEDMOR;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.FØDSELSVILKÅRET_MOR;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.MEDLEMSKAPSVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.OPPTJENINGSPERIODEVILKÅR;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.OPPTJENINGSVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.SØKERSOPPLYSNINGSPLIKT;
import static no.nav.foreldrepenger.inngangsvilkaar.impl.UtledeteVilkår.forAvklartRelasjonsvilkårTilBarn;
import static no.nav.foreldrepenger.inngangsvilkaar.impl.VilkårUtlederFeil.FEILFACTORY;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;

@ApplicationScoped
public class ForeldrepengerVilkårUtleder implements VilkårUtleder {

    private static final Map<FamilieHendelseType, UtledeteVilkår> BEHANDLINGSMOTIV_TIL_UTLEDETE_VILKÅR;
    private static  final List<VilkårType> STANDARDVILKÅR = asList(
        MEDLEMSKAPSVILKÅRET,
        SØKERSOPPLYSNINGSPLIKT,
        OPPTJENINGSPERIODEVILKÅR,
        OPPTJENINGSVILKÅRET,
        BEREGNINGSGRUNNLAGVILKÅR);

    public ForeldrepengerVilkårUtleder() {
        // TODO midlertidig instansiering for å kunne ha forskjellige VilkårUtledere for FP og ES
    }

    static {
        Map<FamilieHendelseType, UtledeteVilkår> map = new HashMap<>();
        UtledeteVilkår utledeteAdopsjonsvilkår = forAvklartRelasjonsvilkårTilBarn(ADOPSJONSVILKARET_FORELDREPENGER, STANDARDVILKÅR);
        UtledeteVilkår utledeteOmsorgsvilkår = forAvklartRelasjonsvilkårTilBarn(FORELDREANSVARSVILKÅRET_2_LEDD, STANDARDVILKÅR);

        //Fødselsvilkår er avhengig av søker rolle så utledes i finnVilkår.
        map.put(ADOPSJON, utledeteAdopsjonsvilkår);
        map.put(OMSORG, utledeteOmsorgsvilkår);

        BEHANDLINGSMOTIV_TIL_UTLEDETE_VILKÅR = Collections.unmodifiableMap(map);
    }

    @Override
    public UtledeteVilkår utledVilkår(Behandling behandling, Optional<FamilieHendelseType> hendelseType) {
        return finnVilkår(behandling, hendelseType);
    }

    private static UtledeteVilkår finnVilkår(Behandling behandling, Optional<FamilieHendelseType> hendelseType) {
        if (!hendelseType.isPresent()) {
            throw FEILFACTORY.behandlingsmotivKanIkkeUtledes(behandling.getId()).toException();
        }

        FamilieHendelseType type = hendelseType.get();
        UtledeteVilkår vilkår = null;

        if (ADOPSJON.equals(type) || OMSORG.equals(type)) {
            vilkår = BEHANDLINGSMOTIV_TIL_UTLEDETE_VILKÅR.get(type);
        } else if (FØDSEL.equals(type) || TERMIN.equals(type)) {
            vilkår = finnFødselsvilkår(behandling.getRelasjonsRolleType());
        }

        if (vilkår == null) {
            throw FEILFACTORY.kunneIkkeUtledeVilkårFor(behandling.getId(), type.getNavn())
                .toException();
        }
        return vilkår;
    }

    private static UtledeteVilkår finnFødselsvilkår(RelasjonsRolleType rolle) {
        if ((RelasjonsRolleType.FARA.equals(rolle)) || (RelasjonsRolleType.MEDMOR.equals(rolle))) {
            return forAvklartRelasjonsvilkårTilBarn(FØDSELSVILKÅRET_FAR_MEDMOR, STANDARDVILKÅR);
        } else {
            return forAvklartRelasjonsvilkårTilBarn(FØDSELSVILKÅRET_MOR, STANDARDVILKÅR);
        }
    }
}
