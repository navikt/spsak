package no.nav.foreldrepenger.domene.arbeidsforhold.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.AktørInntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Opptjeningsnøkkel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdHandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdOverstyringBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdWrapper;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørArbeid;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Permisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektspostType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.PermisjonsbeskrivelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.AnnenAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.ArbeidsforholdAdministrasjonTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.VurderArbeidsforholdTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsforholTjenesteMock;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class InntektArbeidYtelseTjenesteImplTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private ResultatRepositoryProvider resultatProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());
    private FagsakRepository fagsakRepository = new FagsakRepositoryImpl(repoRule.getEntityManager());
    private InntektArbeidYtelseTjeneste tjeneste;
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
    private ArbeidsforholdAdministrasjonTjeneste arbeidsforholdTjeneste;

    private String AREBIDSFORHOLD_ID = "1";
    private AktørId AKTØRID = new AktørId("1");
    private Virksomhet virksomhet;
    private LocalDateTime I_DAG = LocalDateTime.now();

    @Before
    public void setUp() throws Exception {
        virksomhet = getVirksomheten();
        final ArbeidsforholTjenesteMock arbeidsforholTjenesteMock = new ArbeidsforholTjenesteMock();
        TpsTjeneste tpsTjeneste = mock(TpsTjeneste.class);
        final PersonIdent t = new PersonIdent("12345678901");
        when(tpsTjeneste.hentFnrForAktør(Mockito.any(AktørId.class))).thenReturn(t);
        final VirksomhetTjeneste virksomhetTjeneste = mock(VirksomhetTjeneste.class);
        when(virksomhetTjeneste.hentOgLagreOrganisasjon(any())).thenReturn(virksomhet);
        VurderArbeidsforholdTjeneste vurderArbeidsforholdTjeneste = mock(VurderArbeidsforholdTjeneste.class);

        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(getAndreVirsomhenten());
        Set<ArbeidsforholdRef> arbeidsforholdRefSet = new HashSet<>();
        arbeidsforholdRefSet.add(ArbeidsforholdRef.ref(AREBIDSFORHOLD_ID));
        Map<Arbeidsgiver, Set<ArbeidsforholdRef>> arbeidsgiverSetMap = new HashMap<>();
        arbeidsgiverSetMap.put(arbeidsgiver, arbeidsforholdRefSet);
        when(vurderArbeidsforholdTjeneste.vurder(any(Behandling.class))).thenReturn(arbeidsgiverSetMap);

        final SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = mock(SkjæringstidspunktTjeneste.class);
        when(skjæringstidspunktTjeneste.utledSkjæringstidspunktForRegisterInnhenting(any())).thenReturn(I_DAG.toLocalDate());
        when(skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(any())).thenReturn(I_DAG.toLocalDate());
        when(skjæringstidspunktTjeneste.utledSkjæringstidspunktForForeldrepenger(any())).thenReturn(I_DAG.toLocalDate());
        AksjonspunktutlederForVurderOpptjening aksjonspunktutlederForVurderOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, resultatProvider, skjæringstidspunktTjeneste);
        tjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, arbeidsforholTjenesteMock.getMock(), tpsTjeneste, virksomhetTjeneste, skjæringstidspunktTjeneste, aksjonspunktutlederForVurderOpptjening);
        arbeidsforholdTjeneste = new ArbeidsforholdAdministrasjonTjenesteImpl(repositoryProvider, vurderArbeidsforholdTjeneste, tpsTjeneste, skjæringstidspunktTjeneste);
    }

    @Test
    public void skal_vurdere_om_inntektsmeldinger_er_komplett() {
        // Arrange
        final Behandling behandling = opprettBehandling();
        opprettOppgittOpptjening(behandling);
        opprettInntektArbeidYtelseAggregatForYrkesaktivitet(AKTØRID, AREBIDSFORHOLD_ID, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, BigDecimal.TEN, behandling);

        // Act+Assert
        assertThat(tjeneste.utledManglendeInntektsmeldingerFraArkiv(behandling)).isNotEmpty();

        lagreInntektsmelding(I_DAG.minusDays(2), behandling, "1", "123");

        // Act+Assert
        assertThat(tjeneste.utledManglendeInntektsmeldingerFraArkiv(behandling)).isEmpty();
    }

    @Test
    public void skal_utelate_inntektsmeldinger_som_er_mottatt_i_førstegangsbehandlingen_ved_revurdering() {
        // Arrange
        Behandling behandling = opprettBehandling();
        opprettOppgittOpptjening(behandling);
        opprettInntektArbeidYtelseAggregatForYrkesaktivitet(AKTØRID, AREBIDSFORHOLD_ID, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, BigDecimal.TEN, behandling);
        lagreInntektsmelding(I_DAG.minusDays(2), behandling, "1", "123");
        avsluttBehandlingOgFagsak(behandling);

        Behandling revurdering = opprettRevurderingsbehandling(behandling);
        inntektArbeidYtelseRepository.kopierGrunnlagFraEksisterendeBehandling(behandling, revurdering);

        // Act+Assert
        assertThat(tjeneste.utledManglendeInntektsmeldingerFraArkiv(revurdering)).isNotEmpty();

        final Inntektsmelding nyInntektsmelding = lagreInntektsmelding(I_DAG, revurdering, "1", "1234");

        // Act+Assert
        assertThat(tjeneste.utledManglendeInntektsmeldingerFraArkiv(revurdering)).isEmpty();

        final List<Inntektsmelding> nyeInntektsmeldinger = tjeneste.hentAlleInntektsmeldingerMottattEtterGjeldendeVedtak(revurdering);
        assertThat(nyeInntektsmeldinger).containsExactly(nyInntektsmelding);
    }

    @Test
    public void skal_utlede_arbeidsforholdwrapper() {
        LocalDateTime mottattDato = I_DAG.minusDays(2);

        Behandling behandling = opprettBehandling();
        opprettOppgittOpptjening(behandling);
        opprettInntektArbeidYtelseAggregatForYrkesaktivitet(AKTØRID, AREBIDSFORHOLD_ID, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, BigDecimal.TEN, behandling);
        lagreInntektsmelding(mottattDato, behandling, AREBIDSFORHOLD_ID, "123");

        Set<ArbeidsforholdWrapper> wrapperList = arbeidsforholdTjeneste.hentArbeidsforholdFerdigUtledet(behandling);

        assertThat(wrapperList).hasSize(1);
        ArbeidsforholdWrapper arbeidsforhold = wrapperList.iterator().next();

        assertThat(arbeidsforhold.getMottattDatoInntektsmelding()).isEqualTo(mottattDato.toLocalDate());
        assertThat(arbeidsforhold.getBrukArbeidsforholdet()).isEqualTo(true);
        assertThat(arbeidsforhold.getFortsettBehandlingUtenInntektsmelding()).isEqualTo(false);
        assertThat(arbeidsforhold.getFomDato()).isEqualTo(I_DAG.minusMonths(3).toLocalDate());
        assertThat(arbeidsforhold.getTomDato()).isEqualTo(I_DAG.plusMonths(2).toLocalDate());
    }

    @Test
    public void skal_utlede_arbeidsforholdwrapper_etter_overstyring() {
        LocalDateTime mottattDato = I_DAG.minusDays(2);

        Behandling behandling = opprettBehandling();
        opprettOppgittOpptjening(behandling);
        opprettInntektArbeidYtelseAggregatForYrkesaktivitet(AKTØRID, AREBIDSFORHOLD_ID, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, BigDecimal.TEN, behandling);
        lagreInntektsmelding(mottattDato, behandling, AREBIDSFORHOLD_ID, "123");

        ArbeidsforholdInformasjonBuilder informasjonBuilder = arbeidsforholdTjeneste.opprettBuilderFor(behandling);
        ArbeidsforholdInformasjon arbeidsforholdInformasjon = inntektArbeidYtelseRepository.hentArbeidsforholdInformasjon(behandling).get();
        ArbeidsforholdOverstyringBuilder overstyringBuilder = informasjonBuilder.getOverstyringBuilderFor(Arbeidsgiver.virksomhet(virksomhet),
            arbeidsforholdInformasjon.finnForEkstern(Arbeidsgiver.virksomhet(virksomhet), ArbeidsforholdRef.ref(AREBIDSFORHOLD_ID)));
        overstyringBuilder.medHandling(ArbeidsforholdHandlingType.BRUK);
        informasjonBuilder.leggTil(overstyringBuilder);

        arbeidsforholdTjeneste.lagre(behandling, informasjonBuilder);

        Set<ArbeidsforholdWrapper> wrapperList = arbeidsforholdTjeneste.hentArbeidsforholdFerdigUtledet(behandling);

        assertThat(wrapperList).hasSize(1);
        ArbeidsforholdWrapper arbeidsforhold = wrapperList.iterator().next();

        assertThat(arbeidsforhold.getMottattDatoInntektsmelding()).isEqualTo(mottattDato.toLocalDate());
        assertThat(arbeidsforhold.getBrukArbeidsforholdet()).isEqualTo(true);
        assertThat(arbeidsforhold.getFortsettBehandlingUtenInntektsmelding()).isEqualTo(false);
        assertThat(arbeidsforhold.getFomDato()).isEqualTo(I_DAG.minusMonths(3).toLocalDate());
        assertThat(arbeidsforhold.getTomDato()).isEqualTo(I_DAG.plusMonths(2).toLocalDate());
    }

    @Test
    public void skal_finne_inntektsmeldinger_etter_gjeldende_behandling() {
        // Arrange
        Behandling behandling = opprettBehandling();
        opprettOppgittOpptjening(behandling);
        opprettInntektArbeidYtelseAggregatForYrkesaktivitet(AKTØRID, AREBIDSFORHOLD_ID, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, BigDecimal.TEN, behandling);
        lagreInntektsmelding(I_DAG.minusDays(2), behandling, "1", "1");
        lagreInntektsmelding(I_DAG.minusDays(3), behandling, "2", "12");
        avsluttBehandlingOgFagsak(behandling);

        List<Inntektsmelding> inntektsmeldingerFørGjeldendeVedtak = tjeneste.hentAlleInntektsmeldinger(behandling);

        Behandling revurdering = opprettRevurderingsbehandling(behandling);
        inntektArbeidYtelseRepository.kopierGrunnlagFraEksisterendeBehandling(behandling, revurdering);
        lagreInntektsmelding(I_DAG.plusWeeks(2), revurdering, "1", "123");
        lagreInntektsmelding(I_DAG.plusWeeks(3), revurdering, "3", "1234");


        // Act+Assert
        List<Inntektsmelding> inntektsmeldingerEtterGjeldendeVedtak = tjeneste.hentAlleInntektsmeldingerMottattEtterGjeldendeVedtak(revurdering);
        assertThat(inntektsmeldingerEtterGjeldendeVedtak).hasSize(2);
        assertThat(erDisjonkteListerAvInntektsmeldinger(inntektsmeldingerFørGjeldendeVedtak, inntektsmeldingerEtterGjeldendeVedtak)).isTrue();
    }


    @Test
    public void test_hentArbeidsforholdFerdigUtledet_med_aksjonspunkt() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();

        LocalDate skjæringstidspunkt = LocalDate.now();

        opprettArbeidOgInntektForBehandling(scenario, skjæringstidspunkt.minusMonths(5), skjæringstidspunkt.plusMonths(4), true, false);

        Behandling behandling = scenario.lagre(repositoryProvider, resultatProvider);

        opprettAksjonspunkt(behandling, AksjonspunktDefinisjon.VURDER_ARBEIDSFORHOLD, LocalDateTime.now());

        inntektArbeidYtelseRepository.hentAggregatHvisEksisterer(behandling, skjæringstidspunkt);
        arbeidsforholdTjeneste.hentArbeidsforholdFerdigUtledet(behandling);

    }

    private boolean erDisjonkteListerAvInntektsmeldinger(List<Inntektsmelding> imsA, List<Inntektsmelding> imsB) {
        return Collections.disjoint(
            imsA.stream().map(ima -> ima.getJournalpostId()).collect(Collectors.toList()),
            imsB.stream().map(imb -> imb.getJournalpostId()).collect(Collectors.toList()));
    }


    private Inntektsmelding lagreInntektsmelding(LocalDateTime mottattDato, Behandling behandling, String arbeidsforholdId, String journalpostId) {
        Inntektsmelding inntektsmelding = InntektsmeldingBuilder.builder()
            .medStartDatoPermisjon(I_DAG.toLocalDate())
            .medVirksomhet(virksomhet)
            .medBeløp(BigDecimal.TEN)
            .medNærRelasjon(false)
            .medArbeidsforholdId(arbeidsforholdId)
            .medInnsendingstidspunkt(mottattDato)
            .medJournalpostId(new JournalpostId(journalpostId))
            .build();

        inntektArbeidYtelseRepository.lagre(behandling, inntektsmelding);

        return inntektsmelding;
    }

    private void opprettOppgittOpptjening(Behandling behandling) {
        DatoIntervallEntitet periode = DatoIntervallEntitet.fraOgMedTilOgMed(I_DAG.minusMonths(2).toLocalDate(), I_DAG.plusMonths(1).toLocalDate());
        OppgittOpptjeningBuilder oppgitt = OppgittOpptjeningBuilder.ny();
        oppgitt.leggTilAnnenAktivitet(new AnnenAktivitetEntitet(periode, ArbeidType.MILITÆR_ELLER_SIVILTJENESTE));
        inntektArbeidYtelseRepository.lagre(behandling, oppgitt);
    }

    private void opprettInntektArbeidYtelseAggregatForYrkesaktivitet(AktørId aktørId, String arbeidsforhold,
                                                                     ArbeidType type, BigDecimal prosentsats,
                                                                     Behandling behandling) {
        DatoIntervallEntitet periode = DatoIntervallEntitet.fraOgMedTilOgMed(I_DAG.minusMonths(3).toLocalDate(), I_DAG.plusMonths(2).toLocalDate());

        InntektArbeidYtelseAggregatBuilder builder = InntektArbeidYtelseAggregatBuilder
            .oppdatere(Optional.empty(), VersjonType.REGISTER);

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = builder.getAktørArbeidBuilder(aktørId);
        YrkesaktivitetBuilder yrkesaktivitetBuilder = aktørArbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(new Opptjeningsnøkkel(arbeidsforhold, virksomhet.getOrgnr(), null),
            ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();
        YrkesaktivitetEntitet.PermisjonBuilder permisjonBuilder = yrkesaktivitetBuilder.getPermisjonBuilder();

        VirksomhetRepository virksomhetRepository = repositoryProvider.getVirksomhetRepository();
        virksomhetRepository.lagre(virksomhet);

        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale = aktivitetsAvtaleBuilder
            .medPeriode(periode)
            .medProsentsats(prosentsats)
            .medAntallTimer(BigDecimal.valueOf(20.4d))
            .medAntallTimerFulltid(BigDecimal.valueOf(10.2d))
            .medBeskrivelse("Ser greit ut");
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder ansettelsesPeriode = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder().medPeriode(periode);

        Permisjon permisjon = permisjonBuilder
            .medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.UTDANNINGSPERMISJON)
            .medPeriode(periode.getFomDato(), periode.getTomDato())
            .medProsentsats(BigDecimal.valueOf(100))
            .build();

        yrkesaktivitetBuilder
            .medArbeidType(type)
            .medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet))
            .medArbeidsforholdId(ArbeidsforholdRef.ref(AREBIDSFORHOLD_ID))
            .leggTilPermisjon(permisjon)
            .leggTilAktivitetsAvtale(aktivitetsAvtale)
            .leggTilAktivitetsAvtale(ansettelsesPeriode);

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeid = aktørArbeidBuilder
            .leggTilYrkesaktivitet(yrkesaktivitetBuilder);

        builder.leggTilAktørArbeid(aktørArbeid);

        tjeneste.lagre(behandling, builder);
    }

    private VirksomhetEntitet getVirksomheten() {
        return new VirksomhetEntitet.Builder()
            .medOrgnr("973093681")
            .medNavn("Virksomheten")
            .medRegistrert(LocalDate.now().minusYears(2L))
            .medOppstart(LocalDate.now().minusYears(1L))
            .oppdatertOpplysningerNå()
            .build();
    }


    private VirksomhetEntitet getAndreVirsomhenten() {
        return new VirksomhetEntitet.Builder()
            .medOrgnr("52")
            .medNavn("OrgA")
            .medRegistrert(LocalDate.now().minusYears(2L))
            .medOppstart(LocalDate.now().minusYears(1L))
            .oppdatertOpplysningerNå()
            .build();
    }

    private Behandling opprettBehandling() {
        final Personinfo personinfo = new Personinfo.Builder()
            .medNavn("Navn navnesen")
            .medAktørId(AKTØRID)
            .medFødselsdato(I_DAG.minusYears(20).toLocalDate())
            .medLandkode(Landkoder.NOR)
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medPersonIdent(new PersonIdent("12312312312"))
            .medForetrukketSpråk(Språkkode.nb)
            .build();
        final Fagsak fagsak = Fagsak.opprettNy(NavBruker.opprettNy(personinfo));
        fagsakRepository.opprettNy(fagsak);
        final Behandling.Builder builder = Behandling.forFørstegangssøknad(fagsak);
        final Behandling behandling = builder.build();
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));
        return behandling;
    }

    private void avsluttBehandlingOgFagsak(Behandling behandling) {
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.INNVILGET).buildFor(behandling);

        behandlingRepository.lagre(behandling, lås);
        behandlingRepository.lagre(behandlingsresultat, lås);
        BehandlingVedtak behandlingVedtak = BehandlingVedtak.builder()
            .medBehandlingsresultat(behandlingsresultat)
            .medVedtaksdato(I_DAG.minusDays(1).toLocalDate())
            .medAnsvarligSaksbehandler("Nav Navesen")
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .build();
        resultatProvider.getVedtakRepository().lagre(behandlingVedtak, lås);

        repositoryProvider.getBehandlingskontrollRepository().avsluttBehandling(behandling.getId());
    }

    private Behandling opprettRevurderingsbehandling(Behandling opprinneligBehandling) {
        BehandlingType behandlingType = repositoryProvider.getKodeverkRepository().finn(BehandlingType.class, BehandlingType.REVURDERING);
        BehandlingÅrsak.Builder revurderingÅrsak = BehandlingÅrsak.builder(BehandlingÅrsakType.RE_ANNET)
            .medOriginalBehandling(opprinneligBehandling);
        Behandling revurdering = Behandling.fraTidligereBehandling(opprinneligBehandling, behandlingType)
            .medBehandlingÅrsak(revurderingÅrsak).build();
        behandlingRepository.lagre(revurdering, behandlingRepository.taSkriveLås(revurdering));
        return revurdering;
    }

    private Aksjonspunkt opprettAksjonspunkt(Behandling behandling,
                                             AksjonspunktDefinisjon aksjonspunktDefinisjon,
                                             LocalDateTime frist) {

        AksjonspunktRepository aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        Aksjonspunkt aksjonspunkt = aksjonspunktRepository.leggTilAksjonspunkt(behandling, aksjonspunktDefinisjon);
        aksjonspunktRepository.setFrist(aksjonspunkt, frist, Venteårsak.UDEFINERT);
        return aksjonspunkt;
    }

    private InntektArbeidYtelseAggregatBuilder opprettArbeidOgInntektForBehandling(AbstractTestScenario<?> scenario, LocalDate fom, LocalDate tom, boolean harPensjonsgivendeInntekt, boolean skalLageArbeid) {

        VirksomhetEntitet virksomhet = getAndreVirsomhenten();

        repositoryProvider.getVirksomhetRepository().lagre(virksomhet);

        InntektArbeidYtelseAggregatBuilder aggregatBuilder = scenario.getInntektArbeidYtelseScenarioTestBuilder().getKladd();

        if (skalLageArbeid) {
            lagAktørArbeid(aggregatBuilder, scenario.getDefaultBrukerAktørId(), virksomhet, fom, tom, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        }
        for (LocalDate dt = fom; dt.isBefore(tom); dt = dt.plusMonths(1)) {
            lagInntekt(aggregatBuilder, scenario.getDefaultBrukerAktørId(), virksomhet, dt, dt.plusMonths(1), harPensjonsgivendeInntekt);
        }

        return aggregatBuilder;
    }

    private AktørArbeid lagAktørArbeid(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder, AktørId aktørId, Virksomhet virksomhet,
                                       LocalDate fom, LocalDate tom, ArbeidType arbeidType) {
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = inntektArbeidYtelseAggregatBuilder
            .getAktørArbeidBuilder(aktørId);

        Opptjeningsnøkkel opptjeningsnøkkel;
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(virksomhet);

        opptjeningsnøkkel = Opptjeningsnøkkel.forOrgnummer(virksomhet.getOrgnr());


        YrkesaktivitetBuilder yrkesaktivitetBuilder = aktørArbeidBuilder
            .getYrkesaktivitetBuilderForNøkkelAvType(opptjeningsnøkkel, arbeidType);
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();

        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale =
            aktivitetsAvtaleBuilder.medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom));

        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(aktivitetsAvtale)
            .medArbeidType(arbeidType)
            .medArbeidsgiver(arbeidsgiver);

        aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitetBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        return aktørArbeidBuilder.build();
    }

    private void lagInntekt(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder, AktørId aktørId, Virksomhet virksomhet,
                            LocalDate fom, LocalDate tom, boolean harPensjonsgivendeInntekt) {
        Opptjeningsnøkkel opptjeningsnøkkel = Opptjeningsnøkkel.forOrgnummer(virksomhet.getOrgnr());

        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder(aktørId);

        Stream<InntektsKilde> inntektsKildeStream;
        if (harPensjonsgivendeInntekt) {
            inntektsKildeStream = Stream.of(InntektsKilde.INNTEKT_BEREGNING, InntektsKilde.INNTEKT_SAMMENLIGNING, InntektsKilde.INNTEKT_OPPTJENING);
        } else {
            inntektsKildeStream = Stream.of(InntektsKilde.INNTEKT_BEREGNING, InntektsKilde.INNTEKT_SAMMENLIGNING);
        }

        inntektsKildeStream.forEach(kilde -> {
            AktørInntektEntitet.InntektBuilder inntektBuilder = aktørInntektBuilder.getInntektBuilder(kilde, opptjeningsnøkkel);
            InntektEntitet.InntektspostBuilder inntektspost = InntektEntitet.InntektspostBuilder.ny()
                .medBeløp(BigDecimal.valueOf(35000))
                .medPeriode(fom, tom)
                .medInntektspostType(InntektspostType.LØNN);
            inntektBuilder.leggTilInntektspost(inntektspost).medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet));
            aktørInntektBuilder.leggTilInntekt(inntektBuilder);
            inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntektBuilder);
        });
    }
}
