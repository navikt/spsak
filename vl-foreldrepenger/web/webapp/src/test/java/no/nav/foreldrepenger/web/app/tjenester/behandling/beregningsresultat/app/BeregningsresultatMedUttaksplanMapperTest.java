package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.dto.BeregningsresultatMedUttaksplanDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.dto.BeregningsresultatPeriodeAndelDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.dto.BeregningsresultatPeriodeDto;

public class BeregningsresultatMedUttaksplanMapperTest {

    private static final LocalDate P1_FOM = LocalDate.now();
    private static final LocalDate P1_TOM = LocalDate.now().plusDays(10);
    private static final LocalDate P2_FOM = LocalDate.now().plusDays(11);
    private static final LocalDate P2_TOM = LocalDate.now().plusDays(20);
    private static final LocalDate P3_FOM = LocalDate.now().plusDays(21);
    private static final LocalDate P3_TOM = LocalDate.now().plusDays(30);
    private static final AktørId AKTØR_ID = new AktørId("42");

    @Test
    public void skalLageDto() {
        Behandling behandling = lagBehandling(); // Behandling
        Behandlingsresultat.opprettFor(behandling);
        BeregningsresultatFP beregningsresultatFP = lagBeregningsresultatFP(); // Beregingsresultat uten perioder

        BeregningsresultatMedUttaksplanDto dto = BeregningsresultatMedUttaksplanMapper.lagBeregningsresultatMedUttaksplan(behandling, null, beregningsresultatFP);

        assertThat(dto.getSokerErMor()).isTrue();
        assertThat(dto.getPerioder()).isEmpty();
    }

    @Test
    public void skalLageEnPeriodePerBeregningsresultatPeriode() {
        Behandling behandling = lagBehandling(); // Behandling
        Behandlingsresultat.opprettFor(behandling);
        BeregningsresultatFP beregningsresultatFP = lagBeregningsresultatFP(); // Beregingsresultat uten perioder
        VirksomhetEntitet virksomhet = new VirksomhetEntitet.Builder().medOrgnr("123").build();
        UttakAktivitetEntitet aktivitetEntitet = ordinærtArbeidsforholdUttakAktivitet(virksomhet, ArbeidsforholdRef.ref(UUID.randomUUID().toString()));
        UttakResultatEntitet uttakResultat = lagUttakResultatPeriodeMedEnPeriode(Collections.singletonList(aktivitetEntitet)); // Uttaksplan med én periode som inneholder de to beregningsresultatperiodene

        lagP1(beregningsresultatFP); // Legg til en periode

        List<BeregningsresultatPeriodeDto> periodeDtoer = BeregningsresultatMedUttaksplanMapper.lagPerioder(uttakResultat, beregningsresultatFP);

        assertThat(periodeDtoer).hasSize(1);

        lagP2(beregningsresultatFP); // Legg til en periode til

        periodeDtoer = BeregningsresultatMedUttaksplanMapper.lagPerioder(uttakResultat, beregningsresultatFP);

        assertThat(periodeDtoer).hasSize(2);

        BeregningsresultatPeriodeDto p1 = periodeDtoer.get(0);
        assertThat(p1.getDagsats()).isEqualTo(0);
        assertThat(p1.getFom()).isEqualTo(P1_FOM);
        assertThat(p1.getTom()).isEqualTo(P1_TOM);
        assertThat(p1.getAndeler()).isEmpty();

        BeregningsresultatPeriodeDto p2 = periodeDtoer.get(1);
        assertThat(p2.getDagsats()).isEqualTo(0);
        assertThat(p2.getFom()).isEqualTo(P2_FOM);
        assertThat(p2.getTom()).isEqualTo(P2_TOM);
        assertThat(p2.getAndeler()).isEmpty();
    }

    @Test
    public void skalBeregneDagsatsPerPeriode() {
        VirksomhetEntitet virksomhet = virksomhet("1234");

        BeregningsresultatFP beregningsresultatFP = lagBeregningsresultatFP();
        BeregningsresultatPeriode beregningsresultatPeriode1 = lagP1(beregningsresultatFP);
        lagAndelTilSøker(beregningsresultatPeriode1, 100, virksomhet);
        lagAndelTilArbeidsgiver(beregningsresultatPeriode1, virksomhet, 100);
        BeregningsresultatPeriode beregningsresultatPeriode2 = lagP2(beregningsresultatFP);
        lagAndelTilArbeidsgiver(beregningsresultatPeriode2, virksomhet, 100);

        int dagsatsP1 = BeregningsresultatMedUttaksplanMapper.beregnDagsats(beregningsresultatPeriode1);
        int dagsatsP2 = BeregningsresultatMedUttaksplanMapper.beregnDagsats(beregningsresultatPeriode2);

        assertThat(dagsatsP1).isEqualTo(200);
        assertThat(dagsatsP2).isEqualTo(100);
    }

    private VirksomhetEntitet virksomhet(String orgnr) {
        return new VirksomhetEntitet.Builder()
            .medOrgnr(orgnr)
            .medNavn("Virknavn " + orgnr)
            .oppdatertOpplysningerNå()
            .build();
    }

    @Test
    public void skalLageAndelerPerPeriodeEttArbeidsforhold() {
        // Arrange 1: Kun andel for søker
        VirksomhetEntitet virksomhet = virksomhet("1234");

        Behandling behandling = lagBehandling();
        Behandlingsresultat.opprettFor(behandling);
        UttakAktivitetEntitet uttakAktivitet1 = ordinærtArbeidsforholdUttakAktivitet(virksomhet, null);
        UttakResultatEntitet uttakResultat = lagUttakResultatPeriodeMedEnPeriode(Collections.singletonList(uttakAktivitet1));

        BeregningsresultatFP beregningsresultatFP = lagBeregningsresultatFP(); // Beregingsresultat
        BeregningsresultatPeriode beregningsresultatPeriode = lagP1(beregningsresultatFP); // Periode uten andeler
        lagAndelTilSøker(beregningsresultatPeriode, 100, uttakAktivitet1.getVirksomhet(), uttakAktivitet1.getArbeidsforholdId()); // Legg til en andel til søker

        List<BeregningsresultatPeriodeAndelDto> andeler = BeregningsresultatMedUttaksplanMapper.lagAndeler(beregningsresultatPeriode, uttakResultat, Collections.emptyMap());

        assertThat(andeler).hasSize(1);

        // Arrange 2: Andel for søker og arbeidsgiver
        lagAndelTilArbeidsgiver(beregningsresultatPeriode, virksomhet, 100, uttakAktivitet1.getArbeidsforholdId()); // Legg til en andel til arbeidsgiver

        andeler = BeregningsresultatMedUttaksplanMapper.lagAndeler(beregningsresultatPeriode, uttakResultat, Collections.emptyMap());

        assertThat(andeler).hasSize(1);
        assertAndelArbeidsgiver(andeler, virksomhet.getOrgnr(), 100);
    }

    @Test
    public void skalLageAndelerPerPeriodeToArbeidsforhold() {
        VirksomhetEntitet virksomhet1 = virksomhet("1234");
        VirksomhetEntitet virksomhet2 = virksomhet("3456");

        Behandling behandling = lagBehandling();
        Behandlingsresultat.opprettFor(behandling);
        UttakAktivitetEntitet uttakAktivitet1 = ordinærtArbeidsforholdUttakAktivitet(virksomhet1, null);
        UttakAktivitetEntitet uttakAktivitet2 = ordinærtArbeidsforholdUttakAktivitet(virksomhet2, ArbeidsforholdRef.ref("arbeidId"));
        UttakResultatEntitet uttakResultat = lagUttakResultatPeriodeMedEnPeriode(Arrays.asList(uttakAktivitet1, uttakAktivitet2));

        BeregningsresultatFP beregningsresultatFP = lagBeregningsresultatFP(); // Beregingsresultat
        BeregningsresultatPeriode beregningsresultatPeriode = lagP1(beregningsresultatFP); // Periode uten andeler
        lagAndelTilSøker(beregningsresultatPeriode, 100, uttakAktivitet1.getVirksomhet(), uttakAktivitet1.getArbeidsforholdId()); // Legg til en andel til søker
        lagAndelTilSøker(beregningsresultatPeriode, 200, uttakAktivitet2.getVirksomhet(), uttakAktivitet2.getArbeidsforholdId()); // Legg til en andel til søker
        lagAndelTilArbeidsgiver(beregningsresultatPeriode, virksomhet1, 200, uttakAktivitet1.getArbeidsforholdId());
        lagAndelTilArbeidsgiver(beregningsresultatPeriode, virksomhet2, 100, uttakAktivitet2.getArbeidsforholdId());

        List<BeregningsresultatPeriodeAndelDto> andeler = BeregningsresultatMedUttaksplanMapper.lagAndeler(beregningsresultatPeriode, uttakResultat, Collections.emptyMap());

        assertThat(andeler).hasSize(2);
        assertAndelArbeidsgiver(andeler, virksomhet1.getOrgnr(), 200);
        assertAndelArbeidsgiver(andeler, virksomhet2.getOrgnr(), 100);
    }

    @Test
    public void skalLageAndelerForKombibasjonsstatuser() {
        // Arrange 1
        Behandling behandling = lagBehandling();
        Behandlingsresultat.opprettFor(behandling);
        UttakResultatEntitet uttakResultat = lagUttakResultatPeriodeMedEnPeriode(Arrays.asList(
            new UttakAktivitetEntitet.Builder().medUttakArbeidType(UttakArbeidType.ANNET).build(),
            new UttakAktivitetEntitet.Builder().medUttakArbeidType(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE).build())
        );

        BeregningsresultatFP beregningsresultatFP = lagBeregningsresultatFP(); // Beregingsresultat
        BeregningsresultatPeriode beregningsresultatPeriode = lagP1(beregningsresultatFP); // Periode uten andeler

        lagAndelTilSøkerMedAktivitetStatus(beregningsresultatPeriode, 1000, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        lagAndelTilSøkerMedAktivitetStatus(beregningsresultatPeriode, 2000, AktivitetStatus.DAGPENGER);

        List<BeregningsresultatPeriodeAndelDto> andeler = BeregningsresultatMedUttaksplanMapper.lagAndeler(beregningsresultatPeriode, uttakResultat, Collections.emptyMap());

        assertThat(andeler).hasSize(2);
        BeregningsresultatPeriodeAndelDto andel1 = andeler.stream().filter(a -> a.getAktivitetStatus().equals(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)).findFirst().orElse(null);
        BeregningsresultatPeriodeAndelDto andel2 = andeler.stream().filter(a -> a.getAktivitetStatus().equals(AktivitetStatus.DAGPENGER)).findFirst().orElse(null);
        assertThat(andel1.getTilSoker()).isEqualTo(1000);
        assertThat(andel2.getTilSoker()).isEqualTo(2000);
        assertThat(andel1.getRefusjon()).isEqualTo(0);
        assertThat(andel2.getRefusjon()).isEqualTo(0);
    }

    @Test
    public void skalSlåSammenAndelerMedSammeArbeidsforholdId() {
        // Arrange
        VirksomhetEntitet virksomhet = virksomhet("1234");

        Behandling behandling = lagBehandling();
        Behandlingsresultat.opprettFor(behandling);
        ArbeidsforholdRef arbeidsforholdRef = ArbeidsforholdRef.ref(UUID.randomUUID().toString());
        UttakAktivitetEntitet uttakAktivitet = ordinærtArbeidsforholdUttakAktivitet(virksomhet, arbeidsforholdRef);
        UttakResultatEntitet uttakResultat = lagUttakResultatPeriodeMedEnPeriode(Collections.singletonList(uttakAktivitet));

        BeregningsresultatFP beregningsresultatFP = lagBeregningsresultatFP(); // Beregingsresultat
        BeregningsresultatPeriode beregningsresultatPeriode = lagP1(beregningsresultatFP); // Periode uten andeler
        ArrayList<BeregningsresultatPeriode> beregningsresultatPerioder = new ArrayList<>();
        beregningsresultatPerioder.add(beregningsresultatPeriode);

        lagAndelTilSøker(beregningsresultatPeriode, 500, virksomhet, uttakAktivitet.getArbeidsforholdId());
        lagAndelTilSøker(beregningsresultatPeriode, 1000, virksomhet, uttakAktivitet.getArbeidsforholdId());
        lagAndelTilArbeidsgiver(beregningsresultatPeriode, virksomhet, 250, uttakAktivitet.getArbeidsforholdId());
        lagAndelTilArbeidsgiver(beregningsresultatPeriode, virksomhet, 500, uttakAktivitet.getArbeidsforholdId());

        List<BeregningsresultatPeriodeAndelDto> andeler = BeregningsresultatMedUttaksplanMapper.lagAndeler(beregningsresultatPeriode, uttakResultat, Collections.emptyMap());

        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getTilSoker()).isEqualTo(1500);
        assertAndelArbeidsgiver(andeler, virksomhet.getOrgnr(), 750);
    }

    @Test
    public void skalFinneRiktigSisteUtbetalingsdato() {
        VirksomhetEntitet virksomhet1 = virksomhet("1234");
        VirksomhetEntitet virksomhet2 = virksomhet("5678");
        VirksomhetEntitet virksomhet3 = virksomhet("9101112");
        Behandling behandling = lagBehandling();
        Behandlingsresultat.opprettFor(behandling);
        UttakAktivitetEntitet uttakAktivitet1 = ordinærtArbeidsforholdUttakAktivitet(virksomhet1, ArbeidsforholdRef.ref(UUID.randomUUID().toString()));
        UttakAktivitetEntitet uttakAktivitet2 = ordinærtArbeidsforholdUttakAktivitet(virksomhet2, ArbeidsforholdRef.ref(UUID.randomUUID().toString()));
        UttakAktivitetEntitet uttakAktivitet3 = ordinærtArbeidsforholdUttakAktivitet(virksomhet3, ArbeidsforholdRef.ref(UUID.randomUUID().toString()));
        UttakResultatEntitet uttakResultat = lagUttakResultatPeriodeMedEnPeriode(P1_FOM, P3_TOM,
            Arrays.asList(uttakAktivitet1, uttakAktivitet2, uttakAktivitet3));
        BeregningsresultatFP beregningsresultatFP = lagBeregningsresultatFP();
        BeregningsresultatPeriode beregningsresultatPeriode = lagP1(beregningsresultatFP);
        BeregningsresultatPeriode beregningsresultatPeriode2 = lagP2(beregningsresultatFP);
        BeregningsresultatPeriode beregningsresultatPeriode3 = lagP3(beregningsresultatFP);

        lagAndelTilSøker(beregningsresultatPeriode, 500, uttakAktivitet1.getVirksomhet(), uttakAktivitet1.getArbeidsforholdId());
        lagAndelTilSøker(beregningsresultatPeriode, 1000, uttakAktivitet2.getVirksomhet(), uttakAktivitet2.getArbeidsforholdId());
        lagAndelTilSøker(beregningsresultatPeriode2, 0, uttakAktivitet1.getVirksomhet(), uttakAktivitet1.getArbeidsforholdId());
        lagAndelTilSøker(beregningsresultatPeriode2, 1000, uttakAktivitet2.getVirksomhet(), uttakAktivitet2.getArbeidsforholdId());
        lagAndelTilSøker(beregningsresultatPeriode, 300, uttakAktivitet3.getVirksomhet(), uttakAktivitet3.getArbeidsforholdId());
        lagAndelTilArbeidsgiver(beregningsresultatPeriode, uttakAktivitet3.getVirksomhet(), 250, uttakAktivitet3.getArbeidsforholdId());
        lagAndelTilSøker(beregningsresultatPeriode2, 0, uttakAktivitet3.getVirksomhet(), uttakAktivitet3.getArbeidsforholdId());
        lagAndelTilArbeidsgiver(beregningsresultatPeriode2, uttakAktivitet3.getVirksomhet(), 250, uttakAktivitet3.getArbeidsforholdId());
        lagAndelTilSøker(beregningsresultatPeriode3, 0, uttakAktivitet3.getVirksomhet(), uttakAktivitet3.getArbeidsforholdId());

        //Act
        List<BeregningsresultatPeriodeDto> andeler = BeregningsresultatMedUttaksplanMapper.lagPerioder(uttakResultat, beregningsresultatFP);

        //Assert
        andeler.stream().flatMap(a -> Arrays.stream(a.getAndeler())).filter(andel -> andel.getArbeidsgiverOrgnr().equals(virksomhet1.getOrgnr()))
            .forEach(andel1 -> assertThat(andel1.getSisteUtbetalingsdato()).isEqualTo(P1_TOM));
        andeler.stream().flatMap(a -> Arrays.stream(a.getAndeler())).filter(andel -> andel.getArbeidsgiverOrgnr().equals(virksomhet2.getOrgnr()))
            .forEach(andel1 -> assertThat(andel1.getSisteUtbetalingsdato()).isEqualTo(P2_TOM));
        andeler.stream().flatMap(a -> Arrays.stream(a.getAndeler())).filter(andel -> andel.getArbeidsgiverOrgnr().equals(virksomhet3.getOrgnr()))
            .forEach(andel1 -> assertThat(andel1.getSisteUtbetalingsdato()).isEqualTo(P2_TOM));
    }

    private UttakAktivitetEntitet ordinærtArbeidsforholdUttakAktivitet(VirksomhetEntitet virksomhet, ArbeidsforholdRef arbeidsforholdRef) {
        return new UttakAktivitetEntitet.Builder()
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .medArbeidsforhold(virksomhet, arbeidsforholdRef)
            .build();
    }

    private void assertAndelArbeidsgiver(List<BeregningsresultatPeriodeAndelDto> andeler, String arbeidsgiver, int forventetRefusjon) {
        Optional<BeregningsresultatPeriodeAndelDto> andel = hentAndelForArbeidgiver(andeler, arbeidsgiver);
        assertThat(andel).as("arbeidsgiverAndel").hasValueSatisfying(a -> {
            assertThat(a.getArbeidsgiverOrgnr()).as("arbeidsgiver").isEqualTo(arbeidsgiver);
            assertThat(a.getRefusjon()).as("refusjon").isEqualTo(forventetRefusjon);
        });
    }

    private Optional<BeregningsresultatPeriodeAndelDto> hentAndelForArbeidgiver(List<BeregningsresultatPeriodeAndelDto> andeler, String arbeidsgiver) {
        return andeler.stream().filter(a -> a.getArbeidsgiverOrgnr().equals(arbeidsgiver)).findFirst();
    }

    private static Behandling lagBehandling() {
        NavBruker søker = NavBruker.opprettNy(new Personinfo.Builder()
            .medAktørId(AKTØR_ID)
            .medFnr("42424242424")
            .medNavn("42")
            .medFødselsdato(LocalDate.of(42, 42 % 12 + 1, 42 % 31 + 1))
            .medNavBrukerKjønn(NavBrukerKjønn.UDEFINERT)
            .build());
        Fagsak fagsak = FagsakBuilder.nyForeldrepengerForMor().medBruker(søker).build();
        return Behandling.forFørstegangssøknad(fagsak)
            .build();
    }

    private static UttakResultatEntitet lagUttakResultatPeriodeMedEnPeriode(List<UttakAktivitetEntitet> uttakAktiviteter) {
        return lagUttakResultatPeriodeMedEnPeriode(P1_FOM, P1_TOM, uttakAktiviteter);
    }

    private static UttakResultatEntitet lagUttakResultatPeriodeMedEnPeriode(LocalDate p1Fom, LocalDate p1Tom, List<UttakAktivitetEntitet> uttakAktiviteter) {
        UttakResultatPeriodeEntitet uttakResultatPeriode = new UttakResultatPeriodeEntitet.Builder(p1Fom, p1Tom)
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();
        for (UttakAktivitetEntitet uttakAktivitet : uttakAktiviteter) {
            UttakResultatPeriodeAktivitetEntitet periodeAktivitet = new UttakResultatPeriodeAktivitetEntitet.Builder(uttakResultatPeriode, uttakAktivitet)
                .medTrekkonto(StønadskontoType.FELLESPERIODE)
                .medTrekkdager(20)
                .medArbeidsprosent(BigDecimal.ZERO)
                .build();
            uttakResultatPeriode.leggTilAktivitet(periodeAktivitet);
        }

        UttakResultatPerioderEntitet uttakResultatPerioder = new UttakResultatPerioderEntitet();
        uttakResultatPerioder.leggTilPeriode(uttakResultatPeriode);
        UttakResultatEntitet resultat = new UttakResultatEntitet();
        resultat.setOpprinneligPerioder(uttakResultatPerioder);
        return resultat;
    }

    private static BeregningsresultatFP lagBeregningsresultatFP() {
        return BeregningsresultatFP.builder()
            .medRegelInput("")
            .medRegelSporing("")
            .build();
    }

    private static BeregningsresultatPeriode lagP1(BeregningsresultatFP beregningsresultatFP) {
        return BeregningsresultatPeriode.builder()
            .medBeregningsresultatPeriodeFomOgTom(P1_FOM, P1_TOM)
            .build(beregningsresultatFP);
    }

    private static BeregningsresultatPeriode lagP2(BeregningsresultatFP beregningsresultatFP) {
        return BeregningsresultatPeriode.builder()
            .medBeregningsresultatPeriodeFomOgTom(P2_FOM, P2_TOM)
            .build(beregningsresultatFP);
    }

    private static BeregningsresultatPeriode lagP3(BeregningsresultatFP beregningsresultatFP) {
        return BeregningsresultatPeriode.builder()
            .medBeregningsresultatPeriodeFomOgTom(P3_FOM, P3_TOM)
            .build(beregningsresultatFP);
    }

    private static BeregningsresultatAndel lagAndelTilArbeidsgiver(BeregningsresultatPeriode periode, VirksomhetEntitet virksomhet, int refusjon) {
        return lagAndelTilArbeidsgiver(periode, virksomhet, refusjon, null);
    }

    private static BeregningsresultatAndel lagAndelTilArbeidsgiver(BeregningsresultatPeriode periode, VirksomhetEntitet virksomhet, int refusjon, String arbeidsforholdId) {
        return BeregningsresultatAndel.builder()
            .medVirksomhet(virksomhet)
            .medDagsats(refusjon)
            .medArbforholdId(arbeidsforholdId)
            .medAktivitetstatus(AktivitetStatus.ARBEIDSTAKER)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medDagsatsFraBg(refusjon)
            .medStillingsprosent(BigDecimal.valueOf(100))
            .medUtbetalingsgrad(BigDecimal.valueOf(100))
            .medBrukerErMottaker(false)
            .build(periode);
    }

    private static BeregningsresultatAndel lagAndelTilSøker(BeregningsresultatPeriode periode, int tilSøker, VirksomhetEntitet virksomhet) {
        return lagAndelTilSøker(periode, tilSøker, virksomhet, null);
    }

    private static BeregningsresultatAndel lagAndelTilSøker(BeregningsresultatPeriode periode, int tilSøker, VirksomhetEntitet virksomhet, String arbeidsforholdId) {
        return BeregningsresultatAndel.builder()
            .medVirksomhet(virksomhet)
            .medDagsats(tilSøker)
            .medArbforholdId(arbeidsforholdId)
            .medAktivitetstatus(AktivitetStatus.ARBEIDSTAKER)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medDagsatsFraBg(tilSøker)
            .medStillingsprosent(BigDecimal.valueOf(100))
            .medUtbetalingsgrad(BigDecimal.valueOf(100))
            .medBrukerErMottaker(true)
            .build(periode);
    }

    private static BeregningsresultatAndel lagAndelTilSøkerMedAktivitetStatus(BeregningsresultatPeriode periode, int tilSøker, AktivitetStatus aktivitetStatus) {
        return BeregningsresultatAndel.builder()
            .medVirksomhet(null)
            .medDagsats(tilSøker)
            .medAktivitetstatus(aktivitetStatus)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medDagsatsFraBg(tilSøker)
            .medStillingsprosent(BigDecimal.valueOf(100))
            .medUtbetalingsgrad(BigDecimal.valueOf(100))
            .medBrukerErMottaker(true)
            .build(periode);
    }
}
