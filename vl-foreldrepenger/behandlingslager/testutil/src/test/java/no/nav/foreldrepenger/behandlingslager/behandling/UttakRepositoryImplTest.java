package no.nav.foreldrepenger.behandlingslager.behandling;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatDokRegelEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeSøknadEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class UttakRepositoryImplTest {

    @Rule
    public final RepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    private final Repository repository = repoRule.getRepository();
    private final UttakRepository uttakRepository = new UttakRepositoryImpl(repoRule.getEntityManager());
    private Behandling behandling;
    private VirksomhetEntitet virksomhet;

    @Before
    public void setUp() {

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        behandling = scenario.lagre(repositoryProvider);
        repository.lagre(behandling.getBehandlingsresultat());

        virksomhet = new VirksomhetEntitet.Builder().medOrgnr("orgnr").oppdatertOpplysningerNå().build();
        repository.lagre(virksomhet);
    }

    @Test
    public void hentOpprinneligUttakResultat() {
        //Arrange
        LocalDate fom = LocalDate.now();
        LocalDate tom = fom.plusMonths(3);
        StønadskontoType stønadskontoType = StønadskontoType.FORELDREPENGER;
        PeriodeResultatType resultatType = PeriodeResultatType.INNVILGET;
        UttakResultatPerioderEntitet perioder = opprettUttakResultatPeriode(resultatType, fom, tom, stønadskontoType);

        //Act
        uttakRepository.lagreOpprinneligUttakResultatPerioder(behandling, perioder);

        //Assert
        Optional<UttakResultatEntitet> hentetUttakResultatOpt = uttakRepository.hentUttakResultatHvisEksisterer(behandling);
        assertThat(hentetUttakResultatOpt).isPresent();
        UttakResultatEntitet hentetUttakResultat = hentetUttakResultatOpt.get();//NOSONAR

        List<UttakResultatPeriodeEntitet> resultat = hentetUttakResultat.getOpprinneligPerioder().getPerioder();
        assertThat(resultat).hasSize(1);

        assertThat(resultat.get(0).getFom()).isEqualTo(fom);
        assertThat(resultat.get(0).getTom()).isEqualTo(tom);
        assertThat(resultat.get(0).getPeriodeResultatType()).isEqualTo(resultatType);
        assertThat(resultat.get(0).getAktiviteter().get(0).getTrekkonto()).isEqualTo(stønadskontoType);
        assertThat(resultat.get(0).getDokRegel()).isNotNull();
        assertThat(resultat.get(0).getPeriodeSøknad()).isNotNull();
        assertThat(resultat.get(0).getAktiviteter().get(0).getUttakAktivitet()).isNotNull();
    }

    @Test
    public void skal_kunne_endre_opprinnelig_flere_ganger_uten_å_feile_pga_unikhetssjekk_for_aktiv() {
        UttakResultatPerioderEntitet uttakResultat1 = opprettUttakResultatPeriode(PeriodeResultatType.IKKE_FASTSATT, LocalDate.now(), LocalDate.now().plusMonths(3), StønadskontoType.FORELDREPENGER);
        UttakResultatPerioderEntitet overstyrt1 = opprettUttakResultatPeriode(PeriodeResultatType.INNVILGET, LocalDate.now(), LocalDate.now().plusMonths(3), StønadskontoType.FORELDREPENGER);
        UttakResultatPerioderEntitet uttakResultat2 = opprettUttakResultatPeriode(PeriodeResultatType.AVSLÅTT, LocalDate.now(), LocalDate.now().plusMonths(3), StønadskontoType.FORELDREPENGER);
        UttakResultatPerioderEntitet uttakResultat3 = opprettUttakResultatPeriode(PeriodeResultatType.INNVILGET, LocalDate.now(), LocalDate.now().plusMonths(3), StønadskontoType.FORELDREPENGER);

        //Act
        uttakRepository.lagreOpprinneligUttakResultatPerioder(behandling, uttakResultat1);
        uttakRepository.lagreOverstyrtUttakResultatPerioder(behandling, overstyrt1);
        assertOpprinneligHarResultatType(PeriodeResultatType.IKKE_FASTSATT);
        assertThat(uttakRepository.hentUttakResultatHvisEksisterer(behandling).get().getOverstyrtPerioder()).isNotNull(); //NOSONAR
        uttakRepository.lagreOpprinneligUttakResultatPerioder(behandling, uttakResultat2);
        assertOpprinneligHarResultatType(PeriodeResultatType.AVSLÅTT);
        assertThat(uttakRepository.hentUttakResultatHvisEksisterer(behandling).get().getOverstyrtPerioder()).isNull(); //NOSONAR
        uttakRepository.lagreOpprinneligUttakResultatPerioder(behandling, uttakResultat3);
        assertOpprinneligHarResultatType(PeriodeResultatType.INNVILGET);
    }

    @Test
    public void hentOverstyrtUttakResultat() {
        //Arrange
        UttakResultatPerioderEntitet opprinnelig = opprettUttakResultatPeriode(PeriodeResultatType.INNVILGET,
            LocalDate.now(), LocalDate.now().plusMonths(3), StønadskontoType.FORELDREPENGER);
        uttakRepository.lagreOpprinneligUttakResultatPerioder(behandling, opprinnelig);

        LocalDate overstyrtFom = LocalDate.now().plusDays(1);
        LocalDate overstyrtTom = LocalDate.now().plusMonths(4);
        PeriodeResultatType overstyrtResultatType = PeriodeResultatType.AVSLÅTT;
        StønadskontoType overstyrtKonto = StønadskontoType.FORELDREPENGER_FØR_FØDSEL;
        UttakResultatPerioderEntitet overstyrt = opprettUttakResultatPeriode(
            overstyrtResultatType,
            overstyrtFom,
            overstyrtTom,
            overstyrtKonto);

        //Act
        uttakRepository.lagreOverstyrtUttakResultatPerioder(behandling, overstyrt);

        //Assert
        Optional<UttakResultatEntitet> hentetUttakResultatOpt = uttakRepository.hentUttakResultatHvisEksisterer(behandling);
        assertThat(hentetUttakResultatOpt).isPresent();
        UttakResultatEntitet hentetUttakResultat = hentetUttakResultatOpt.get();//NOSONAR

        assertThat(hentetUttakResultat.getOpprinneligPerioder().getPerioder()).hasSize(1);
        List<UttakResultatPeriodeEntitet> resultat = hentetUttakResultat.getOverstyrtPerioder().getPerioder();
        assertThat(resultat).hasSize(1);

        assertThat(resultat.get(0).getFom()).isEqualTo(overstyrtFom);
        assertThat(resultat.get(0).getTom()).isEqualTo(overstyrtTom);
        assertThat(resultat.get(0).getPeriodeResultatType()).isEqualTo(overstyrtResultatType);
        assertThat(resultat.get(0).getAktiviteter().get(0).getTrekkonto()).isEqualTo(overstyrtKonto);
    }

    @Test
    public void endringAvOverstyrtSkalResultereINyttUttakResultatMedSammeOpprinnelig() {
        UttakResultatPerioderEntitet opprinnelig = opprettUttakResultatPeriode(PeriodeResultatType.IKKE_FASTSATT, LocalDate.now(), LocalDate.now().plusMonths(3), StønadskontoType.FORELDREPENGER);
        UttakResultatPerioderEntitet overstyrt1 = opprettUttakResultatPeriode(PeriodeResultatType.AVSLÅTT, LocalDate.now(), LocalDate.now().plusMonths(3), StønadskontoType.FORELDREPENGER);
        UttakResultatPerioderEntitet overstyrt2 = opprettUttakResultatPeriode(PeriodeResultatType.INNVILGET, LocalDate.now(), LocalDate.now().plusMonths(3), StønadskontoType.FORELDREPENGER);
        uttakRepository.lagreOpprinneligUttakResultatPerioder(behandling, opprinnelig);

        //Act
        uttakRepository.lagreOverstyrtUttakResultatPerioder(behandling, overstyrt1);
        assertOverstyrtHarResultatType(PeriodeResultatType.AVSLÅTT);
        assertOpprinneligHarResultatType(PeriodeResultatType.IKKE_FASTSATT);
        uttakRepository.lagreOverstyrtUttakResultatPerioder(behandling, overstyrt2);
        assertOverstyrtHarResultatType(PeriodeResultatType.INNVILGET);
        assertOpprinneligHarResultatType(PeriodeResultatType.IKKE_FASTSATT);
    }

    @Test
    public void utbetalingsprosentOgArbeidstidsprosentSkalHa2Desimaler() {
        //Arrange
        UttakResultatPerioderEntitet opprinnelig = opprettUttakResultatPeriode(PeriodeResultatType.INNVILGET,
            LocalDate.now(), LocalDate.now().plusMonths(3), StønadskontoType.FORELDREPENGER,
            new BigDecimal("10.55"), new BigDecimal("20.57"));
        uttakRepository.lagreOpprinneligUttakResultatPerioder(behandling, opprinnelig);

        //Assert
        Optional<UttakResultatEntitet> hentetUttakResultatOpt = uttakRepository.hentUttakResultatHvisEksisterer(behandling);

        UttakResultatPeriodeAktivitetEntitet aktivitet = hentetUttakResultatOpt.get().getGjeldendePerioder().getPerioder().get(0).getAktiviteter().get(0);
        assertThat(aktivitet.getUtbetalingsprosent()).isEqualTo(new BigDecimal("20.57"));
        assertThat(aktivitet.getArbeidsprosent()).isEqualTo(new BigDecimal("10.55"));
    }

    private void assertOverstyrtHarResultatType(PeriodeResultatType type) {
        Optional<UttakResultatEntitet> uttakResultatEntitet = uttakRepository.hentUttakResultatHvisEksisterer(behandling);
        assertThat(uttakResultatEntitet).isPresent();
        assertHarResultatType(type, uttakResultatEntitet.get().getOverstyrtPerioder()); //NOSONAR
    }

    private void assertOpprinneligHarResultatType(PeriodeResultatType type) {
        Optional<UttakResultatEntitet> uttakResultatEntitet = uttakRepository.hentUttakResultatHvisEksisterer(behandling);
        assertThat(uttakResultatEntitet).isPresent();
        assertHarResultatType(type, uttakResultatEntitet.get().getOpprinneligPerioder()); //NOSONAR
    }

    private void assertHarResultatType(PeriodeResultatType type, UttakResultatPerioderEntitet perioderEntitet) {
        List<UttakResultatPeriodeEntitet> perioder = perioderEntitet.getPerioder();
        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).getPeriodeResultatType()).isEqualTo(type);
    }

    private UttakResultatPerioderEntitet opprettUttakResultatPeriode(PeriodeResultatType resultat,
                                                                     LocalDate fom,
                                                                     LocalDate tom,
                                                                     StønadskontoType stønadskontoType) {
        return opprettUttakResultatPeriode(resultat, fom, tom, stønadskontoType, new BigDecimal("100.00"));
    }

    private UttakResultatPerioderEntitet opprettUttakResultatPeriode(PeriodeResultatType resultat,
                                                                     LocalDate fom,
                                                                     LocalDate tom,
                                                                     StønadskontoType stønadskontoType,
                                                                     BigDecimal graderingArbeidsprosent) {
        return opprettUttakResultatPeriode(resultat, fom, tom, stønadskontoType, graderingArbeidsprosent, BigDecimal.valueOf(100));
    }

    private UttakResultatPerioderEntitet opprettUttakResultatPeriode(PeriodeResultatType resultat,
                                                                     LocalDate fom,
                                                                     LocalDate tom,
                                                                     StønadskontoType stønadskontoType,
                                                                     BigDecimal graderingArbeidsprosent,
                                                                     BigDecimal utbetalingsprosent) {

        UttakAktivitetEntitet uttakAktivitet = new UttakAktivitetEntitet.Builder()
            .medArbeidsforhold(virksomhet, ArbeidsforholdRef.ref("arb_id"))
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();
        UttakResultatPeriodeSøknadEntitet periodeSøknad = new UttakResultatPeriodeSøknadEntitet.Builder()
            .medMottattDato(LocalDate.now())
            .medUttakPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medGraderingArbeidsprosent(graderingArbeidsprosent)
            .medSamtidigUttak(true)
            .medSamtidigUttaksprosent(BigDecimal.TEN)
            .build();
        UttakResultatDokRegelEntitet dokRegel = UttakResultatDokRegelEntitet.utenManuellBehandling()
            .medRegelInput(" ")
            .medRegelEvaluering(" ")
            .build();
        UttakResultatPeriodeEntitet uttakResultatPeriode = new UttakResultatPeriodeEntitet.Builder(fom, tom)
            .medDokRegel(dokRegel)
            .medPeriodeResultat(resultat, PeriodeResultatÅrsak.UKJENT)
            .medPeriodeSoknad(periodeSøknad)
            .build();

        UttakResultatPeriodeAktivitetEntitet periodeAktivitet = UttakResultatPeriodeAktivitetEntitet.builder(uttakResultatPeriode,
            uttakAktivitet)
            .medTrekkonto(stønadskontoType)
            .medTrekkdager(10)
            .medArbeidsprosent(graderingArbeidsprosent)
            .medUtbetalingsprosent(utbetalingsprosent)
            .build();

        uttakResultatPeriode.leggTilAktivitet(periodeAktivitet);

        UttakResultatPerioderEntitet perioder = new UttakResultatPerioderEntitet();
        perioder.leggTilPeriode(uttakResultatPeriode);

        return perioder;
    }

}
