package no.nav.foreldrepenger.inngangsvilkaar.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.AktørInntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Opptjeningsnøkkel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørArbeid;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektspostType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapManuellVurderingType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittLandOpphold;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittLandOppholdEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittTilknytning;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittTilknytningEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskap;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskapBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.medlem.impl.MedlemskapPerioderTjenesteImpl;
import no.nav.foreldrepenger.domene.personopplysning.BasisPersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.impl.BasisPersonopplysningTjenesteImpl;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.MedlemskapsvilkårGrunnlag;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class InngangsvilkårOversetterTest {
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private InngangsvilkårOversetter oversetter;

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    private KodeverkRepository kodeverkRepository = new KodeverkRepositoryImpl(repoRule.getEntityManager());
    private YrkesaktivitetBuilder yrkesaktivitetBuilder;

    @Before
    public void oppsett() {
        SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider, Period.of(0, 10, 0));
        BasisPersonopplysningTjeneste personopplysningTjeneste = new BasisPersonopplysningTjenesteImpl(repositoryProvider, skjæringstidspunktTjeneste);
        oversetter = new InngangsvilkårOversetter(repositoryProvider, new MedlemskapPerioderTjenesteImpl(12, 6, skjæringstidspunktTjeneste), skjæringstidspunktTjeneste, personopplysningTjeneste);
    }

    public void skal_mappe_fra_domenemedlemskap_til_regelmedlemskap() {
        // Arrange

        LocalDate skjæringstidspunkt = LocalDate.now();

        ScenarioMorSøkerEngangsstønad scenario = oppsett(skjæringstidspunkt);

        opprettArbeidOgInntektForBehandling(scenario, skjæringstidspunkt.minusMonths(5), skjæringstidspunkt.plusMonths(4), true);

        Behandling behandling = scenario.lagre(repositoryProvider);

        VurdertMedlemskap vurdertMedlemskap = new VurdertMedlemskapBuilder()
            .medMedlemsperiodeManuellVurdering(MedlemskapManuellVurderingType.MEDLEM)
            .medBosattVurdering(true)
            .medLovligOppholdVurdering(true)
            .medOppholdsrettVurdering(true)
            .build();
        MedlemskapRepository medlemskapRepository = repositoryProvider.getMedlemskapRepository();
        medlemskapRepository.lagreMedlemskapVurdering(behandling, vurdertMedlemskap);


        // Act
        MedlemskapsvilkårGrunnlag grunnlag = oversetter.oversettTilRegelModellMedlemskap(behandling);

        // Assert
        assertTrue(grunnlag.isBrukerAvklartBosatt());
        assertTrue(grunnlag.isBrukerAvklartLovligOppholdINorge());
        assertTrue(grunnlag.isBrukerAvklartOppholdsrett());
        assertTrue(grunnlag.isBrukerAvklartPliktigEllerFrivillig());
        assertTrue(grunnlag.isBrukerNorskNordisk());
        assertFalse(grunnlag.isBrukerBorgerAvEUEOS());
        assertTrue(grunnlag.harSøkerArbeidsforholdOgInntekt());
    }


    @Test
    public void skal_mappe_fra_domenemedlemskap_til_regelmedlemskap_med_ingen_relevant_arbeid_og_inntekt() {

        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.now();
        ScenarioMorSøkerEngangsstønad scenario = oppsett(skjæringstidspunkt);
        opprettArbeidOgInntektForBehandling(scenario, skjæringstidspunkt.minusMonths(5), skjæringstidspunkt.minusDays(1), true);
        Behandling behandling = scenario.lagre(repositoryProvider);

        // Act
        MedlemskapsvilkårGrunnlag grunnlag = oversetter.oversettTilRegelModellMedlemskap(behandling);

        // Assert
        assertFalse(grunnlag.harSøkerArbeidsforholdOgInntekt());
    }

    @Test
    public void skal_mappe_fra_domenemedlemskap_til_regelmedlemskap_med_relevant_arbeid_og_ingen_pensjonsgivende_inntekt() {

        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.now();
        ScenarioMorSøkerEngangsstønad scenario = oppsett(skjæringstidspunkt);
        opprettArbeidOgInntektForBehandling(scenario, skjæringstidspunkt.minusMonths(5), skjæringstidspunkt.plusDays(10), false);
        Behandling behandling = scenario.lagre(repositoryProvider);
        // Act
        MedlemskapsvilkårGrunnlag grunnlag = oversetter.oversettTilRegelModellMedlemskap(behandling);

        // Assert
        assertFalse(grunnlag.harSøkerArbeidsforholdOgInntekt());
    }

    private ScenarioMorSøkerEngangsstønad oppsett(LocalDate skjæringstidspunkt) {
        OppgittLandOpphold oppholdNorgeSistePeriode = new OppgittLandOppholdEntitet.Builder()
            .erTidligereOpphold(true)
            .medLand(Landkoder.NOR)
            .medPeriode(skjæringstidspunkt.minusYears(1), skjæringstidspunkt)
            .build();
        OppgittLandOpphold oppholdNorgeNestePeriode = new OppgittLandOppholdEntitet.Builder()
            .erTidligereOpphold(false)
            .medLand(Landkoder.NOR)
            .medPeriode(skjæringstidspunkt, skjæringstidspunkt.plusYears(1))
            .build();

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        OppgittLandOpphold utlandsopphold1 = new OppgittLandOppholdEntitet.Builder()
            .medLand(kodeverkRepository.finn(Landkoder.class, "PNG"))
            .medPeriode(LocalDate.of(2017, 1, 1), LocalDate.of(2017, 2, 1))
            .build();
        OppgittLandOpphold utlandsopphold2 = new OppgittLandOppholdEntitet.Builder()
            .medLand(kodeverkRepository.finn(Landkoder.class, "ALA"))
            .medPeriode(LocalDate.of(2017, 3, 1), LocalDate.of(2017, 4, 1))
            .build();
        OppgittTilknytning oppgittTilknytning = new OppgittTilknytningEntitet.Builder()
            .medOppholdNå(false)
            .leggTilOpphold(oppholdNorgeNestePeriode)
            .leggTilOpphold(oppholdNorgeSistePeriode)
            .leggTilOpphold(utlandsopphold1)
            .leggTilOpphold(utlandsopphold2)
            .build();
        scenario.medSøknad()
            .medMottattDato(LocalDate.of(2017, 3, 15))
            .medOppgittTilknytning(oppgittTilknytning);

        PersonInformasjon søker = scenario.opprettBuilderForRegisteropplysninger()
            .medPersonas()
            .kvinne(scenario.getDefaultBrukerAktørId(), SivilstandType.GIFT, Region.NORDEN)
            .personstatus(PersonstatusType.BOSA)
            .statsborgerskap(Landkoder.NOR)
            .build();
        scenario.medRegisterOpplysninger(søker);
        return scenario;
    }

    private InntektArbeidYtelseAggregatBuilder opprettArbeidOgInntektForBehandling(AbstractTestScenario<?> scenario, LocalDate fom, LocalDate tom, boolean harPensjonsgivendeInntekt) {

        VirksomhetEntitet virksomhet = new VirksomhetEntitet.Builder().medNavn("OrgA").medOrgnr("42").oppdatertOpplysningerNå().build();
        repositoryProvider.getVirksomhetRepository().lagre(virksomhet);

        InntektArbeidYtelseAggregatBuilder aggregatBuilder = scenario.getInntektArbeidYtelseScenarioTestBuilder().getKladd();

        lagAktørArbeid(aggregatBuilder, scenario.getDefaultBrukerAktørId(), virksomhet, fom, tom, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, Optional.empty());
        for (LocalDate dt = fom; dt.isBefore(tom); dt = dt.plusMonths(1)) {
            lagInntekt(aggregatBuilder, scenario.getDefaultBrukerAktørId(), virksomhet, dt, dt.plusMonths(1), harPensjonsgivendeInntekt);
        }

        return aggregatBuilder;
    }

    private AktørArbeid lagAktørArbeid(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder, AktørId aktørId, Virksomhet virksomhet,
                                       LocalDate fom, LocalDate tom, ArbeidType arbeidType, Optional<String> arbeidsforholdRef) {
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = inntektArbeidYtelseAggregatBuilder
            .getAktørArbeidBuilder(aktørId);

        Opptjeningsnøkkel opptjeningsnøkkel;
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(virksomhet);
        if (arbeidsforholdRef.isPresent()) {
            opptjeningsnøkkel = new Opptjeningsnøkkel(arbeidsforholdRef.get(), arbeidsgiver.getIdentifikator(), null);
        } else {
            opptjeningsnøkkel = Opptjeningsnøkkel.forOrgnummer(virksomhet.getOrgnr());
        }


        yrkesaktivitetBuilder = aktørArbeidBuilder
            .getYrkesaktivitetBuilderForNøkkelAvType(opptjeningsnøkkel, arbeidType);
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();

        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale =
            aktivitetsAvtaleBuilder.medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom));

        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(aktivitetsAvtale)
            .medArbeidType(arbeidType)
            .medArbeidsgiver(arbeidsgiver);

        yrkesaktivitetBuilder.medArbeidsforholdId(arbeidsforholdRef.isPresent() ? ArbeidsforholdRef.ref(arbeidsforholdRef.get()) : null);

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
            inntektBuilder.leggTilInntektspost(inntektspost).medArbeidsgiver(yrkesaktivitetBuilder.build().getArbeidsgiver());
            aktørInntektBuilder.leggTilInntekt(inntektBuilder);
            inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntektBuilder);
        });
    }

}
