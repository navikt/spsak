package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Period;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.RegisterInnhentingIntervallEndringTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Permisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.GraderingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.UtsettelsePeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.PermisjonsbeskrivelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.AksjonspunktutlederForVurderOpptjening;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.InntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.uttak.UttakArbeidTjeneste;
import no.nav.foreldrepenger.domene.uttak.UttakArbeidTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.UttakStillingsprosentTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.BeregnMorsMaksdatoTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.RelatertBehandlingTjenesteImpl;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ArbeidTidslinje;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class ArbeidTidslinjeTjenesteImplTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider, new BeregnMorsMaksdatoTjenesteImpl(repositoryProvider, new RelatertBehandlingTjenesteImpl(repositoryProvider)),
        new RegisterInnhentingIntervallEndringTjeneste(Period.of(1, 0, 0), Period.of(0, 4, 0)),
        Period.of(0, 3, 0),
        Period.of(0, 10, 0));
    private AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, skjæringstidspunktTjeneste);

    @Test
    public void hent_riktig_arbeidstidprosent_selvstendig_næringsdrivende() {
        LocalDate fom = LocalDate.of(2018, Month.MAY, 28);

        String aktørId = "123";
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(new AktørId(aktørId));

        String orgnr = "456";
        VirksomhetEntitet virksomhet = new VirksomhetEntitet.Builder().oppdatertOpplysningerNå().medOrgnr(orgnr).build();
        repositoryProvider.getVirksomhetRepository().lagre(virksomhet);

        BigDecimal arbeidsprosent = BigDecimal.valueOf(30);
        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, fom.plusWeeks(5))
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medArbeidsprosent(arbeidsprosent)
            .medErArbeidstaker(false)
            .medVirksomhet(virksomhet)
            .build();

        scenario.medFordeling(new OppgittFordelingEntitet(Collections.singletonList(oppgittPeriode), true));

        Behandling behandling = scenario.lagre(repositoryProvider);

        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilSelvNæringdrivende(virksomhet);
        Map<AktivitetIdentifikator, ArbeidTidslinje> resultat = tjeneste(beregningsandelTjeneste).lagTidslinjer(behandling);

        assertThat(resultat).isNotNull();
        assertThat(resultat.keySet().iterator().hasNext()).isTrue();
        //key

        AktivitetIdentifikator id = resultat.keySet().iterator().next();
        assertThat(id.getAktivitetType()).isEqualTo(AktivitetType.SELVSTENDIG_NÆRINGSDRIVENDE);
        Optional<BigDecimal> arbeidsprosentResultat = resultat.get(id).getArbeidsprosent(new LukketPeriode(oppgittPeriode.getFom(), oppgittPeriode.getTom()));
        assertThat(arbeidsprosentResultat).isPresent();
        assertThat(arbeidsprosentResultat.get()).isEqualTo(arbeidsprosent);
    }

    @Test
    public void hent_riktig_arbeidstidprosent_frilans() {
        LocalDate fom = LocalDate.of(2018, Month.MAY, 28);
        BigDecimal arbeidsprosent = BigDecimal.valueOf(50);

        String aktørId = "123";
        final AktørId aktørId1 = new AktørId(aktørId);
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(aktørId1);

        YrkesaktivitetBuilder yrkesaktivitet = YrkesaktivitetBuilder.oppdatere(Optional.empty())
            .medArbeidsgiver(Arbeidsgiver.person(aktørId1))
            .medArbeidType(ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER);
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeid = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty())
            .leggTilYrkesaktivitet(yrkesaktivitet)
            .medAktørId(aktørId1);

        scenario.getInntektArbeidYtelseScenarioTestBuilder().getKladd().leggTilAktørArbeid(aktørArbeid);

        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, fom.plusWeeks(5))
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medArbeidsprosent(arbeidsprosent)
            .medErArbeidstaker(false)
            .medVirksomhet(null)
            .build();

        scenario.medFordeling(new OppgittFordelingEntitet(Collections.singletonList(oppgittPeriode), true));

        Behandling behandling = scenario.lagre(repositoryProvider);

        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilFrilans();

        Map<AktivitetIdentifikator, ArbeidTidslinje> resultat = tjeneste(beregningsandelTjeneste).lagTidslinjer(behandling);

        assertThat(resultat).isNotNull();
        assertThat(resultat.keySet().iterator().hasNext()).isTrue();
        //key

        AktivitetIdentifikator id = resultat.keySet().iterator().next();
        assertThat(id.getAktivitetType()).isEqualTo(AktivitetType.FRILANS);
        Optional<BigDecimal> arbeidsprosentResultat = resultat.get(id).getArbeidsprosent(new LukketPeriode(oppgittPeriode.getFom(), oppgittPeriode.getTom()));
        assertThat(arbeidsprosentResultat).isPresent();
        assertThat(arbeidsprosentResultat.get()).isEqualTo(arbeidsprosent);
    }

    @Test
    public void hent_riktig_arbeidstidsprosent_ordinærtarbeid() {
        LocalDate gradertFom = LocalDate.of(2018, Month.MAY, 28);
        LocalDate gradertTom = gradertFom.plusWeeks(2);
        LocalDate uGradertFom = gradertTom.plusDays(1);
        LocalDate uGradertTom = uGradertFom.plusWeeks(3);
        String orgnr = "12312314";
        VirksomhetEntitet virksomhet = virksomhet(orgnr);
        final Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(virksomhet);
        String aktørId = "123";
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(new AktørId(aktørId));
        BigDecimal arbeidsprosent = BigDecimal.valueOf(50);
        OppgittPeriode gradertPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(gradertFom, gradertTom)
            .medArbeidsprosent(arbeidsprosent)
            .medVirksomhet(virksomhet)
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medErArbeidstaker(true)
            .build();

        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(uGradertFom, uGradertTom)
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medErArbeidstaker(true)
            .build();

        LocalDate utsettelseFom = uGradertTom.plusDays(1);
        LocalDate utsettelseTom = utsettelseFom.plusWeeks(2);

        OppgittPeriode utsettelsePeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(utsettelseFom, utsettelseTom)
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medÅrsak(UtsettelseÅrsak.ARBEID)
            .medVirksomhet(virksomhet)
            .medErArbeidstaker(true)
            .build();

        scenario.medFordeling(new OppgittFordelingEntitet(Arrays.asList(gradertPeriode, oppgittPeriode, utsettelsePeriode), true));
        Behandling behandling = scenario.lagre(repositoryProvider);

        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, null);

        InntektArbeidYtelseAggregatBuilder builder = InntektArbeidYtelseAggregatBuilder
            .oppdatere(Optional.empty(), VersjonType.REGISTER);

        LocalDate fraOgMed = LocalDate.of(2018, Month.MAY, 1);
        LocalDate tilOgMed = LocalDate.of(2019, Month.MAY, 1);

        YrkesaktivitetBuilder yrkesaktivitetBuilder = YrkesaktivitetBuilder.oppdatere(Optional.empty());
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();
        BigDecimal stillingsprosent = BigDecimal.valueOf(50);
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale = aktivitetsAvtaleBuilder
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fraOgMed, tilOgMed))
            .medProsentsats(stillingsprosent)
            .medAntallTimer(BigDecimal.valueOf(20.4d))
            .medAntallTimerFulltid(BigDecimal.valueOf(10.2d));

        yrkesaktivitetBuilder
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .medArbeidsgiver(arbeidsgiver)
            .leggTilAktivitetsAvtale(aktivitetsAvtale);

        BigDecimal permisjonsprosent = BigDecimal.valueOf(50);
        Permisjon permisjon = YrkesaktivitetBuilder.oppdatere(Optional.empty()).getPermisjonBuilder()
            .medProsentsats(permisjonsprosent)
            .medPeriode(uGradertFom, uGradertTom)
            .medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.UDEFINERT)
            .build();

        yrkesaktivitetBuilder.leggTilPermisjon(permisjon);

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = builder.getAktørArbeidBuilder(behandling.getAktørId());

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeid = aktørArbeidBuilder
            .leggTilYrkesaktivitet(yrkesaktivitetBuilder);

        builder.leggTilAktørArbeid(aktørArbeid);

        repositoryProvider.getInntektArbeidYtelseRepository().lagre(behandling, builder);


        Map<AktivitetIdentifikator, ArbeidTidslinje> resultat = tjeneste(beregningsandelTjeneste).lagTidslinjer(behandling);

        assertThat(resultat).isNotNull();
        assertThat(resultat).hasSize(1);
        assertThat(resultat.keySet().iterator().hasNext()).isTrue();
        AktivitetIdentifikator id = resultat.keySet().iterator().next();
        assertThat(id.getAktivitetType()).isEqualTo(AktivitetType.ARBEID);
        Optional<BigDecimal> arbeidsprosentResultat = resultat.get(id).getArbeidsprosent(new LukketPeriode(gradertFom, gradertTom));
        assertThat(arbeidsprosentResultat).isPresent();
        assertThat(arbeidsprosentResultat.get()).isEqualTo(arbeidsprosent);
        arbeidsprosentResultat = resultat.get(id).getArbeidsprosent(new LukketPeriode(uGradertFom, uGradertTom));
        assertThat(arbeidsprosentResultat).isPresent();
        assertThat(arbeidsprosentResultat.get()).isEqualTo(new BigDecimal("25.00"));
        arbeidsprosentResultat = resultat.get(id).getArbeidsprosent(new LukketPeriode(utsettelsePeriode.getFom(), utsettelsePeriode.getTom()));
        assertThat(arbeidsprosentResultat).isPresent();
        assertThat(arbeidsprosentResultat.get()).isEqualTo(BigDecimal.valueOf(50));
    }

    @Test
    public void skalSetteArbeidstidsprosentLikStillingsprosentVedSøktUtsettelsePgaArbeid() {
        VirksomhetEntitet virksomhet = virksomhet("12312314");
        final Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(virksomhet);
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(new AktørId("123"));
        LocalDate fom = LocalDate.of(2018, 1, 1);
        LocalDate tom = LocalDate.of(2018, 1, 5);
        OppgittPeriode oppgittUtsettelse = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medVirksomhet(virksomhet)
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medÅrsak(UtsettelseÅrsak.ARBEID)
            .build();

        scenario.medFordeling(new OppgittFordelingEntitet(Collections.singletonList(oppgittUtsettelse), true));
        Behandling behandling = scenario.lagre(repositoryProvider);

        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, null);

        InntektArbeidYtelseAggregatBuilder builder = InntektArbeidYtelseAggregatBuilder
            .oppdatere(Optional.empty(), VersjonType.REGISTER);

        LocalDate fraOgMed = LocalDate.of(2017, 1, 1);

        YrkesaktivitetBuilder yrkesaktivitetBuilder = YrkesaktivitetBuilder.oppdatere(Optional.empty());
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();
        BigDecimal stillingsprosent = BigDecimal.valueOf(50);
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale = aktivitetsAvtaleBuilder
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fraOgMed, fraOgMed.plusYears(5)))
            .medProsentsats(stillingsprosent)
            .medAntallTimer(BigDecimal.valueOf(40))
            .medAntallTimerFulltid(BigDecimal.valueOf(40));
        yrkesaktivitetBuilder
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .medArbeidsgiver(arbeidsgiver)
            .leggTilAktivitetsAvtale(aktivitetsAvtale);
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = builder.getAktørArbeidBuilder(behandling.getAktørId());
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeid = aktørArbeidBuilder
            .leggTilYrkesaktivitet(yrkesaktivitetBuilder);
        builder.leggTilAktørArbeid(aktørArbeid);

        repositoryProvider.getInntektArbeidYtelseRepository().lagre(behandling, builder);


        Map<AktivitetIdentifikator, ArbeidTidslinje> resultat = tjeneste(beregningsandelTjeneste).lagTidslinjer(behandling);

        assertThat(resultat).hasSize(1);
        assertThat(resultat.keySet().iterator().hasNext()).isTrue();
        AktivitetIdentifikator id = resultat.keySet().iterator().next();
        assertThat(id.getAktivitetType()).isEqualTo(AktivitetType.ARBEID);
        Optional<BigDecimal> arbeidsprosentResultat = resultat.get(id).getArbeidsprosent(new LukketPeriode(fom, tom));
        assertThat(arbeidsprosentResultat).isPresent();
        assertThat(arbeidsprosentResultat.get()).isEqualTo(stillingsprosent);
    }

    @Test
    public void skalSetteArbeidstidsprosentLikStillingsprosentUtenSøktUtsettelsePgaArbeidHvisInntektsmeldingInneholderUtsettelsePgaArbeid() {
        VirksomhetEntitet virksomhet = virksomhet("12312314");
        final Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(virksomhet);
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(new AktørId("123"));
        LocalDate fom = LocalDate.of(2018, 1, 1);
        LocalDate tom = LocalDate.of(2018, 1, 5);
        OppgittPeriode oppgittUtsettelse = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .build();

        scenario.medFordeling(new OppgittFordelingEntitet(Collections.singletonList(oppgittUtsettelse), true));
        Behandling behandling = scenario.lagre(repositoryProvider);

        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, null);

        InntektArbeidYtelseAggregatBuilder builder = InntektArbeidYtelseAggregatBuilder
            .oppdatere(Optional.empty(), VersjonType.REGISTER);

        LocalDate fraOgMed = LocalDate.of(2017, 1, 1);

        YrkesaktivitetBuilder yrkesaktivitetBuilder = YrkesaktivitetBuilder.oppdatere(Optional.empty());
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();
        BigDecimal stillingsprosent = BigDecimal.valueOf(50);
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale = aktivitetsAvtaleBuilder
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fraOgMed, fraOgMed.plusYears(5)))
            .medProsentsats(stillingsprosent)
            .medAntallTimer(BigDecimal.valueOf(40))
            .medAntallTimerFulltid(BigDecimal.valueOf(40));
        yrkesaktivitetBuilder
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .medArbeidsgiver(arbeidsgiver)
            .leggTilAktivitetsAvtale(aktivitetsAvtale);
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = builder.getAktørArbeidBuilder(behandling.getAktørId());
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeid = aktørArbeidBuilder
            .leggTilYrkesaktivitet(yrkesaktivitetBuilder);
        builder.leggTilAktørArbeid(aktørArbeid);

        repositoryProvider.getInntektArbeidYtelseRepository().lagre(behandling, builder);

        lagreInntektsmeldingMedUtsettelsePgaArbeid(behandling, fom, tom, virksomhet);

        Map<AktivitetIdentifikator, ArbeidTidslinje> resultat = tjeneste(beregningsandelTjeneste).lagTidslinjer(behandling);

        assertThat(resultat).hasSize(1);
        assertThat(resultat.keySet().iterator().hasNext()).isTrue();
        AktivitetIdentifikator id = resultat.keySet().iterator().next();
        assertThat(id.getAktivitetType()).isEqualTo(AktivitetType.ARBEID);
        Optional<BigDecimal> arbeidsprosentResultat = resultat.get(id).getArbeidsprosent(new LukketPeriode(fom, tom));
        assertThat(arbeidsprosentResultat).isPresent();
        assertThat(arbeidsprosentResultat.get()).isEqualTo(stillingsprosent);
    }

    @Test
    public void permisjonsprosent_over_100_skal_føre_til_() {
        LocalDate fom = LocalDate.of(2018, Month.MAY, 28);
        LocalDate tom = fom.plusWeeks(6).minusDays(1);
        String orgnr = "12312314";
        VirksomhetEntitet virksomhet = virksomhet(orgnr);
        final Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(virksomhet);
        String aktørId = "123";
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(new AktørId(aktørId));

        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medErArbeidstaker(true)
            .build();


        scenario.medFordeling(new OppgittFordelingEntitet(Collections.singletonList(oppgittPeriode), true));
        Behandling behandling = scenario.lagre(repositoryProvider);

        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, null);

        InntektArbeidYtelseAggregatBuilder builder = InntektArbeidYtelseAggregatBuilder
            .oppdatere(Optional.empty(), VersjonType.REGISTER);

        LocalDate fraOgMed = LocalDate.of(2018, Month.MAY, 1);
        LocalDate tilOgMed = LocalDate.of(2019, Month.MAY, 1);

        YrkesaktivitetBuilder yrkesaktivitetBuilder = YrkesaktivitetBuilder.oppdatere(Optional.empty());
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();
        BigDecimal stillingsprosent = BigDecimal.valueOf(50);
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale = aktivitetsAvtaleBuilder
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fraOgMed, tilOgMed))
            .medProsentsats(stillingsprosent)
            .medAntallTimer(BigDecimal.valueOf(20L))
            .medAntallTimerFulltid(BigDecimal.valueOf(40L));


        yrkesaktivitetBuilder
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .medArbeidsgiver(arbeidsgiver)
            .leggTilAktivitetsAvtale(aktivitetsAvtale);

        BigDecimal permisjonsprosent = BigDecimal.valueOf(120);
        Permisjon permisjon = YrkesaktivitetBuilder.oppdatere(Optional.empty()).getPermisjonBuilder()
            .medProsentsats(permisjonsprosent)
            .medPeriode(fom, tom)
            .medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.UDEFINERT)
            .build();

        yrkesaktivitetBuilder.leggTilPermisjon(permisjon);

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = builder.getAktørArbeidBuilder(behandling.getAktørId());

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeid = aktørArbeidBuilder
            .leggTilYrkesaktivitet(yrkesaktivitetBuilder);

        builder.leggTilAktørArbeid(aktørArbeid);

        repositoryProvider.getInntektArbeidYtelseRepository().lagre(behandling, builder);


        Map<AktivitetIdentifikator, ArbeidTidslinje> resultat = tjeneste(beregningsandelTjeneste).lagTidslinjer(behandling);

        assertThat(resultat).isNotNull();
        assertThat(resultat).hasSize(1);
        assertThat(resultat.keySet().iterator().hasNext()).isTrue();
        AktivitetIdentifikator id = resultat.keySet().iterator().next();
        assertThat(id.getAktivitetType()).isEqualTo(AktivitetType.ARBEID);
        Optional<BigDecimal> arbeidsprosentResultat = resultat.get(id).getArbeidsprosent(new LukketPeriode(fom, tom));
        assertThat(arbeidsprosentResultat).isPresent();
        assertThat(arbeidsprosentResultat.get()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void hent_riktig_arbeidstidsprosent_fra_inntektsmelding_ved_gradering_når_ikke_permisjon() {
        LocalDate fom = LocalDate.now();
        LocalDate tom = fom.plusWeeks(3);
        String orgnr = "12312314";
        VirksomhetEntitet virksomhet = virksomhet(orgnr);
        final Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(virksomhet);
        String aktørId = "123";
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(new AktørId(aktørId));
        BigDecimal arbeidsprosent = BigDecimal.valueOf(50);

        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .build();

        scenario.medFordeling(new OppgittFordelingEntitet(Collections.singletonList(oppgittPeriode), true));
        Behandling behandling = scenario.lagre(repositoryProvider);

        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, null);

        lagreIAYGrunnlag(fom, tom, arbeidsgiver, behandling);
        lagreInntektsmeldingMedGradering(behandling, fom, tom, arbeidsprosent, virksomhet);

        Map<AktivitetIdentifikator, ArbeidTidslinje> resultat = tjeneste(beregningsandelTjeneste).lagTidslinjer(behandling);

        assertThat(resultat).hasSize(1);
        AktivitetIdentifikator id = resultat.keySet().iterator().next();
        Optional<BigDecimal> arbeidsprosentResultat = resultat.get(id).getArbeidsprosent(new LukketPeriode(fom, tom));
        assertThat(arbeidsprosentResultat.get()).isEqualTo(arbeidsprosent);
    }

    @Test
    public void skal_være_0_arbeidstidsprosent_ved_manglende_permisjon_og_inntektsmelding_fra_riktig_virksomhet_hvis_søkt_uten_gradering_og_vi_skal_egentlig_sjekke_inntektsmelding() {
        LocalDate fom = LocalDate.now();
        LocalDate tom = fom.plusWeeks(3);
        String orgnr = "12312314";
        VirksomhetEntitet virksomhet = virksomhet(orgnr);
        final Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(virksomhet);
        String aktørId = "123";
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(new AktørId(aktørId));

        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .build();

        scenario.medFordeling(new OppgittFordelingEntitet(Collections.singletonList(oppgittPeriode), true));
        Behandling behandling = scenario.lagre(repositoryProvider);

        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, null);

        lagreInntektsmeldingMedGradering(behandling, fom, tom, BigDecimal.TEN, virksomhet("annenVirksomhet"));
        lagreIAYGrunnlag(fom, tom, arbeidsgiver, behandling);

        Map<AktivitetIdentifikator, ArbeidTidslinje> resultat = tjeneste(beregningsandelTjeneste).lagTidslinjer(behandling);

        assertThat(resultat).hasSize(1);
        AktivitetIdentifikator id = resultat.keySet().iterator().next();
        Optional<BigDecimal> arbeidsprosentResultat = resultat.get(id).getArbeidsprosent(new LukketPeriode(fom, tom));
        assertThat(arbeidsprosentResultat.get()).isZero();
    }

    private void lagreIAYGrunnlag(LocalDate fom, LocalDate tom, Arbeidsgiver arbeidsgiver, Behandling behandling) {
        InntektArbeidYtelseAggregatBuilder builder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonType.REGISTER);

        YrkesaktivitetBuilder yrkesaktivitetBuilder = YrkesaktivitetBuilder.oppdatere(Optional.empty());
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale = aktivitetsAvtaleBuilder
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom))
            .medProsentsats(BigDecimal.valueOf(100))
            .medAntallTimer(BigDecimal.valueOf(40))
            .medAntallTimerFulltid(BigDecimal.valueOf(40));

        yrkesaktivitetBuilder
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .medArbeidsgiver(arbeidsgiver)
            .leggTilAktivitetsAvtale(aktivitetsAvtale);

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = builder.getAktørArbeidBuilder(behandling.getAktørId());
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeid = aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitetBuilder);
        builder.leggTilAktørArbeid(aktørArbeid);
        repositoryProvider.getInntektArbeidYtelseRepository().lagre(behandling, builder);
    }

    private void lagreInntektsmeldingMedGradering(Behandling behandling, LocalDate fom, LocalDate tom, BigDecimal arbeidsprosent, Virksomhet virksomhet) {
        MottattDokument mottattDokument = lagreMottattDokument(behandling);
        repositoryProvider.getMottatteDokumentRepository().lagre(mottattDokument);
        Inntektsmelding inntektsmelding = InntektsmeldingBuilder.builder()
            .leggTil(new GraderingEntitet(fom, tom, arbeidsprosent))
            .medMottattDokument(mottattDokument)
            .medBeløp(BigDecimal.valueOf(35000))
            .medStartDatoPermisjon(LocalDate.now())
            .medInnsendingstidspunkt(LocalDateTime.now())
            .medVirksomhet(virksomhet)
            .build();
        lagreInntektsmelding(behandling, inntektsmelding);
    }

    private void lagreInntektsmelding(Behandling behandling, Inntektsmelding inntektsmelding) {
        repositoryProvider.getInntektArbeidYtelseRepository().lagre(behandling, inntektsmelding);
    }

    private MottattDokument lagreMottattDokument(Behandling behandling) {
        return new MottattDokument.Builder()
            .medFagsakId(behandling.getFagsakId())
            .build();
    }

    private void lagreInntektsmeldingMedUtsettelsePgaArbeid(Behandling behandling, LocalDate fom, LocalDate tom, Virksomhet virksomhet) {
        MottattDokument mottattDokument = lagreMottattDokument(behandling);
        repositoryProvider.getMottatteDokumentRepository().lagre(mottattDokument);
        Inntektsmelding inntektsmelding = InntektsmeldingBuilder.builder()
            .leggTil(UtsettelsePeriodeEntitet.utsettelse(fom, tom, UtsettelseÅrsak.ARBEID))
            .medMottattDokument(mottattDokument)
            .medBeløp(BigDecimal.valueOf(35000))
            .medStartDatoPermisjon(LocalDate.now())
            .medInnsendingstidspunkt(LocalDateTime.now())
            .medVirksomhet(virksomhet)
            .build();
        lagreInntektsmelding(behandling, inntektsmelding);
    }

    private ArbeidTidslinjeTjenesteImpl tjeneste(UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste) {
        InntektArbeidYtelseTjenesteImpl inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, null, skjæringstidspunktTjeneste, apOpptjening);
        UttakArbeidTjeneste uttakArbeidTjeneste = new UttakArbeidTjenesteImpl(inntektArbeidYtelseTjeneste, beregningsandelTjeneste);
        return new ArbeidTidslinjeTjenesteImpl(repositoryProvider, new UttakStillingsprosentTjenesteImpl(uttakArbeidTjeneste), beregningsandelTjeneste, uttakArbeidTjeneste);
    }

    private VirksomhetEntitet virksomhet(String orgnr) {
        VirksomhetEntitet virksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr(orgnr)
            .oppdatertOpplysningerNå()
            .build();
        repositoryProvider.getVirksomhetRepository().lagre(virksomhet);
        return virksomhet;
    }

}
