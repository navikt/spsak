package no.nav.foreldrepenger.inngangsvilkaar.impl;

import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
public class UtledeteVilkår {
     // Liste av potensielle betingede vilkårtyper. Vil være > 1 når vilkår ikke kan avgjøres automatisk,
     // settes da manuelt gjennom aksjonspunkt.
     private List<VilkårType> potensielleBetingedeVilkårtyper;

     // Avklart betinget vilkår. Kan initielt settes til null, men må avklares før steg Kontroller fakta er ferdig.
     // Betingede vilkårtyper er: FØDSELSVILKÅRET_MOR, ADOPSJONSVILKÅRET_ENGANGSSTØNAD, OMSORGSVILKÅRET, FORELDREANSVARSVILKÅRET_2_LEDD,
     // FORELDREANSVARSVILKÅRET_4_LEDD
     private VilkårType avklartBetingetVilkårType;

     // I tillegg til betinget vilkår vil det følge tilhørende vilkår
     private List<VilkårType> tilhørendeVilkår;

     static no.nav.foreldrepenger.inngangsvilkaar.impl.UtledeteVilkår forAvklartRelasjonsvilkårTilBarn(VilkårType betingetVilkårType, List<VilkårType> tilhørendeVilkårTyper) {
         no.nav.foreldrepenger.inngangsvilkaar.impl.UtledeteVilkår utledeteVilkår = new no.nav.foreldrepenger.inngangsvilkaar.impl.UtledeteVilkår();
         utledeteVilkår.potensielleBetingedeVilkårtyper = singletonList(betingetVilkårType);
         utledeteVilkår.avklartBetingetVilkårType = betingetVilkårType;
         utledeteVilkår.tilhørendeVilkår = tilhørendeVilkårTyper;

         return utledeteVilkår;
     }

     static no.nav.foreldrepenger.inngangsvilkaar.impl.UtledeteVilkår forPotensielleRelasjonsvilkårTilBarn(List<VilkårType> potensielleVilkårtyper,
                                                                                                           List<VilkårType> tilhørendeVilkårTyper) {
         no.nav.foreldrepenger.inngangsvilkaar.impl.UtledeteVilkår utledeteVilkår = new no.nav.foreldrepenger.inngangsvilkaar.impl.UtledeteVilkår();
         utledeteVilkår.potensielleBetingedeVilkårtyper = potensielleVilkårtyper;
         utledeteVilkår.avklartBetingetVilkårType = null;
         utledeteVilkår.tilhørendeVilkår = tilhørendeVilkårTyper;

         return utledeteVilkår;
     }

     Optional<VilkårType> getBetinget() {
         return Optional.ofNullable(avklartBetingetVilkårType);
     }

     public List<VilkårType> getPotensielleBetingedeVilkårtyper() {
         return potensielleBetingedeVilkårtyper;
     }

     public List<VilkårType> getAlleAvklarte() {
         List<VilkårType> avklarteVilkår = new ArrayList<>();
         // Betinget vilkår kan være uavklart inntil aksjonspunkt er løst.
         if (avklartBetingetVilkårType != null) {
             avklarteVilkår.add(avklartBetingetVilkårType);
         }
         avklarteVilkår.addAll(tilhørendeVilkår);
         return avklarteVilkår;
     }
 }
