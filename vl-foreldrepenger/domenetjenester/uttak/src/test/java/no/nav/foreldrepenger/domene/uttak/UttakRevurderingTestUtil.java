package no.nav.foreldrepenger.domene.uttak;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittRettighetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordeling;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.Uttaksperiodegrense;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.JournalpostId;

public class UttakRevurderingTestUtil {

    public static final AktørId AKTØR_ID_MOR = new AktørId("1");
    public static final AktørId AKTØR_ID_FAR = new AktørId("2");
    public static final LocalDate FØDSELSDATO = LocalDate.now();
    public static final LocalDate FØRSTE_UTTAKSDATO = LocalDate.now().plusDays(1);
    public static final LocalDate FØRSTE_UTTAKSDATO_SØKNAD_MOR_FPFF = LocalDate.now().minusDays(20);
    public static final LocalDate FØRSTE_UTTAKSDATO_SØKNAD_MOR_FELLES = LocalDate.now().plusDays(40);
    public static final LocalDate FØRSTE_UTTAKSDATO_SØKNAD_FAR = LocalDate.now().plusDays(80);
    public static final LocalDate MANUELT_SATT_FØRSTE_UTTAKSDATO = LocalDate.now().plusDays(1);
    public static final LocalDate OMSORGSOVERTAKELSEDATO = LocalDate.now().plusDays(10);
    public static final LocalDate ANKOMSTDATO = LocalDate.now().plusDays(11);

    private Virksomhet virksomhet;

    private UnittestRepositoryRule repoRule;
    private BehandlingRepositoryProvider repositoryProvider;

    public UttakRevurderingTestUtil(UnittestRepositoryRule repoRule, BehandlingRepositoryProvider repositoryProvider) {
        this.repoRule = repoRule;
        this.repositoryProvider = repositoryProvider;
    }

    public Behandling opprettRevurdering() {
        return opprettRevurdering(AKTØR_ID_MOR, BehandlingÅrsakType.RE_HENDELSE_FØDSEL);
    }

    public Behandling opprettRevurdering(AktørId aktørId, BehandlingÅrsakType behandlingÅrsakType) {
        return opprettRevurdering(aktørId, behandlingÅrsakType, defaultUttaksresultat(),
            new OppgittFordelingEntitet(Collections.emptyList(), true), null);
    }

    private List<UttakResultatPeriodeEntitet> defaultUttaksresultat() {
        return Collections.singletonList(new UttakResultatPeriodeEntitet.Builder(FØRSTE_UTTAKSDATO, FØRSTE_UTTAKSDATO.plusDays(10))
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build());
    }

    public Behandling opprettRevurdering(AktørId aktørId,
                                         BehandlingÅrsakType behandlingÅrsakType,
                                         List<UttakResultatPeriodeEntitet> opprinneligUttaksResultatPerioder,
                                         OppgittFordeling nyFordeling,
                                         FamilieHendelseBuilder hendelseBuilder) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(aktørId);
        if (hendelseBuilder == null) {
            scenario.medDefaultSøknadTerminbekreftelse();
        } else {
            scenario.medBekreftetHendelse(hendelseBuilder);
        }
        Behandling førstegangsbehandling = byggFørstegangsbehandling(scenario, opprinneligUttaksResultatPerioder);

        ScenarioMorSøkerForeldrepenger revurderingsscenario = ScenarioMorSøkerForeldrepenger.forFødsel(false, aktørId)
            .medOriginalBehandling(førstegangsbehandling, behandlingÅrsakType)
            .medBehandlingType(BehandlingType.REVURDERING)
            .medOppgittRettighet(new OppgittRettighetEntitet(true, true, false));
        revurderingsscenario.medBehandlingVedtak()
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medAnsvarligSaksbehandler("saksbehandler")
            .medVedtaksdato(LocalDate.now());

        Behandling revurdering = revurderingsscenario.lagre(repositoryProvider);
        if (nyFordeling != null) {
            revurderingsscenario.medSøknad().medSøknadsdato(LocalDate.now()).medErEndringssøknad(true).medFordeling(nyFordeling);
            repositoryProvider.getYtelsesFordelingRepository().lagre(revurdering, nyFordeling);
        }
        lagreUttaksperiodegrense(revurdering);
        kopierGrunnlagsdata(revurdering);
        return revurdering;
    }

    public Behandling opprettRevurderingAdopsjon(LocalDate ankomstDato) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forAdopsjon();
        scenario.medSøknadHendelse().medAdopsjon(
            scenario.medSøknadHendelse().getAdopsjonBuilder()
                .medOmsorgsovertakelseDato(OMSORGSOVERTAKELSEDATO)
                .medAnkomstDato(ankomstDato));
        Behandling førstegangsbehandling = byggFørstegangsbehandling(scenario, defaultUttaksresultat());

        ScenarioMorSøkerForeldrepenger revurderingsscenario = ScenarioMorSøkerForeldrepenger.forAdopsjon()
            .medOriginalBehandling(førstegangsbehandling, BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING)
            .medBehandlingType(BehandlingType.REVURDERING);

        Behandling revurdering = revurderingsscenario.lagre(repositoryProvider);
        kopierGrunnlagsdata(revurdering);
        return revurdering;
    }

    private Behandling byggFørstegangsbehandling(ScenarioMorSøkerForeldrepenger scenario, List<UttakResultatPeriodeEntitet> perioder) {
        scenario.medDefaultInntektArbeidYtelse();
        scenario.medBehandlingVedtak()
            .medVedtaksdato(LocalDate.now().minusDays(7))
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medAnsvarligSaksbehandler("Saksbehandler Saksbehandlersen")
            .build();
        scenario.medFordeling(defaultFordeling());
        Behandling førstegangsbehandling = scenario.lagre(repositoryProvider);
        opprettUttakResultat(førstegangsbehandling, perioder);
        avsluttBehandlingOgFagsak(førstegangsbehandling);
        return førstegangsbehandling;
    }

    private OppgittFordeling defaultFordeling() {
        OppgittPeriodeBuilder oppgittPeriodeBuilder = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medPeriode(FØRSTE_UTTAKSDATO, LocalDate.now().plusDays(10));
        return new OppgittFordelingEntitet(Collections.singletonList(oppgittPeriodeBuilder.build()), true);
    }

    private void lagreUttaksperiodegrense(Behandling behandling) {
        Uttaksperiodegrense grense = new Uttaksperiodegrense.Builder(behandling)
            .medFørsteLovligeUttaksdag(LocalDate.of(2018,1,1))
            .medMottattDato(LocalDate.now())
            .build();
        repositoryProvider.getUttakRepository().lagreUttaksperiodegrense(behandling, grense);
    }

    private void opprettUttakResultat(Behandling førstegangsbehandling, List<UttakResultatPeriodeEntitet> perioder) {
        if (perioder == null || perioder.isEmpty()) {
            return;
        }
        UttakResultatPerioderEntitet uttakResultatPerioder = new UttakResultatPerioderEntitet();
        for (UttakResultatPeriodeEntitet periode : perioder) {
            uttakResultatPerioder.leggTilPeriode(periode);
        }
        repositoryProvider.getUttakRepository().lagreOpprinneligUttakResultatPerioder(førstegangsbehandling, uttakResultatPerioder);
    }

    private void avsluttBehandlingOgFagsak(Behandling behandling) {
        behandling.avsluttBehandling();
        repositoryProvider.getBehandlingRepository().lagre(behandling, repositoryProvider.getBehandlingRepository().taSkriveLås(behandling));
        FagsakRepository fagsakRepository = repositoryProvider.getFagsakRepository();
        fagsakRepository.oppdaterFagsakStatus(behandling.getFagsakId(), FagsakStatus.LØPENDE);
    }

    private void kopierGrunnlagsdata(Behandling revurdering) {
        Behandling originalBehandling = revurdering.getOriginalBehandling().get();
        repositoryProvider.getFamilieGrunnlagRepository().kopierGrunnlagFraEksisterendeBehandling(originalBehandling, revurdering);
        repositoryProvider.getInntektArbeidYtelseRepository().kopierGrunnlagFraEksisterendeBehandling(originalBehandling, revurdering);
    }

    public void byggOgLagreSøknadMedOppgittFordeling(Behandling behandling, OppgittFordeling oppgittFordeling) {
        FamilieHendelse familieHendelse = byggFamilieHendelse(behandling);

        Søknad søknad = new SøknadEntitet.Builder().medElektroniskRegistrert(true)
            .medFordeling(oppgittFordeling)
            .medFamilieHendelse(familieHendelse)
            .medSøknadsdato(LocalDate.now())
            .medMottattDato(LocalDate.now())
            .medErEndringssøknad(true)
            .build();
        repositoryProvider.getSøknadRepository().lagreOgFlush(behandling, søknad);
    }

    public OppgittFordeling byggOgLagreOppgittFordelingForMorFPFF(Behandling behandling) {
        OppgittPeriodeBuilder periode1 = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL)
            .medPeriode(FØRSTE_UTTAKSDATO_SØKNAD_MOR_FPFF, FØRSTE_UTTAKSDATO_SØKNAD_MOR_FPFF.plusWeeks(2))
            .medVirksomhet(getVirksomhet());
        OppgittPeriodeBuilder periode2 = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(FØRSTE_UTTAKSDATO_SØKNAD_MOR_FPFF.plusWeeks(2).plusDays(1), FØRSTE_UTTAKSDATO_SØKNAD_MOR_FPFF.plusWeeks(10))
            .medVirksomhet(virksomhet);

        OppgittFordelingEntitet oppgittFordeling = new OppgittFordelingEntitet(asList(periode2.build(), periode1.build()), true);
        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, oppgittFordeling);
        return oppgittFordeling;
    }

    public OppgittFordeling byggOgLagreOppgittFordelingMedPeriode(Behandling behandling, LocalDate fom, LocalDate tom, UttakPeriodeType uttakPeriodeType) {
        OppgittPeriodeBuilder periode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(uttakPeriodeType)
            .medPeriode(fom, tom)
            .medVirksomhet(getVirksomhet());

        OppgittFordelingEntitet oppgittFordeling = new OppgittFordelingEntitet(asList(periode.build()), true);
        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, oppgittFordeling);
        return oppgittFordeling;
    }

    public OppgittFordeling byggOgLagreOppgittFordelingForMorFelles(Behandling behandling) {
        OppgittPeriodeBuilder periode1 = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medPeriode(FØRSTE_UTTAKSDATO_SØKNAD_MOR_FELLES, FØRSTE_UTTAKSDATO_SØKNAD_MOR_FELLES.plusWeeks(1))
            .medVirksomhet(getVirksomhet());

        OppgittFordelingEntitet oppgittFordeling = new OppgittFordelingEntitet(singletonList(periode1.build()), true);
        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, oppgittFordeling);
        return oppgittFordeling;
    }

    public OppgittFordeling byggOgLagreOppgittFordelingForFar(Behandling behandling) {
        OppgittPeriodeBuilder periode1 = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FEDREKVOTE)
            .medPeriode(FØRSTE_UTTAKSDATO_SØKNAD_FAR, FØRSTE_UTTAKSDATO_SØKNAD_FAR.plusWeeks(2))
            .medVirksomhet(getVirksomhet());
        OppgittPeriodeBuilder periode2 = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medPeriode(FØRSTE_UTTAKSDATO_SØKNAD_FAR.plusWeeks(2).plusDays(1), FØRSTE_UTTAKSDATO_SØKNAD_FAR.plusWeeks(4))
            .medVirksomhet(getVirksomhet());

        OppgittFordelingEntitet oppgittFordeling = new OppgittFordelingEntitet(asList(periode2.build(), periode1.build()), true);
        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, oppgittFordeling);
        return oppgittFordeling;
    }

    private Virksomhet getVirksomhet() {
        if (virksomhet == null) {
            opprettOgLagreVirksomhet();
        }
        return virksomhet;
    }

    private void opprettOgLagreVirksomhet() {
        virksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr("75674554355")
            .medNavn("Virksomhet")
            .medRegistrert(LocalDate.now().minusYears(10L))
            .medOppstart(LocalDate.now().minusYears(10L))
            .oppdatertOpplysningerNå()
            .build();
        repoRule.getEntityManager().persist(virksomhet);
    }

    private FamilieHendelse byggFamilieHendelse(Behandling behandling) {
        FamilieHendelseBuilder søknadHendelse = repositoryProvider.getFamilieGrunnlagRepository().opprettBuilderFor(behandling)
            .medAntallBarn(1);
        søknadHendelse.medTerminbekreftelse(søknadHendelse.getTerminbekreftelseBuilder()
            .medTermindato(FØDSELSDATO).medUtstedtDato(LocalDate.now()));
        repositoryProvider.getFamilieGrunnlagRepository().lagre(behandling, søknadHendelse);
        return repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling).getSøknadVersjon();
    }

    public void opprettInntektsmelding(Behandling revurdering) {
        MottattDokument mottattDokument = new MottattDokument.Builder()
            .medDokumentTypeId(DokumentTypeId.INNTEKTSMELDING)
            .medFagsakId(revurdering.getFagsakId())
            .medMottattDato(LocalDate.now())
            .medBehandlingId(revurdering.getId())
            .medElektroniskRegistrert(true)
            .medJournalPostId(new JournalpostId("2"))
            .medDokumentId("3")
            .build();
        repositoryProvider.getMottatteDokumentRepository().lagre(mottattDokument);
        VirksomhetEntitet virksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr("1")
            .oppdatertOpplysningerNå()
            .build();
        repositoryProvider.getVirksomhetRepository().lagre(virksomhet);
        InntektsmeldingBuilder inntektsmeldingBuilder = InntektsmeldingBuilder.builder()
            .medBeløp(BigDecimal.TEN)
            .medStartDatoPermisjon(LocalDate.now())
            .medVirksomhet(virksomhet)
            .medInnsendingstidspunkt(LocalDateTime.now())
            .medMottattDokument(mottattDokument);
        repositoryProvider.getInntektArbeidYtelseRepository().lagre(revurdering, inntektsmeldingBuilder.build());
    }

    public Behandling byggFørstegangsbehandlingForRevurderingBerørtSak(AktørId aktørId, List<UttakResultatPeriodeEntitet> perioder) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(aktørId);
        scenario.medDefaultSøknadTerminbekreftelse();
        scenario.medDefaultInntektArbeidYtelse();
        scenario.medBehandlingVedtak()
            .medVedtaksdato(LocalDate.now().minusDays(7))
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medAnsvarligSaksbehandler("Saksbehandler Saksbehandlersen")
            .build();
        Behandling førstegangsbehandling = scenario.lagre(repositoryProvider);
        opprettUttakResultat(førstegangsbehandling, perioder);
        avsluttBehandlingOgFagsak(førstegangsbehandling);
        return førstegangsbehandling;
    }

    public List<UttakResultatPeriodeEntitet> uttaksresultatBerørtSak(LocalDate fom) {
        return Collections.singletonList(new UttakResultatPeriodeEntitet.Builder(fom, fom.plusDays(10))
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build());
    }

    public Behandling opprettRevurderingBerørtSak(AktørId aktørId,
                                         BehandlingÅrsakType behandlingÅrsakType,
                                         Behandling førstegangsbehandling) {

        ScenarioMorSøkerForeldrepenger revurderingsscenario = ScenarioMorSøkerForeldrepenger.forFødsel(false, aktørId)
            .medOriginalBehandling(førstegangsbehandling, behandlingÅrsakType)
            .medBehandlingType(BehandlingType.REVURDERING);
        revurderingsscenario.medBehandlingVedtak()
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medAnsvarligSaksbehandler("saksbehandler")
            .medVedtaksdato(LocalDate.now());

        Behandling revurdering = revurderingsscenario.lagre(repositoryProvider);
        lagreUttaksperiodegrense(revurdering);
        kopierGrunnlagsdata(revurdering);
        return revurdering;
    }


}
