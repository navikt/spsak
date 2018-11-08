package no.nav.foreldrepenger.domene.uttak.fastsettuttaksgrunnlag.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.MorsAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.FordelingPeriodeKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordeling;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeMapper;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakUtsettelseType;

class VedtaksperioderHelper {
    private static final KodeMapper<UttakUtsettelseType, UtsettelseÅrsak> utsettelseÅrsakMapper = initUtsettelseÅrsakMapper();

    private static final KodeMapper<StønadskontoType, UttakPeriodeType> periodeTypeMapper = initPeriodeTypeMapper();

    OppgittFordeling opprettOppgittFordeling(UttakResultatEntitet uttakResultatFraForrigeBehandling,
                                             OppgittFordeling oppgittFordeling,
                                             LocalDate endringsdato) {
        Objects.requireNonNull(oppgittFordeling);

        Optional<LocalDate> førsteSøknadsdato = OppgittPeriodeUtil.finnFørsteSøknadsdato(oppgittFordeling);
        List<OppgittPeriode> vedtaksperioder = lagVedtaksperioder(uttakResultatFraForrigeBehandling, endringsdato, førsteSøknadsdato);
        List<OppgittPeriode> søknadsperioder = oppgittFordeling.getOppgittePerioder();

        final List<OppgittPeriode> søknadOgVedtaksperioder = new ArrayList<>();
        søknadsperioder.forEach(op -> søknadOgVedtaksperioder.add(OppgittPeriodeBuilder.fraEksisterende(op).build()));
        søknadOgVedtaksperioder.addAll(vedtaksperioder);
        return new OppgittFordelingEntitet(OppgittPeriodeUtil.sorterEtterFom(søknadOgVedtaksperioder), oppgittFordeling.getErAnnenForelderInformert());
    }

    OppgittFordeling kopierOppgittFordelingFraForrigeBehandling(OppgittFordeling forrigeBehandlingFordeling) {
        List<OppgittPeriode> kopiertFordeling = forrigeBehandlingFordeling.getOppgittePerioder().stream()
            .map(periode -> OppgittPeriodeBuilder.fraEksisterende(periode).build())
            .collect(Collectors.toList());
        return new OppgittFordelingEntitet(kopiertFordeling, forrigeBehandlingFordeling.getErAnnenForelderInformert());
    }

    private List<OppgittPeriode> lagVedtaksperioder(UttakResultatEntitet uttakResultat, LocalDate endringsdato, Optional<LocalDate> førsteSøknadsdato) {
        return uttakResultat.getGjeldendePerioder().getPerioder()
            .stream()
            .filter(p -> filtrerUttaksperioder(p, endringsdato, førsteSøknadsdato))
            .filter(this::erPeriodeFraSøknad)
            .map(this::konverter)
            .flatMap(p -> klipp(p, endringsdato, førsteSøknadsdato))
            .collect(Collectors.toList());
    }

    private boolean filtrerUttaksperioder(UttakResultatPeriodeEntitet periode, LocalDate endringsdato, Optional<LocalDate> førsteSøknadsdatoOptional) {
        Objects.requireNonNull(endringsdato);

        if (periode.getTom().isBefore(endringsdato)) {
            //Perioder før endringsdato skal filtreres bort
            return false;
        }
        if (førsteSøknadsdatoOptional.isPresent()) {
            LocalDate førsteSøknadsdato = førsteSøknadsdatoOptional.get();
            if (periode.getFom().equals(førsteSøknadsdato) || periode.getFom().isAfter(førsteSøknadsdato)) {
                //Perioder som starter på eller etter første søknadsdato skal filtreres bort
                return false;
            }
        }
        return true;
    }

    private boolean erPeriodeFraSøknad(UttakResultatPeriodeEntitet periode) {
        return periode.getPeriodeSøknad().isPresent();
    }

    private Stream<OppgittPeriode> klipp(OppgittPeriode op, LocalDate endringsdato, Optional<LocalDate> førsteSøknadsdato) {
        Objects.requireNonNull(endringsdato);

        OppgittPeriodeBuilder opb = OppgittPeriodeBuilder.fraEksisterende(op);
        LocalDate fom = op.getFom();
        LocalDate tom = op.getTom();
        if (endringsdato.isAfter(fom)) {
            fom = endringsdato;
        }
        if (førsteSøknadsdato.isPresent() && (førsteSøknadsdato.get().isBefore(tom) || førsteSøknadsdato.get().isEqual(tom))) {
            tom = førsteSøknadsdato.get().minusDays(1);
        }
        if (!fom.isAfter(tom)) {
            return Stream.of(opb.medPeriode(fom, tom).build());
        }
        return Stream.empty();
    }

    OppgittPeriode konverter(UttakResultatPeriodeEntitet up) {
        OppgittPeriodeBuilder builder = OppgittPeriodeBuilder.ny()
            .medPeriode(up.getTidsperiode().getFomDato(), up.getTidsperiode().getTomDato())
            .medPeriodeType(finnPeriodetype(up))
            .medSamtidigUttak(up.isSamtidigUttak())
            .medSamtidigUttaksprosent(up.getSamtidigUttaksprosent())
            .medFlerbarnsdager(up.isFlerbarnsdager())
            .medErArbeidstaker(erArbeidstaker(up))
            .medPeriodeKilde(FordelingPeriodeKilde.TIDLIGERE_VEDTAK);

        finnMorsAktivitet(up).ifPresent(builder::medMorsAktivitet);
        finnGraderingArbeidsprosent(up).ifPresent(builder::medArbeidsprosent);
        finnUtsettelsesÅrsak(up).ifPresent(builder::medÅrsak);
        finnGradertVirksomhet(up).ifPresent(builder::medVirksomhet);
        finnSamtidigUttaksprosent(up).ifPresent(builder::medSamtidigUttaksprosent);
        return builder.build();
    }

    private Optional<BigDecimal> finnSamtidigUttaksprosent(UttakResultatPeriodeEntitet up) {
        if (up.getPeriodeSøknad().isPresent()) {
            return Optional.ofNullable(up.getPeriodeSøknad().get().getSamtidigUttaksprosent());
        }
        return Optional.empty();
    }

    private Optional<MorsAktivitet> finnMorsAktivitet(UttakResultatPeriodeEntitet up) {
        if (up.getPeriodeSøknad().isPresent()) {
            return Optional.of(up.getPeriodeSøknad().get().getMorsAktivitet());
        }
        return Optional.empty();
    }

    private Optional<VirksomhetEntitet> finnGradertVirksomhet(UttakResultatPeriodeEntitet up) {
        return up.getAktiviteter().stream().filter(UttakResultatPeriodeAktivitetEntitet::isGraderingInnvilget)
            .filter(p -> p.getUttakAktivitet().getVirksomhet() != null)
            .map(p -> p.getUttakAktivitet().getVirksomhet())
            .findFirst();
    }

    private boolean erArbeidstaker(UttakResultatPeriodeEntitet up) {
        return up.getAktiviteter().stream()
            .filter(UttakResultatPeriodeAktivitetEntitet::isGraderingInnvilget)
            .anyMatch(a -> UttakArbeidType.ORDINÆRT_ARBEID.equals(a.getUttakArbeidType()));
    }

    private UttakPeriodeType finnPeriodetype(UttakResultatPeriodeEntitet uttakResultatPeriode) {
        Optional<StønadskontoType> stønadskontoType = uttakResultatPeriode.getAktiviteter().stream()
            .max(Comparator.comparing(UttakResultatPeriodeAktivitetEntitet::getTrekkdager))
            .map(UttakResultatPeriodeAktivitetEntitet::getTrekkonto);
        if (stønadskontoType.isPresent()) {
            Optional<UttakPeriodeType> uttakPeriodeType = periodeTypeMapper.map(stønadskontoType.get());
            if (uttakPeriodeType.isPresent()) {
                return uttakPeriodeType.get();
            }
        }
        throw new IllegalStateException("Uttaksperiode mangler stønadskonto");
    }

    private Optional<UtsettelseÅrsak> finnUtsettelsesÅrsak(UttakResultatPeriodeEntitet uttakResultatPeriode) {
        if (erInnvilgetUtsettelse(uttakResultatPeriode)) {
            return utsettelseÅrsakMapper.map(uttakResultatPeriode.getUtsettelseType());
        }
        return Optional.empty();
    }

    private boolean erInnvilgetUtsettelse(UttakResultatPeriodeEntitet uttakResultatPeriode) {
        UttakUtsettelseType utsettelseType = uttakResultatPeriode.getUtsettelseType();
        if (utsettelseType != null && !UttakUtsettelseType.UDEFINERT.equals(utsettelseType)) {
            return uttakResultatPeriode.getAktiviteter().stream()
                .noneMatch(a -> a.getUtbetalingsprosent() != null && a.getUtbetalingsprosent().compareTo(BigDecimal.ZERO) > 0);
        }
        return false;
    }

    private static KodeMapper<UttakUtsettelseType, UtsettelseÅrsak> initUtsettelseÅrsakMapper() {
        return KodeMapper
            .medMapping(UttakUtsettelseType.FERIE, UtsettelseÅrsak.FERIE)
            .medMapping(UttakUtsettelseType.ARBEID, UtsettelseÅrsak.ARBEID)
            .medMapping(UttakUtsettelseType.SYKDOM_SKADE, UtsettelseÅrsak.SYKDOM)
            .medMapping(UttakUtsettelseType.SØKER_INNLAGT, UtsettelseÅrsak.INSTITUSJON_SØKER)
            .medMapping(UttakUtsettelseType.BARN_INNLAGT, UtsettelseÅrsak.INSTITUSJON_BARN)
            .build();
    }

    private static KodeMapper<StønadskontoType, UttakPeriodeType> initPeriodeTypeMapper() {
        return KodeMapper
            .medMapping(StønadskontoType.FEDREKVOTE, UttakPeriodeType.FEDREKVOTE)
            .medMapping(StønadskontoType.FELLESPERIODE, UttakPeriodeType.FELLESPERIODE)
            .medMapping(StønadskontoType.FORELDREPENGER_FØR_FØDSEL, UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL)
            .medMapping(StønadskontoType.MØDREKVOTE, UttakPeriodeType.MØDREKVOTE)
            .medMapping(StønadskontoType.FORELDREPENGER, UttakPeriodeType.FORELDREPENGER)
            .build();
    }


    private Optional<BigDecimal> finnGraderingArbeidsprosent(UttakResultatPeriodeEntitet up) {
        BigDecimal sumGraderingProsent = BigDecimal.ZERO;
        for (UttakResultatPeriodeAktivitetEntitet akt : up.getAktiviteter()) {
            if (akt.isGraderingInnvilget() && akt.getArbeidsprosent() != null) {
                sumGraderingProsent = sumGraderingProsent.add(akt.getArbeidsprosent());
            }
        }
        return sumGraderingProsent.compareTo(BigDecimal.ZERO) > 0 ? Optional.of(sumGraderingProsent) : Optional.empty();
    }
}
