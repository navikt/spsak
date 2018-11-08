package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeSøknadEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.RelatertBehandlingTjeneste;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.UttakResultatPerioderDto;

public class UttakPerioderDtoTjenesteImplTest {

    private static final Behandling BEHANDLING = mock(Behandling.class);

    @Test
    public void skalHenteUttaksPerioderFraRepository() {
        UttakResultatPerioderEntitet perioder = new UttakResultatPerioderEntitet();
        String arbeidsforholdId = "arbeidsforholdId";
        UttakAktivitetEntitet uttakAktivitet = new UttakAktivitetEntitet.Builder()
            .medArbeidsforhold(new VirksomhetEntitet.Builder().medOrgnr("orgnr").build(), ArbeidsforholdRef.ref(arbeidsforholdId))
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();
        UttakPeriodeType periodeType = UttakPeriodeType.MØDREKVOTE;
        UttakResultatPeriodeSøknadEntitet periodeSøknad = new UttakResultatPeriodeSøknadEntitet.Builder()
            .medUttakPeriodeType(periodeType)
            .medMottattDato(LocalDate.now())
            .build();
        UttakResultatPeriodeEntitet periode = periodeBuilder(LocalDate.now(), LocalDate.now().plusWeeks(2))
            .medGraderingInnvilget(true)
            .medSamtidigUttak(true)
            .medSamtidigUttaksprosent(BigDecimal.TEN)
            .medPeriodeResultat(PeriodeResultatType.AVSLÅTT, PeriodeResultatÅrsak.UKJENT)
            .medPeriodeSoknad(periodeSøknad)
            .build();
        UttakResultatPeriodeAktivitetEntitet periodeAktivitet = new UttakResultatPeriodeAktivitetEntitet.Builder(periode, uttakAktivitet)
            .medTrekkonto(StønadskontoType.FELLESPERIODE)
            .medArbeidsprosent(BigDecimal.TEN)
            .medErSøktGradering(true)
            .medUtbetalingsprosent(BigDecimal.ONE)
            .build();
        perioder.leggTilPeriode(periode);
        UttakResultatEntitet uttakResultat = new UttakResultatEntitet();
        uttakResultat.setOpprinneligPerioder(perioder);
        UttakPerioderDtoTjenesteImpl tjeneste = lagTjenesteSomReturnererResultat(uttakResultat);

        Optional<UttakResultatPerioderDto> result = tjeneste.mapFra(BEHANDLING);

        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getPerioderSøker()).hasSize(1);
        assertThat(result.get().getPerioderSøker().get(0).getFom()).isEqualTo(periode.getFom());
        assertThat(result.get().getPerioderSøker().get(0).getTom()).isEqualTo(periode.getTom());
        assertThat(result.get().getPerioderSøker().get(0).isSamtidigUttak()).isEqualTo(periode.isSamtidigUttak());
        assertThat(result.get().getPerioderSøker().get(0).getPeriodeResultatType()).isEqualTo(periode.getPeriodeResultatType());
        assertThat(result.get().getPerioderSøker().get(0).getBegrunnelse()).isEqualTo(periode.getBegrunnelse());
        assertThat(result.get().getPerioderSøker().get(0).getGradertAktivitet().getArbeidsforholdId()).isEqualTo(arbeidsforholdId);
        assertThat(result.get().getPerioderSøker().get(0).isSamtidigUttak()).isEqualTo(periode.isSamtidigUttak());
        assertThat(result.get().getPerioderSøker().get(0).getSamtidigUttaksprosent()).isEqualTo(periode.getSamtidigUttaksprosent());
        assertThat(result.get().getPerioderSøker().get(0).getPeriodeType()).isEqualTo(periodeType);
        assertThat(result.get().getPerioderSøker().get(0).getAktiviteter()).hasSize(1);
        assertThat(result.get().getPerioderSøker().get(0).getAktiviteter().get(0).getArbeidsforholdId()).isEqualTo(periodeAktivitet.getArbeidsforholdId());
        assertThat(result.get().getPerioderSøker().get(0).getAktiviteter().get(0).getArbeidsforholdOrgnr()).isEqualTo(periodeAktivitet.getArbeidsforholdOrgnr());
        assertThat(result.get().getPerioderSøker().get(0).getAktiviteter().get(0).getStønadskontoType()).isEqualTo(periodeAktivitet.getTrekkonto());
        assertThat(result.get().getPerioderSøker().get(0).getAktiviteter().get(0).getTrekkdager()).isEqualTo(periodeAktivitet.getTrekkdager());
        assertThat(result.get().getPerioderSøker().get(0).getAktiviteter().get(0).getProsentArbeid()).isEqualTo(periodeAktivitet.getArbeidsprosent());
        assertThat(result.get().getPerioderSøker().get(0).getAktiviteter().get(0).getUtbetalingsgrad()).isEqualTo(periodeAktivitet.getUtbetalingsprosent());
        assertThat(result.get().getPerioderSøker().get(0).getAktiviteter().get(0).getUttakArbeidType()).isEqualTo(periodeAktivitet.getUttakArbeidType());
        assertThat(result.get().getPerioderSøker().get(0).getAktiviteter().get(0).getUttakArbeidType()).isEqualTo(periodeAktivitet.getUttakArbeidType());
    }

    @Test
    public void skalHenteUttaksPerioderMedFlereAktiviteter() {
        UttakResultatPerioderEntitet perioder = new UttakResultatPerioderEntitet();
        LocalDate periode1Fom = LocalDate.now();
        LocalDate periode1Tom = LocalDate.now().plusDays(10);
        LocalDate periode2Fom = LocalDate.now().plusDays(11);
        LocalDate periode2Tom = LocalDate.now().plusDays(15);
        UttakResultatPeriodeEntitet periode1 = periodeBuilder(periode1Fom, periode1Tom).build();
        UttakResultatPeriodeEntitet periode2 = periodeBuilder(periode2Fom, periode2Tom).build();
        periode1.leggTilAktivitet(periodeAktivitet(periode1));
        periode1.leggTilAktivitet(periodeAktivitet(periode1));
        periode2.leggTilAktivitet(periodeAktivitet(periode2));
        perioder.leggTilPeriode(periode1);
        perioder.leggTilPeriode(periode2);
        UttakResultatEntitet uttakResultat = new UttakResultatEntitet();
        uttakResultat.setOpprinneligPerioder(perioder);
        UttakPerioderDtoTjenesteImpl tjeneste = lagTjenesteSomReturnererResultat(uttakResultat);

        Optional<UttakResultatPerioderDto> result = tjeneste.mapFra(BEHANDLING);

        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getPerioderSøker()).hasSize(2);
        assertThat(result.get().getPerioderSøker().get(0).getAktiviteter()).hasSize(2);
        assertThat(result.get().getPerioderSøker().get(1).getAktiviteter()).hasSize(1);
    }

    private UttakResultatPeriodeAktivitetEntitet periodeAktivitet(UttakResultatPeriodeEntitet periode) {
        UttakAktivitetEntitet uttakAktivitet = new UttakAktivitetEntitet.Builder()
            .medArbeidsforhold(new VirksomhetEntitet.Builder().medOrgnr(UUID.randomUUID().toString()).build(), ArbeidsforholdRef.ref("1234"))
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();
        return new UttakResultatPeriodeAktivitetEntitet.Builder(periode, uttakAktivitet)
            .medArbeidsprosent(BigDecimal.ZERO)
            .build();
    }

    @Test
    public void skalSetteRiktigNavnForVirksomhet() {
        UttakResultatPerioderEntitet perioder = new UttakResultatPerioderEntitet();
        String orgnr = "orgnr";
        String navn = "Virksomhetsnavn";

        UttakResultatPeriodeEntitet periode = periodeBuilder(LocalDate.now(), LocalDate.now().plusDays(2))
            .build();

        UttakAktivitetEntitet uttakAktivitet = new UttakAktivitetEntitet.Builder()
            .medArbeidsforhold(new VirksomhetEntitet.Builder().medOrgnr(orgnr).build(), ArbeidsforholdRef.ref("arbeidsforholdId"))
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();

        UttakResultatPeriodeAktivitetEntitet periodeAktivitet = new UttakResultatPeriodeAktivitetEntitet.Builder(periode, uttakAktivitet)
            .medArbeidsprosent(BigDecimal.ZERO)
            .build();

        periode.leggTilAktivitet(periodeAktivitet);

        perioder.leggTilPeriode(periode);
        UttakResultatEntitet uttakResultat = new UttakResultatEntitet();
        uttakResultat.setOpprinneligPerioder(perioder);
        VirksomhetTjeneste virksomhetTjeneste = mock(VirksomhetTjeneste.class);
        when(virksomhetTjeneste.finnOrganisasjon(orgnr)).thenReturn(Optional.of(new VirksomhetEntitet.Builder().medOrgnr(orgnr).medNavn(navn).build()));
        UttakPerioderDtoTjenesteImpl tjeneste = lagTjenesteSomReturnererResultat(uttakResultat, virksomhetTjeneste, null);

        Optional<UttakResultatPerioderDto> result = tjeneste.mapFra(BEHANDLING);

        assertThat(result.get().getPerioderSøker().get(0).getAktiviteter().get(0).getArbeidsforholdNavn()).isEqualTo(navn);
    }

    @Test
    public void skalHenteUttaksPerioderForSøkerOgAnnenpart() {
        UttakResultatPerioderEntitet perioderSøker = new UttakResultatPerioderEntitet();
        LocalDate periode1FomSøker = LocalDate.now();
        LocalDate periode1TomSøker = LocalDate.now().plusDays(10);
        LocalDate periode2FomSøker = LocalDate.now().plusDays(11);
        LocalDate periode2TomSøker = LocalDate.now().plusDays(15);
        UttakResultatPeriodeEntitet periode1Søker = periodeBuilder(periode1FomSøker, periode1TomSøker).build();
        UttakResultatPeriodeEntitet periode2Søker = periodeBuilder(periode2FomSøker, periode2TomSøker).build();
        periode1Søker.leggTilAktivitet(periodeAktivitet(periode1Søker));
        periode2Søker.leggTilAktivitet(periodeAktivitet(periode2Søker));
        perioderSøker.leggTilPeriode(periode1Søker);
        perioderSøker.leggTilPeriode(periode2Søker);
        UttakResultatEntitet uttakResultatSøker = new UttakResultatEntitet();
        uttakResultatSøker.setOpprinneligPerioder(perioderSøker);

        UttakResultatPerioderEntitet perioderAnnenpart = new UttakResultatPerioderEntitet();
        LocalDate periode1FomAnnenpart = periode2TomSøker.plusDays(1);
        LocalDate periode1TomAnnenpart = periode1FomAnnenpart.plusDays(10);
        UttakResultatPeriodeEntitet periode1Annenpart = periodeBuilder(periode1FomAnnenpart, periode1TomAnnenpart).build();
        periode1Annenpart.leggTilAktivitet(periodeAktivitet(periode1Annenpart));
        perioderAnnenpart.leggTilPeriode(periode1Annenpart);
        UttakResultatEntitet uttakResultatAnnenpart = new UttakResultatEntitet();
        uttakResultatAnnenpart.setOpprinneligPerioder(perioderAnnenpart);
        UttakPerioderDtoTjenesteImpl tjeneste = lagTjenesteSomReturnererResultatForBådeSøkerOgAnnenpart(uttakResultatSøker, uttakResultatAnnenpart);

        Optional<UttakResultatPerioderDto> result = tjeneste.mapFra(BEHANDLING);

        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getPerioderSøker()).hasSize(2);
        assertThat(result.get().getPerioderSøker().get(0).getAktiviteter()).hasSize(1);
        assertThat(result.get().getPerioderSøker().get(1).getAktiviteter()).hasSize(1);

        assertThat(result.get().getPerioderAnnenpart()).hasSize(1);
        assertThat(result.get().getPerioderAnnenpart().get(0).getAktiviteter()).hasSize(1);
    }

    @Test
    public void dtoSkalInneholdeSamtidigUttak() {
        UttakResultatPerioderEntitet perioder = new UttakResultatPerioderEntitet();


        UttakResultatPeriodeEntitet periode = periodeBuilder(LocalDate.now(), LocalDate.now().plusDays(2))
            .medSamtidigUttak(true)
            .medSamtidigUttaksprosent(BigDecimal.TEN)
            .build();

        UttakAktivitetEntitet uttakAktivitet = new UttakAktivitetEntitet.Builder()
            .medUttakArbeidType(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE)
            .build();

        UttakResultatPeriodeAktivitetEntitet periodeAktivitet = new UttakResultatPeriodeAktivitetEntitet.Builder(periode, uttakAktivitet)
            .medArbeidsprosent(BigDecimal.ZERO)
            .build();

        periode.leggTilAktivitet(periodeAktivitet);

        perioder.leggTilPeriode(periode);
        UttakResultatEntitet uttakResultat = new UttakResultatEntitet();
        uttakResultat.setOpprinneligPerioder(perioder);
        UttakPerioderDtoTjenesteImpl tjeneste = lagTjenesteSomReturnererResultat(uttakResultat, null, null);

        Optional<UttakResultatPerioderDto> result = tjeneste.mapFra(BEHANDLING);

        assertThat(result.get().getPerioderSøker().get(0).isSamtidigUttak()).isEqualTo(periode.isSamtidigUttak());
        assertThat(result.get().getPerioderSøker().get(0).getSamtidigUttaksprosent()).isEqualTo(periode.getSamtidigUttaksprosent());
    }

    private UttakPerioderDtoTjenesteImpl lagTjeneste(UttakRepository repository,
                                                     VirksomhetTjeneste virksomhetTjeneste,
                                                     RelatertBehandlingTjeneste relatertBehandlingTjeneste) {
        return new UttakPerioderDtoTjenesteImpl(repository, virksomhetTjeneste, relatertBehandlingTjeneste);
    }

    private UttakPerioderDtoTjenesteImpl lagTjenesteSomReturnererResultat(UttakResultatEntitet uttakResultat) {
        VirksomhetTjeneste virksomhetsTjeneste = mock(VirksomhetTjeneste.class);
        when(virksomhetsTjeneste.finnOrganisasjon(any())).thenReturn(Optional.of(new VirksomhetEntitet.Builder().medNavn("navn").build()));
        return lagTjenesteSomReturnererResultat(uttakResultat, virksomhetsTjeneste, null);
    }

    private UttakPerioderDtoTjenesteImpl lagTjenesteSomReturnererResultatForBådeSøkerOgAnnenpart(UttakResultatEntitet uttakResultat, UttakResultatEntitet uttakResultatAnnenpart) {
        VirksomhetTjeneste virksomhetsTjeneste = mock(VirksomhetTjeneste.class);
        when(virksomhetsTjeneste.finnOrganisasjon(any())).thenReturn(Optional.of(new VirksomhetEntitet.Builder().medNavn("navn").build()));
        return lagTjenesteSomReturnererResultat(uttakResultat, virksomhetsTjeneste, uttakResultatAnnenpart);
    }

    private UttakPerioderDtoTjenesteImpl lagTjenesteSomReturnererResultat(UttakResultatEntitet uttakResultat, VirksomhetTjeneste virksomhetTjeneste, UttakResultatEntitet uttakResultatAnnenpart) {
        UttakRepository repository = mock(UttakRepository.class);
        RelatertBehandlingTjeneste relatertBehandlingTjeneste = mock(RelatertBehandlingTjeneste.class);
        if(uttakResultatAnnenpart != null) {
            when(relatertBehandlingTjeneste.hentAnnenPartsGjeldendeUttaksplan(BEHANDLING)).thenReturn(Optional.of(uttakResultatAnnenpart));
        }
        when(repository.hentUttakResultatHvisEksisterer(BEHANDLING)).thenReturn(Optional.of(uttakResultat));
        return lagTjeneste(repository, virksomhetTjeneste, relatertBehandlingTjeneste);
    }


    private UttakResultatPeriodeEntitet.Builder periodeBuilder(LocalDate fom, LocalDate tom) {
        return new UttakResultatPeriodeEntitet.Builder(fom, tom)
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT);
    }
}
