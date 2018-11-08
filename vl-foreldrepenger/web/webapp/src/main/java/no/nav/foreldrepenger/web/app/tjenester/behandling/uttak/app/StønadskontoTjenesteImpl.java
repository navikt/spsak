package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app;

import static no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType.ORDINÆRT_ARBEID;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjonRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskonto;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskontoberegning;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.BeregnUttaksaldoTjeneste;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.RelatertBehandlingTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.AktivitetIdentifikatorDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.StønadskontoerDto;

@ApplicationScoped
public class StønadskontoTjenesteImpl implements StønadskontoTjeneste {

    private UttakRepository uttakRepository;
    private FagsakRelasjonRepository relasjonRepository;
    private VirksomhetRepository virksomhetRepository;
    private RelatertBehandlingTjeneste relatertBehandlingTjeneste;
    private BeregnUttaksaldoTjeneste beregnUttaksaldoTjeneste;

    StønadskontoTjenesteImpl() {
        // For CDI
    }

    @Inject
    public StønadskontoTjenesteImpl(UttakRepository uttakRepository,
                                    FagsakRelasjonRepository relasjonRepository,
                                    VirksomhetRepository virksomhetRepository,
                                    RelatertBehandlingTjeneste relatertBehandlingTjeneste,
                                    BeregnUttaksaldoTjeneste beregnUttaksaldoTjeneste) {
        this.uttakRepository = uttakRepository;
        this.relasjonRepository = relasjonRepository;
        this.virksomhetRepository = virksomhetRepository;
        this.relatertBehandlingTjeneste = relatertBehandlingTjeneste;
        this.beregnUttaksaldoTjeneste = beregnUttaksaldoTjeneste;
    }

    @Override
    public Optional<StønadskontoerDto> lagStønadskontoerDto(Behandling behandling) {
        return relasjonRepository.finnRelasjonFor(behandling.getFagsak()).getStønadskontoberegning()
            .map(Stønadskontoberegning::getStønadskontoer)
            .filter(stønadskontoer -> !stønadskontoer.isEmpty())
            .map(stønadskontoer -> lagStønadskontoerDto(behandling, stønadskontoer));
    }

    private StønadskontoerDto lagStønadskontoerDto(Behandling behandling, Set<Stønadskonto> stønadskontoer) {

        StønadskontoerDto.Builder builder = new StønadskontoerDto.Builder();
        Optional<UttakResultatEntitet> uttakResultat = uttakRepository.hentUttakResultatHvisEksisterer(behandling);
        Optional<UttakResultatEntitet> uttakResultatAnnenPart = relatertBehandlingTjeneste.hentAnnenPartsGjeldendeUttaksplan(behandling);
        Optional<LocalDate> sisteUttaksdato = finnSisteUttaksdato(uttakResultat, uttakResultatAnnenPart);

        if (uttakResultat.isPresent()) {
            UttakResultatEntitet uttakResultatEntitet = uttakResultat.get();
            lagStønadskontoerDto(stønadskontoer, builder, uttakResultatEntitet);
            uttakResultatAnnenPart.ifPresent(uttakResultatEntitet1 -> leggTilAktiviteterAnnenPart(uttakResultat.get(), uttakResultatEntitet1, builder, stønadskontoer));
        }

        return builder.create(behandling.getFagsak(), sisteUttaksdato);
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

    private void lagStønadskontoerDto(Set<Stønadskonto> stønadskontoer, StønadskontoerDto.Builder builder, UttakResultatEntitet uttakResultat) {
        for (Stønadskonto stønadskonto : stønadskontoer) {
            for (UttakResultatPeriodeEntitet periode : uttakResultat.getGjeldendePerioder().getPerioder()) {
                if (periode.getPeriodeResultatType().equals(PeriodeResultatType.MANUELL_BEHANDLING) ||
                    beregnUttaksaldoTjeneste.erInnvilgetEllerAvslåttMedTrekkdager(periode)) {
                    for (UttakResultatPeriodeAktivitetEntitet aktivititet : periode.getAktiviteter()) {
                        int trekkdager = henteTrekkdager(stønadskonto, periode, aktivititet);
                        builder.leggTil(
                            new AktivitetIdentifikatorDto(aktivititet.getUttakAktivitet(),
                                hentVirksomhetNavn(aktivititet).orElse(null)),
                            trekkdager,
                            stønadskonto
                        );
                    }
                }
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
                                             StønadskontoerDto.Builder builder,
                                             Set<Stønadskonto> stønadskontoer) {
        Map<StønadskontoType, Stønadskonto> stønadskontoMap = stønadskontoer.stream().collect(Collectors.toMap(Stønadskonto::getStønadskontoType, s -> s));
        List<UttakResultatPeriodeEntitet> annenPartPerioder = annenPartUttak.getGjeldendePerioder().getPerioder()
            .stream().sorted(Comparator.comparing(UttakResultatPeriodeEntitet::getFom)).collect(Collectors.toList());

        for (UttakResultatPeriodeEntitet periode : annenPartPerioder) {
            List<UttakResultatPeriodeEntitet> overlappendePerioder = beregnUttaksaldoTjeneste.finnOverlappendePerioderUtenSamtidigUttak(periode.getFom(), periode.getTom(),
                uttakResultatEntitet.getGjeldendePerioder().getPerioder());

            for (UttakResultatPeriodeAktivitetEntitet aktivititet : periode.getAktiviteter()) {
                int trekkdager = beregnUttaksaldoTjeneste.finnAntallTrekkdagerUtenOverlapp(aktivititet, overlappendePerioder);
                Stønadskonto stønadskonto = stønadskontoMap.get(aktivititet.getTrekkonto());
                if (stønadskonto == null) {
                    throw new IllegalStateException("Finner ikke stønadskonto på fagsaken for annen parts uttaksperiode: " + aktivititet.getTrekkonto());
                }
                builder.leggTilForAnnenPart(
                    new AktivitetIdentifikatorDto(aktivititet.getUttakAktivitet(),
                        hentVirksomhetNavn(aktivititet).orElse(null)),
                    trekkdager,
                    stønadskonto
                );
            }
        }
    }


    private Optional<String> hentVirksomhetNavn(UttakResultatPeriodeAktivitetEntitet aktivititet) {
        if (aktivititet.getUttakAktivitet().getUttakArbeidType().equals(ORDINÆRT_ARBEID)) {
            Optional<Virksomhet> virksomhet = virksomhetRepository.hent(aktivititet.getArbeidsforholdOrgnr());
            return virksomhet.map(Virksomhet::getNavn);
        }
        return Optional.empty();
    }

}
