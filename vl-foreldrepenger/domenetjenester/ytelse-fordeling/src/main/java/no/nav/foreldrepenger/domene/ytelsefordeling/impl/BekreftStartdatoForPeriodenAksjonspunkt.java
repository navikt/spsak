package no.nav.foreldrepenger.domene.ytelsefordeling.impl;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.AvklarteUttakDatoer;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.AvklarteUttakDatoerEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.domene.ytelsefordeling.BekreftStartdatoForPerioden;

class BekreftStartdatoForPeriodenAksjonspunkt {

    private final YtelsesFordelingRepository repository;

    BekreftStartdatoForPeriodenAksjonspunkt(BehandlingRepositoryProvider behandlingRepositoryProvider) {
        this.repository = behandlingRepositoryProvider.getYtelsesFordelingRepository();
    }

    public void oppdater(Behandling behandling, BekreftStartdatoForPerioden adapter) {
        final YtelseFordelingAggregat aggregat = repository.hentAggregat(behandling);

        final Optional<AvklarteUttakDatoer> avklarteDatoer = aggregat.getAvklarteDatoer();
        final AvklarteUttakDatoerEntitet entitet = new AvklarteUttakDatoerEntitet(adapter.getStartdatoForPerioden(),
            avklarteDatoer.map(AvklarteUttakDatoer::getEndringsdato).orElse(null));

        repository.lagre(behandling, entitet);
    }
}
