package no.nav.foreldrepenger.inngangsvilkaar.impl;

import static java.util.Arrays.asList;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.ADOPSJONSVILKÅRET_ENGANGSSTØNAD;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.FORELDREANSVARSVILKÅRET_2_LEDD;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.FORELDREANSVARSVILKÅRET_4_LEDD;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.FØDSELSVILKÅRET_MOR;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.MEDLEMSKAPSVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.OMSORGSVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.SØKERSOPPLYSNINGSPLIKT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.SØKNADSFRISTVILKÅRET;
import static no.nav.foreldrepenger.inngangsvilkaar.impl.UtledeteVilkår.forAvklartRelasjonsvilkårTilBarn;
import static no.nav.foreldrepenger.inngangsvilkaar.impl.UtledeteVilkår.forPotensielleRelasjonsvilkårTilBarn;
import static no.nav.foreldrepenger.inngangsvilkaar.impl.VilkårUtlederFeil.FEILFACTORY;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;

/*
 * Denne klassen for å utlede vilkår følger funksjonell flyt som vist i
 * https://confluence.adeo.no/display/MODNAV/PK-42836+-+Funksjonell+beskrivelse
 */
@ApplicationScoped
public class EngangsstønadVilkårUtleder implements VilkårUtleder {

    private static final Map<FamilieHendelseType, UtledeteVilkår> FAKTA_TIL_UTLEDETE_VILKÅR;
    private static final List<VilkårType> STANDARDVILKÅR = asList(
        MEDLEMSKAPSVILKÅRET,
        SØKNADSFRISTVILKÅRET,
        SØKERSOPPLYSNINGSPLIKT);
    private static final List<VilkårType> POTENSIELLE_OMSORGSVILKÅR = asList(
        OMSORGSVILKÅRET,
        FORELDREANSVARSVILKÅRET_2_LEDD,
        FORELDREANSVARSVILKÅRET_4_LEDD);

    public EngangsstønadVilkårUtleder(){
        //TODO midlertidig instansiering for å kunne ha forskjellige VilkårUtledere for FP og ES
    }

    @Override
    public UtledeteVilkår utledVilkår(Behandling behandling, Optional<FamilieHendelseType> hendelseType) {
        verifiserSomFagsakForEngangsstønad(behandling.getFagsak());
        return finnVilkår(behandling, hendelseType);
    }

    static {
        Map<FamilieHendelseType, UtledeteVilkår> map = new HashMap<>();
        UtledeteVilkår utledeteAdopsjonsvilkår = forAvklartRelasjonsvilkårTilBarn(ADOPSJONSVILKÅRET_ENGANGSSTØNAD, STANDARDVILKÅR);
        UtledeteVilkår utledeteFødselsvilkår = forAvklartRelasjonsvilkårTilBarn(FØDSELSVILKÅRET_MOR, STANDARDVILKÅR);
        UtledeteVilkår utledeteOmsorgsvilkår = forPotensielleRelasjonsvilkårTilBarn(POTENSIELLE_OMSORGSVILKÅR, STANDARDVILKÅR);

        map.put(FamilieHendelseType.FØDSEL, utledeteFødselsvilkår);
        map.put(FamilieHendelseType.TERMIN, utledeteFødselsvilkår);
        map.put(FamilieHendelseType.ADOPSJON, utledeteAdopsjonsvilkår);
        map.put(FamilieHendelseType.OMSORG, utledeteOmsorgsvilkår);

        FAKTA_TIL_UTLEDETE_VILKÅR = Collections.unmodifiableMap(map);
    }

    private static UtledeteVilkår finnVilkår(Behandling behandling, Optional<FamilieHendelseType> behandlingsmotiv1) {
        if (!behandlingsmotiv1.isPresent()) {
            throw FEILFACTORY.behandlingsmotivKanIkkeUtledes(behandling.getId()).toException();
        }

        FamilieHendelseType behandlingsmotiv = behandlingsmotiv1.get();
        UtledeteVilkår vilkår = FAKTA_TIL_UTLEDETE_VILKÅR.get(behandlingsmotiv);

        if (vilkår == null) {
            throw FEILFACTORY.kunneIkkeUtledeVilkårFor(behandling.getId(), behandlingsmotiv.getNavn())
                .toException();
        }
        return vilkår;
    }

    private static void verifiserSomFagsakForEngangsstønad(Fagsak fagsak) {
        if (!FagsakYtelseType.ENGANGSTØNAD.equals(fagsak.getYtelseType())) {
            throw FEILFACTORY.støtterIkkeStønadstype(fagsak.getYtelseType().getNavn()).toException();
        }
    }

}
