package no.nav.foreldrepenger.domene.arbeidsforhold.impl;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Opptjeningsnøkkel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsmeldingInnsendingsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.VurderArbeidsforholdTjeneste;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class VurderArbeidsforholdTjenesteImplTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now().minusDays(30);
    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = mock(SkjæringstidspunktTjeneste.class);
    private VirksomhetTjeneste virksomhetTjeneste = mock(VirksomhetTjeneste.class);
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, virksomhetTjeneste, skjæringstidspunktTjeneste, null);
    private VurderArbeidsforholdTjeneste tjeneste = new VurderArbeidsforholdTjenesteImpl(inntektArbeidYtelseTjeneste, virksomhetTjeneste);

    @Before
    public void setUp() throws Exception {
        when(skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(any())).thenReturn(SKJÆRINGSTIDSPUNKT);
    }

    @Test
    public void skal_ikke_gi_aksjonspunkt() {
        final ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();

        final Behandling behandling = scenario.lagre(repositoryProvider);

        final InntektArbeidYtelseAggregatBuilder builder = inntektArbeidYtelseTjeneste.opprettBuilderForRegister(behandling);
        final InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder arbeidBuilder = builder.getAktørArbeidBuilder(behandling.getAktørId());
        final YrkesaktivitetBuilder yrkesBuilder = arbeidBuilder.getYrkesaktivitetBuilderForType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        final Arbeidsgiver virksomhet = Arbeidsgiver.virksomhet(opprettVirksomhet("123123123"));
        final ArbeidsforholdRef ref = ArbeidsforholdRef.ref("ref");
        yrkesBuilder.medArbeidsgiver(virksomhet)
            .medArbeidsforholdId(ref);
        final YrkesaktivitetEntitet.AktivitetsAvtaleBuilder avtaleBuilder = yrkesBuilder.getAktivitetsAvtaleBuilder();
        avtaleBuilder.medPeriode(DatoIntervallEntitet.fraOgMed(LocalDate.now().minusYears(1)))
            .medProsentsats(BigDecimal.TEN);
        final YrkesaktivitetEntitet.AktivitetsAvtaleBuilder avtaleBuilder1 = yrkesBuilder.getAktivitetsAvtaleBuilder();
        avtaleBuilder1.medPeriode(DatoIntervallEntitet.fraOgMed(LocalDate.now().minusYears(1)));
        yrkesBuilder.leggTilAktivitetsAvtale(avtaleBuilder).leggTilAktivitetsAvtale(avtaleBuilder1);
        arbeidBuilder.leggTilYrkesaktivitet(yrkesBuilder);
        builder.leggTilAktørArbeid(arbeidBuilder);
        inntektArbeidYtelseTjeneste.lagre(behandling, builder);
        final InntektArbeidYtelseRepository iayrep = sendNyInntektsmelding(behandling, virksomhet, ref);

        Map<Arbeidsgiver, Set<ArbeidsforholdRef>> vurder = tjeneste.vurder(behandling);
        assertThat(vurder).isEmpty();

        avsluttBehandlingOgFagsak(behandling);
        final Behandling revurdering = opprettRevurderingsbehandling(behandling);

        sendInnInntektsmelding(behandling, virksomhet, null, iayrep, revurdering);

        vurder = tjeneste.vurder(revurdering);
        assertThat(vurder).isEmpty();
    }

    private InntektArbeidYtelseRepository sendNyInntektsmelding(Behandling behandling, Arbeidsgiver virksomhet, ArbeidsforholdRef ref) {
        InntektsmeldingBuilder inntektsmeldingBuilder = InntektsmeldingBuilder.builder()
            .medVirksomhet(virksomhet.getVirksomhet())
            .medArbeidsforholdId(ref.getReferanse())
            .medBeløp(BigDecimal.TEN)
            .medStartDatoPermisjon(SKJÆRINGSTIDSPUNKT)
            .medInntektsmeldingaarsak(InntektsmeldingInnsendingsårsak.NY)
            .medInnsendingstidspunkt(LocalDateTime.now())
            .medJournalpostId(new JournalpostId("123"));

        final InntektArbeidYtelseRepository iayrep = repositoryProvider.getInntektArbeidYtelseRepository();
        iayrep.lagre(behandling, inntektsmeldingBuilder.build());
        return iayrep;
    }

    @Test
    public void skal_ikke_gi_aksjonspunkt_2() {
        final ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();

        final Behandling behandling = scenario.lagre(repositoryProvider);

        final InntektArbeidYtelseAggregatBuilder builder = inntektArbeidYtelseTjeneste.opprettBuilderForRegister(behandling);
        final InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder arbeidBuilder = builder.getAktørArbeidBuilder(behandling.getAktørId());
        final YrkesaktivitetBuilder yrkesBuilder = arbeidBuilder.getYrkesaktivitetBuilderForType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        final Arbeidsgiver virksomhet = Arbeidsgiver.virksomhet(opprettVirksomhet("123123123"));
        final ArbeidsforholdRef ref = ArbeidsforholdRef.ref("ref");
        yrkesBuilder.medArbeidsgiver(virksomhet)
            .medArbeidsforholdId(ref);
        final YrkesaktivitetEntitet.AktivitetsAvtaleBuilder avtaleBuilder = yrkesBuilder.getAktivitetsAvtaleBuilder();
        avtaleBuilder.medPeriode(DatoIntervallEntitet.fraOgMed(LocalDate.now().minusYears(1)))
            .medProsentsats(BigDecimal.TEN);
        final YrkesaktivitetEntitet.AktivitetsAvtaleBuilder avtaleBuilder1 = yrkesBuilder.getAktivitetsAvtaleBuilder();
        avtaleBuilder1.medPeriode(DatoIntervallEntitet.fraOgMed(LocalDate.now().minusYears(1)));
        yrkesBuilder.leggTilAktivitetsAvtale(avtaleBuilder)
            .leggTilAktivitetsAvtale(avtaleBuilder1);
        arbeidBuilder.leggTilYrkesaktivitet(yrkesBuilder);
        builder.leggTilAktørArbeid(arbeidBuilder);
        inntektArbeidYtelseTjeneste.lagre(behandling, builder);

        final InntektArbeidYtelseRepository iayrep = sendNyInntektsmelding(behandling, virksomhet, ref);

        Map<Arbeidsgiver, Set<ArbeidsforholdRef>> vurder = tjeneste.vurder(behandling);
        assertThat(vurder).isEmpty();

        avsluttBehandlingOgFagsak(behandling);
        final Behandling revurdering = opprettRevurderingsbehandling(behandling);

        sendInnInntektsmelding(behandling, virksomhet, ref, iayrep, revurdering);

        vurder = tjeneste.vurder(revurdering);
        assertThat(vurder).isEmpty();
    }

    @Test
    public void skal_ikke_gi_aksjonspunkt_3() {
        final ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        scenario.removeDodgyDefaultInntektArbeidYTelse();
        final Behandling behandling = scenario.lagre(repositoryProvider);

        final InntektArbeidYtelseAggregatBuilder builder = inntektArbeidYtelseTjeneste.opprettBuilderForRegister(behandling);
        final InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder arbeidBuilder = builder.getAktørArbeidBuilder(behandling.getAktørId());
        final String orgnummer = "123123123";
        final Arbeidsgiver virksomhet = Arbeidsgiver.virksomhet(opprettVirksomhet(orgnummer));
        final ArbeidsforholdRef ref = ArbeidsforholdRef.ref("ref1");
        final YrkesaktivitetBuilder yrkesBuilder = arbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(Opptjeningsnøkkel.forArbeidsforholdIdMedArbeidgiver(ref, virksomhet), ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        yrkesBuilder.medArbeidsgiver(virksomhet)
            .medArbeidsforholdId(ref);
        final YrkesaktivitetEntitet.AktivitetsAvtaleBuilder avtaleBuilder = yrkesBuilder.getAktivitetsAvtaleBuilder();
        avtaleBuilder.medPeriode(DatoIntervallEntitet.fraOgMed(LocalDate.now().minusYears(1)))
            .medProsentsats(BigDecimal.TEN);
        final YrkesaktivitetEntitet.AktivitetsAvtaleBuilder avtaleBuilder3 = yrkesBuilder.getAktivitetsAvtaleBuilder();
        avtaleBuilder3.medPeriode(DatoIntervallEntitet.fraOgMed(LocalDate.now().minusYears(1)));
        yrkesBuilder.leggTilAktivitetsAvtale(avtaleBuilder).leggTilAktivitetsAvtale(avtaleBuilder3);
        arbeidBuilder.leggTilYrkesaktivitet(yrkesBuilder);
        final ArbeidsforholdRef ref1 = ArbeidsforholdRef.ref("ref2");
        final YrkesaktivitetBuilder yrkesBuilder1 = arbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(Opptjeningsnøkkel.forArbeidsforholdIdMedArbeidgiver(ref1, virksomhet), ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        yrkesBuilder1.medArbeidsgiver(virksomhet)
            .medArbeidsforholdId(ref1);
        final YrkesaktivitetEntitet.AktivitetsAvtaleBuilder avtaleBuilder1 = yrkesBuilder1.getAktivitetsAvtaleBuilder();
        avtaleBuilder1.medPeriode(DatoIntervallEntitet.fraOgMed(LocalDate.now().minusYears(1)))
            .medProsentsats(BigDecimal.TEN);
        final YrkesaktivitetEntitet.AktivitetsAvtaleBuilder avtaleBuilder2 = yrkesBuilder1.getAktivitetsAvtaleBuilder();
        avtaleBuilder2.medPeriode(DatoIntervallEntitet.fraOgMed(LocalDate.now().minusYears(1)));
        yrkesBuilder1.leggTilAktivitetsAvtale(avtaleBuilder1).leggTilAktivitetsAvtale(avtaleBuilder2);
        arbeidBuilder.leggTilYrkesaktivitet(yrkesBuilder1);
        builder.leggTilAktørArbeid(arbeidBuilder);
        inntektArbeidYtelseTjeneste.lagre(behandling, builder);

        sendNyInntektsmelding(behandling, virksomhet, ref);
        sendNyInntektsmelding(behandling, virksomhet, ref1);

        Map<Arbeidsgiver, Set<ArbeidsforholdRef>> vurder = tjeneste.vurder(behandling);
        assertThat(vurder).isEmpty();
    }

    private void sendInnInntektsmelding(Behandling behandling, Arbeidsgiver virksomhet, ArbeidsforholdRef ref, InntektArbeidYtelseRepository iayrep, Behandling revurdering) {
        InntektsmeldingBuilder inntektsmeldingBuilder;

        inntektsmeldingBuilder = InntektsmeldingBuilder.builder()
            .medVirksomhet(virksomhet.getVirksomhet())
            .medArbeidsforholdId(ref != null ? ref.getReferanse() : null)
            .medBeløp(BigDecimal.TEN)
            .medStartDatoPermisjon(SKJÆRINGSTIDSPUNKT)
            .medInntektsmeldingaarsak(InntektsmeldingInnsendingsårsak.ENDRING)
            .medInnsendingstidspunkt(LocalDateTime.now())
            .medJournalpostId(new JournalpostId("123"));

        iayrep.lagre(revurdering, inntektsmeldingBuilder.build());
    }

    private void avsluttBehandlingOgFagsak(Behandling behandling) {
        BehandlingLås lås = repositoryProvider.getBehandlingRepository().taSkriveLås(behandling);
        Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.INNVILGET).buildFor(behandling);
        behandling.avsluttBehandling();
        repositoryProvider.getBehandlingRepository().lagre(behandling, lås);
        BehandlingVedtak behandlingVedtak = BehandlingVedtak.builder()
            .medBehandlingsresultat(behandling.getBehandlingsresultat())
            .medVedtaksdato(LocalDate.now().minusDays(1))
            .medAnsvarligSaksbehandler("Nav Navesen")
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .build();
        repositoryProvider.getBehandlingVedtakRepository().lagre(behandlingVedtak, lås);
        FagsakRepository fagsakRepository = repositoryProvider.getFagsakRepository();
        fagsakRepository.oppdaterFagsakStatus(behandling.getFagsakId(), FagsakStatus.LØPENDE);
    }


    private Behandling opprettRevurderingsbehandling(Behandling opprinneligBehandling) {
        BehandlingType behandlingType = repositoryProvider.getKodeverkRepository().finn(BehandlingType.class, BehandlingType.REVURDERING);
        BehandlingÅrsak.Builder revurderingÅrsak = BehandlingÅrsak.builder(BehandlingÅrsakType.RE_ANNET)
            .medOriginalBehandling(opprinneligBehandling);
        Behandling revurdering = Behandling.fraTidligereBehandling(opprinneligBehandling, behandlingType)
            .medBehandlingÅrsak(revurderingÅrsak).build();
        repositoryProvider.getBehandlingRepository().lagre(revurdering, repositoryProvider.getBehandlingRepository().taSkriveLås(revurdering));
        repositoryProvider.getInntektArbeidYtelseRepository().kopierGrunnlagFraEksisterendeBehandling(opprinneligBehandling, revurdering);
        return revurdering;
    }

    private Virksomhet opprettVirksomhet(String orgnummer) {
        final VirksomhetEntitet.Builder builder = new VirksomhetEntitet.Builder();
        builder.medOrgnr(orgnummer)
            .medNavn("Bedrift " + orgnummer)
        .oppdatertOpplysningerNå();
        final VirksomhetEntitet build = builder.build();
        repositoryRule.getEntityManager().persist(build);
        when(virksomhetTjeneste.finnOrganisasjon(orgnummer)).thenReturn(Optional.ofNullable(build));
        return build;
    }
}
