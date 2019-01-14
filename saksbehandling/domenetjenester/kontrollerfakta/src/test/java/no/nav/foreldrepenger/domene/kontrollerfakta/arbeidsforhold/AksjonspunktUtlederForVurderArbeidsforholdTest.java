package no.nav.foreldrepenger.domene.kontrollerfakta.arbeidsforhold;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingskontrollRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.AktørInntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Opptjeningsnøkkel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsmeldingInnsendingsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektspostType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingskontrollRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.VurderArbeidsforholdTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.AksjonspunktutlederForVurderOpptjening;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.InntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.VurderArbeidsforholdTjenesteImpl;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;
import no.nav.foreldrepenger.domene.virksomhet.impl.VirksomhetTjenesteImpl;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.util.FPDateUtil;

@RunWith(CdiRunner.class)
public class AksjonspunktUtlederForVurderArbeidsforholdTest {
    private final SkjæringstidspunktTjeneste mock = mock(SkjæringstidspunktTjeneste.class);
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
    private VirksomhetTjeneste virksomhetTjeneste = new VirksomhetTjenesteImpl(null, repositoryProvider.getVirksomhetRepository());
    private AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, resultatRepositoryProvider, mock);
    private InntektArbeidYtelseTjeneste iayTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, virksomhetTjeneste, mock, apOpptjening);
    private BehandlingskontrollRepository behandlingskontrollRepository = new BehandlingskontrollRepositoryImpl(repositoryProvider, repoRule.getEntityManager());
    private VurderArbeidsforholdTjeneste tjeneste = new VurderArbeidsforholdTjenesteImpl(iayTjeneste, virksomhetTjeneste, behandlingskontrollRepository);

    
    @Spy
    private AksjonspunktUtlederForVurderArbeidsforhold utleder = new AksjonspunktUtlederForVurderArbeidsforhold(tjeneste);

    @Test
    public void skal_få_aksjonspunkt_når_det_finnes_inntekt() {
        // Arrange
        AktørId aktørId1 = new AktørId("123");
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør().medBruker(aktørId1, NavBrukerKjønn.KVINNE);
        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);

        opprettInntekt(aktørId1, behandling, opprettVirksomhet(), "99999");

        // Act
        List<AksjonspunktResultat> aksjonspunktResultater = utleder.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(aksjonspunktResultater).hasSize(1);
        assertThat(aksjonspunktResultater.get(0).getAksjonspunktDefinisjon()).isEqualTo(AksjonspunktDefinisjon.VURDER_ARBEIDSFORHOLD);
    }

    @Test
    public void skal_ikke_få_aksjonspunkt_når_det_ikke_finnes_inntekt_eller_arbeidsforhold() {
        // Arrange
        AktørId aktørId1 = new AktørId("123");
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør().medBruker(aktørId1, NavBrukerKjønn.KVINNE);
        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        // Act
        List<AksjonspunktResultat> aksjonspunktResultater = utleder.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(aksjonspunktResultater).isEmpty();
    }

    @Test
    public void skal_få_aksjonspunkt_når_mottatt_inntektsmelding_men_ikke_arbeidsforhold() {
        // Arrange
        AktørId aktørId1 = new AktørId("123");
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør().medBruker(aktørId1, NavBrukerKjønn.KVINNE);

        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        Virksomhet virksomhet = opprettVirksomhet();
        sendInnInntektsmeldingPå(behandling, virksomhet, "123");

        // Act
        List<AksjonspunktResultat> aksjonspunktResultater = utleder.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(aksjonspunktResultater).isNotEmpty();
    }

    @Test
    public void skal_ikke_få_aksjonspunkt_ved_komplett_søknad_med_inntektsmeldinger() {
        // Arrange
        AktørId aktørId1 = new AktørId("123");
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør().medBruker(aktørId1, NavBrukerKjønn.KVINNE);
        scenario.medBehandlingStegStart(BehandlingStegType.KONTROLLER_FAKTA);
        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        Virksomhet virksomhet = opprettVirksomhet();
        final String arbeidsforholdId = "1234";

        final InntektArbeidYtelseAggregatBuilder builder = repositoryProvider.getInntektArbeidYtelseRepository().opprettBuilderFor(behandling, VersjonType.REGISTER);
        leggTilArbeidsforholdPåBehandling(behandling, virksomhet, arbeidsforholdId, builder);
        repositoryProvider.getInntektArbeidYtelseRepository().lagre(behandling, builder);

        sendInnInntektsmeldingPå(behandling, virksomhet, arbeidsforholdId);

        // Act
        List<AksjonspunktResultat> aksjonspunktResultater = utleder.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(aksjonspunktResultater).isEmpty();
    }

    @Test
    public void skal_få_aksjonspunkt_ved_inntektsmelding_som_er_ukjent() {
        // Arrange
        AktørId aktørId1 = new AktørId("123");
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør().medBruker(aktørId1, NavBrukerKjønn.KVINNE);
        scenario.medBehandlingStegStart(BehandlingStegType.KONTROLLER_FAKTA);
        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        Virksomhet virksomhet = opprettVirksomhet();
        final String arbeidsforholdId = "1234";

        final InntektArbeidYtelseAggregatBuilder builder = repositoryProvider.getInntektArbeidYtelseRepository().opprettBuilderFor(behandling, VersjonType.REGISTER);
        leggTilArbeidsforholdPåBehandling(behandling, virksomhet, arbeidsforholdId, builder);
        repositoryProvider.getInntektArbeidYtelseRepository().lagre(behandling, builder);
        final InntektArbeidYtelseAggregatBuilder builder2 = repositoryProvider.getInntektArbeidYtelseRepository().opprettBuilderFor(behandling, VersjonType.REGISTER);
        leggTilArbeidsforholdPåBehandling(behandling, virksomhet, arbeidsforholdId, builder2);
        final String arbeidsforholdId1 = "0981348901234";
        leggTilArbeidsforholdPåBehandling(behandling, virksomhet, arbeidsforholdId1, builder2);
        repositoryProvider.getInntektArbeidYtelseRepository().lagre(behandling, builder2);

        sendInnInntektsmeldingPå(behandling, virksomhet, arbeidsforholdId);
        sendInnInntektsmeldingPå(behandling, virksomhet, arbeidsforholdId1); // Kommer inntektsmelding på arbeidsforhold vi ikke kjenner før STP

        // Act
        List<AksjonspunktResultat> aksjonspunktResultater = utleder.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(aksjonspunktResultater).isNotEmpty();
    }

    private void leggTilArbeidsforholdPåBehandling(Behandling behandling, Virksomhet virksomhet, String arbeidsforholdId, InntektArbeidYtelseAggregatBuilder builder) {
        final ArbeidsforholdRef ref = ArbeidsforholdRef.ref(arbeidsforholdId);
        final Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(virksomhet);
        final InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder arbeidBuilder = builder.getAktørArbeidBuilder(behandling.getAktørId());
        final Opptjeningsnøkkel nøkkel = Opptjeningsnøkkel.forArbeidsforholdIdMedArbeidgiver(ref, arbeidsgiver);
        final YrkesaktivitetBuilder yrkesaktivitetBuilderForType = arbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(nøkkel,
            ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        yrkesaktivitetBuilderForType
            .medArbeidsgiver(arbeidsgiver)
            .medArbeidsforholdId(ref)
            .leggTilAktivitetsAvtale(yrkesaktivitetBuilderForType
                .getAktivitetsAvtaleBuilder(DatoIntervallEntitet.fraOgMed(LocalDate.now().minusMonths(3)), false)
                .medProsentsats(BigDecimal.valueOf(100))
                .medAntallTimer(BigDecimal.valueOf(40))
                .medAntallTimerFulltid(BigDecimal.valueOf(40)))
            .leggTilAktivitetsAvtale(yrkesaktivitetBuilderForType
                .getAktivitetsAvtaleBuilder(DatoIntervallEntitet.fraOgMed(LocalDate.now().minusMonths(3)), true));
        arbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitetBuilderForType);
        builder.leggTilAktørArbeid(arbeidBuilder);
    }

    private Virksomhet opprettVirksomhet() {
        String orgNr = "21542512";
        final VirksomhetRepository virksomhetRepository = repositoryProvider.getVirksomhetRepository();

        final Optional<Virksomhet> hent = virksomhetRepository.hent(orgNr);
        if (hent.isPresent()) {
            return hent.get();
        }
        String orgNavn = "Sopra Steria";
        LocalDate virksomhetRegistrert = LocalDate.now(FPDateUtil.getOffset()).minusYears(3L).minusYears(2L);
        LocalDate virksomhetOppstart = LocalDate.now(FPDateUtil.getOffset()).minusYears(3L).minusYears(1L);
        Virksomhet virksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr(orgNr)
            .medNavn(orgNavn)
            .medRegistrert(virksomhetRegistrert)
            .medOppstart(virksomhetOppstart)
            .oppdatertOpplysningerNå()
            .build();
        virksomhetRepository.lagre(virksomhet);
        return virksomhet;
    }

    private void sendInnInntektsmeldingPå(Behandling behandling, Virksomhet virksomhet, String arbeidsforholdId) {
        final InntektsmeldingBuilder inntektsmeldingBuilder = InntektsmeldingBuilder.builder()
            .medVirksomhet(virksomhet)
            .medInnsendingstidspunkt(LocalDateTime.now())
            .medArbeidsforholdId(arbeidsforholdId)
            .medJournalpostId(new JournalpostId("2"))
            .medBeløp(BigDecimal.TEN)
            .medStartDatoPermisjon(LocalDate.now())
            .medNærRelasjon(false)
            .medInntektsmeldingaarsak(InntektsmeldingInnsendingsårsak.NY);
        repositoryProvider.getInntektArbeidYtelseRepository().lagre(behandling, inntektsmeldingBuilder.build());
    }

    private void opprettInntekt(AktørId aktørId1, Behandling behandling, Virksomhet virksomhet, String arbeidsforholdRef) {
        InntektArbeidYtelseAggregatBuilder builder = inntektArbeidYtelseRepository.opprettBuilderFor(behandling, VersjonType.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder inntektBuilder = builder.getAktørInntektBuilder(aktørId1);
        final Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(virksomhet);
        final Opptjeningsnøkkel opptjeningsnøkkel = Opptjeningsnøkkel.forArbeidsforholdIdMedArbeidgiver(ArbeidsforholdRef.ref(arbeidsforholdRef), arbeidsgiver);
        AktørInntektEntitet.InntektBuilder tilInntektspost = inntektBuilder.getInntektBuilder(InntektsKilde.INNTEKT_OPPTJENING, opptjeningsnøkkel);
        tilInntektspost.medArbeidsgiver(arbeidsgiver);
        InntektEntitet.InntektspostBuilder inntektspostBuilder = tilInntektspost.getInntektspostBuilder();

        InntektEntitet.InntektspostBuilder inntektspost = inntektspostBuilder
            .medBeløp(BigDecimal.TEN)
            .medPeriode(LocalDate.now().minusMonths(1), LocalDate.now())
            .medInntektspostType(InntektspostType.LØNN);

        tilInntektspost
            .leggTilInntektspost(inntektspost)
            .medInntektsKilde(InntektsKilde.INNTEKT_OPPTJENING);

        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntekt = inntektBuilder
            .leggTilInntekt(tilInntektspost);

        builder.leggTilAktørInntekt(aktørInntekt);

        inntektArbeidYtelseRepository.lagre(behandling, builder);
    }
}
