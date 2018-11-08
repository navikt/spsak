package no.nav.foreldrepenger.jsonfeed;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtakRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatusEvent;
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
import no.nav.foreldrepenger.domene.feed.FeedRepository;
import no.nav.foreldrepenger.domene.feed.FeedRepositoryImpl;
import no.nav.foreldrepenger.domene.feed.VedtakUtgåendeHendelse;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.kontrakter.feed.vedtak.v1.ForeldrepengerEndret;
import no.nav.foreldrepenger.kontrakter.feed.vedtak.v1.ForeldrepengerInnvilget;
import no.nav.foreldrepenger.kontrakter.feed.vedtak.v1.ForeldrepengerOpphoert;
import no.nav.foreldrepenger.kontrakter.feed.vedtak.v1.Meldingstype;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.rest.JsonMapper;

public class HendelsePublisererTjenesteImplTest {

    private static final LocalDate INNVILGET_PERIODE_FØRSTE_DAG = LocalDate.now();
    private static final LocalDate INNVILGET_PERIODE_SISTE_DAG = LocalDate.now().plusMonths(3);
    private static final LocalDate AVSLÅTT_PERIODE_START = LocalDate.now().minusMonths(3);
    private static final LocalDate AVSLÅTT_PERIODE_SLUTT = LocalDate.now().minusDays(1);
    private static final LocalDate NY_PERIODE_FØRSTE_DAG = LocalDate.now().minusDays(3);
    private static final LocalDate NY_PERIODE_SISTE_DAG = LocalDate.now().plusMonths(4);

    private static final String FAGSAK_PREFIX = "FS";
    private static final String VEDTAK_PREFIX = "VT";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private FeedRepository feedRepository = new FeedRepositoryImpl(repoRule.getEntityManager());
    private BehandlingVedtakRepository vedtakRepo = new BehandlingVedtakRepositoryImpl(repoRule.getEntityManager());
    private UttakRepository uttakRepository = new UttakRepositoryImpl(repoRule.getEntityManager());
    private BehandlingRepository behandlingRepository = new BehandlingRepositoryImpl(repoRule.getEntityManager());
    private FagsakRepository fagsakRepository = new FagsakRepositoryImpl(repoRule.getEntityManager());

    private HendelsePublisererTjeneste tjeneste;
    private VirksomhetEntitet virksomhet;

    @Before
    public void setUp() {
        tjeneste = new HendelsePublisererTjenesteImpl(feedRepository, uttakRepository, fagsakRepository, behandlingRepository);
        virksomhet = new VirksomhetEntitet.Builder().medOrgnr("orgnr").oppdatertOpplysningerNå().build();
        repoRule.getRepository().lagre(virksomhet);
    }

    @Test
    public void skal_kaste_teknisk_feil_ved_manglende_uttaksplan() {
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FP-343184:Finner ikke noen relevant uttaksplan for vedtak");

        BehandlingVedtak vedtak = byggBehandlingVedtak(false, BehandlingType.FØRSTEGANGSSØKNAD, BehandlingResultatType.INNVILGET, null, VedtakResultatType.INNVILGET);
        tjeneste.lagreVedtak(vedtak);
    }

    @Test
    public void skal_sett_første_og_siste_stønadsdag_lik_fom_på_første_periode_hvis_ingen_perioder_er_innvilget(){
        UttakResultatPerioderEntitet uttaksPlan = lagUttaksPlan(AVSLÅTT_PERIODE_START, AVSLÅTT_PERIODE_SLUTT, true, false);
        BehandlingVedtak vedtak = byggBehandlingVedtak(true, BehandlingType.REVURDERING, BehandlingResultatType.OPPHØR, uttaksPlan, VedtakResultatType.AVSLAG);
        tjeneste.lagreVedtak(vedtak);

        List<VedtakUtgåendeHendelse> alle = feedRepository.hentAlle(VedtakUtgåendeHendelse.class);
        assertThat(alle).hasSize(1);
        assertThat(alle.get(0).getType()).isEqualTo(Meldingstype.FORELDREPENGER_OPPHOERT.getType());
        assertThat(alle.get(0).getKildeId()).isEqualTo(VEDTAK_PREFIX + vedtak.getId().toString());
        ForeldrepengerOpphoert opphørt = JsonMapper.fromJson(alle.get(0).getPayload(), ForeldrepengerOpphoert.class);
        assertThat(opphørt.getAktoerId()).isEqualTo(alle.get(0).getAktørId()).isNotNull();
        assertThat(opphørt.getFoersteStoenadsdag()).isEqualTo(AVSLÅTT_PERIODE_START);
        assertThat(opphørt.getSisteStoenadsdag()).isEqualTo(AVSLÅTT_PERIODE_START);
        assertThat(opphørt.getGsakId()).isEqualTo(vedtak.getBehandlingsresultat().getBehandling().getFagsak().getSaksnummer().getVerdi());
    }

    @Test
    public void skal_lagre_ned_førstegangssøknad() {
        BehandlingVedtak vedtak = byggBehandlingVedtak(true, BehandlingType.FØRSTEGANGSSØKNAD, BehandlingResultatType.INNVILGET, null, VedtakResultatType.INNVILGET);
        tjeneste.lagreVedtak(vedtak);

        List<VedtakUtgåendeHendelse> alle = feedRepository.hentAlle(VedtakUtgåendeHendelse.class);
        assertThat(alle).hasSize(1);
        assertThat(alle.get(0).getType()).isEqualTo(Meldingstype.FORELDREPENGER_INNVILGET.getType());
        assertThat(alle.get(0).getKildeId()).isEqualTo(VEDTAK_PREFIX + vedtak.getId().toString());
        ForeldrepengerInnvilget innvilget = JsonMapper.fromJson(alle.get(0).getPayload(), ForeldrepengerInnvilget.class);
        assertThat(innvilget.getAktoerId()).isEqualTo(alle.get(0).getAktørId()).isNotNull();
        assertThat(innvilget.getFoersteStoenadsdag()).isEqualTo(INNVILGET_PERIODE_FØRSTE_DAG);
        assertThat(innvilget.getSisteStoenadsdag()).isEqualTo(INNVILGET_PERIODE_SISTE_DAG);
        assertThat(innvilget.getGsakId()).isEqualTo(vedtak.getBehandlingsresultat().getBehandling().getFagsak().getSaksnummer().getVerdi());
    }

    @Test
    public void skal_ikke_lagre_ned_vedtak_som_ikke_endrer_stønadsperiode() {
        BehandlingVedtak vedtak = byggBehandlingVedtak(true, BehandlingType.REVURDERING, BehandlingResultatType.INNVILGET, opprettUttakResultatPerioder(), VedtakResultatType.INNVILGET);
        tjeneste.lagreVedtak(vedtak);

        List<VedtakUtgåendeHendelse> alle = feedRepository.hentAlle(VedtakUtgåendeHendelse.class);
        assertThat(alle).hasSize(0);
    }

    @Test
    public void skal_lagre_ned_revurdering_innvilget() {
        BehandlingVedtak vedtak = byggBehandlingVedtak(true, BehandlingType.REVURDERING, BehandlingResultatType.INNVILGET, opprettNyUttaksplan(), VedtakResultatType.INNVILGET);
        tjeneste.lagreVedtak(vedtak);

        List<VedtakUtgåendeHendelse> alle = feedRepository.hentAlle(VedtakUtgåendeHendelse.class);
        assertThat(alle).hasSize(1);
        assertThat(alle.get(0).getType()).isEqualTo(Meldingstype.FORELDREPENGER_INNVILGET.getType());
        assertThat(alle.get(0).getKildeId()).isEqualTo(VEDTAK_PREFIX + vedtak.getId().toString());
        ForeldrepengerInnvilget innvilget = JsonMapper.fromJson(alle.get(0).getPayload(), ForeldrepengerInnvilget.class);
        assertThat(innvilget.getAktoerId()).isEqualTo(alle.get(0).getAktørId()).isNotNull();
        assertThat(innvilget.getFoersteStoenadsdag()).isEqualTo(NY_PERIODE_FØRSTE_DAG);
        assertThat(innvilget.getSisteStoenadsdag()).isEqualTo(NY_PERIODE_SISTE_DAG);
        assertThat(innvilget.getGsakId()).isEqualTo(vedtak.getBehandlingsresultat().getBehandling().getFagsak().getSaksnummer().getVerdi());
    }

    @Test
    public void skal_lagre_ned_revurdering_endret() {
        BehandlingVedtak vedtak = byggBehandlingVedtak(true, BehandlingType.REVURDERING, BehandlingResultatType.FORELDREPENGER_ENDRET, opprettNyUttaksplan(), VedtakResultatType.INNVILGET);
        tjeneste.lagreVedtak(vedtak);

        List<VedtakUtgåendeHendelse> alle = feedRepository.hentAlle(VedtakUtgåendeHendelse.class);
        assertThat(alle).hasSize(1);
        assertThat(alle.get(0).getType()).isEqualTo(Meldingstype.FORELDREPENGER_ENDRET.getType());
        assertThat(alle.get(0).getKildeId()).isEqualTo(VEDTAK_PREFIX + vedtak.getId().toString());
        ForeldrepengerEndret endret = JsonMapper.fromJson(alle.get(0).getPayload(), ForeldrepengerEndret.class);
        assertThat(endret.getAktoerId()).isEqualTo(alle.get(0).getAktørId()).isNotNull();
        assertThat(endret.getFoersteStoenadsdag()).isEqualTo(NY_PERIODE_FØRSTE_DAG);
        assertThat(endret.getSisteStoenadsdag()).isEqualTo(NY_PERIODE_SISTE_DAG);
        assertThat(endret.getGsakId()).isEqualTo(vedtak.getBehandlingsresultat().getBehandling().getFagsak().getSaksnummer().getVerdi());
    }

    @Test
    public void skal_lagre_ned_revurdering_opphørt() {
        BehandlingVedtak vedtak = byggBehandlingVedtak(true, BehandlingType.REVURDERING, BehandlingResultatType.OPPHØR, opprettNyUttaksplan(), VedtakResultatType.AVSLAG);
        tjeneste.lagreVedtak(vedtak);

        List<VedtakUtgåendeHendelse> alle = feedRepository.hentAlle(VedtakUtgåendeHendelse.class);
        assertThat(alle).hasSize(1);
        assertThat(alle.get(0).getType()).isEqualTo(Meldingstype.FORELDREPENGER_OPPHOERT.getType());
        assertThat(alle.get(0).getKildeId()).isEqualTo(VEDTAK_PREFIX + vedtak.getId().toString());
        ForeldrepengerOpphoert opphørt = JsonMapper.fromJson(alle.get(0).getPayload(), ForeldrepengerOpphoert.class);
        assertThat(opphørt.getAktoerId()).isEqualTo(alle.get(0).getAktørId()).isNotNull();
        assertThat(opphørt.getFoersteStoenadsdag()).isEqualTo(NY_PERIODE_FØRSTE_DAG);
        assertThat(opphørt.getSisteStoenadsdag()).isEqualTo(NY_PERIODE_SISTE_DAG);
        assertThat(opphørt.getGsakId()).isEqualTo(vedtak.getBehandlingsresultat().getBehandling().getFagsak().getSaksnummer().getVerdi());
    }

    @Test
    public void skal_ikkje_lagre_ned_beslutningsvedtak() {
        BehandlingVedtak vedtak = byggBehandlingVedtak(true, BehandlingType.REVURDERING, BehandlingResultatType.INGEN_ENDRING, opprettUttakResultatPerioder(), VedtakResultatType.INNVILGET);
        tjeneste.lagreVedtak(vedtak);

        List<VedtakUtgåendeHendelse> alle = feedRepository.hentAlle(VedtakUtgåendeHendelse.class);
        assertThat(alle).hasSize(0);
    }

    @Test
    public void skal_lagre_fagsak_avsluttet() {
        BehandlingVedtak vedtak = byggBehandlingVedtak(true, BehandlingType.FØRSTEGANGSSØKNAD, BehandlingResultatType.INNVILGET, null, VedtakResultatType.INNVILGET);
        AktørId aktørId = vedtak.getBehandlingsresultat().getBehandling().getAktørId();
        Long fagsakId = vedtak.getBehandlingsresultat().getBehandling().getFagsakId();
        FagsakStatusEvent event = new FagsakStatusEvent(fagsakId, aktørId, FagsakStatus.LØPENDE, FagsakStatus.AVSLUTTET);
        tjeneste.lagreFagsakAvsluttet(event);

        List<VedtakUtgåendeHendelse> alle = feedRepository.hentAlle(VedtakUtgåendeHendelse.class);
        assertThat(alle).hasSize(1);
        assertThat(alle.get(0).getType()).isEqualTo(Meldingstype.FORELDREPENGER_OPPHOERT.getType());
        assertThat(alle.get(0).getKildeId()).isEqualTo(FAGSAK_PREFIX + fagsakId.toString());
        ForeldrepengerOpphoert opphørt = JsonMapper.fromJson(alle.get(0).getPayload(), ForeldrepengerOpphoert.class);
        assertThat(opphørt.getAktoerId()).isEqualTo(aktørId.getId()).isNotNull();
        assertThat(opphørt.getFoersteStoenadsdag()).isEqualTo(INNVILGET_PERIODE_FØRSTE_DAG);
        assertThat(opphørt.getSisteStoenadsdag()).isEqualTo(INNVILGET_PERIODE_SISTE_DAG);
        assertThat(opphørt.getGsakId()).isEqualTo(vedtak.getBehandlingsresultat().getBehandling().getFagsak().getSaksnummer().getVerdi());
    }

    private BehandlingVedtak byggBehandlingVedtak(boolean medUttaksPlan, BehandlingType behandlingType, BehandlingResultatType behandlingResultatType,
                                                  UttakResultatPerioderEntitet nyUttaksPlan, VedtakResultatType nyttVedtakResultat) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        scenario.medBehandlingType(behandlingType);
        final FamilieHendelseBuilder familieHendelseBuilder = scenario.medSøknadHendelse();
        familieHendelseBuilder.medAntallBarn(1)
            .medFødselsDato(LocalDate.now());
        Behandling behandling = scenario.lagre(repositoryProvider);
        Behandlingsresultat.Builder behandlingresultatBuilder = Behandlingsresultat.builder();
        behandlingresultatBuilder.medBehandlingResultatType(nyUttaksPlan == null ? behandlingResultatType : BehandlingResultatType.INNVILGET);
        Behandlingsresultat behandingsresultat = behandlingresultatBuilder.buildFor(behandling);
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));
        BehandlingVedtak.Builder vedtakBuilder = scenario.medBehandlingVedtak();
        vedtakBuilder.medBehandlingsresultat(behandingsresultat)
            .medVedtaksdato(INNVILGET_PERIODE_FØRSTE_DAG.minusDays(7))
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medAnsvarligSaksbehandler("Nav Navsdotter")
            .build();
        vedtakRepo.lagre(vedtakBuilder.build(), behandlingRepository.taSkriveLås(behandling));
        if (medUttaksPlan) {
            uttakRepository.lagreOpprinneligUttakResultatPerioder(behandling, opprettUttakResultatPerioder());
        }
        Long behandlingId = behandling.getId();
        if (nyUttaksPlan != null) {
            Behandling nyBehandling = Behandling.fraTidligereBehandling(behandling, BehandlingType.REVURDERING)
                .medBehandlingÅrsak(BehandlingÅrsak.builder(BehandlingÅrsakType.RE_HENDELSE_FØDSEL).medOriginalBehandling(behandling)).build();
            Behandlingsresultat.Builder nyttBehandlingresultatBuilder = Behandlingsresultat.builder();
            nyttBehandlingresultatBuilder.medBehandlingResultatType(behandlingResultatType);
            Behandlingsresultat nyttBehandingsresultat = nyttBehandlingresultatBuilder.buildFor(nyBehandling);
            behandlingRepository.lagre(nyBehandling, behandlingRepository.taSkriveLås(nyBehandling));
            BehandlingVedtak.Builder nyttVedtakBuilder = scenario.medBehandlingVedtak();
            nyttVedtakBuilder.medBehandlingsresultat(nyttBehandingsresultat)
                .medVedtaksdato(INNVILGET_PERIODE_FØRSTE_DAG.minusDays(7))
                .medVedtakResultatType(nyttVedtakResultat)
                .medAnsvarligSaksbehandler("Nav Navsdotter")
                .build();
            vedtakRepo.lagre(nyttVedtakBuilder.build(), behandlingRepository.taSkriveLås(nyBehandling));
            behandlingId = nyBehandling.getId();
            uttakRepository.lagreOpprinneligUttakResultatPerioder(nyBehandling, nyUttaksPlan);
        }
        repoRule.getEntityManager().flush();

        Optional<BehandlingVedtak> hentBehandlingvedtakForBehandlingId = vedtakRepo.hentBehandlingvedtakForBehandlingId(behandlingId);
        return hentBehandlingvedtakForBehandlingId.orElse(null);
    }

    private UttakResultatPerioderEntitet opprettUttakResultatPerioder() {
        return lagUttaksPlan(INNVILGET_PERIODE_FØRSTE_DAG, INNVILGET_PERIODE_SISTE_DAG, true, true);
    }

    private UttakResultatPerioderEntitet opprettNyUttaksplan() {
        return lagUttaksPlan(NY_PERIODE_FØRSTE_DAG, NY_PERIODE_SISTE_DAG, false, true);
    }

    private UttakResultatPerioderEntitet lagUttaksPlan(LocalDate fom, LocalDate tom, boolean medAvslått, boolean medInnvilget) {
        UttakResultatPerioderEntitet perioder = new UttakResultatPerioderEntitet();
        if(medInnvilget) {
            UttakResultatPeriodeEntitet innvilgetPeriode1 = opprettUttakResultatPeriode(PeriodeResultatType.INNVILGET, fom, fom.plusWeeks(6));
            UttakResultatPeriodeEntitet innvilgetPeriode2 = opprettUttakResultatPeriode(PeriodeResultatType.INNVILGET, tom.minusWeeks(2), tom);

            perioder.leggTilPeriode(innvilgetPeriode1);
            perioder.leggTilPeriode(innvilgetPeriode2);
        }
        if (medAvslått) {
            UttakResultatPeriodeEntitet avslåttPeriode = opprettUttakResultatPeriode(PeriodeResultatType.AVSLÅTT, AVSLÅTT_PERIODE_START, AVSLÅTT_PERIODE_SLUTT);
            perioder.leggTilPeriode(avslåttPeriode);
        }
        return perioder;
    }

    private UttakResultatPeriodeEntitet opprettUttakResultatPeriode(PeriodeResultatType resultat,
                                                                    LocalDate fom,
                                                                    LocalDate tom) {

        UttakResultatPeriodeEntitet uttakResultatPeriode = new UttakResultatPeriodeEntitet.Builder(fom, tom)
            .medPeriodeResultat(resultat, PeriodeResultatÅrsak.UKJENT)
            .build();

        UttakAktivitetEntitet uttakAktivitet = new UttakAktivitetEntitet.Builder()
            .medArbeidsforhold(virksomhet, ArbeidsforholdRef.ref("arb_id"))
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();

        UttakResultatPeriodeAktivitetEntitet periodeAktivitet = UttakResultatPeriodeAktivitetEntitet.builder(uttakResultatPeriode,
            uttakAktivitet)
            .medTrekkonto(StønadskontoType.FELLESPERIODE)
            .medTrekkdager(10)
            .medArbeidsprosent(BigDecimal.ZERO)
            .medUtbetalingsprosent(BigDecimal.valueOf(100))
            .build();

        uttakResultatPeriode.leggTilAktivitet(periodeAktivitet);

        return uttakResultatPeriode;
    }
}
