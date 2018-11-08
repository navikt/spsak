package no.nav.foreldrepenger.domene.ytelsefordeling.impl;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PeriodeAleneOmsorgEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PeriodeUtenOmsorg;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PeriodeUtenOmsorgEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PerioderAleneOmsorgEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PerioderUtenOmsorgEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.domene.ytelsefordeling.BekreftFaktaForOmsorgVurderingAksjonspunktDto;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

class BekreftFaktaForOmsorgAksjonspunkt {

    private YtelsesFordelingRepository ytelsesFordelingRepository;

    public BekreftFaktaForOmsorgAksjonspunkt(BehandlingRepositoryProvider behandlingRepositoryProvider) {
        this.ytelsesFordelingRepository = behandlingRepositoryProvider.getYtelsesFordelingRepository();
    }

    void oppdater(Behandling behandling, BekreftFaktaForOmsorgVurderingAksjonspunktDto adapter) {
        if(adapter.getAleneomsorg() != null) {
            bekreftFaktaAleneomsorg(behandling, adapter);
        }
        if(adapter.getOmsorg() != null) {
            bekreftFaktaOmsorg(behandling, adapter);
        }
    }

    private void bekreftFaktaOmsorg(Behandling behandling, BekreftFaktaForOmsorgVurderingAksjonspunktDto adapter) {
        PerioderUtenOmsorgEntitet perioderUtenOmsorg = new PerioderUtenOmsorgEntitet();
        if(Boolean.FALSE.equals(adapter.getOmsorg())) {
            mapPeriodeUtenOmsorgperioder(adapter.getIkkeOmsorgPerioder())
                .forEach(perioderUtenOmsorg::leggTil);
            ytelsesFordelingRepository.lagre(behandling, new PerioderUtenOmsorgEntitet(perioderUtenOmsorg));
        } else {
            ytelsesFordelingRepository.lagre(behandling, perioderUtenOmsorg);
        }
    }

    private void bekreftFaktaAleneomsorg(Behandling behandling, BekreftFaktaForOmsorgVurderingAksjonspunktDto adapter) {
        PerioderAleneOmsorgEntitet perioderAleneOmsorgEntitet = new PerioderAleneOmsorgEntitet();
        if(Boolean.FALSE.equals(adapter.getAleneomsorg())) {
            ytelsesFordelingRepository.lagre(behandling, perioderAleneOmsorgEntitet);
        } else {
            // Legger inn en dummy periode for å indikere saksbehandlers valg. Inntil vi faktisk har perioder her
            perioderAleneOmsorgEntitet.leggTil(new PeriodeAleneOmsorgEntitet(LocalDate.now(), LocalDate.now()));
            ytelsesFordelingRepository.lagre(behandling, new PerioderAleneOmsorgEntitet(perioderAleneOmsorgEntitet));
        }
    }

    private static List<PeriodeUtenOmsorg> mapPeriodeUtenOmsorgperioder(List<DatoIntervallEntitet> ikkeOmsorgPeriodes) {
       return ikkeOmsorgPeriodes.stream().map(BekreftFaktaForOmsorgAksjonspunkt::mapPeriodeUtenOmsorg).collect(Collectors.toList());
    }

    private static PeriodeUtenOmsorg mapPeriodeUtenOmsorg(DatoIntervallEntitet ikkeOmsorgPeriode) {
        return new PeriodeUtenOmsorgEntitet(ikkeOmsorgPeriode.getFomDato(), ikkeOmsorgPeriode.getTomDato());
    }
}
