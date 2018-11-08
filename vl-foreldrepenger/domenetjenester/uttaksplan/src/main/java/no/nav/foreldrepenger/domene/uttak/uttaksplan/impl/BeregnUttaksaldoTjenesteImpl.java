package no.nav.foreldrepenger.domene.uttak.uttaksplan.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskonto;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskontoberegning;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.domene.uttak.perioder.PerioderUtenHelgUtil;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.BeregnUttaksaldoTjeneste;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.RelatertBehandlingTjeneste;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Virkedager;

@ApplicationScoped
public class BeregnUttaksaldoTjenesteImpl implements BeregnUttaksaldoTjeneste {

    private BehandlingRepositoryProvider repositoryProvider;
    private RelatertBehandlingTjeneste relatertBehandlingTjeneste;

    BeregnUttaksaldoTjenesteImpl() {
        //CDI
    }

    @Inject
    public BeregnUttaksaldoTjenesteImpl(BehandlingRepositoryProvider repositoryProvider, RelatertBehandlingTjeneste relatertBehandlingTjeneste) {
        this.repositoryProvider = repositoryProvider;
        this.relatertBehandlingTjeneste = relatertBehandlingTjeneste;
    }

    @Override
    public Optional<Integer> beregnDisponibleDager(Behandling behandling) {
        Optional<Stønadskontoberegning> stønadskontoberegning = repositoryProvider.getFagsakRelasjonRepository().finnRelasjonFor(behandling.getFagsak()).getStønadskontoberegning();
        int maxDager = 0;
        if (stønadskontoberegning.isPresent()) {
            for (Stønadskonto stønadskonto : stønadskontoberegning.get().getStønadskontoer()) {
                if (!StønadskontoType.FLERBARNSDAGER.equals(stønadskonto.getStønadskontoType())) {
                    maxDager += stønadskonto.getMaxDager();
                }
            }
            int trekkdagerForSøker = beregnTrekkdagerForSøker(behandling);
            int trekkdagerForAnnenPart = beregnTrekkdagerForAnnenPart(behandling);

            int disponibleDager = maxDager - trekkdagerForSøker - trekkdagerForAnnenPart;
            return Optional.of(disponibleDager);
        }
        return Optional.empty();
    }


    private int beregnTrekkdagerForSøker(Behandling behandling) {
        //søker
        Optional<UttakResultatEntitet> uttakResultatSøker = repositoryProvider.getUttakRepository().hentUttakResultatHvisEksisterer(behandling);

        Map<StønadskontoType, Map<UttakAktivitetEntitet, Integer>> trekkdagerPerStønadskonto = new HashMap<>();
        if (uttakResultatSøker.isPresent()) {
            List<UttakResultatPeriodeEntitet> uttakPerioderSøker = uttakResultatSøker.get().getGjeldendePerioder().getPerioder();
            for (UttakResultatPeriodeEntitet periode : uttakPerioderSøker) {
                List<UttakResultatPeriodeAktivitetEntitet> aktiviteter = periode.getAktiviteter();
                for (UttakResultatPeriodeAktivitetEntitet aktivitet : aktiviteter) {
                    leggTilTrekkdager(trekkdagerPerStønadskonto, aktivitet, aktivitet.getTrekkdager());
                }
            }
        }

        return beregnMinimumBrukteDager(trekkdagerPerStønadskonto);
    }

    private int beregnMinimumBrukteDager(Map<StønadskontoType, Map<UttakAktivitetEntitet, Integer>> trekkdagerPerStønadskonto) {
        int brukteDager = 0;
        for (Map.Entry<StønadskontoType, Map<UttakAktivitetEntitet, Integer>> entry : trekkdagerPerStønadskonto.entrySet()) {
            Map<UttakAktivitetEntitet, Integer> value = entry.getValue();
            int minsteAntallTrekkdager = value.values().stream()
                .filter(antallDager -> antallDager > 0)
                .mapToInt(v -> v).min().orElse(0);
            brukteDager += minsteAntallTrekkdager;
        }
        return brukteDager;
    }

    private int beregnTrekkdagerForAnnenPart(Behandling behandling) {
        // søker
        Optional<UttakResultatEntitet> uttakResultatSøker = repositoryProvider.getUttakRepository().hentUttakResultatHvisEksisterer(behandling);
        List<UttakResultatPeriodeEntitet> søkerPerioder;

        if (uttakResultatSøker.isPresent()) {
            søkerPerioder = uttakResultatSøker.get().getGjeldendePerioder().getPerioder();
        } else {
            søkerPerioder = Collections.emptyList();
        }

        // annenpart
        Optional<UttakResultatEntitet> uttakResultatAnnenPart = relatertBehandlingTjeneste.hentAnnenPartsGjeldendeUttaksplan(behandling);

        Map<StønadskontoType, Map<UttakAktivitetEntitet, Integer>> trekkdagerPerStønadskonto = new HashMap<>();
        if (uttakResultatAnnenPart.isPresent()) {
            List<UttakResultatPeriodeEntitet> uttakPerioderAnnenPart = uttakResultatAnnenPart.get().getGjeldendePerioder().getPerioder();
            for (UttakResultatPeriodeEntitet periode : uttakPerioderAnnenPart) {
                List<UttakResultatPeriodeEntitet> overlappendePerioder = finnOverlappendePerioderUtenSamtidigUttak(periode.getFom(), periode.getTom(),
                    søkerPerioder);

                for (UttakResultatPeriodeAktivitetEntitet aktivitet : periode.getAktiviteter()) {
                    int trekkdager = finnAntallTrekkdagerUtenOverlapp(aktivitet, overlappendePerioder);
                    leggTilTrekkdager(trekkdagerPerStønadskonto, aktivitet, trekkdager);
                }
            }
        }
        return beregnMinimumBrukteDager(trekkdagerPerStønadskonto);
    }


    private void leggTilTrekkdager(Map<StønadskontoType, Map<UttakAktivitetEntitet, Integer>> trekkdagerPerStønadskonto, UttakResultatPeriodeAktivitetEntitet aktivitet, int trekkdager) {
        if (trekkdager > 0) {
            Map<UttakAktivitetEntitet, Integer> trekkdagerPerAktivitet = trekkdagerPerStønadskonto.computeIfAbsent(aktivitet.getTrekkonto(), a -> new HashMap());
            UttakAktivitetEntitet uttakAktivitet = aktivitet.getUttakAktivitet();
            if (trekkdagerPerAktivitet.containsKey(uttakAktivitet)) {
                trekkdagerPerAktivitet.compute(uttakAktivitet, (k, v) -> v + trekkdager);
            } else {
                trekkdagerPerAktivitet.put(uttakAktivitet, trekkdager);
            }
        }
    }

    @Override
    public List<UttakResultatPeriodeEntitet> finnOverlappendePerioderUtenSamtidigUttak(LocalDate fom, LocalDate tom, List<UttakResultatPeriodeEntitet> perioder) {
        return perioder.stream()
            .filter(p -> !p.isSamtidigUttak())
            .filter(p -> PerioderUtenHelgUtil.perioderUtenHelgOverlapper(fom, tom, p.getFom(), p.getTom()))
            .filter(this::erInnvilgetEllerAvslåttMedTrekkdager)
            .sorted(Comparator.comparing(UttakResultatPeriodeEntitet::getFom))
            .collect(Collectors.toList());
    }

    @Override
    public boolean erInnvilgetEllerAvslåttMedTrekkdager(UttakResultatPeriodeEntitet uttakResultatPeriode) {
        return PeriodeResultatType.INNVILGET.equals(uttakResultatPeriode.getPeriodeResultatType()) ||
            (PeriodeResultatType.AVSLÅTT.equals(uttakResultatPeriode.getPeriodeResultatType()) && uttakResultatPeriode.getAktiviteter().stream()
                .anyMatch(aktivitet -> aktivitet.getTrekkdager() > 0));
    }

    @Override
    public int finnAntallTrekkdagerUtenOverlapp(UttakResultatPeriodeAktivitetEntitet aktivititet, List<UttakResultatPeriodeEntitet> overlappendePerioder) {
        if (overlappendePerioder.isEmpty()) {
            return aktivititet.getTrekkdager();
        }

        int antallTrekkdager = 0;
        LocalDate startdato = aktivititet.getFom();
        for (UttakResultatPeriodeEntitet periode : overlappendePerioder) {
            if (periode.getFom().isAfter(startdato)) {
                antallTrekkdager += finnVektetTrekkdager(startdato, periode.getFom().minusDays(1), aktivititet);
            }
            startdato = periode.getTom().plusDays(1);
        }

        if (!startdato.isAfter(aktivititet.getTom())) {
            antallTrekkdager += finnVektetTrekkdager(startdato, aktivititet.getTom(), aktivititet);
        }
        return antallTrekkdager;
    }

    private int finnVektetTrekkdager(LocalDate fom, LocalDate tom, UttakResultatPeriodeAktivitetEntitet aktivititet) {
        int virkedagerInnenfor = Virkedager.beregnAntallVirkedager(fom, tom);
        int virkedagerHele = Virkedager.beregnAntallVirkedager(aktivititet.getFom(), aktivititet.getTom());
        return BigDecimal.valueOf(aktivititet.getTrekkdager() * virkedagerInnenfor) //NOSONAR
            .divide(BigDecimal.valueOf(virkedagerHele), 0, RoundingMode.DOWN)
            .intValue();
    }
}
