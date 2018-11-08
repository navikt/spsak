package no.nav.foreldrepenger.domene.uttak.uttaksplan.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjon;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskonto;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskontoberegning;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.BeregnMorsMaksdatoTjeneste;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.RelatertBehandlingTjeneste;

@ApplicationScoped
public class BeregnMorsMaksdatoTjenesteImpl implements BeregnMorsMaksdatoTjeneste {

    private RelatertBehandlingTjeneste relatertBehandlingTjeneste;
    private BehandlingRepositoryProvider repositoryProvider;

    BeregnMorsMaksdatoTjenesteImpl() {
        // CDI
    }

    @Inject
    public BeregnMorsMaksdatoTjenesteImpl(BehandlingRepositoryProvider repositoryProvider, RelatertBehandlingTjeneste relatertBehandlingTjeneste) {
        this.repositoryProvider = repositoryProvider;
        this.relatertBehandlingTjeneste = relatertBehandlingTjeneste;
    }

    @Override
    public Optional<LocalDate> beregnMorsMaksdato(Behandling behandling) {
        if (RelasjonsRolleType.MORA.equals(behandling.getFagsak().getRelasjonsRolleType())) {
            return Optional.empty();
        }

        Optional<UttakResultatEntitet> uttakResultat = relatertBehandlingTjeneste.hentAnnenPartsGjeldendeUttaksplan(behandling);
        if (uttakResultat.isPresent()) {
            UttakResultatPerioderEntitet gjeldenePerioder = uttakResultat.get().getGjeldendePerioder();

            Map<UttakAktivitetEntitet, List<UttakResultatPeriodeAktivitetEntitet>> perArbeidsforhold = finnPerioderPerArbeidsforhold(gjeldenePerioder.getPerioder());
            LocalDate maksdato = null;
            for (Map.Entry<UttakAktivitetEntitet, List<UttakResultatPeriodeAktivitetEntitet>> entry : perArbeidsforhold.entrySet()) {
                List<UttakResultatPeriodeAktivitetEntitet> perioder = entry.getValue();
                LocalDate sisteUttaksdag = finnMorsSisteUttaksdag(perioder);
                int tilgjengeligeStønadsdager = beregnTilgjengeligeStønadsdager(perioder, behandling.getFagsak());
                LocalDate tmpMaksdato = plusVirkedager(sisteUttaksdag, tilgjengeligeStønadsdager);
                if (maksdato == null || maksdato.isBefore(tmpMaksdato)) {
                    maksdato = tmpMaksdato;
                }
            }
            return Optional.ofNullable(maksdato);
        }
        return Optional.empty();
    }

    // TODO PK-48734 Her trengs det litt refaktorering
    @Override
    public Optional<LocalDate> beregnMaksdatoForeldrepenger(Behandling behandling) {
        Optional<UttakResultatEntitet> uttakResultat = relatertBehandlingTjeneste.hentAnnenPartsGjeldendeUttaksplan(behandling);
        if (uttakResultat.isPresent()) {
            UttakResultatPerioderEntitet gjeldenePerioder = uttakResultat.get().getGjeldendePerioder();

            Map<UttakAktivitetEntitet, List<UttakResultatPeriodeAktivitetEntitet>> perArbeidsforhold = finnPerioderPerArbeidsforhold(gjeldenePerioder.getPerioder());
            LocalDate maksdato = null;
            for (Map.Entry<UttakAktivitetEntitet, List<UttakResultatPeriodeAktivitetEntitet>> entry : perArbeidsforhold.entrySet()) {
                List<UttakResultatPeriodeAktivitetEntitet> perioder = entry.getValue();
                LocalDate sisteUttaksdag = finnMorsSisteUttaksdag(perioder);
                int tilgjengeligeStønadsdager = beregnTilgjengeligeStønadsdagerForeldrepenger(perioder, behandling.getFagsak());
                LocalDate tmpMaksdato = plusVirkedager(sisteUttaksdag, tilgjengeligeStønadsdager);
                if (maksdato == null || maksdato.isBefore(tmpMaksdato)) {
                    maksdato = tmpMaksdato;
                }
            }
            return Optional.ofNullable(maksdato);
        }
        return Optional.empty();
    }

    private LocalDate finnMorsSisteUttaksdag(List<UttakResultatPeriodeAktivitetEntitet> perioder) {
        return perioder.stream()
            .filter(p -> p.getTrekkdager() > 0 || PeriodeResultatType.INNVILGET.equals(p.getPeriode().getPeriodeResultatType()))
            .max(Comparator.comparing(UttakResultatPeriodeAktivitetEntitet::getTom))
            .map(UttakResultatPeriodeAktivitetEntitet::getTom)
            .orElseThrow(IllegalStateException::new);
    }

    private Map<UttakAktivitetEntitet, List<UttakResultatPeriodeAktivitetEntitet>> finnPerioderPerArbeidsforhold(List<UttakResultatPeriodeEntitet> perioder) {
        return perioder.stream()
            .map(UttakResultatPeriodeEntitet::getAktiviteter)
            .flatMap(Collection::stream)
            .collect(Collectors.groupingBy(UttakResultatPeriodeAktivitetEntitet::getUttakAktivitet));
    }

    private int beregnTilgjengeligeStønadsdager(List<UttakResultatPeriodeAktivitetEntitet> perioder, Fagsak fagsak) {
        FagsakRelasjon fagsakRelasjon = repositoryProvider.getFagsakRelasjonRepository().finnRelasjonFor(fagsak);
        Optional<Stønadskontoberegning> optionalStønadskontoberegning = fagsakRelasjon.getStønadskontoberegning();
        if (optionalStønadskontoberegning.isPresent()) {
            Set<Stønadskonto> stønadskontoer = optionalStønadskontoberegning.get().getStønadskontoer();
            int tilgjengeligMødrekvote = beregnTilgjengeligeDagerFor(StønadskontoType.MØDREKVOTE, perioder, stønadskontoer);
            int tilgjengeligFellesperiode = beregnTilgjengeligeDagerFor(StønadskontoType.FELLESPERIODE, perioder, stønadskontoer);
            return tilgjengeligMødrekvote + tilgjengeligFellesperiode;
        }
        return 0;
    }

    private int beregnTilgjengeligeStønadsdagerForeldrepenger(List<UttakResultatPeriodeAktivitetEntitet> perioder, Fagsak fagsak) {
        FagsakRelasjon fagsakRelasjon = repositoryProvider.getFagsakRelasjonRepository().finnRelasjonFor(fagsak);
        Optional<Stønadskontoberegning> optionalStønadskontoberegning = fagsakRelasjon.getStønadskontoberegning();
        if (optionalStønadskontoberegning.isPresent()) {
            Set<Stønadskonto> stønadskontoer = optionalStønadskontoberegning.get().getStønadskontoer();
            return beregnTilgjengeligeDagerFor(StønadskontoType.FORELDREPENGER, perioder, stønadskontoer);
        }
        return 0;
    }

    private int beregnTilgjengeligeDagerFor(StønadskontoType stønadskontoType, List<UttakResultatPeriodeAktivitetEntitet> perioder, Set<Stønadskonto> stønadskontoer) {
        Optional<Stønadskonto> optionalStønadskonto = stønadskontoer.stream().filter(s -> stønadskontoType.equals(s.getStønadskontoType())).findFirst();
        if (optionalStønadskonto.isPresent()) {
            int brukteDager = perioder.stream()
                .filter(p -> stønadskontoType.equals(p.getTrekkonto()) && p.getTrekkdager() > 0)
                .mapToInt(UttakResultatPeriodeAktivitetEntitet::getTrekkdager).sum();

            int tilgjengeligeDager = optionalStønadskonto.get().getMaxDager() - brukteDager;
            return tilgjengeligeDager > 0 ? tilgjengeligeDager : 0;
        }
        return 0;
    }

    private LocalDate plusVirkedager(LocalDate fom, int virkedager) {
        int virkedager_pr_uke = 5;
        int dager_pr_uke = 7;
        LocalDate justertDatoForHelg = fom;
        if (fom.getDayOfWeek().equals(DayOfWeek.SATURDAY) || fom.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            justertDatoForHelg = fom.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));
        }
        int padBefore = justertDatoForHelg.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();

        int paddedVirkedager = virkedager + padBefore;

        int uker = paddedVirkedager / virkedager_pr_uke;
        int dager = paddedVirkedager % virkedager_pr_uke;
        return justertDatoForHelg.plusDays((uker * dager_pr_uke) + dager - (long) padBefore);
    }
}
