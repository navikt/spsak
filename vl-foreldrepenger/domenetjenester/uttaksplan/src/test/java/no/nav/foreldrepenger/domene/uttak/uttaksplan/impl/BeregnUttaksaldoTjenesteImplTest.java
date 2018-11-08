package no.nav.foreldrepenger.domene.uttak.uttaksplan.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.fagsak.Dekningsgrad;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.testutilities.aktør.NavBrukerBuilder;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskonto;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskontoberegning;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.RelatertBehandlingTjeneste;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Virkedager;

public class BeregnUttaksaldoTjenesteImplTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private UttakRepository uttakRepository = new UttakRepositoryImpl(repositoryRule.getEntityManager());
    private RelatertBehandlingTjeneste relatertBehandlingTjeneste = new RelatertBehandlingTjenesteImpl(repositoryProvider);
    private BeregnUttaksaldoTjenesteImpl tjeneste;

    private static Stønadskonto lagStønadskonto(StønadskontoType fellesperiode, int maxDager) {
        return Stønadskonto.builder().medMaxDager(maxDager).medStønadskontoType(fellesperiode).build();
    }

    private static Stønadskontoberegning lagStønadskontoberegning(Stønadskonto... stønadskontoer) {
        Stønadskontoberegning.Builder builder = Stønadskontoberegning.builder()
            .medRegelEvaluering("asdf")
            .medRegelInput("asdf");
        Stream.of(stønadskontoer)
            .forEach(builder::medStønadskonto);
        return builder.build();
    }

    @Before
    public void setUp() {
        tjeneste = new BeregnUttaksaldoTjenesteImpl(repositoryProvider, relatertBehandlingTjeneste);
    }

    @Test
    public void skalFinneTotaltDisponibleDager() {
        Behandling morsBehandling = lagBehandling("42");
        int maxDagerFelles = 16;
        int maxDagerFK = 15;
        final Stønadskontoberegning stønadskontoberegning = lagStønadskontoberegning(lagStønadskonto(StønadskontoType.FELLESPERIODE, maxDagerFelles));
        repositoryProvider.getFagsakRelasjonRepository().lagre(morsBehandling, stønadskontoberegning);
        LocalDate fom = LocalDate.of(2018, Month.MAY, 28);
        LocalDate tom = fom.plusDays(11);

        UttakResultatPeriodeEntitet periode = new UttakResultatPeriodeEntitet.Builder(fom, tom)
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();
        UttakResultatPerioderEntitet uttakResultatPerioder1 = new UttakResultatPerioderEntitet();
        uttakResultatPerioder1.leggTilPeriode(periode);

        VirksomhetEntitet virksomhet = new VirksomhetEntitet.Builder().medOrgnr("virksomhetId").oppdatertOpplysningerNå().build();
        repositoryRule.getRepository().lagre(virksomhet);

        UttakAktivitetEntitet uttakAktivitet1 = new UttakAktivitetEntitet.Builder()
            .medArbeidsforhold(virksomhet, ArbeidsforholdRef.ref("1"))
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();

        UttakAktivitetEntitet uttakAktivitet2 = new UttakAktivitetEntitet.Builder()
            .medArbeidsforhold(virksomhet, ArbeidsforholdRef.ref("2"))
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();

        UttakResultatPeriodeAktivitetEntitet aktivitet1 = aktivitet(uttakAktivitet1, periode, 5, StønadskontoType.FELLESPERIODE, BigDecimal.valueOf(50));
        UttakResultatPeriodeAktivitetEntitet aktivitet2 = aktivitet(uttakAktivitet2, periode, 10, StønadskontoType.FELLESPERIODE, BigDecimal.ZERO);
        periode.leggTilAktivitet(aktivitet1);
        periode.leggTilAktivitet(aktivitet2);

        Behandlingsresultat behandlingsresultat = morsBehandling.getBehandlingsresultat();
        Behandlingsresultat.builderEndreEksisterende(behandlingsresultat).medBehandlingResultatType(BehandlingResultatType.INNVILGET);
        repositoryRule.getRepository().lagre(behandlingsresultat);

        uttakRepository.lagreOpprinneligUttakResultatPerioder(morsBehandling, uttakResultatPerioder1);

        morsBehandling.avsluttBehandling();
        repositoryRule.getRepository().lagre(morsBehandling);

        Behandling farsBehandling = lagBehandling("43");
        final Stønadskontoberegning stønadskontoberegning1 = lagStønadskontoberegning(lagStønadskonto(StønadskontoType.FELLESPERIODE, maxDagerFelles),
            lagStønadskonto(StønadskontoType.FEDREKVOTE, maxDagerFK));
        repositoryProvider.getFagsakRelasjonRepository().kobleFagsaker(morsBehandling.getFagsak(), farsBehandling.getFagsak());
        repositoryProvider.getFagsakRelasjonRepository().lagre(farsBehandling, stønadskontoberegning1);
        repositoryRule.getRepository().flushAndClear();


        UttakResultatPeriodeEntitet periodeFar = new UttakResultatPeriodeEntitet.Builder(fom.plusDays(2), tom)
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();
        UttakResultatPerioderEntitet uttakResultatPerioder2 = new UttakResultatPerioderEntitet();
        uttakResultatPerioder2.leggTilPeriode(periodeFar);

        UttakAktivitetEntitet uttakAktivitetFar1 = new UttakAktivitetEntitet.Builder()
            .medArbeidsforhold(virksomhet, ArbeidsforholdRef.ref("4"))
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();

        UttakAktivitetEntitet uttakAktivitetFar2 = new UttakAktivitetEntitet.Builder()
            .medArbeidsforhold(virksomhet, ArbeidsforholdRef.ref("5"))
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();

        UttakResultatPeriodeAktivitetEntitet aktivitetFar1 = aktivitet(uttakAktivitetFar1, periode, 4, StønadskontoType.FEDREKVOTE, BigDecimal.valueOf(50));
        UttakResultatPeriodeAktivitetEntitet aktivitetFar2 = aktivitet(uttakAktivitetFar2, periode, 5, StønadskontoType.FEDREKVOTE, BigDecimal.valueOf(62, 5));
        periodeFar.leggTilAktivitet(aktivitetFar1);
        periodeFar.leggTilAktivitet(aktivitetFar2);

        uttakRepository.lagreOpprinneligUttakResultatPerioder(farsBehandling, uttakResultatPerioder2);

        Optional<Integer> maxDisponibleDager = tjeneste.beregnDisponibleDager(farsBehandling);

        assertThat(maxDisponibleDager).isPresent();
        assertThat(maxDisponibleDager.get()).isEqualTo((maxDagerFelles - 1) + (maxDagerFK - 4));

    }

    private Behandling lagBehandling(String aktørId) {
        NavBruker bruker = new NavBrukerBuilder()
            .medAktørId(new AktørId(aktørId))
            .build();
        repositoryRule.getEntityManager().persist(bruker);
        final Behandling behandling = Behandling
            .forFørstegangssøknad(Fagsak
                .opprettNy(FagsakYtelseType.FORELDREPENGER,
                    bruker
                ))
            .build();
        Behandlingsresultat.opprettFor(behandling);
        repositoryRule.getEntityManager().persist(behandling.getFagsak());
        repositoryRule.getEntityManager().flush();
        final BehandlingLås lås = repositoryProvider.getBehandlingLåsRepository().taLås(behandling.getId());
        repositoryProvider.getBehandlingRepository().lagre(behandling, lås);
        repositoryProvider.getFagsakRelasjonRepository().opprettRelasjon(behandling.getFagsak(), Dekningsgrad._100);
        return behandling;
    }

    private UttakResultatPeriodeAktivitetEntitet aktivitet(UttakAktivitetEntitet uttakAktivitet,
                                                           UttakResultatPeriodeEntitet uttakPeriode,
                                                           int trekkdager,
                                                           StønadskontoType stønadskontoType,
                                                           BigDecimal arbeidsprosent) {
        return new UttakResultatPeriodeAktivitetEntitet.Builder(uttakPeriode, uttakAktivitet)
            .medTrekkdager(trekkdager)
            .medTrekkonto(stønadskontoType)
            .medArbeidsprosent(arbeidsprosent)
            .build();
    }

    @Test
    public void skalFinneTotaltDisponibleDagerOverlappendePerioder() {
        int maxDagerFelles = 90;
        int maxDagerFK = 95;
        int maxDagerMK = 95;
        int maxDagerFFF = 15;

        // ---------- MOR --------------
        Behandling morsBehandling = lagBehandling("42");
        final Stønadskontoberegning stønadskontoberegning = lagStønadskontoberegning(
            lagStønadskonto(StønadskontoType.FELLESPERIODE, maxDagerFelles),
            lagStønadskonto(StønadskontoType.MØDREKVOTE, maxDagerMK),
            lagStønadskonto(StønadskontoType.FEDREKVOTE, maxDagerFK),
            lagStønadskonto(StønadskontoType.FORELDREPENGER_FØR_FØDSEL, maxDagerFFF),
            lagStønadskonto(StønadskontoType.FLERBARNSDAGER, 10));
        repositoryProvider.getFagsakRelasjonRepository().lagre(morsBehandling, stønadskontoberegning);

        VirksomhetEntitet virksomhet = new VirksomhetEntitet.Builder().medOrgnr("virksomhetId").oppdatertOpplysningerNå().build();
        repositoryRule.getRepository().lagre(virksomhet);

        UttakResultatPerioderEntitet uttakPerioderMor = new UttakResultatPerioderEntitet();

        // Foreldrepenger før fødsel
        UttakResultatPeriodeEntitet periodeFFF = new UttakResultatPeriodeEntitet.Builder(
            LocalDate.of(2018, Month.JULY, 12), LocalDate.of(2018, Month.AUGUST, 1))
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();

        periodeFFF.leggTilAktivitet(
            aktivitet(new UttakAktivitetEntitet.Builder()
                    .medArbeidsforhold(virksomhet, ArbeidsforholdRef.ref("1"))
                    .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
                    .build(),
                periodeFFF, Virkedager.beregnAntallVirkedager(periodeFFF.getFom(), periodeFFF.getTom()), StønadskontoType.FORELDREPENGER_FØR_FØDSEL, BigDecimal.ZERO));

        uttakPerioderMor.leggTilPeriode(periodeFFF);

        // Mødrekvote
        UttakResultatPeriodeEntitet periodeMK = new UttakResultatPeriodeEntitet.Builder(
            LocalDate.of(2018, Month.AUGUST, 2), LocalDate.of(2018, Month.DECEMBER, 12))
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();

        periodeMK.leggTilAktivitet(
            aktivitet(new UttakAktivitetEntitet.Builder()
                    .medArbeidsforhold(virksomhet, ArbeidsforholdRef.ref("1"))
                    .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
                    .build(),
                periodeMK, Virkedager.beregnAntallVirkedager(periodeMK.getFom(), periodeMK.getTom()), StønadskontoType.MØDREKVOTE, BigDecimal.ZERO));

        uttakPerioderMor.leggTilPeriode(periodeMK);

        // Fellesperiode
        UttakResultatPeriodeEntitet periodeFP = new UttakResultatPeriodeEntitet.Builder(
            LocalDate.of(2018, Month.DECEMBER, 13), LocalDate.of(2019, Month.JANUARY, 1))
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();

        periodeFP.leggTilAktivitet(
            aktivitet(new UttakAktivitetEntitet.Builder()
                    .medArbeidsforhold(virksomhet, ArbeidsforholdRef.ref("1"))
                    .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
                    .build(),
                periodeFP, Virkedager.beregnAntallVirkedager(periodeFP.getFom(), periodeFP.getTom()), StønadskontoType.FELLESPERIODE, BigDecimal.ZERO));

        uttakPerioderMor.leggTilPeriode(periodeFP);

        Behandlingsresultat behandlingsresultat = morsBehandling.getBehandlingsresultat();
        Behandlingsresultat.builderEndreEksisterende(behandlingsresultat).medBehandlingResultatType(BehandlingResultatType.INNVILGET);
        repositoryRule.getRepository().lagre(behandlingsresultat);

        uttakRepository.lagreOpprinneligUttakResultatPerioder(morsBehandling, uttakPerioderMor);

        morsBehandling.avsluttBehandling();
        repositoryRule.getRepository().lagre(morsBehandling);


        // ---------- FAR --------------
        Behandling farsBehandling = lagBehandling("43");
        repositoryProvider.getFagsakRelasjonRepository().kobleFagsaker(morsBehandling.getFagsak(), farsBehandling.getFagsak());
        repositoryRule.getRepository().flushAndClear();

        UttakResultatPerioderEntitet uttakPerioderFar = new UttakResultatPerioderEntitet();

        UttakResultatPeriodeEntitet periodeFarFK = new UttakResultatPeriodeEntitet.Builder(
            LocalDate.of(2018, Month.SEPTEMBER, 19), LocalDate.of(2018, Month.SEPTEMBER, 28))
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();

        periodeFarFK.leggTilAktivitet(
            aktivitet(new UttakAktivitetEntitet.Builder()
                    .medArbeidsforhold(virksomhet, ArbeidsforholdRef.ref("4"))
                    .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
                    .build(),
                periodeFarFK, Virkedager.beregnAntallVirkedager(periodeFarFK.getFom(), periodeFarFK.getTom()), StønadskontoType.FEDREKVOTE, BigDecimal.ZERO));

        uttakPerioderFar.leggTilPeriode(periodeFarFK);

        uttakRepository.lagreOpprinneligUttakResultatPerioder(farsBehandling, uttakPerioderFar);

        Optional<Integer> maxDisponibleDager = tjeneste.beregnDisponibleDager(farsBehandling);

        assertThat(maxDisponibleDager).isPresent();
        assertThat(maxDisponibleDager.get()).isEqualTo(171);

    }
}
