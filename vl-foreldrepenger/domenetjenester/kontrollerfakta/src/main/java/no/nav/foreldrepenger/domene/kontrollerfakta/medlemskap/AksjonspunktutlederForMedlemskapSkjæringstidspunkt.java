
package no.nav.foreldrepenger.domene.kontrollerfakta.medlemskap;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtleder;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.domene.medlem.api.VurderMedlemskapTjeneste;
import no.nav.foreldrepenger.domene.medlem.impl.MedlemResultat;

@ApplicationScoped
public class AksjonspunktutlederForMedlemskapSkjæringstidspunkt implements AksjonspunktUtleder {

    private VurderMedlemskapTjeneste tjeneste;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private static Map<MedlemResultat, AksjonspunktDefinisjon> mapMedlemResulatTilAkDef = new HashMap<>();

    static {
        mapMedlemResulatTilAkDef.put(MedlemResultat.AVKLAR_OM_ER_BOSATT, AksjonspunktDefinisjon.AVKLAR_OM_ER_BOSATT);
        mapMedlemResulatTilAkDef.put(MedlemResultat.AVKLAR_GYLDIG_MEDLEMSKAPSPERIODE, AksjonspunktDefinisjon.AVKLAR_GYLDIG_MEDLEMSKAPSPERIODE);
        mapMedlemResulatTilAkDef.put(MedlemResultat.AVKLAR_LOVLIG_OPPHOLD, AksjonspunktDefinisjon.AVKLAR_LOVLIG_OPPHOLD);
        mapMedlemResulatTilAkDef.put(MedlemResultat.AVKLAR_OPPHOLDSRETT, AksjonspunktDefinisjon.AVKLAR_OPPHOLDSRETT);
        mapMedlemResulatTilAkDef.put(MedlemResultat.VENT_PÅ_FØDSEL, AksjonspunktDefinisjon.VENT_PÅ_FØDSEL);
    }

    AksjonspunktutlederForMedlemskapSkjæringstidspunkt() {
        //CDI
    }

    @Inject
    public AksjonspunktutlederForMedlemskapSkjæringstidspunkt(VurderMedlemskapTjeneste tjeneste, SkjæringstidspunktTjeneste skjæringstidspunktTjeneste) {
        this.tjeneste = tjeneste;
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
    }

    @Override
    public List<AksjonspunktResultat> utledAksjonspunkterFor(Behandling behandling) {
        LocalDate skjæringstidspunkt = skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling);
        Set<MedlemResultat> resultat = tjeneste.vurderMedlemskap(behandling.getId(), skjæringstidspunkt);
        return mapTilAksjonspunktDef(resultat);
    }

    private List<AksjonspunktResultat> mapTilAksjonspunktDef(Set<MedlemResultat> resultat) {
       return resultat
            .stream()
            .map(mr -> {
                AksjonspunktDefinisjon aksjonspunktDefinisjon = mapMedlemResulatTilAkDef.get(mr);
                if (aksjonspunktDefinisjon == null) {
                    throw new IllegalStateException("Utvikler-feil: Mangler mapping til aksjonspunktDefinisjon for  " + mr.name()); //$NON-NLS-1$
                }
                return AksjonspunktResultat.opprettForAksjonspunkt(aksjonspunktDefinisjon);
            }).collect(Collectors.toList());
    }
}
