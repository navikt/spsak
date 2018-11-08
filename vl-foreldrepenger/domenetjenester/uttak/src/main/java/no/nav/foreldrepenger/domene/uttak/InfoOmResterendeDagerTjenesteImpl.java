package no.nav.foreldrepenger.domene.uttak;

import static no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType.FEDREKVOTE;
import static no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType.FELLESPERIODE;
import static no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType.FORELDREPENGER;
import static no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType.MØDREKVOTE;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.domene.uttak.saldo.Saldoer;
import no.nav.foreldrepenger.domene.uttak.saldo.StønadskontoSaldoTjeneste;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.RelatertBehandlingTjeneste;

@ApplicationScoped
public class InfoOmResterendeDagerTjenesteImpl implements InfoOmResterendeDagerTjeneste {

    private UttakRepository uttakRepository;
    private StønadskontoSaldoTjeneste stønadskontoSaldoTjeneste;
    private RelatertBehandlingTjeneste relatertBehandlingTjeneste;

    public InfoOmResterendeDagerTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public InfoOmResterendeDagerTjenesteImpl(StønadskontoSaldoTjeneste stønadskontoSaldoTjeneste,
                                             UttakRepository uttakRepository,
                                             RelatertBehandlingTjeneste relatertBehandlingTjeneste) {
        this.stønadskontoSaldoTjeneste = stønadskontoSaldoTjeneste;
        this.uttakRepository = uttakRepository;
        this.relatertBehandlingTjeneste = relatertBehandlingTjeneste;
    }

    @Override
    public Integer getDisponibleDager(Behandling behandling, Boolean aleneOmsorg, Boolean annenForelderHarRett) {
        Saldoer saldoer = stønadskontoSaldoTjeneste.finnSaldoer(behandling);
        boolean harRettPåAlle = aleneOmsorg || (!annenForelderHarRett);
        return (morEllerFar(behandling) ? getAktuelleStønadskontoerForMor(harRettPåAlle) : getAktuelleStønadskontoerForFar(harRettPåAlle)).stream()
            .mapToInt(saldoer::saldo)
            .sum();
    }

    private Set<StønadskontoType> getAktuelleStønadskontoerForMor(boolean rettPåAlleRestDager) {
        return rettPåAlleRestDager ?
            Stream.of(FORELDREPENGER, FORELDREPENGER_FØR_FØDSEL).collect(Collectors.toSet()) :
            Stream.of(FORELDREPENGER_FØR_FØDSEL, MØDREKVOTE).collect(Collectors.toSet());
    }

    private Set<StønadskontoType> getAktuelleStønadskontoerForFar(boolean rettPåAlleRestDager) {
        return rettPåAlleRestDager ?
            Stream.of(FORELDREPENGER).collect(Collectors.toSet()) :
            Stream.of(FEDREKVOTE).collect(Collectors.toSet());
    }

    private boolean morEllerFar(Behandling behandling) {
        return RelasjonsRolleType.MORA.equals(behandling.getRelasjonsRolleType());
    }

    @Override
    public Integer getDisponibleFellesDager(Behandling behandling) {
        return stønadskontoSaldoTjeneste.finnSaldoer(behandling).saldo(FELLESPERIODE);
    }

    @Override
    public Optional<LocalDate> getSisteDagAvSistePeriodeTilAnnenForelder(Behandling behandling) {
        List<UttakResultatPeriodeEntitet> annenPartsPerioder = hentAnnenPartsUttakResultat(behandling)
            .map(UttakResultatEntitet::getGjeldendePerioder)
            .map(UttakResultatPerioderEntitet::getPerioder)
            .orElse(Collections.emptyList());
        return annenPartsPerioder.stream()
            .filter(this::erInnvilgetPeriode)
            .map(UttakResultatPeriodeEntitet::getTom)
            .max(LocalDate::compareTo);
    }

    private Optional<UttakResultatEntitet> hentAnnenPartsUttakResultat(Behandling behandling) {
        Optional<Behandling> annenPartsBehandling = relatertBehandlingTjeneste.hentAnnenPartsGjeldendeBehandling(behandling.getFagsak());
        if (annenPartsBehandling.map(Behandling::getFagsak)
            .filter(this::erLøpendeFagsak)
            .isPresent()) {
            return uttakRepository.hentUttakResultatHvisEksisterer(annenPartsBehandling.get());
        }
        return Optional.empty();
    }

    private boolean erInnvilgetPeriode(UttakResultatPeriodeEntitet periode) {
        return PeriodeResultatType.INNVILGET.equals(periode.getPeriodeResultatType());
    }

    private boolean erLøpendeFagsak(Fagsak fagsak) {
            return FagsakStatus.LØPENDE.getKode().equals(fagsak.getStatus().getKode());
    }
}
