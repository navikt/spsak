package no.nav.foreldrepenger.domene.registerinnhenting.startpunkt;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;

public class FørstePermisjonsdagTjenesteTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private UttakRepository uttakRepository = new UttakRepositoryImpl(repoRule.getEntityManager());
    private VirksomhetEntitet virksomhet;

    @Before
    public void setUp() {
        virksomhet = new VirksomhetEntitet.Builder().medOrgnr("orgnr").oppdatertOpplysningerNå().build();
        repoRule.getRepository().lagre(virksomhet);
    }

    @Test
    public void skal_hente_riktig_første_permisjonsdag() {

        Behandling behandling = opprettBehandling();

        LocalDate fom = LocalDate.now();
        LocalDate tom = fom.plusMonths(3);
        StønadskontoType stønadskontoType = StønadskontoType.FORELDREPENGER_FØR_FØDSEL;
        PeriodeResultatType resultatType = PeriodeResultatType.INNVILGET;
        UttakResultatPeriodeEntitet periode = opprettUttakResultatPeriode(resultatType, fom, tom, stønadskontoType);
        UttakResultatPerioderEntitet perioder = new UttakResultatPerioderEntitet();
        perioder.leggTilPeriode(periode);
        uttakRepository.lagreOpprinneligUttakResultatPerioder(behandling, perioder);

        repoRule.getEntityManager().flush();

        Optional<LocalDate> førsteDag =
            new FørstePermisjonsdagTjeneste(repositoryProvider).henteFørstePermisjonsdag(behandling);

        assertThat(førsteDag.isPresent()).isTrue();
        assertThat(førsteDag.get()).isEqualTo(fom);
    }

    @Test
    public void skal_telle_perioder_unansett_innvilget_eller_avslått() {
        Behandling behandling = opprettBehandling();

        LocalDate fom = LocalDate.now();
        LocalDate tom = fom.plusWeeks(1);
        StønadskontoType stønadskontoType = StønadskontoType.FORELDREPENGER_FØR_FØDSEL;
        PeriodeResultatType resultatType = PeriodeResultatType.AVSLÅTT;
        UttakResultatPeriodeEntitet periode = opprettUttakResultatPeriode(resultatType, fom, tom, stønadskontoType);
        UttakResultatPerioderEntitet perioder = new UttakResultatPerioderEntitet();
        perioder.leggTilPeriode(periode);

        LocalDate fom_periode2 = tom.plusDays(1);
        LocalDate tom_periode2 = fom_periode2.plusWeeks(3).minusDays(1);
        stønadskontoType = StønadskontoType.FORELDREPENGER_FØR_FØDSEL;
        resultatType = PeriodeResultatType.INNVILGET;
        periode = opprettUttakResultatPeriode(resultatType, fom_periode2, tom_periode2, stønadskontoType);
        perioder.leggTilPeriode(periode);

        uttakRepository.lagreOpprinneligUttakResultatPerioder(behandling, perioder);

        repoRule.getEntityManager().flush();

        Optional<LocalDate> førsteDag =
            new FørstePermisjonsdagTjeneste(repositoryProvider).henteFørstePermisjonsdag(behandling);

        assertThat(førsteDag.isPresent()).isTrue();
        assertThat(førsteDag.get()).isEqualTo(fom);
    }

    private Behandling opprettBehandling() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        scenario.medBehandlingVedtak()
            .medVedtaksdato(LocalDate.now().minusDays(7))
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medAnsvarligSaksbehandler("Nav Navsdotter")
            .build();
        return scenario.lagre(repositoryProvider);
    }

    private UttakResultatPeriodeEntitet opprettUttakResultatPeriode(PeriodeResultatType resultat,
                                                                    LocalDate fom,
                                                                    LocalDate tom,
                                                                    StønadskontoType stønadskontoType) {
        UttakAktivitetEntitet uttakAktivitet = new UttakAktivitetEntitet.Builder()
            .medArbeidsforhold(virksomhet, ArbeidsforholdRef.ref("arb_id"))
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();

        UttakResultatPeriodeEntitet uttakResultatPeriode = new UttakResultatPeriodeEntitet.Builder(fom, tom)
            .medPeriodeResultat(resultat, PeriodeResultatÅrsak.UKJENT)
            .build();

        UttakResultatPeriodeAktivitetEntitet periodeAktivitet = new UttakResultatPeriodeAktivitetEntitet.Builder(uttakResultatPeriode, uttakAktivitet)
            .medTrekkonto(stønadskontoType)
            .medArbeidsprosent(BigDecimal.TEN)
            .medTrekkdager(10)
            .build();

        uttakResultatPeriode.leggTilAktivitet(periodeAktivitet);

        return uttakResultatPeriode;
    }
}
