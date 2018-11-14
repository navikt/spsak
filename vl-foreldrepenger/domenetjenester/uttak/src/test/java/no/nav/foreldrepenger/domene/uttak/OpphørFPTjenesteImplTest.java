package no.nav.foreldrepenger.domene.uttak;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.RegisterInnhentingIntervallEndringTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapManuellVurderingType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskap;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskapBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.AvklarteUttakDatoerEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.uttak.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.InnvilgetÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.medlem.api.MedlemTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

@RunWith(Enclosed.class)
public class OpphørFPTjenesteImplTest {
    private static BehandlingRepositoryProvider repositoryProvider;
    private static PersonopplysningTjeneste personopplysningsTjeneste;
    private static MedlemTjeneste medlemTjeneste;
    private static SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private static OpphørFPTjeneste opphørTjeneste;

    private static void init(RepositoryRule repositoryRule) {
        repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
        personopplysningsTjeneste = mock(PersonopplysningTjeneste.class);
        medlemTjeneste = mock(MedlemTjeneste.class);
        skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider,
            new RegisterInnhentingIntervallEndringTjeneste(Period.of(1, 0, 0), Period.of(0, 4, 0)),
            Period.of(0, 3, 0),
            Period.of(0, 10, 0));
        opphørTjeneste = new OpphørFPTjenesteImpl(repositoryProvider, personopplysningsTjeneste, medlemTjeneste, skjæringstidspunktTjeneste);
    }

    private static Behandling opprettOriginalBehandling(BehandlingRepositoryProvider repositoryProvider, RepositoryRule repoRule) {
        Behandlingsresultat.Builder originalResultat = Behandlingsresultat.builderForInngangsvilkår()
            .medBehandlingResultatType(BehandlingResultatType.INNVILGET);

        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger
            .forFødsel()
            .medDefaultBekreftetTerminbekreftelse()
            .medVilkårResultatType(VilkårResultatType.INNVILGET);
        scenario.medBehandlingsresultat(originalResultat);
        scenario.medBehandlingVedtak()
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medVedtaksdato(LocalDate.now().minusDays(1))
            .medAnsvarligSaksbehandler("Saksbehandler")
            .build();
        scenario.buildAvsluttet(repositoryProvider.getBehandlingRepository(), repositoryProvider);

        repoRule.getRepository().flushAndClear();
        return repoRule.getEntityManager().find(Behandling.class, scenario.getBehandling().getId());
    }

    public static class Opphør_dato {
        @Rule
        public RepositoryRule repositoryRule = new UnittestRepositoryRule();
        private Behandling originalBehandling;
        private Behandling revurdering;

        public Opphør_dato() {
            init(repositoryRule);
        }

        @Before
        public void oppsett() {
            BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

            originalBehandling = opprettOriginalBehandling(repositoryProvider, repositoryRule);
            revurdering = Behandling.fraTidligereBehandling(originalBehandling, BehandlingType.REVURDERING)
                .medBehandlingÅrsak(BehandlingÅrsak.builder(BehandlingÅrsakType.RE_MANGLER_FØDSEL)
                    .medOriginalBehandling(originalBehandling)).build();

            Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.OPPHØR).buildFor(revurdering);
            BehandlingLås lås = behandlingRepository.taSkriveLås(revurdering);
            behandlingRepository.lagre(revurdering, lås);
        }

        @Test
        public void skal_kun_komme_når_behandlingsresultatet_er_av_typen_opphør() {
            lagreSkjæringstidspunkt(revurdering, LocalDate.now());

            Optional<LocalDate> kallPåOpphørTjenesteForOpphørBehandling = opphørTjeneste.getOpphørsdato(revurdering);
            assertThat(kallPåOpphørTjenesteForOpphørBehandling).isNotEmpty();

            Optional<LocalDate> kallPåOpphørTjenesteForInnvilgetBehandling = opphørTjeneste.getOpphørsdato(originalBehandling);
            assertThat(kallPåOpphørTjenesteForInnvilgetBehandling).isEmpty();
        }

        @Test
        public void skal_ikke_være_tidligere_enn_skjæringstidspunkt() {
            LocalDate endringIMedlemskapFom = LocalDate.now();
            lagreOpphørIMedlemskapDato(endringIMedlemskapFom);

            LocalDate skjæringstidspunkt = lagreSkjæringstidspunkt(revurdering, endringIMedlemskapFom.plusDays(3));
            Optional<LocalDate> opphørsdato = opphørTjeneste.getOpphørsdato(revurdering);
            assertThat(opphørsdato.get()).isEqualTo(skjæringstidspunkt);

            lagreSkjæringstidspunkt(revurdering, endringIMedlemskapFom.minusDays(3));
            opphørsdato = opphørTjeneste.getOpphørsdato(revurdering);
            assertThat(opphørsdato.get()).isEqualTo(endringIMedlemskapFom);
        }


        @Test
        public void skal_bruke_tidligste_dato_av_opphør_i_medlemskap_og_opphør_i_uttak() {
            // arrange
            LocalDate skjæringstidspunkt = lagreSkjæringstidspunkt(revurdering, LocalDate.now());
            LocalDate opphørMedlemskap = lagreOpphørIMedlemskapDato(skjæringstidspunkt.plusDays(7));
            LocalDate uttakFom = opphørMedlemskap.plusDays(7);
            LocalDate tom1 = uttakFom.plusMonths(1);
            LocalDate tom2 = tom1.plusMonths(1);
            UttakResultatPerioderEntitet uttakResultatPerioder = new UttakResultatPerioderEntitet();
            uttakResultatPerioder.leggTilPeriode(new UttakResultatPeriodeEntitet.Builder(uttakFom, tom1).medPeriodeResultat(PeriodeResultatType.INNVILGET, InnvilgetÅrsak.UTTAK_OPPFYLT).build());
            uttakResultatPerioder.leggTilPeriode(new UttakResultatPeriodeEntitet.Builder(tom1.plusDays(1), tom2).medPeriodeResultat(PeriodeResultatType.AVSLÅTT, IkkeOppfyltÅrsak.opphørsAvslagÅrsaker().iterator().next()).build());
            repositoryProvider.getUttakRepository().lagreOpprinneligUttakResultatPerioder(revurdering, uttakResultatPerioder);
            // act
            Optional<LocalDate> opphørsdato = opphørTjeneste.getOpphørsdato(revurdering);
            // assert
            assertThat(opphørsdato.get()).isEqualTo(opphørMedlemskap);
        }

        @Test
        public void skal_bruke_skjæringstidspunkt_hvis_alle_perioder_har_fått_opphør_eller_avslagsårsak() {
            // arrange
            LocalDate skjæringstidspunkt = lagreSkjæringstidspunkt(revurdering, LocalDate.now());
            LocalDate uttakFom = skjæringstidspunkt.plusDays(7);
            LocalDate tom1 = uttakFom.plusMonths(1);
            LocalDate tom2 = tom1.plusMonths(1);
            PeriodeResultatÅrsak opphørÅrsak = IkkeOppfyltÅrsak.opphørsAvslagÅrsaker().iterator().next();
            UttakResultatPerioderEntitet uttakResultatPerioder = new UttakResultatPerioderEntitet();
            uttakResultatPerioder.leggTilPeriode(new UttakResultatPeriodeEntitet.Builder(uttakFom, tom1).medPeriodeResultat(PeriodeResultatType.AVSLÅTT, IkkeOppfyltÅrsak.MOR_TAR_IKKE_ALLE_UKENE).build());
            uttakResultatPerioder.leggTilPeriode(new UttakResultatPeriodeEntitet.Builder(tom1.plusDays(1), tom2).medPeriodeResultat(PeriodeResultatType.AVSLÅTT, opphørÅrsak).build());
            repositoryProvider.getUttakRepository().lagreOpprinneligUttakResultatPerioder(revurdering, uttakResultatPerioder);
            // act
            Optional<LocalDate> opphørsdato = opphørTjeneste.getOpphørsdato(revurdering);
            // assert
            assertThat(opphørsdato.get()).isEqualTo(skjæringstidspunkt);
        }

        private LocalDate lagreOpphørIMedlemskapDato(LocalDate endringIMedlemskapFom) {
            VurdertMedlemskap vurdertMedlemskap = new VurdertMedlemskapBuilder()
                .medMedlemsperiodeManuellVurdering(MedlemskapManuellVurderingType.SAKSBEHANDLER_SETTER_OPPHØR_AV_MEDL_PGA_ENDRINGER_I_TPS)
                .medFom(endringIMedlemskapFom).build();
            repositoryProvider.getMedlemskapRepository().lagreMedlemskapVurdering(revurdering, vurdertMedlemskap);
            return endringIMedlemskapFom;
        }

        private LocalDate lagreSkjæringstidspunkt(Behandling behandling, LocalDate skjæringstidspunkt) {
            repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, new AvklarteUttakDatoerEntitet(skjæringstidspunkt, null));
            return skjæringstidspunkt;
        }
    }

    public static class Første_stønadsDato {
        @Rule
        public RepositoryRule repositoryRule = new UnittestRepositoryRule();
        private Behandling originalBehandling;
        private Behandling revurdering;

        public Første_stønadsDato() {
            init(repositoryRule);
        }

        @Before
        public void oppsett() {
            originalBehandling = opprettOriginalBehandling(repositoryProvider, repositoryRule);
            revurdering = Behandling.fraTidligereBehandling(originalBehandling, BehandlingType.REVURDERING)
                .medBehandlingÅrsak(BehandlingÅrsak.builder(BehandlingÅrsakType.RE_MANGLER_FØDSEL)
                    .medOriginalBehandling(originalBehandling)).build();
            Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.OPPHØR)
                .buildFor(revurdering);

            BehandlingLås lås = repositoryProvider.getBehandlingRepository().taSkriveLås(revurdering);
            repositoryProvider.getBehandlingRepository().lagre(revurdering, lås);
        }

        @Test
        public void skal_returnere_f_o_m_dato_i_tidligste_innvilgede_periode_fra_orginal_behandling() {
            LocalDate uttakFom = LocalDate.now();
            LocalDate tom1 = uttakFom.plusMonths(1);
            LocalDate tom2 = tom1.plusMonths(1);

            UttakResultatPerioderEntitet uttakResultatPerioder = new UttakResultatPerioderEntitet();
            uttakResultatPerioder.leggTilPeriode(new UttakResultatPeriodeEntitet.Builder(uttakFom, tom1).medPeriodeResultat(PeriodeResultatType.AVSLÅTT, IkkeOppfyltÅrsak.MOR_TAR_IKKE_ALLE_UKENE).build());
            uttakResultatPerioder.leggTilPeriode(new UttakResultatPeriodeEntitet.Builder(tom1.plusDays(1), tom2).medPeriodeResultat(PeriodeResultatType.INNVILGET, InnvilgetÅrsak.UTTAK_OPPFYLT).build());
            repositoryProvider.getUttakRepository().lagreOpprinneligUttakResultatPerioder(originalBehandling, uttakResultatPerioder);

            Optional<LocalDate> førsteStønadsDato = opphørTjeneste.getFørsteStønadsDato(revurdering);
            assertThat(førsteStønadsDato.get()).isEqualTo(tom1.plusDays(1));
        }

        @Test
        public void skal_returnere_empty_hvis_innvilgede_perioder_ikke_finnes() {
            LocalDate uttakFom = LocalDate.now();
            LocalDate tom1 = uttakFom.plusMonths(1);
            LocalDate tom2 = tom1.plusMonths(1);

            Optional<LocalDate> førsteStønadsDato = opphørTjeneste.getFørsteStønadsDato(revurdering);
            assertThat(førsteStønadsDato).isEmpty();

            UttakResultatPerioderEntitet uttakResultatPerioder = new UttakResultatPerioderEntitet();
            uttakResultatPerioder.leggTilPeriode(new UttakResultatPeriodeEntitet.Builder(uttakFom, tom1).medPeriodeResultat(PeriodeResultatType.AVSLÅTT, IkkeOppfyltÅrsak.MOR_TAR_IKKE_ALLE_UKENE).build());
            uttakResultatPerioder.leggTilPeriode(new UttakResultatPeriodeEntitet.Builder(tom1.plusDays(1), tom2).medPeriodeResultat(PeriodeResultatType.AVSLÅTT, IkkeOppfyltÅrsak.SØKER_ER_DØD).build());
            repositoryProvider.getUttakRepository().lagreOpprinneligUttakResultatPerioder(originalBehandling, uttakResultatPerioder);

            førsteStønadsDato = opphørTjeneste.getFørsteStønadsDato(revurdering);
            assertThat(førsteStønadsDato).isEmpty();
        }

    }
}
