package no.nav.foreldrepenger.behandling.steg.uttak.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtleder;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;

@ApplicationScoped
class FastsettUttakManueltAksjonspunktUtleder implements AksjonspunktUtleder {

    private UttakRepository uttakRepository;
    private AksjonspunktRepository aksjonspunktRepository;

    FastsettUttakManueltAksjonspunktUtleder() {
        //CDI
    }

    @Inject
    FastsettUttakManueltAksjonspunktUtleder(UttakRepository uttakRepository, AksjonspunktRepository aksjonspunktRepository){
        this.uttakRepository = uttakRepository;
        this.aksjonspunktRepository = aksjonspunktRepository;
    }

    public List<AksjonspunktResultat> utledAksjonspunkterFor(Behandling behandling) {
        avsluttÅpentManuellFastsetteUttakAksjonspunkt(behandling);

        UttakResultatEntitet uttakResultat = uttakRepository.hentUttakResultat(behandling);

        List<AksjonspunktResultat> aksjonspunktArray = new ArrayList<>();

        for (UttakResultatPeriodeEntitet periode : uttakResultat.getGjeldendePerioder().getPerioder()) {

            if (periode.getPeriodeResultatType().equals(PeriodeResultatType.MANUELL_BEHANDLING)){
                aksjonspunktArray.add(AksjonspunktResultat.opprettForAksjonspunkt(AksjonspunktDefinisjon.FASTSETT_UTTAKPERIODER));
                break;
            }
        }

        if (aksjonspunktArray.isEmpty()) {
            return Collections.emptyList();
        }
        return aksjonspunktArray.stream().distinct().collect(Collectors.toList());
    }

    private void avsluttÅpentManuellFastsetteUttakAksjonspunkt(Behandling behandling) {
        //Hack for å unnga å ha et åpent aksjonspunkt for uttak selv om ny utleding ikke gir aksjonspunkt. Skjer ved tilbakehopp
        behandling.getAksjonspunkter().stream()
            .filter(ap -> AksjonspunktDefinisjon.FASTSETT_UTTAKPERIODER.equals(ap.getAksjonspunktDefinisjon()))
            .filter(ap -> !ap.erUtført())
            .forEach(ap -> aksjonspunktRepository.setTilAvbrutt(ap));
    }

}
