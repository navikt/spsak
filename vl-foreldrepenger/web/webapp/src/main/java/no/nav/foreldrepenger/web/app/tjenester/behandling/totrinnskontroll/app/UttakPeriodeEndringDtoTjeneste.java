package no.nav.foreldrepenger.web.app.tjenester.behandling.totrinnskontroll.app;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.Totrinnresultatgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.FastsettePerioderEndringTjeneste;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.KontrollerFaktaUttakTjeneste;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.UttakPeriodeEndringDto;

@ApplicationScoped
public class UttakPeriodeEndringDtoTjeneste {
    private KontrollerFaktaUttakTjeneste kontrollerFaktaUttakTjeneste;
    private FastsettePerioderEndringTjeneste fastsettePerioderEndringTjeneste;

    protected UttakPeriodeEndringDtoTjeneste() {
        // for CDI proxy
    }

    @Inject
    public UttakPeriodeEndringDtoTjeneste(@FagsakYtelseTypeRef("FP") KontrollerFaktaUttakTjeneste kontrollerFaktaUttakTjeneste,
                                          FastsettePerioderEndringTjeneste fastsettePerioderEndringTjeneste) {
        this.kontrollerFaktaUttakTjeneste = kontrollerFaktaUttakTjeneste;
        this.fastsettePerioderEndringTjeneste = fastsettePerioderEndringTjeneste;
    }

    List<UttakPeriodeEndringDto> hentEndringPåUttakPerioder(Totrinnsvurdering aksjonspunkt,
                                                                    Behandling behandling,
                                                                    Optional<Totrinnresultatgrunnlag> totrinnresultatgrunnlag) {
        if (aksjonspunkt.getAksjonspunktDefinisjon().equals(AksjonspunktDefinisjon.AVKLAR_FAKTA_UTTAK) ||
            aksjonspunkt.getAksjonspunktDefinisjon().equals(AksjonspunktDefinisjon.MANUELL_AVKLAR_FAKTA_UTTAK)) {
            if (totrinnresultatgrunnlag.flatMap(Totrinnresultatgrunnlag::getYtelseFordelingGrunnlagEntitetId).isPresent()) {
                return kontrollerFaktaUttakTjeneste.finnEndringMellomOppgittOgGjeldendePerioder(totrinnresultatgrunnlag.flatMap(Totrinnresultatgrunnlag::getYtelseFordelingGrunnlagEntitetId).get()); // NOSONAR
            }
            return kontrollerFaktaUttakTjeneste.finnEndringMellomOppgittOgGjeldendePerioder(behandling);
        }
        if (aksjonspunkt.getAksjonspunktDefinisjon().equals(AksjonspunktDefinisjon.OVERSTYRING_AV_UTTAKPERIODER) ||
            aksjonspunkt.getAksjonspunktDefinisjon().equals(AksjonspunktDefinisjon.FASTSETT_UTTAKPERIODER) ||
            aksjonspunkt.getAksjonspunktDefinisjon().equals(AksjonspunktDefinisjon.TILKNYTTET_STORTINGET)) {
            if (totrinnresultatgrunnlag.flatMap(Totrinnresultatgrunnlag::getUttakResultatEntitetId).isPresent()) {
                return fastsettePerioderEndringTjeneste.finnEndringerMellomOpprinneligOgOverstyrt(behandling, totrinnresultatgrunnlag.flatMap(Totrinnresultatgrunnlag::getUttakResultatEntitetId).get()); // NOSONAR
            }
            return fastsettePerioderEndringTjeneste.finnEndringerMellomOpprinneligOgOverstyrt(behandling);
        }
        return Collections.emptyList();
    }
}
