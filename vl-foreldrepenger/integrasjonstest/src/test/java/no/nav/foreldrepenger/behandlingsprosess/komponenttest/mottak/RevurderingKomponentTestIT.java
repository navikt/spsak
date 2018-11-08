package no.nav.foreldrepenger.behandlingsprosess.komponenttest.mottak;

import static java.util.Arrays.asList;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.JournalpostMottakDtoBuilder.journalpostEndringssøknadBuilder;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingModell;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellRepository;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellRepositoryImpl;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.ForeldreType;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
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
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.RegisterKontekst;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.søknad.SøknadTestdataBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.JournalpostMottakDtoBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.ArbeidsforholdTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.TpsTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsPerson;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.apiwrapper.FordelRestTjenesteTestAPI;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.IAYRegisterInnhentingTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.testutilities.Whitebox;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;

@RunWith(CdiRunner.class)
public class RevurderingKomponentTestIT {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();

    @Inject
    private BehandlingRepositoryProvider repositoryProvider;

    @Inject
    private BehandlingRepository behandlingRepo;

    @Inject
    @FagsakYtelseTypeRef("FP")
    private IAYRegisterInnhentingTjeneste iayRegisterInnhentingTjeneste;

    private YtelsesFordelingRepository ytelsesFordelingRepository;

    private UttakRepository uttakRepository;

    @Inject
    private BehandlingModellRepository behandlingModellRepository;

    @Inject
    private RegisterKontekst registerKontekst;
    // Test-API-er rundt REST-tjenestene
    @Inject
    private FordelRestTjenesteTestAPI fordelRestTjenesteAPI;

    @Before
    public void oppsett() throws Exception {
        // Fjern alle andre steg enn SØKNADSFRIST_FORELDREPENGER
        ((BehandlingModellRepositoryImpl)behandlingModellRepository).close();

        EntityManager entityManager = repoRule.getEntityManager();
        Query query = entityManager.createNativeQuery("delete from BEHANDLING_TYPE_STEG_SEKV btss " +
            "where btss.FAGSAK_YTELSE_TYPE=:fagsakYtelseType " +
            "and btss.BEHANDLING_TYPE=:behandlingType " +
            "and btss.BEHANDLING_STEG_TYPE NOT IN (:stegtyper) "
        );
        query.setParameter("behandlingType", BehandlingType.REVURDERING.getKode()); //$NON-NLS-1$
        query.setParameter("fagsakYtelseType", FagsakYtelseType.FORELDREPENGER.getKode()); //$NON-NLS-1$
        query.setParameter("stegtyper", asList(BehandlingStegType.SØKNADSFRIST_FORELDREPENGER.getKode())); //$NON-NLS-1$
        query.executeUpdate();
        entityManager.flush();
        BehandlingModell modell = behandlingModellRepository.getModell(BehandlingType.REVURDERING, FagsakYtelseType.FORELDREPENGER);
        assertThat(modell.hvertSteg().collect(Collectors.toList())).hasSize(1);

        this.ytelsesFordelingRepository = repositoryProvider.getYtelsesFordelingRepository();
        this.uttakRepository = repositoryProvider.getUttakRepository();

        registerKontekst.intialiser();
    }

    @After
    public void teardown() {
        registerKontekst.nullstill();
    }

    @Test
    public void skal_motta_endringssøknad_med_endringer_på_ytelsefordeling() {
        // Pre-Arrange: Registerdata
        TpsPerson mor = TpsTestSett.kvinneUtenBarn().getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(mor.getFnr());

        // Arrange steg 1 - opprette behandling og innsende endringssøknad - startpunkt skal settes til Inngangsvilkår
        LocalDate termindato = LocalDate.now();
        LocalDate uttaksdato = termindato; // Fødsel sammenfaller med uttaksdato - skjæringstidspunkt endres ikke

        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medBruker(mor.getAktørId(), NavBrukerKjønn.KVINNE);
        // Oppgitte opplysninger fra førstegangssøknad
        // KUNNE VI HELLER HA KONVERTERT EN SØKNAD FRA TESTSETTET TIL BEHANDLINGSGRUNNLAG?
        // 1) Familiehendelse
        scenario.medSøknadHendelse().medTerminbekreftelse(byggOppgittTerminbekreftelse(scenario, termindato));
        // 2) Ytelsefordeling
        OppgittPeriode periodeFpFørFødsel = byggOppgittePerioder(OppgittPeriodeBuilder.ny()
            .medPeriode(termindato.minusDays(1).minusWeeks(3), termindato.minusDays(1))
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL));
        OppgittFordelingEntitet fordeling = new OppgittFordelingEntitet(Collections.singletonList(periodeFpFørFødsel), true);
        scenario.medFordeling(fordeling);

        // Vedtaksresultat
        scenario.medVilkårResultatType(VilkårResultatType.INNVILGET);

        Behandling originalBehandling = scenario.lagre(repositoryProvider);
        avsluttBehandlingOgFagsak(originalBehandling, FagsakStatus.LØPENDE);
        // Postaktiviteter ikke støttet av testbuilder :(
        opprettUttakResultat(originalBehandling, termindato, false);
        oppdaterVedtaksresultat(originalBehandling, VedtakResultatType.INNVILGET);

        // Endringssøknad
        LocalDate endretUttaksdato = uttaksdato.plusDays(2);
        Soeknad soeknad = endringssøknadMedOppgittFordeling(mor.getAktørId(), termindato, endretUttaksdato);
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostEndringssøknadBuilder(originalBehandling.getFagsak(), soeknad, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling revurdering = behandlingRepo.hentSisteBehandlingForFagsakId(originalBehandling.getFagsakId()).get();
        YtelseFordelingAggregat ytelseFordelingAggregat = ytelsesFordelingRepository.hentAggregat(revurdering);
        assertThat(ytelseFordelingAggregat.getOppgittFordeling().getOppgittePerioder()).hasSize(2);
        assertThat(ytelseFordelingAggregat.getOppgittFordeling().getOppgittePerioder().stream()
            .filter(oppgittPeriode -> oppgittPeriode.getPeriodeType().equals(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL))
            .map(OppgittPeriode::getFom)
            .findFirst()
            .get())
            .isEqualTo(endretUttaksdato.minusWeeks(3));
    }

    @Test
    public void skal_opprette_revurdering_på_avvist_behandling() {
        // Pre-Arrange: Registerdata
        TpsPerson mor = TpsTestSett.kvinneUtenBarn().getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(mor.getFnr());

        // Arrange steg 1 - opprette behandling og innsende endringssøknad - startpunkt skal settes til Inngangsvilkår
        LocalDate termindato = LocalDate.now();
        LocalDate uttaksdato = termindato; // Fødsel sammenfaller med uttaksdato - skjæringstidspunkt endres ikke

        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medBruker(mor.getAktørId(), NavBrukerKjønn.KVINNE);
        // Oppgitte opplysninger fra førstegangssøknad
        // 1) Familiehendelse
        scenario.medSøknadHendelse().medTerminbekreftelse(byggOppgittTerminbekreftelse(scenario, termindato));
        // 2) Ytelsefordeling
        OppgittPeriode periodeFpFørFødsel = byggOppgittePerioder(OppgittPeriodeBuilder.ny()
            .medPeriode(termindato.minusDays(1).minusWeeks(3), termindato.minusDays(1))
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL));
        OppgittFordelingEntitet fordeling = new OppgittFordelingEntitet(Collections.singletonList(periodeFpFørFødsel), true);
        scenario.medFordeling(fordeling);

        // Vedtaksresultat
        scenario.medVilkårResultatType(VilkårResultatType.INNVILGET);

        Behandling originalBehandling = scenario.lagre(repositoryProvider);
        avsluttBehandlingOgFagsak(originalBehandling, FagsakStatus.AVSLUTTET);
        // Postaktiviteter ikke støttet av testbuilder
        opprettUttakResultat(originalBehandling, termindato, true);
        oppdaterVedtaksresultat(originalBehandling, VedtakResultatType.AVSLAG);

        // Endringssøknad
        LocalDate endretUttaksdato = uttaksdato.plusDays(2);
        Soeknad soeknad = endringssøknadMedOppgittFordeling(mor.getAktørId(), termindato, endretUttaksdato);
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostEndringssøknadBuilder(originalBehandling.getFagsak(), soeknad, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling behandling = behandlingRepo.hentSisteBehandlingForFagsakId(originalBehandling.getFagsakId()).get();
        YtelseFordelingAggregat ytelseFordelingAggregatRevurdering = ytelsesFordelingRepository.hentAggregat(behandling);
        assertThat(ytelseFordelingAggregatRevurdering.getOppgittFordeling().getOppgittePerioder()).hasSize(1);
        assertThat(originalBehandling).isEqualTo(behandling);
    }

    private void oppdaterVedtaksresultat(Behandling origBehandling, VedtakResultatType vedtakResultatType) {
        BehandlingVedtak vedtak = BehandlingVedtak.builder()
            .medVedtakResultatType(vedtakResultatType)
            .medVedtaksdato(LocalDate.now())
            .medBehandlingsresultat(origBehandling.getBehandlingsresultat())
            .medAnsvarligSaksbehandler("Severin Saksbehandler")
            .build();
        Whitebox.setInternalState(origBehandling.getBehandlingsresultat(), "behandlingVedtak", vedtak);
        repository.lagre(origBehandling.getBehandlingsresultat());
    }

    private OppgittPeriode byggOppgittePerioder(OppgittPeriodeBuilder periodeFpFørFødsel) {
        return periodeFpFørFødsel
                .build();
    }

    private FamilieHendelseBuilder.TerminbekreftelseBuilder byggOppgittTerminbekreftelse(ScenarioMorSøkerForeldrepenger førstegangsscenario, LocalDate termindato) {
        return førstegangsscenario.medSøknadHendelse().getTerminbekreftelseBuilder()
            .medTermindato(termindato);
    }

    private Soeknad endringssøknadMedOppgittFordeling(AktørId aktørId, LocalDate fødselsdato, LocalDate uttaksdato) {
        return new SøknadTestdataBuilder().endringssøknadForeldrepenger()
            .medSøker(ForeldreType.MOR, aktørId)
            .medMottattdato(fødselsdato)
            .medFordeling(new SøknadTestdataBuilder.FordelingBuilder()
                .leggTilPeriode(uttaksdato.minusWeeks(3), uttaksdato.minusDays(1), UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL)
                .leggTilPeriode(uttaksdato, uttaksdato.plusWeeks(10).minusDays(1), UttakPeriodeType.FORELDREPENGER))
            .build();
    }

    private void avsluttBehandlingOgFagsak(Behandling behandling, FagsakStatus fagsakStatus) {
        behandling.avsluttBehandling();
        BehandlingLås lås = behandlingRepo.taSkriveLås(behandling);
        behandlingRepo.lagre(behandling, lås);

        FagsakRepository fagsakRepository = repositoryProvider.getFagsakRepository();
        fagsakRepository.oppdaterFagsakStatus(behandling.getFagsakId(), fagsakStatus);
    }

    private void opprettUttakResultat(Behandling behandling, LocalDate uttaksdato, boolean harAvslag) {
        UttakResultatEntitet.Builder uttakResultatPlanBuilder = UttakResultatEntitet.builder(behandling);

        UttakResultatPeriodeEntitet uttakResultatPeriode = new UttakResultatPeriodeEntitet
            .Builder(uttaksdato, uttaksdato.plusDays(7))
            .medPeriodeResultat(harAvslag ? PeriodeResultatType.AVSLÅTT : PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .medPeriodeSoknad(new UttakResultatPeriodeSøknadEntitet.Builder().medUttakPeriodeType(UttakPeriodeType.MØDREKVOTE).build())
            .build();

        VirksomhetEntitet virksomhet = new VirksomhetEntitet.Builder().medOrgnr("000000000").oppdatertOpplysningerNå().build();
        repoRule.getRepository().lagre(virksomhet);

        UttakAktivitetEntitet uttakAktivitet = new UttakAktivitetEntitet.Builder()
            .medArbeidsforhold(virksomhet, ArbeidsforholdRef.ref("123"))
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();

        UttakResultatPeriodeAktivitetEntitet periodeAktivitet = UttakResultatPeriodeAktivitetEntitet.builder(uttakResultatPeriode,
            uttakAktivitet)
            .medTrekkonto(StønadskontoType.FORELDREPENGER)
            .medTrekkdager(10)
            .medArbeidsprosent(new BigDecimal(100))
            .medUtbetalingsprosent(new BigDecimal(100))
            .build();

        uttakResultatPeriode.leggTilAktivitet(periodeAktivitet);

        UttakResultatPerioderEntitet uttakResultatPerioder = new UttakResultatPerioderEntitet();
        uttakResultatPerioder.leggTilPeriode(uttakResultatPeriode);

        UttakResultatEntitet uttakResultat = uttakResultatPlanBuilder.medOpprinneligPerioder(uttakResultatPerioder).build();
        uttakRepository.lagreOpprinneligUttakResultatPerioder(behandling, uttakResultat.getGjeldendePerioder());
    }
}
