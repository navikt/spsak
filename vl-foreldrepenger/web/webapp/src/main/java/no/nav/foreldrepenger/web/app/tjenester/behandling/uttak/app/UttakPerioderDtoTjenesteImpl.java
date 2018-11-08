package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeSøknadEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.RelatertBehandlingTjeneste;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.UttakResultatPeriodeAktivitetDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.UttakResultatPeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.UttakResultatPerioderDto;

@ApplicationScoped
public class UttakPerioderDtoTjenesteImpl implements UttakPerioderDtoTjeneste {

    private UttakRepository uttakRepository;
    private VirksomhetTjeneste virksomhetTjeneste;
    private RelatertBehandlingTjeneste relatertBehandlingTjeneste;

    public UttakPerioderDtoTjenesteImpl() {
        // For CDI
    }

    @Inject
    public UttakPerioderDtoTjenesteImpl(UttakRepository uttakRepository,
                                        VirksomhetTjeneste virksomhetTjeneste,
                                        RelatertBehandlingTjeneste relatertBehandlingTjeneste) {
        this.uttakRepository = uttakRepository;
        this.virksomhetTjeneste = virksomhetTjeneste;
        this.relatertBehandlingTjeneste = relatertBehandlingTjeneste;
    }

    @Override
    public Optional<UttakResultatPerioderDto> mapFra(Behandling behandling) {
        UttakResultatPerioderDto perioder = new UttakResultatPerioderDto(finnUttakResultatPerioderSøker(behandling), finnUttakResultatPerioderAnnenpart(behandling));
        return Optional.of(perioder);
    }

    private List<UttakResultatPeriodeDto> finnUttakResultatPerioderSøker(Behandling behandling) {
        Optional<UttakResultatEntitet> uttakResultat = uttakRepository.hentUttakResultatHvisEksisterer(behandling);

        if (!uttakResultat.isPresent()) {
            return Collections.emptyList();
        }

        return finnUttakResultatPerioder(uttakResultat.get());
    }

    private List<UttakResultatPeriodeDto> finnUttakResultatPerioderAnnenpart(Behandling behandling) {
        Optional<UttakResultatEntitet> uttakResultat = relatertBehandlingTjeneste.hentAnnenPartsGjeldendeUttaksplan(behandling);

        if (!uttakResultat.isPresent()) {
            return Collections.emptyList();
        }

        return finnUttakResultatPerioder(uttakResultat.get());
    }

    private List<UttakResultatPeriodeDto> finnUttakResultatPerioder(UttakResultatEntitet uttakResultat) {

        UttakResultatPerioderEntitet gjeldenePerioder = uttakResultat.getGjeldendePerioder();

        List<UttakResultatPeriodeDto> list = new ArrayList<>();

        for (UttakResultatPeriodeEntitet entitet : gjeldenePerioder.getPerioder()) {
            UttakResultatPeriodeDto periode = map(entitet);
            list.add(periode);
        }

        return sortedByFom(list);
    }

    private UttakResultatPeriodeDto map(UttakResultatPeriodeEntitet entitet) {
        UttakResultatPeriodeDto.Builder builder = new UttakResultatPeriodeDto.Builder()
            .medTidsperiode(entitet.getFom(), entitet.getTom())
            .medManuellBehandlingÅrsak(entitet.getManuellBehandlingÅrsak())
            .medUtsettelseType(entitet.getUtsettelseType())
            .medPeriodeResultatType(entitet.getPeriodeResultatType())
            .medBegrunnelse(entitet.getBegrunnelse())
            .medPeriodeResultatÅrsak(entitet.getPeriodeResultatÅrsak())
            .medFlerbarnsdager(entitet.isFlerbarnsdager())
            .medSamtidigUttak(entitet.isSamtidigUttak())
            .medSamtidigUttaksprosent(entitet.getSamtidigUttaksprosent())
            .medGraderingInnvilget(entitet.isGraderingInnvilget())
            .medGraderingAvslåttÅrsak(entitet.getGraderingAvslagÅrsak());
        if (entitet.getPeriodeSøknad().isPresent()) {
            UttakResultatPeriodeSøknadEntitet periodeSøknad = entitet.getPeriodeSøknad().get();
            builder
                .medPeriodeType(periodeSøknad.getUttakPeriodeType());
        }
        UttakResultatPeriodeDto periode = builder.build();

        for (UttakResultatPeriodeAktivitetEntitet aktivitet : entitet.getAktiviteter()) {
            periode.leggTilAktivitet(map(aktivitet));
        }
        return periode;
    }

    private List<UttakResultatPeriodeDto> sortedByFom(List<UttakResultatPeriodeDto> list) {
        return list
            .stream()
            .sorted(Comparator.comparing(UttakResultatPeriodeDto::getFom))
            .collect(Collectors.toList());
    }

    private UttakResultatPeriodeAktivitetDto map(UttakResultatPeriodeAktivitetEntitet aktivitet) {
        UttakResultatPeriodeAktivitetDto.Builder builder = new UttakResultatPeriodeAktivitetDto.Builder()
            .medProsentArbeid(aktivitet.getArbeidsprosent())
            .medGradering(aktivitet.isSøktGradering())
            .medTrekkdager(aktivitet.getTrekkdager())
            .medStønadskontoType(aktivitet.getTrekkonto())
            .medUttakArbeidType(aktivitet.getUttakArbeidType());
        mapArbeidsforhold(aktivitet, builder);
        if (!aktivitet.getPeriode().opprinneligSendtTilManueltBehandling()) {
            builder.medUtbetalingsgrad(aktivitet.getUtbetalingsprosent());
        }
        return builder.build();
    }

    private void mapArbeidsforhold(UttakResultatPeriodeAktivitetEntitet aktivitet, UttakResultatPeriodeAktivitetDto.Builder builder) {
        if (UttakArbeidType.ORDINÆRT_ARBEID.equals(aktivitet.getUttakArbeidType())) {
            builder.medArbeidsforhold(aktivitet.getArbeidsforholdId(),
                aktivitet.getArbeidsforholdOrgnr(),
                virksomhetTjeneste.finnOrganisasjon(aktivitet.getArbeidsforholdOrgnr()).map(Virksomhet::getNavn)
                    .orElse(aktivitet.getUttakArbeidType().getNavn()));
        } else {
            builder.medArbeidsforhold(aktivitet.getArbeidsforholdId(),
                aktivitet.getArbeidsforholdOrgnr(),
                null);
        }
    }
}
