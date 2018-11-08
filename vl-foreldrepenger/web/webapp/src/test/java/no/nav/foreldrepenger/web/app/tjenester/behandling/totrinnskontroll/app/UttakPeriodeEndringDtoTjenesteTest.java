package no.nav.foreldrepenger.web.app.tjenester.behandling.totrinnskontroll.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.FordelingPeriodeKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordeling;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatDokRegelEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeSøknadEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.UttakPeriodeEndringDto;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;

@RunWith(CdiRunner.class)
public class UttakPeriodeEndringDtoTjenesteTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private final Repository repository = repoRule.getRepository();

    @Inject
    private UttakPeriodeEndringDtoTjeneste uttakPeriodeEndringDtoTjeneste;

    private Behandling behandling;
    private LocalDate dato;
    private YtelsesFordelingRepository ytelsesFordelingRepository;
    private UttakRepository uttakRepository;
    private VirksomhetEntitet virksomhet;


    @Before
    public void setUp() {
        ytelsesFordelingRepository = repositoryProvider.getYtelsesFordelingRepository();
        uttakRepository = repositoryProvider.getUttakRepository();
        dato = LocalDate.of(2018, 8, 1);
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        behandling = scenario.lagre(repositoryProvider);
        repository.lagre(behandling.getBehandlingsresultat()); // Hvorfor er dette nødvendig?
        virksomhet = new VirksomhetEntitet.Builder().medOrgnr("000000000").oppdatertOpplysningerNå().build();
        repoRule.getRepository().lagre(virksomhet);
    }

    @Test
    public void hent_endring_på_uttak_perioder_med_aksjonspunkt_avklar_fakta_uttak_finn_en_endret_periode_en_avklart_periode_og_en_slettet_periode() {

        // Legg til 3 gamle perioder
        OppgittPeriode gammelPeriode1 = OppgittPeriodeBuilder.ny()
            .medPeriode(dato.minusMonths(3), dato.minusMonths(2))
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medErArbeidstaker(false)
            .build();
        OppgittPeriode gammelPeriode2 = OppgittPeriodeBuilder.ny()
            .medPeriode(dato.minusMonths(2).plusDays(1), dato.minusMonths(1).plusDays(1))
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medErArbeidstaker(false)
            .build();
        OppgittPeriode gammelPeriode3 = OppgittPeriodeBuilder.ny()
            .medPeriode(dato.minusMonths(1).plusDays(2), dato.plusDays(2))
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medErArbeidstaker(false)
            .build();
        ArrayList<OppgittPeriode> gamlePerioder = new ArrayList<>();
        gamlePerioder.add(gammelPeriode1);
        gamlePerioder.add(gammelPeriode2);
        gamlePerioder.add(gammelPeriode3);
        OppgittFordeling gammelFordeling = new OppgittFordelingEntitet(gamlePerioder, true);
        ytelsesFordelingRepository.lagre(behandling, gammelFordeling);

        // Legg til 2 ny periode
        OppgittPeriode nyPeriode1 = OppgittPeriodeBuilder.ny()
            .medPeriode(dato.minusMonths(2).plusWeeks(1), dato.minusMonths(1).plusWeeks(1))
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medErArbeidstaker(false)
            .build();
        OppgittPeriode nyPeriode2 = OppgittPeriodeBuilder.ny()
            .medPeriode(dato.minusMonths(1).plusWeeks(1).plusDays(1), dato.plusWeeks(1).plusDays(1))
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medErArbeidstaker(false)
            .medBegrunnelse("Dette er en kort begrunnelse for hvorfor denne ble avklart.")
            .build();
        ArrayList<OppgittPeriode> nyePerioder = new ArrayList<>();
        nyePerioder.add(nyPeriode1);
        nyePerioder.add(nyPeriode2);
        OppgittFordeling nyFordeling = new OppgittFordelingEntitet(nyePerioder, true);
        ytelsesFordelingRepository.lagreOverstyrtFordeling(behandling, nyFordeling);

        // Legg til data i totrinnsvurdering.
        Totrinnsvurdering.Builder ttvurderingBuilder = new Totrinnsvurdering.Builder(behandling, AksjonspunktDefinisjon.AVKLAR_FAKTA_UTTAK);
        Totrinnsvurdering ttvurdering = ttvurderingBuilder.medGodkjent(false).medBegrunnelse("").build();

        // Hent endring på perioder
        List<UttakPeriodeEndringDto> uttakPeriodeEndringer = uttakPeriodeEndringDtoTjeneste.hentEndringPåUttakPerioder(ttvurdering, behandling, Optional.empty());

        assertThat(uttakPeriodeEndringer.size()).isEqualTo(3);

        // assert på første av 3 endringer
        assertThat(uttakPeriodeEndringer.get(0).getFom()).isEqualTo(LocalDate.of(2018,6,8));
        assertThat(uttakPeriodeEndringer.get(0).getTom()).isEqualTo(LocalDate.of(2018,7,8));
        assertThat(uttakPeriodeEndringer.get(0).getErEndret()).isTrue();

        // assert på andre av 3 endringer
        assertThat(uttakPeriodeEndringer.get(1).getFom()).isEqualTo(LocalDate.of(2018,7,3));
        assertThat(uttakPeriodeEndringer.get(1).getTom()).isEqualTo(LocalDate.of(2018,8,3));
        assertThat(uttakPeriodeEndringer.get(1).getErSlettet()).isTrue();

        // assert på tredje av 3 endringer
        assertThat(uttakPeriodeEndringer.get(2).getFom()).isEqualTo(LocalDate.of(2018,7,9));
        assertThat(uttakPeriodeEndringer.get(2).getTom()).isEqualTo(LocalDate.of(2018,8,9));
        assertThat(uttakPeriodeEndringer.get(2).getErAvklart()).isTrue();

    }

    @Test
    public void hent_endring_på_uttak_perioder_med_aksjonspunkt_avklar_fakta_uttak_finn_en_endret_periode_en_lagt_til_periode_og_filtrer_ut_en_periode() {

        // Legg til 1 gammel perioder
        OppgittPeriode gammelPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(dato.minusMonths(2), dato.minusMonths(1))
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medErArbeidstaker(false)
            .build();
        OppgittFordeling gammelFordeling = new OppgittFordelingEntitet(Collections.singletonList(gammelPeriode), true);
        ytelsesFordelingRepository.lagre(behandling, gammelFordeling);

        // Legg til 2 ny periode
        OppgittPeriode nyPeriode1 = OppgittPeriodeBuilder.ny()
            .medPeriode(dato.minusMonths(3).plusDays(3), dato.minusMonths(2).plusDays(3))
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medErArbeidstaker(false)
            .build();
        OppgittPeriode nyPeriode2 = OppgittPeriodeBuilder.ny()
            .medPeriode(dato.minusMonths(2).plusDays(4), dato.minusMonths(1).plusDays(4))
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medErArbeidstaker(false)
            .medBegrunnelse("Dette er en kort begrunnelse for hvorfor denne ble avklart.")
            .build();
        OppgittPeriode nyPeriode3 = OppgittPeriodeBuilder.ny()
            .medPeriode(dato.minusMonths(1).plusDays(5), dato.plusDays(5))
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medErArbeidstaker(false)
            .medPeriodeKilde(FordelingPeriodeKilde.TIDLIGERE_VEDTAK) // Denne burde bli ignorert pga. kilde
            .build();
        ArrayList<OppgittPeriode> nyePerioder = new ArrayList<>();
        nyePerioder.add(nyPeriode1);
        nyePerioder.add(nyPeriode2);
        nyePerioder.add(nyPeriode3);
        OppgittFordeling nyFordeling = new OppgittFordelingEntitet(nyePerioder, true);
        ytelsesFordelingRepository.lagreOverstyrtFordeling(behandling, nyFordeling);

        // Legg til data i totrinnsvurdering.
        Totrinnsvurdering.Builder ttvurderingBuilder = new Totrinnsvurdering.Builder(behandling, AksjonspunktDefinisjon.AVKLAR_FAKTA_UTTAK);
        Totrinnsvurdering ttvurdering = ttvurderingBuilder.medGodkjent(false).medBegrunnelse("").build();

        // Hent endring på perioder
        List<UttakPeriodeEndringDto> uttakPeriodeEndringer = uttakPeriodeEndringDtoTjeneste.hentEndringPåUttakPerioder(ttvurdering, behandling, Optional.empty());

        assertThat(uttakPeriodeEndringer.size()).isEqualTo(3);

        // assert på første av 2 endringer
        assertThat(uttakPeriodeEndringer.get(0).getFom()).isEqualTo(LocalDate.of(2018,5,4));
        assertThat(uttakPeriodeEndringer.get(0).getTom()).isEqualTo(LocalDate.of(2018,6,4));
        assertThat(uttakPeriodeEndringer.get(0).getErEndret()).isTrue();

        // assert på andre av 2 endringer
        assertThat(uttakPeriodeEndringer.get(1).getFom()).isEqualTo(LocalDate.of(2018,6,5));
        assertThat(uttakPeriodeEndringer.get(1).getTom()).isEqualTo(LocalDate.of(2018,7,5));
        assertThat(uttakPeriodeEndringer.get(1).getErLagtTil()).isTrue();

    }

    @Test
    public void hent_endring_på_uttak_perioder_med_aksjonspunkt_fastsett_uttakperioder_finn_endret_utakk_resultat_periode() {

        // Legg til opprinnelig periode
        UttakResultatPeriodeEntitet opprinneligPeriode = opprettUttakResultatPeriode(PeriodeResultatType.IKKE_FASTSATT, dato, dato.plusMonths(1),
            StønadskontoType.FORELDREPENGER, new BigDecimal("100"), BigDecimal.valueOf(100));
        UttakResultatPerioderEntitet opprinneligFordeling = new UttakResultatPerioderEntitet();
        opprinneligFordeling.leggTilPeriode(opprinneligPeriode);
        uttakRepository.lagreOpprinneligUttakResultatPerioder(behandling, opprinneligFordeling);

        // Legg til overstyrende periode
        UttakResultatPeriodeEntitet overstyrendePeriode = opprettUttakResultatPeriode(PeriodeResultatType.INNVILGET, dato, dato.plusMonths(1),
            StønadskontoType.FORELDREPENGER, new BigDecimal("100"), BigDecimal.valueOf(100));
        UttakResultatPerioderEntitet overstyrendeFordeling = new UttakResultatPerioderEntitet();
        overstyrendeFordeling.leggTilPeriode(overstyrendePeriode);
        uttakRepository.lagreOverstyrtUttakResultatPerioder(behandling, overstyrendeFordeling);

        // Legg til data i totrinnsvurdering.
        Totrinnsvurdering.Builder ttvurderingBuilder = new Totrinnsvurdering.Builder(behandling, AksjonspunktDefinisjon.FASTSETT_UTTAKPERIODER);
        Totrinnsvurdering ttvurdering = ttvurderingBuilder.medGodkjent(false).medBegrunnelse("").build();

        // Hent endring på perioder
        List<UttakPeriodeEndringDto> uttakPeriodeEndringer = uttakPeriodeEndringDtoTjeneste.hentEndringPåUttakPerioder(ttvurdering, behandling, Optional.empty());

        assertThat(uttakPeriodeEndringer.size()).isEqualTo(1);
        assertThat(uttakPeriodeEndringer.get(0).getErEndret()).isTrue();
        assertThat(uttakPeriodeEndringer.get(0).getFom()).isEqualTo(LocalDate.of(2018,8,1));
        assertThat(uttakPeriodeEndringer.get(0).getTom()).isEqualTo(LocalDate.of(2018,9,1));

    }

    @Test
    public void hent_endring_på_uttak_perioder_med_aksjonspunkt_overstyring_av_uttakperioder_finn_lagt_til_utakk_resultat_periode() {

        // Legg til opprinnelig periode
        UttakResultatPeriodeEntitet opprinneligPeriode = opprettUttakResultatPeriode(PeriodeResultatType.IKKE_FASTSATT, dato, dato.plusMonths(1),
            StønadskontoType.FORELDREPENGER, new BigDecimal("100"), BigDecimal.valueOf(100));
        UttakResultatPerioderEntitet opprinneligFordeling = new UttakResultatPerioderEntitet();
        opprinneligFordeling.leggTilPeriode(opprinneligPeriode);
        uttakRepository.lagreOpprinneligUttakResultatPerioder(behandling, opprinneligFordeling);

        // Legg til overstyrende periode
        UttakResultatPeriodeEntitet overstyrendePeriode = opprettUttakResultatPeriode(PeriodeResultatType.INNVILGET, dato.plusWeeks(2),
            dato.plusMonths(1).plusWeeks(2), StønadskontoType.FORELDREPENGER, new BigDecimal("100"), BigDecimal.valueOf(100));
        UttakResultatPerioderEntitet overstyrendeFordeling = new UttakResultatPerioderEntitet();
        overstyrendeFordeling.leggTilPeriode(overstyrendePeriode);
        uttakRepository.lagreOverstyrtUttakResultatPerioder(behandling, overstyrendeFordeling);

        // Legg til data i totrinnsvurdering.
        Totrinnsvurdering.Builder ttvurderingBuilder = new Totrinnsvurdering.Builder(behandling, AksjonspunktDefinisjon.OVERSTYRING_AV_UTTAKPERIODER);
        Totrinnsvurdering ttvurdering = ttvurderingBuilder.medGodkjent(false).medBegrunnelse("").build();

        // Hent endring på perioder
        List<UttakPeriodeEndringDto> uttakPeriodeEndringer = uttakPeriodeEndringDtoTjeneste.hentEndringPåUttakPerioder(ttvurdering, behandling, Optional.empty());

        assertThat(uttakPeriodeEndringer.size()).isEqualTo(1);
        assertThat(uttakPeriodeEndringer.get(0).getErLagtTil()).isTrue();
        assertThat(uttakPeriodeEndringer.get(0).getFom()).isEqualTo(LocalDate.of(2018,8,15));
        assertThat(uttakPeriodeEndringer.get(0).getTom()).isEqualTo(LocalDate.of(2018,9,15));

    }

    @Test
    public void hent_endring_på_uttak_perioder_med_aksjonspunkt_tilknyttet_stortinget_finn_slettet_utakk_resultat_periode() {

        // Legg til opprinnelig periode
        UttakResultatPeriodeEntitet opprinneligPeriode = opprettUttakResultatPeriode(PeriodeResultatType.IKKE_FASTSATT, dato, dato.plusMonths(1),
            StønadskontoType.FORELDREPENGER, new BigDecimal("100"), BigDecimal.valueOf(100));
        UttakResultatPerioderEntitet opprinneligFordeling = new UttakResultatPerioderEntitet();
        opprinneligFordeling.leggTilPeriode(opprinneligPeriode);
        uttakRepository.lagreOpprinneligUttakResultatPerioder(behandling, opprinneligFordeling);

        // Legg til overstyrende periode
        UttakResultatPeriodeEntitet overstyrendePeriode = opprettUttakResultatPeriode(PeriodeResultatType.INNVILGET, dato, dato.plusMonths(1).minusDays(1),
            StønadskontoType.FORELDREPENGER, new BigDecimal("100"), BigDecimal.valueOf(100));
        UttakResultatPerioderEntitet overstyrendeFordeling = new UttakResultatPerioderEntitet();
        overstyrendeFordeling.leggTilPeriode(overstyrendePeriode);
        uttakRepository.lagreOverstyrtUttakResultatPerioder(behandling, overstyrendeFordeling);

        // Legg til data i totrinnsvurdering.
        Totrinnsvurdering.Builder ttvurderingBuilder = new Totrinnsvurdering.Builder(behandling, AksjonspunktDefinisjon.TILKNYTTET_STORTINGET);
        Totrinnsvurdering ttvurdering = ttvurderingBuilder.medGodkjent(false).medBegrunnelse("").build();

        // Hent endring på perioder
        List<UttakPeriodeEndringDto> uttakPeriodeEndringer = uttakPeriodeEndringDtoTjeneste.hentEndringPåUttakPerioder(ttvurdering, behandling, Optional.empty());

        assertThat(uttakPeriodeEndringer.size()).isEqualTo(2);
        assertThat(uttakPeriodeEndringer.get(0).getFom()).isEqualTo(uttakPeriodeEndringer.get(1).getFom());
        assertThat(uttakPeriodeEndringer.get(1).getTom()).isBefore(uttakPeriodeEndringer.get(0).getTom());

        assertThat(uttakPeriodeEndringer.get(0).getErSlettet()).isTrue();
        assertThat(uttakPeriodeEndringer.get(0).getFom()).isEqualTo(LocalDate.of(2018,8,1));
        assertThat(uttakPeriodeEndringer.get(0).getTom()).isEqualTo(LocalDate.of(2018,9,1));

        assertThat(uttakPeriodeEndringer.get(1).getErLagtTil()).isTrue();
        assertThat(uttakPeriodeEndringer.get(1).getFom()).isEqualTo(LocalDate.of(2018,8,1));
        assertThat(uttakPeriodeEndringer.get(1).getTom()).isEqualTo(LocalDate.of(2018,8,31));

    }

    private UttakResultatPeriodeEntitet opprettUttakResultatPeriode(PeriodeResultatType resultat, LocalDate fom, LocalDate tom, StønadskontoType stønadskontoType, BigDecimal graderingArbeidsprosent, BigDecimal utbetalingsprosent) {
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
        return uttakResultatPeriode;
    }


}
