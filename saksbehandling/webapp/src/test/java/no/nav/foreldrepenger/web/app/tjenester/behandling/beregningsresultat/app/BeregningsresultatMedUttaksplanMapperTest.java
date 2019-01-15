package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.dto.BeregningsresultatMedUttaksplanDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.dto.BeregningsresultatPeriodeAndelDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.dto.BeregningsresultatPeriodeDto;

public class BeregningsresultatMedUttaksplanMapperTest {

    private static final LocalDate P1_FOM = LocalDate.now();
    private static final LocalDate P1_TOM = LocalDate.now().plusDays(10);
    private static final LocalDate P2_FOM = LocalDate.now().plusDays(11);
    private static final LocalDate P2_TOM = LocalDate.now().plusDays(20);
    private static final AktørId AKTØR_ID = new AktørId("42");

    @Test
    public void skalLageDto() {
        Behandling behandling = lagBehandling(); // Behandling
        Behandlingsresultat.opprettFor(behandling);
        BeregningsresultatPerioder beregningsresultat = lagBeregningsresultatFP(); // Beregingsresultat uten perioder

        BeregningsresultatMedUttaksplanDto dto = BeregningsresultatMedUttaksplanMapper.lagBeregningsresultatMedUttaksplan(behandling, beregningsresultat);

        assertThat(dto.getPerioder()).isEmpty();
    }

    @Test
    public void skalLageEnPeriodePerBeregningsresultatPeriode() {
        Behandling behandling = lagBehandling(); // Behandling
        Behandlingsresultat.opprettFor(behandling);
        BeregningsresultatPerioder beregningsresultat = lagBeregningsresultatFP(); // Beregingsresultat uten perioder

        lagP1(beregningsresultat); // Legg til en periode

        List<BeregningsresultatPeriodeDto> periodeDtoer = BeregningsresultatMedUttaksplanMapper.lagPerioder(beregningsresultat);

        assertThat(periodeDtoer).hasSize(1);

        lagP2(beregningsresultat); // Legg til en periode til

        periodeDtoer = BeregningsresultatMedUttaksplanMapper.lagPerioder(beregningsresultat);

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

        BeregningsresultatPerioder beregningsresultat = lagBeregningsresultatFP();
        BeregningsresultatPeriode beregningsresultatPeriode1 = lagP1(beregningsresultat);
        lagAndelTilSøker(beregningsresultatPeriode1, 100, virksomhet);
        lagAndelTilArbeidsgiver(beregningsresultatPeriode1, virksomhet, 100);
        BeregningsresultatPeriode beregningsresultatPeriode2 = lagP2(beregningsresultat);
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

        BeregningsresultatPerioder beregningsresultat = lagBeregningsresultatFP(); // Beregingsresultat
        BeregningsresultatPeriode beregningsresultatPeriode = lagP1(beregningsresultat); // Periode uten andeler
        lagAndelTilSøker(beregningsresultatPeriode, 100, virksomhet, "arbeidsforhold1"); // Legg til en andel til søker

        List<BeregningsresultatPeriodeAndelDto> andeler = BeregningsresultatMedUttaksplanMapper.lagAndeler(beregningsresultatPeriode, Collections.emptyMap());

        assertThat(andeler).hasSize(1);

        // Arrange 2: Andel for søker og arbeidsgiver
        lagAndelTilArbeidsgiver(beregningsresultatPeriode, virksomhet, 100, "arbeidsforhold1"); // Legg til en andel til arbeidsgiver

        andeler = BeregningsresultatMedUttaksplanMapper.lagAndeler(beregningsresultatPeriode, Collections.emptyMap());

        assertThat(andeler).hasSize(1);
        assertAndelArbeidsgiver(andeler, virksomhet.getOrgnr(), 100);
    }

    @Test
    public void skalLageAndelerPerPeriodeToArbeidsforhold() {
        VirksomhetEntitet virksomhet1 = virksomhet("1234");
        VirksomhetEntitet virksomhet2 = virksomhet("3456");

        Behandling behandling = lagBehandling();
        Behandlingsresultat.opprettFor(behandling);

        BeregningsresultatPerioder beregningsresultat = lagBeregningsresultatFP(); // Beregingsresultat
        BeregningsresultatPeriode beregningsresultatPeriode = lagP1(beregningsresultat); // Periode uten andeler
        lagAndelTilSøker(beregningsresultatPeriode, 100, virksomhet1, "arbeidsforhold1"); // Legg til en andel til søker
        lagAndelTilSøker(beregningsresultatPeriode, 200, virksomhet2, "arbeidsforhold2"); // Legg til en andel til søker
        lagAndelTilArbeidsgiver(beregningsresultatPeriode, virksomhet1, 200, "arbeidsforhold1");
        lagAndelTilArbeidsgiver(beregningsresultatPeriode, virksomhet2, 100, "arbeidsforhold2");

        List<BeregningsresultatPeriodeAndelDto> andeler = BeregningsresultatMedUttaksplanMapper.lagAndeler(beregningsresultatPeriode, Collections.emptyMap());

        assertThat(andeler).hasSize(2);
        assertAndelArbeidsgiver(andeler, virksomhet1.getOrgnr(), 200);
        assertAndelArbeidsgiver(andeler, virksomhet2.getOrgnr(), 100);
    }

    @Test
    public void skalLageAndelerForKombibasjonsstatuser() {
        // Arrange 1
        Behandling behandling = lagBehandling();
        Behandlingsresultat.opprettFor(behandling);

        BeregningsresultatPerioder beregningsresultat = lagBeregningsresultatFP(); // Beregingsresultat
        BeregningsresultatPeriode beregningsresultatPeriode = lagP1(beregningsresultat); // Periode uten andeler

        lagAndelTilSøkerMedAktivitetStatus(beregningsresultatPeriode, 1000, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        lagAndelTilSøkerMedAktivitetStatus(beregningsresultatPeriode, 2000, AktivitetStatus.DAGPENGER);

        List<BeregningsresultatPeriodeAndelDto> andeler = BeregningsresultatMedUttaksplanMapper.lagAndeler(beregningsresultatPeriode, Collections.emptyMap());

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

        BeregningsresultatPerioder beregningsresultat = lagBeregningsresultatFP(); // Beregingsresultat
        BeregningsresultatPeriode beregningsresultatPeriode = lagP1(beregningsresultat); // Periode uten andeler
        ArrayList<BeregningsresultatPeriode> beregningsresultatPerioder = new ArrayList<>();
        beregningsresultatPerioder.add(beregningsresultatPeriode);

        lagAndelTilSøker(beregningsresultatPeriode, 500, virksomhet, "arbeidsforhold1");
        lagAndelTilSøker(beregningsresultatPeriode, 1000, virksomhet, "arbeidsforhold1");
        lagAndelTilArbeidsgiver(beregningsresultatPeriode, virksomhet, 250, "arbeidsforhold1");
        lagAndelTilArbeidsgiver(beregningsresultatPeriode, virksomhet, 500, "arbeidsforhold1");

        List<BeregningsresultatPeriodeAndelDto> andeler = BeregningsresultatMedUttaksplanMapper.lagAndeler(beregningsresultatPeriode, Collections.emptyMap());

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
        BeregningsresultatPerioder beregningsresultat = lagBeregningsresultatFP();

        //Act
        List<BeregningsresultatPeriodeDto> andeler = BeregningsresultatMedUttaksplanMapper.lagPerioder(beregningsresultat);

        //Assert
        andeler.stream().flatMap(a -> Arrays.stream(a.getAndeler())).filter(andel -> andel.getArbeidsgiverOrgnr().equals(virksomhet1.getOrgnr()))
            .forEach(andel1 -> assertThat(andel1.getSisteUtbetalingsdato()).isEqualTo(P1_TOM));
        andeler.stream().flatMap(a -> Arrays.stream(a.getAndeler())).filter(andel -> andel.getArbeidsgiverOrgnr().equals(virksomhet2.getOrgnr()))
            .forEach(andel1 -> assertThat(andel1.getSisteUtbetalingsdato()).isEqualTo(P2_TOM));
        andeler.stream().flatMap(a -> Arrays.stream(a.getAndeler())).filter(andel -> andel.getArbeidsgiverOrgnr().equals(virksomhet3.getOrgnr()))
            .forEach(andel1 -> assertThat(andel1.getSisteUtbetalingsdato()).isEqualTo(P2_TOM));
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
            .medPersonIdent(PersonIdent.fra("42424242424"))
            .medNavn("42")
            .medFødselsdato(LocalDate.of(42, 42 % 12 + 1, 42 % 31 + 1))
            .medNavBrukerKjønn(NavBrukerKjønn.UDEFINERT)
            .build());
        Fagsak fagsak = FagsakBuilder.nyFagsak().medBruker(søker).build();
        return Behandling.forFørstegangssøknad(fagsak)
            .build();
    }

    private static BeregningsresultatPerioder lagBeregningsresultatFP() {
        return BeregningsresultatPerioder.builder()
            .medRegelInput("")
            .medRegelSporing("")
            .build();
    }

    private static BeregningsresultatPeriode lagP1(BeregningsresultatPerioder beregningsresultat) {
        return BeregningsresultatPeriode.builder()
            .medBeregningsresultatPeriodeFomOgTom(P1_FOM, P1_TOM)
            .build(beregningsresultat);
    }

    private static BeregningsresultatPeriode lagP2(BeregningsresultatPerioder beregningsresultat) {
        return BeregningsresultatPeriode.builder()
            .medBeregningsresultatPeriodeFomOgTom(P2_FOM, P2_TOM)
            .build(beregningsresultat);
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
