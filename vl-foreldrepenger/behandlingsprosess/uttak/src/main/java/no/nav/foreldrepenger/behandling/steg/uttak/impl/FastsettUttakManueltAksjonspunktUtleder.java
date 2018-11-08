package no.nav.foreldrepenger.behandling.steg.uttak.impl;

import java.util.ArrayList;
import java.util.Collection;
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
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.domene.uttak.UttakArbeidTjeneste;

@ApplicationScoped
class FastsettUttakManueltAksjonspunktUtleder implements AksjonspunktUtleder {

    private UttakRepository uttakRepository;
    private AksjonspunktRepository aksjonspunktRepository;
    private UttakArbeidTjeneste uttakArbeidTjeneste;

    FastsettUttakManueltAksjonspunktUtleder() {
        //CDI
    }

    @Inject
    FastsettUttakManueltAksjonspunktUtleder(UttakRepository uttakRepository, AksjonspunktRepository aksjonspunktRepository,
                                            UttakArbeidTjeneste uttakArbeidTjeneste){
        this.uttakRepository = uttakRepository;
        this.aksjonspunktRepository = aksjonspunktRepository;
        this.uttakArbeidTjeneste = uttakArbeidTjeneste;
    }

    public List<AksjonspunktResultat> utledAksjonspunkterFor(Behandling behandling) {
        avsluttÅpentManuellFastsetteUttakAksjonspunkt(behandling);

        UttakResultatEntitet uttakResultat = uttakRepository.hentUttakResultat(behandling);
        Collection<Yrkesaktivitet> yrkesaktiviteter = uttakArbeidTjeneste.hentAlleYrkesaktiviteter(behandling);

        List<AksjonspunktResultat> aksjonspunktArray = new ArrayList<>();

        for (Yrkesaktivitet yrkesaktivitet : yrkesaktiviteter) {
            if (yrkesaktivitet.getArbeidsgiver() != null && yrkesaktivitet.getArbeidsgiver().getVirksomhet() != null && aktivitetErTilknyttetStortinget(yrkesaktivitet)) {
                aksjonspunktArray.add(AksjonspunktResultat.opprettForAksjonspunkt(AksjonspunktDefinisjon.TILKNYTTET_STORTINGET));
                break;
            }
        }

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

    private boolean aktivitetErTilknyttetStortinget(Yrkesaktivitet yrkesaktivitet) {
        return yrkesaktivitet.getArbeidsgiver().getVirksomhet().getOrgnr() != null
            && !uttakRepository.finnOrgManuellÅrsak(yrkesaktivitet.getArbeidsgiver().getVirksomhet().getOrgnr()).isEmpty();
    }

}
