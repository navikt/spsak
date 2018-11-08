package no.nav.foreldrepenger.domene.uttak.saldo.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjonRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskonto;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskontoberegning;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.domene.uttak.saldo.Aktivitet;
import no.nav.foreldrepenger.domene.uttak.saldo.Saldoer;
import no.nav.foreldrepenger.domene.uttak.saldo.StønadskontoSaldoTjeneste;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.BeregnUttaksaldoTjeneste;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.RelatertBehandlingTjeneste;

@Dependent
public class StønadskontoSaldoTjenesteImpl implements StønadskontoSaldoTjeneste {

    private FagsakRelasjonRepository fagsakRelasjonRepository;
    private UttakRepository uttakRepository;
    private BeregnUttaksaldoTjeneste beregnUttaksaldoTjeneste;
    private RelatertBehandlingTjeneste relatertBehandlingTjeneste;
    private MaxDatoUttakTjeneste maxDatoUttakTjeneste = new MaxDatoUttakTjeneste();

    StønadskontoSaldoTjenesteImpl() {
        //For CDI
    }

    @Inject
    public StønadskontoSaldoTjenesteImpl(BehandlingRepositoryProvider behandlingRepositoryProvider, BeregnUttaksaldoTjeneste beregnUttaksaldoTjeneste, RelatertBehandlingTjeneste relatertBehandlingTjeneste) {
        this.fagsakRelasjonRepository = behandlingRepositoryProvider.getFagsakRelasjonRepository();
        this.uttakRepository = behandlingRepositoryProvider.getUttakRepository();
        this.beregnUttaksaldoTjeneste = beregnUttaksaldoTjeneste;
        this.relatertBehandlingTjeneste = relatertBehandlingTjeneste;
        this.maxDatoUttakTjeneste = new MaxDatoUttakTjeneste();
    }

    @Override
    public Saldoer finnSaldoer(Behandling behandling) {

        Optional<Set<Stønadskonto>> stønadskontoer = fagsakRelasjonRepository
            .finnRelasjonFor(behandling.getFagsak())
            .getStønadskontoberegning()
            .map(Stønadskontoberegning::getStønadskontoer);

        SaldoerImpl saldoer = new SaldoerImpl();

        if (stønadskontoer.isPresent()) {
            Optional<UttakResultatEntitet> uttakResultat = uttakRepository.hentUttakResultatHvisEksisterer(behandling);
            Optional<UttakResultatEntitet> uttakResultatAnnenPart = relatertBehandlingTjeneste.hentAnnenPartsGjeldendeUttaksplan(behandling);
            settMaxDager(saldoer, stønadskontoer.get());
            oppdaterSaldoer(uttakResultat, uttakResultatAnnenPart, stønadskontoer.get(), saldoer);
            Optional<LocalDate> sisteUttaksdato = finnSisteUttaksdato(uttakResultat, uttakResultatAnnenPart);
            saldoer.setMaksDatoUttak(maxDatoUttakTjeneste.beregnMaxDatoUttak(saldoer, behandling.getFagsak().getRelasjonsRolleType(), sisteUttaksdato));
        }
        return saldoer;
    }


    private void settMaxDager(SaldoerImpl saldoer, Set<Stønadskonto> stønadskontoer) {
        stønadskontoer.forEach(
            stønadskonto -> saldoer.setMaxDager(stønadskonto.getStønadskontoType(), stønadskonto.getMaxDager())
        );
    }

    private void oppdaterSaldoer(Optional<UttakResultatEntitet> uttakResultat, Optional<UttakResultatEntitet> uttakResultatAnnenPart, Set<Stønadskonto> stønadskontoer, SaldoerImpl saldoer) {
        if (uttakResultat.isPresent()) {
            UttakResultatEntitet uttakResultatEntitet = uttakResultat.get();
            lagSaldoer(stønadskontoer, saldoer, uttakResultatEntitet);
            uttakResultatAnnenPart.ifPresent(uttakResultatEntitetAnnenPart -> leggTilAktiviteterAnnenPart(uttakResultat.get(), uttakResultatEntitetAnnenPart, saldoer, stønadskontoer));
        }
    }

    private Optional<LocalDate> finnSisteUttaksdato(Optional<UttakResultatEntitet> uttakResultat, Optional<UttakResultatEntitet> uttakResultatAnnenPart) {
        if (uttakResultat.isPresent()) {
            boolean erManuellBehandling = uttakResultat.get().getGjeldendePerioder().getPerioder()
                .stream()
                .anyMatch(UttakResultatPeriodeEntitet::opprinneligSendtTilManueltBehandling);

            if (erManuellBehandling) {
                return Optional.empty();
            }
        }

        List<UttakResultatPeriodeEntitet> allePerioder = new ArrayList<>();

        uttakResultatAnnenPart.ifPresent(uttakResultatEntitet -> allePerioder.addAll(uttakResultatEntitet.getGjeldendePerioder().getPerioder()));
        uttakResultat.ifPresent(uttakResultatEntitet -> allePerioder.addAll(uttakResultatEntitet.getGjeldendePerioder().getPerioder()));

        return allePerioder.stream()
            .filter(p -> beregnUttaksaldoTjeneste.erInnvilgetEllerAvslåttMedTrekkdager(p))
            .max(Comparator.comparing(UttakResultatPeriodeEntitet::getTom))
            .map(UttakResultatPeriodeEntitet::getTom);
    }

    private void lagSaldoer(Set<Stønadskonto> stønadskontoer, SaldoerImpl saldoer, UttakResultatEntitet uttakResultat) {
        for (Stønadskonto stønadskonto : stønadskontoer) {
            for (UttakResultatPeriodeEntitet periode : uttakResultat.getGjeldendePerioder().getPerioder()) {
                if (periode.getPeriodeResultatType().equals(PeriodeResultatType.MANUELL_BEHANDLING) || beregnUttaksaldoTjeneste.erInnvilgetEllerAvslåttMedTrekkdager(periode)) {
                    lagSaldoerForPeriode(saldoer, stønadskonto, periode);
                }
            }
        }
    }

    private void lagSaldoerForPeriode(SaldoerImpl saldoer, Stønadskonto stønadskonto, UttakResultatPeriodeEntitet periode) {
        for (UttakResultatPeriodeAktivitetEntitet aktivititet : periode.getAktiviteter()) {
            int trekkdager = henteTrekkdager(stønadskonto, periode, aktivititet);
            if (trekkdager > 0) {
                saldoer.trekkForSøker(stønadskonto.getStønadskontoType(), new Aktivitet(aktivititet.getUttakArbeidType(), aktivititet.getArbeidsforholdOrgnr(), aktivititet.getArbeidsforholdId()), trekkdager);
            }
        }
    }

    private int henteTrekkdager(Stønadskonto stønadskonto, UttakResultatPeriodeEntitet periode, UttakResultatPeriodeAktivitetEntitet aktivititet) {
        if (StønadskontoType.FLERBARNSDAGER.equals(stønadskonto.getStønadskontoType()) && periode.isFlerbarnsdager()) {
            return aktivititet.getTrekkdager();
        }
        return Objects.equals(aktivititet.getTrekkonto(), stønadskonto.getStønadskontoType()) ? aktivititet.getTrekkdager() : 0;
    }

    private void leggTilAktiviteterAnnenPart(UttakResultatEntitet uttakResultatEntitet,
                                             UttakResultatEntitet annenPartUttak,
                                             SaldoerImpl saldoer,
                                             Set<Stønadskonto> stønadskontoer) {
        Map<StønadskontoType, Stønadskonto> stønadskontoMap = stønadskontoer.stream().collect(Collectors.toMap(Stønadskonto::getStønadskontoType, s -> s));
        List<UttakResultatPeriodeEntitet> annenPartPerioder = annenPartUttak.getGjeldendePerioder().getPerioder()
            .stream().sorted(Comparator.comparing(UttakResultatPeriodeEntitet::getFom)).collect(Collectors.toList());

        for (UttakResultatPeriodeEntitet periode : annenPartPerioder) {
            List<UttakResultatPeriodeEntitet> overlappendePerioder =
                beregnUttaksaldoTjeneste.finnOverlappendePerioderUtenSamtidigUttak(
                    periode.getFom(),
                    periode.getTom(),
                    uttakResultatEntitet.getGjeldendePerioder().getPerioder());

            for (UttakResultatPeriodeAktivitetEntitet aktivititet : periode.getAktiviteter()) {
                int trekkdager = beregnUttaksaldoTjeneste.finnAntallTrekkdagerUtenOverlapp(aktivititet, overlappendePerioder);
                Stønadskonto stønadskonto = stønadskontoMap.get(aktivititet.getTrekkonto());
                if (stønadskonto == null) {
                    throw new IllegalStateException("Finner ikke stønadskonto på fagsaken for annen parts uttaksperiode: " + aktivititet.getTrekkonto());
                }
                saldoer.trekkForAnnenPart(stønadskonto.getStønadskontoType(),
                    new Aktivitet(aktivititet.getUttakArbeidType(), aktivititet.getArbeidsforholdOrgnr(), aktivititet.getArbeidsforholdId()),
                    trekkdager);
            }
        }
    }

}
