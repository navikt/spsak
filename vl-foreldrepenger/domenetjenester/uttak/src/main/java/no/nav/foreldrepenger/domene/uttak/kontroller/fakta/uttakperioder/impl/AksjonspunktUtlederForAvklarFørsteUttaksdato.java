package no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtleder;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.AvklarteUttakDatoer;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;

@ApplicationScoped
class AksjonspunktUtlederForAvklarFørsteUttaksdato implements AksjonspunktUtleder {

    private YtelsesFordelingRepository ytelsesFordelingRepository;

    AksjonspunktUtlederForAvklarFørsteUttaksdato() {
        // For CDI
    }

    @Inject
    public AksjonspunktUtlederForAvklarFørsteUttaksdato(BehandlingRepositoryProvider behandlingRepositoryProvider) {
        this.ytelsesFordelingRepository = behandlingRepositoryProvider.getYtelsesFordelingRepository();
    }

    @Override
    public List<AksjonspunktResultat> utledAksjonspunkterFor(Behandling behandling) {
        YtelseFordelingAggregat ytelseFordelingAggregat = ytelsesFordelingRepository.hentAggregat(behandling);
        Optional<AvklarteUttakDatoer> avklarteUttakDatoer = ytelseFordelingAggregat.getAvklarteDatoer();
        if (erManueltSattFørsteUttaksdatoForskjelligFraFørstePeriodeISøknad(ytelseFordelingAggregat, avklarteUttakDatoer)) {
            return Collections.singletonList(AksjonspunktResultat.opprettForAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_FØRSTE_UTTAKSDATO));
        }

        return Collections.emptyList();
    }

    private boolean erManueltSattFørsteUttaksdatoForskjelligFraFørstePeriodeISøknad(YtelseFordelingAggregat ytelseFordelingAggregat,
                                                                                    Optional<AvklarteUttakDatoer> avklarteUttakDatoer) {
        if (avklarteUttakDatoer.isPresent()) {
            Optional<OppgittPeriode> førsteSøknadsperiode = ytelseFordelingAggregat.getGjeldendeSøknadsperioder().getOppgittePerioder()
                .stream()
                .min(Comparator.comparing(OppgittPeriode::getFom));
            if (førsteSøknadsperiode.isPresent() && avklarteUttakDatoer.get().getFørsteUttaksDato() != null) {
                return !førsteSøknadsperiode.get().getFom().equals(avklarteUttakDatoer.get().getFørsteUttaksDato());
            }
        }
        return false;
    }

}
