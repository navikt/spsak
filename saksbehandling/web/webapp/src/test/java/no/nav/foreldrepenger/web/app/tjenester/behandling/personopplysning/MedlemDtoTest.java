package no.nav.foreldrepenger.web.app.tjenester.behandling.personopplysning;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.Test;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.AktørInntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektspostType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapManuellVurderingType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapPerioderBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.sykemelding.SykemeldingBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.sykemelding.SykemeldingerBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.AksjonspunktutlederForVurderOpptjening;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.InntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.medlem.api.MedlemTjeneste;
import no.nav.foreldrepenger.domene.person.impl.TpsTjenesteImpl;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.typer.Prosentsats;
import no.nav.foreldrepenger.web.app.tjenester.behandling.medlem.InntektDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.medlem.MedlemDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.medlem.MedlemDtoTjenesteImpl;
import no.nav.vedtak.util.Tuple;

public class MedlemDtoTest {

    @Test
    public void skal_lage_medlem_dto() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        String navn = "Lisa gikk til skolen";
        AktørId søkerAktørId = new AktørId("123");

        PersonInformasjon søker = scenario.opprettBuilderForRegisteropplysninger()
            .leggTilPersonopplysninger(
                Personopplysning.builder()
                    .aktørId(søkerAktørId)
                    .navn(navn)
            )
            .build();

        scenario.medRegisterOpplysninger(søker);
        scenario.leggTilMedlemskapPeriode(new MedlemskapPerioderBuilder().medMedlemskapType(MedlemskapType.ENDELIG).build());

        scenario.medMedlemskap()
            .medErEosBorger(true)
            .medBosattVurdering(true)
            .medOppholdsrettVurdering(true)
            .medMedlemsperiodeManuellVurdering(MedlemskapManuellVurderingType.MEDLEM)
            .medLovligOppholdVurdering(true);

        SykemeldingerBuilder smBuilder = scenario.getSykemeldingerBuilder();
        SykemeldingBuilder sykemeldingBuilder = smBuilder.sykemeldingBuilder("ASDF-ASDF-ASDF");
        sykemeldingBuilder.medPeriode(LocalDate.now(), LocalDate.now().plusDays(36))
            .medArbeidsgiver(Arbeidsgiver.person(new AktørId(1234L)))
            .medGrad(new Prosentsats(100));
        smBuilder.medSykemelding(sykemeldingBuilder);
        scenario.medSykemeldinger(smBuilder);

        Behandling behandling = scenario.lagMocked();
        Tuple<GrunnlagRepositoryProvider, ResultatRepositoryProvider> providerTuple = scenario.mockBehandlingRepositoryProvider();
        final GrunnlagRepositoryProvider repositoryProvider = providerTuple.getElement1();
        final ResultatRepositoryProvider resultatRepositoryProvider = providerTuple.getElement2();
        InntektEntitet.InntektspostBuilder builder = InntektEntitet.InntektspostBuilder.ny();

        InntektEntitet.InntektspostBuilder inntektspost = builder
            .medBeløp(BigDecimal.TEN)
            .medPeriode(LocalDate.now(), LocalDate.now().plusMonths(1))
            .medInntektspostType(InntektspostType.UDEFINERT);

        lagreOpptjening(scenario, behandling, søkerAktørId, inntektspost);

        SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider, resultatRepositoryProvider);
        AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, resultatRepositoryProvider, skjæringstidspunktTjeneste);
        InntektArbeidYtelseTjenesteImpl opptjeningTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, null, skjæringstidspunktTjeneste, apOpptjening);
        TpsTjenesteImpl tpsTjeneste = mock(TpsTjenesteImpl.class);

        PersonopplysningTjeneste personopplysningTjenesteMock = mock(PersonopplysningTjeneste.class);
        MedlemTjeneste medlemTjenesteMock = mock(MedlemTjeneste.class);

        MedlemDtoTjenesteImpl dtoTjeneste = new MedlemDtoTjenesteImpl(repositoryProvider, tpsTjeneste, skjæringstidspunktTjeneste, opptjeningTjeneste, repositoryProvider.getBehandlingRepository(), medlemTjenesteMock, personopplysningTjenesteMock, mock(PersonopplysningDtoTjeneste.class));
        Personinfo person = new Personinfo.Builder()
            .medNavn(navn)
            .medAktørId(søkerAktørId)
            .medPersonIdent(PersonIdent.fra("12312411252"))
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medFødselsdato(LocalDate.now())
            .build();

        when(tpsTjeneste.hentBrukerForAktør(søkerAktørId)).thenReturn(Optional.ofNullable(person));

        Optional<MedlemDto> medlemDtoOpt = dtoTjeneste.lagMedlemDto(behandling);
        assertThat(medlemDtoOpt).hasValueSatisfying(medlemDto -> {
            assertThat(medlemDto.getMedlemskapPerioder()).hasSize(1);
            assertThat(medlemDto.getErEosBorger()).isTrue();
            assertThat(medlemDto.getBosattVurdering()).isTrue();
            assertThat(medlemDto.getOppholdsrettVurdering()).isTrue();
            assertThat(medlemDto.getMedlemskapManuellVurderingType()).isEqualTo(MedlemskapManuellVurderingType.MEDLEM);
            assertThat(medlemDto.getLovligOppholdVurdering()).isTrue();
            assertThat(medlemDto.getInntekt()).hasSize(1);
            InntektDto inntektDto = medlemDto.getInntekt().get(0);
            assertThat(inntektDto.getNavn()).isEqualTo(navn);
        });
    }

    @Test
    public void skal_lage_inntekt_for_ektefelle() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        String navn = "Lisa gikk til skolen";
        String annenPart = "Tripp, tripp, tripp, det sa";
        AktørId aktørIdSøker = new AktørId("123");
        AktørId aktørIdAnnenPart = new AktørId("124");

        scenario.leggTilMedlemskapPeriode(new MedlemskapPerioderBuilder().medMedlemskapType(MedlemskapType.ENDELIG).build());

        PersonInformasjon personInformasjon = scenario.opprettBuilderForRegisteropplysninger()
            .leggTilPersonopplysninger(
                Personopplysning.builder()
                    .aktørId(aktørIdSøker)
                    .navn(navn)
            )
            .leggTilPersonopplysninger(
                Personopplysning.builder()
                    .aktørId(aktørIdAnnenPart)
                    .navn(annenPart)
            )
            .build();

        scenario.medRegisterOpplysninger(personInformasjon);

        scenario.medMedlemskap()
            .medErEosBorger(true)
            .medBosattVurdering(true)
            .medOppholdsrettVurdering(true)
            .medMedlemsperiodeManuellVurdering(MedlemskapManuellVurderingType.MEDLEM)
            .medLovligOppholdVurdering(true);

        SykemeldingerBuilder smBuilder = scenario.getSykemeldingerBuilder();
        SykemeldingBuilder sykemeldingBuilder = smBuilder.sykemeldingBuilder("ASDF-ASDF-ASDF");
        sykemeldingBuilder.medPeriode(LocalDate.now(), LocalDate.now().plusDays(36))
            .medArbeidsgiver(Arbeidsgiver.person(new AktørId(1234L)))
            .medGrad(new Prosentsats(100));
        smBuilder.medSykemelding(sykemeldingBuilder);
        scenario.medSykemeldinger(smBuilder);

        Behandling behandling = scenario.lagMocked();
        Tuple<GrunnlagRepositoryProvider, ResultatRepositoryProvider> providerTuple = scenario.mockBehandlingRepositoryProvider();
        final GrunnlagRepositoryProvider repositoryProvider = providerTuple.getElement1();
        final ResultatRepositoryProvider resultatRepositoryProvider = providerTuple.getElement2();

        InntektEntitet.InntektspostBuilder inntektspost = InntektEntitet.InntektspostBuilder.ny()
            .medBeløp(BigDecimal.TEN)
            .medPeriode(LocalDate.now(), LocalDate.now().plusMonths(1))
            .medInntektspostType(InntektspostType.UDEFINERT);

        lagreOpptjening(scenario, behandling, aktørIdAnnenPart, inntektspost);
        SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider, resultatRepositoryProvider);
        AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, resultatRepositoryProvider, skjæringstidspunktTjeneste);
        InntektArbeidYtelseTjenesteImpl opptjeningTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, null, skjæringstidspunktTjeneste, apOpptjening);
        TpsTjenesteImpl tpsTjeneste = mock(TpsTjenesteImpl.class);

        PersonopplysningTjeneste personopplysningTjenesteMock = mock(PersonopplysningTjeneste.class);
        MedlemTjeneste medlemTjenesteMock = mock(MedlemTjeneste.class);

        MedlemDtoTjenesteImpl dtoTjeneste = new MedlemDtoTjenesteImpl(repositoryProvider, tpsTjeneste, skjæringstidspunktTjeneste, opptjeningTjeneste, repositoryProvider.getBehandlingRepository(), medlemTjenesteMock, personopplysningTjenesteMock, mock(PersonopplysningDtoTjeneste.class));
        Personinfo person = new Personinfo.Builder()
            .medNavn(annenPart)
            .medAktørId(new AktørId("124"))
            .medPersonIdent(PersonIdent.fra("12312411252"))
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medFødselsdato(LocalDate.now())
            .build();

        when(tpsTjeneste.hentBrukerForAktør(new AktørId("124"))).thenReturn(Optional.ofNullable(person));

        Optional<MedlemDto> medlemDtoOpt = dtoTjeneste.lagMedlemDto(behandling);
        assertThat(medlemDtoOpt).hasValueSatisfying(medlemDto -> {
            assertThat(medlemDto.getMedlemskapPerioder()).hasSize(1);
            assertThat(medlemDto.getErEosBorger()).isTrue();
            assertThat(medlemDto.getBosattVurdering()).isTrue();
            assertThat(medlemDto.getOppholdsrettVurdering()).isTrue();
            assertThat(medlemDto.getMedlemskapManuellVurderingType()).isEqualTo(MedlemskapManuellVurderingType.MEDLEM);
            assertThat(medlemDto.getLovligOppholdVurdering()).isTrue();
            assertThat(medlemDto.getInntekt()).hasSize(1);
            InntektDto inntektDto = medlemDto.getInntekt().get(0);
            assertThat(inntektDto.getNavn()).isEqualTo(annenPart);
        });
    }

    private void lagreOpptjening(ScenarioMorSøkerEngangsstønad scenario, Behandling behandling, AktørId aktørId, InntektEntitet.InntektspostBuilder inntektspost) {
        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonType.REGISTER);

        Virksomhet virksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr("21542512")
            .medNavn("Virksomheten")
            .oppdatertOpplysningerNå()
            .build();

        final YrkesaktivitetBuilder oppdatere = YrkesaktivitetBuilder.oppdatere(Optional.empty());
        Yrkesaktivitet yrkesaktivitet = oppdatere
            .medArbeidType(ArbeidType.UDEFINERT)
            .medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet))
            .build();

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder builder = inntektArbeidYtelseAggregatBuilder.getAktørArbeidBuilder(aktørId);
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeid = builder
            .leggTilYrkesaktivitet(oppdatere);

        AktørInntektEntitet.InntektBuilder inntekt = AktørInntektEntitet.InntektBuilder.oppdatere(Optional.empty())
            .leggTilInntektspost(inntektspost)
            .medArbeidsgiver(yrkesaktivitet.getArbeidsgiver())
            .medInntektsKilde(InntektsKilde.INNTEKT_OPPTJENING);

        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntekt = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder(aktørId)
            .leggTilInntekt(inntekt);

        inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntekt);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørArbeid(aktørArbeid);


        scenario.getMockInntektArbeidYtelseRepository().lagre(behandling, inntektArbeidYtelseAggregatBuilder);
    }
}
