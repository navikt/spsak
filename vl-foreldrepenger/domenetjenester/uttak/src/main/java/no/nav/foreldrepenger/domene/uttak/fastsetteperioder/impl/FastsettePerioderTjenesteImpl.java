package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.AvklarteUttakDatoer;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.FastsettePerioderTjeneste;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPeriode;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPeriodeAktivitet;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPerioder;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.validering.OverstyrUttakResultatValidator;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.vedtak.feil.FeilFactory;

@ApplicationScoped
public class FastsettePerioderTjenesteImpl implements FastsettePerioderTjeneste {

    private BehandlingRepositoryProvider repositoryProvider;
    private OverstyrUttakResultatValidator uttakResultatValidator;
    private FastsettePerioderRegelAdapter regelAdapter;

    FastsettePerioderTjenesteImpl() {
        // For CDI
    }

    @Inject
    public FastsettePerioderTjenesteImpl(BehandlingRepositoryProvider repositoryProvider,
                                         OverstyrUttakResultatValidator uttakResultatValidator,
                                         FastsettePerioderRegelAdapter regelAdapter) {
        this.repositoryProvider = repositoryProvider;
        this.uttakResultatValidator = uttakResultatValidator;
        this.regelAdapter = regelAdapter;
    }

    @Override
    public void fastsettePerioder(Behandling behandling) {
        UttakResultatPerioderEntitet resultat = regelAdapter.fastsettePerioder(behandling);
        repositoryProvider.getUttakRepository().lagreOpprinneligUttakResultatPerioder(behandling, resultat);
    }

    @Override
    public void manueltFastsettePerioder(Behandling behandling, UttakResultatPerioder perioder) {
        valider(perioder, behandling);
        lagreManueltFastsatt(perioder, behandling);
    }

    private void valider(UttakResultatPerioder perioder, Behandling behandling) {
        UttakResultatPerioder opprinnelig = hentOpprinnelig(behandling);
        uttakResultatValidator.valider(behandling.getFagsak(), opprinnelig, perioder);
    }

    private UttakResultatPerioder hentOpprinnelig(Behandling behandling) {
        UttakResultatEntitet entitet = repositoryProvider.getUttakRepository().hentUttakResultat(behandling);
        Optional<YtelseFordelingAggregat> ytelseFordelingAggregat = repositoryProvider.getYtelsesFordelingRepository().hentAggregatHvisEksisterer(behandling);
        Optional<LocalDate> endringsdato = Optional.empty();
        if (ytelseFordelingAggregat.isPresent()) {
            if (ytelseFordelingAggregat.get().getAvklarteDatoer().isPresent()) {
                AvklarteUttakDatoer avklarteUttakDatoer = ytelseFordelingAggregat.get().getAvklarteDatoer().get();
                if (avklarteUttakDatoer.getEndringsdato() != null) {
                    endringsdato = Optional.of(avklarteUttakDatoer.getEndringsdato());
                }

            }
        }
        return map(entitet, endringsdato);
    }

    private UttakResultatPerioder map(UttakResultatEntitet uttakResultatEntitet, Optional<LocalDate> endringsdato) {

        List<UttakResultatPeriode> perioder = new ArrayList<>();

        for (UttakResultatPeriodeEntitet entitet : uttakResultatEntitet.getOpprinneligPerioder().getPerioder()) {
            UttakResultatPeriode periode = map(entitet);
            perioder.add(periode);
        }

        return new UttakResultatPerioder(perioder, endringsdato);
    }

    private UttakResultatPeriode map(UttakResultatPeriodeEntitet entitet) {
        List<UttakResultatPeriodeAktivitet> aktiviteter = new ArrayList<>();
        for (UttakResultatPeriodeAktivitetEntitet aktivitet : entitet.getAktiviteter()) {
            aktiviteter.add(map(aktivitet));
        }
        UttakResultatPeriode.Builder periodeBuilder = new UttakResultatPeriode.Builder()
            .medTidsperiode(new LocalDateInterval(entitet.getFom(), entitet.getTom()))
            .medAktiviteter(aktiviteter)
            .medBegrunnelse(entitet.getBegrunnelse())
            .medType(entitet.getPeriodeResultatType())
            .medÅrsak(entitet.getPeriodeResultatÅrsak())
            .medFlerbarnsdager(entitet.isFlerbarnsdager())
            .medUtsettelseType(entitet.getUtsettelseType())
            .medSamtidigUttak(entitet.isSamtidigUttak())
            .medSamtidigUttaksprosent(entitet.getSamtidigUttaksprosent());
        return periodeBuilder.build();
    }

    private UttakResultatPeriodeAktivitet map(UttakResultatPeriodeAktivitetEntitet periodeAktivitet) {
        return new UttakResultatPeriodeAktivitet.Builder()
            .medArbeidsprosent(periodeAktivitet.getArbeidsprosent())
            .medTrekkonto(periodeAktivitet.getTrekkonto())
            .medTrekkdager(periodeAktivitet.getTrekkdager())
            .medUtbetalingsgrad(periodeAktivitet.getUtbetalingsprosent())
            .medArbeidsforholdId(periodeAktivitet.getArbeidsforholdId())
            .medArbeidsforholdOrgnr(periodeAktivitet.getArbeidsforholdOrgnr())
            .medUttakArbeidType(periodeAktivitet.getUttakArbeidType())
            .build();
    }

    private void lagreManueltFastsatt(UttakResultatPerioder perioder, Behandling behandling) {

        UttakResultatPerioderEntitet overstyrtEntitet = new UttakResultatPerioderEntitet();
        UttakResultatEntitet opprinnelig = repositoryProvider.getUttakRepository().hentUttakResultat(behandling);
        for (UttakResultatPeriode periode : perioder.getPerioder()) {
            UttakResultatPeriodeEntitet matchendeOpprinneligPeriode = matchendeOpprinneligPeriode(periode, opprinnelig.getOpprinneligPerioder());
            UttakResultatPeriodeEntitet periodeEntitet = map(matchendeOpprinneligPeriode, periode);
            overstyrtEntitet.leggTilPeriode(periodeEntitet);
        }

        repositoryProvider.getUttakRepository().lagreOverstyrtUttakResultatPerioder(behandling, overstyrtEntitet);
    }

    private UttakResultatPeriodeEntitet matchendeOpprinneligPeriode(UttakResultatPeriode periode,
                                                                    UttakResultatPerioderEntitet opprinnelig) {
        Optional<UttakResultatPeriodeEntitet> matchende = opprinnelig.getPerioder()
            .stream()
            .filter(oPeriode -> {
                LocalDateInterval tidsperiode = periode.getTidsperiode();
                return (tidsperiode.getFomDato().isEqual(oPeriode.getFom()) || tidsperiode.getFomDato().isAfter(oPeriode.getFom()))
                    && (tidsperiode.getTomDato().isEqual(oPeriode.getTom()) || tidsperiode.getTomDato().isBefore(oPeriode.getTom()));
            })
            .findFirst();

        return matchende.orElseThrow(()
            -> FeilFactory.create(FastsettePerioderFeil.class).manglendeOpprinneligPeriode(periode).toException());
    }

    private UttakResultatPeriodeEntitet map(UttakResultatPeriodeEntitet opprinneligPeriode,
                                            UttakResultatPeriode nyPeriode) {
        UttakResultatPeriodeEntitet.Builder builder = new UttakResultatPeriodeEntitet.Builder(nyPeriode.getTidsperiode().getFomDato(),
            nyPeriode.getTidsperiode().getTomDato())
            .medPeriodeSoknad(opprinneligPeriode.getPeriodeSøknad().orElse(null))
            .medPeriodeResultat(nyPeriode.getResultatType(), nyPeriode.getResultatÅrsak())
            .medBegrunnelse(nyPeriode.getBegrunnelse())
            .medGraderingInnvilget(nyPeriode.isGraderingInnvilget())
            .medGraderingAvslagÅrsak(nyPeriode.getGraderingAvslagÅrsak())
            .medUtsettelseType(opprinneligPeriode.getUtsettelseType())
            .medSamtidigUttak(nyPeriode.isSamtidigUttak())
            .medSamtidigUttaksprosent(nyPeriode.getSamtidigUttaksprosent())
            .medFlerbarnsdager(nyPeriode.isFlerbarnsdager())
            .medManueltBehandlet(nyPeriode.getBegrunnelse() != null);
        UttakResultatPeriodeEntitet periodeEntitet = builder.build();

        for (UttakResultatPeriodeAktivitet nyAktivitet : nyPeriode.getAktiviteter()) {
            UttakResultatPeriodeAktivitetEntitet matchendeOpprinneligAktivitet = matchendeOpprinneligAktivitet(opprinneligPeriode, nyAktivitet);
            UttakResultatPeriodeAktivitetEntitet periodeAktivitet = new UttakResultatPeriodeAktivitetEntitet.Builder(periodeEntitet, matchendeOpprinneligAktivitet.getUttakAktivitet())
                .medTrekkonto(nyAktivitet.getTrekkonto())
                .medTrekkdager(nyAktivitet.getTrekkdager())
                .medArbeidsprosent(nyAktivitet.getArbeidsprosent())
                .medUtbetalingsprosent(nyAktivitet.getUtbetalingsgrad())
                .medErSøktGradering(matchendeOpprinneligAktivitet.isSøktGradering())
                .build();
            periodeEntitet.leggTilAktivitet(periodeAktivitet);
        }
        return periodeEntitet;
    }

    private UttakResultatPeriodeAktivitetEntitet matchendeOpprinneligAktivitet(UttakResultatPeriodeEntitet opprinneligPeriode,
                                                                               UttakResultatPeriodeAktivitet nyAktivitet) {
        return opprinneligPeriode.getAktiviteter()
            .stream()
            .filter(opprinneligAktivitet -> Objects.equals(opprinneligAktivitet.getUttakArbeidType(), nyAktivitet.getUttakArbeidType()) &&
                Objects.equals(opprinneligAktivitet.getArbeidsforholdId(), nyAktivitet.getArbeidsforholdId()) &&
                Objects.equals(opprinneligAktivitet.getArbeidsforholdOrgnr(), nyAktivitet.getArbeidsforholdOrgnr()))
            .findFirst().orElseThrow(() -> FeilFactory.create(FastsettePerioderFeil.class).manglendeOpprinneligAktivitet(nyAktivitet, opprinneligPeriode.getAktiviteter()).toException());
    }
}
