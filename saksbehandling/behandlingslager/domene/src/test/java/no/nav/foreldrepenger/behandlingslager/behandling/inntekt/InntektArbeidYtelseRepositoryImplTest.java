package no.nav.foreldrepenger.behandlingslager.behandling.inntekt;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.AktørInntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.OffentligYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Opptjeningsnøkkel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.PensjonTrygdType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseStørrelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdHandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdOverstyringBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørInntekt;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Inntekt;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Permisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Ytelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseAnvist;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseStørrelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektspostType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.PermisjonsbeskrivelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.TemaUnderkategori;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.AnnenAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.UtenlandskVirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.OppgittOpptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.kodeverk.VirksomhetType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class InntektArbeidYtelseRepositoryImplTest {

    private static LocalDate FOM_DATO = LocalDate.now().minusDays(3);
    private static LocalDate TOM_DATO = LocalDate.now().minusDays(2);
    private static LocalDate ANVIST_FOM = LocalDate.now().minusDays(200);
    private static LocalDate ANVIST_TOM = LocalDate.now().minusDays(100);
    private static LocalDate OPPRINNELIG_IDENTDATO = LocalDate.now().minusDays(100);
    @Rule
    public RepositoryRule repositoryRule = new UnittestRepositoryRule();
    private AktørId AKTØRID = new AktørId("123");
    private ArbeidsforholdRef AREBIDSFORHOLD_ID = ArbeidsforholdRef.ref("12001");
    private FagsakRepository fagsakRepository = new FagsakRepositoryImpl(repositoryRule.getEntityManager());
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repositoryRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();


    @Test
    public void skal_kunne_fjerne_arbeidsforhold() {
        final Personinfo personinfo = new Personinfo.Builder()
            .medNavn("Navn navnesen")
            .medAktørId(AKTØRID)
            .medFødselsdato(LocalDate.now().minusYears(20))
            .medLandkode(Landkoder.NOR)
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medPersonIdent(new PersonIdent("12345678901"))
            .medForetrukketSpråk(Språkkode.nb)
            .build();
        final Fagsak fagsak = Fagsak.opprettNy(NavBruker.opprettNy(personinfo));
        fagsakRepository.opprettNy(fagsak);
        final Behandling.Builder builder = Behandling.forFørstegangssøknad(fagsak);
        final Behandling behandling = builder.build();
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));

        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = inntektArbeidYtelseRepository.opprettBuilderFor(behandling, VersjonType.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder(AKTØRID);
        AktørInntektEntitet.InntektBuilder inntektBuilder = aktørInntektBuilder.getInntektBuilder(InntektsKilde.INNTEKT_OPPTJENING, new Opptjeningsnøkkel(AREBIDSFORHOLD_ID.getReferanse(), null, null));
        InntektEntitet.InntektspostBuilder inntektspostBuilder = inntektBuilder.getInntektspostBuilder();

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = inntektArbeidYtelseAggregatBuilder.getAktørArbeidBuilder(AKTØRID);
        YrkesaktivitetBuilder yrkesaktivitetBuilder = aktørArbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(new Opptjeningsnøkkel(AREBIDSFORHOLD_ID.getReferanse(), null, null),
            ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);

        InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder = inntektArbeidYtelseAggregatBuilder.getAktørYtelseBuilder(AKTØRID);
        aktørYtelseBuilder.leggTilYtelse(lagYtelse(aktørYtelseBuilder));

        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();
        YrkesaktivitetEntitet.PermisjonBuilder permisjonBuilder = yrkesaktivitetBuilder.getPermisjonBuilder();

        LocalDate fraOgMed = LocalDate.now().minusWeeks(1);
        LocalDate tilOgMed = LocalDate.now().plusMonths(1);

        Permisjon permisjon = permisjonBuilder
            .medProsentsats(BigDecimal.valueOf(100))
            .medPeriode(fraOgMed, tilOgMed)
            .medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.UDEFINERT)
            .build();

        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale = aktivitetsAvtaleBuilder
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fraOgMed, tilOgMed))
            .medProsentsats(BigDecimal.TEN)
            .medAntallTimer(BigDecimal.valueOf(20.4d))
            .medAntallTimerFulltid(BigDecimal.valueOf(10.2d));

        Virksomhet virksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr("21542512")
            .medNavn("Virksomheten")
            .medRegistrert(fraOgMed.minusYears(2L))
            .medOppstart(fraOgMed.minusYears(1L))
            .oppdatertOpplysningerNå()
            .build();

        repositoryRule.getEntityManager().persist(virksomhet);

        Yrkesaktivitet yrkesaktivitet = yrkesaktivitetBuilder
            .medArbeidType(ArbeidType.UDEFINERT)
            .medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet))
            .medArbeidsforholdId(AREBIDSFORHOLD_ID)
            .leggTilAktivitetsAvtale(aktivitetsAvtale)
            .leggTilPermisjon(permisjon)
            .build();

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeid = aktørArbeidBuilder
            .leggTilYrkesaktivitet(yrkesaktivitetBuilder);

        InntektEntitet.InntektspostBuilder inntektspost = inntektspostBuilder
            .medBeløp(BigDecimal.TEN)
            .medPeriode(fraOgMed, tilOgMed)
            .medInntektspostType(InntektspostType.YTELSE)
            .medYtelse(OffentligYtelseType.UDEFINERT);

        inntektBuilder
            .leggTilInntektspost(inntektspost)
            .medArbeidsgiver(yrkesaktivitet.getArbeidsgiver())
            .medInntektsKilde(InntektsKilde.INNTEKT_OPPTJENING);

        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntekt = aktørInntektBuilder
            .leggTilInntekt(inntektBuilder);

        inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntekt);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørArbeid(aktørArbeid);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørYtelse(aktørYtelseBuilder);

        inntektArbeidYtelseRepository.lagre(behandling, inntektArbeidYtelseAggregatBuilder);
        InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag = inntektArbeidYtelseRepository.hentAggregat(behandling, null);

        assertThat(inntektArbeidYtelseGrunnlag.getAktørInntektForFørStp()).hasSize(1);
        assertThat(inntektArbeidYtelseGrunnlag.getAktørInntektForFørStp().iterator().next().getAktørId()).isEqualTo(AKTØRID);
        assertThat(inntektArbeidYtelseGrunnlag.getAktørInntektForFørStp().iterator().next().getInntektPensjonsgivende()).hasSize(1);
        assertThat(inntektArbeidYtelseGrunnlag.getAktørInntektForFørStp().iterator().next().getInntektPensjonsgivende().iterator().next().getArbeidsgiver().getVirksomhet()).isEqualTo(virksomhet);
        assertThat(inntektArbeidYtelseGrunnlag.getAktørInntektForFørStp().iterator().next().getInntektPensjonsgivende().iterator().next().getInntektspost()).hasSize(1);
        assertThat(inntektArbeidYtelseGrunnlag.getAktørInntektForFørStp().iterator().next().getInntektPensjonsgivende().iterator().next().getInntektspost().iterator().next().getBeløp().getVerdi()).isEqualTo
            (BigDecimal.TEN);

        assertThat(inntektArbeidYtelseGrunnlag.getAktørArbeidFørStp()).hasSize(1);
        assertThat(inntektArbeidYtelseGrunnlag.getAktørArbeidFørStp().iterator().next().getAktørId()).isEqualTo(AKTØRID);
        assertThat(inntektArbeidYtelseGrunnlag.getAktørArbeidFørStp().iterator().next().getYrkesaktiviteter()).hasSize(1);
        final Yrkesaktivitet yrkesaktivitet1 = inntektArbeidYtelseGrunnlag.getAktørArbeidFørStp().iterator().next().getYrkesaktiviteter().iterator().next();
        final Arbeidsgiver arbeidsgiver = yrkesaktivitet1.getArbeidsgiver();
        assertThat(arbeidsgiver.getVirksomhet()).isEqualTo(virksomhet);

        final ArbeidsforholdInformasjonBuilder informasjonBuilder = inntektArbeidYtelseRepository.opprettInformasjonBuilderFor(behandling);
        final ArbeidsforholdOverstyringBuilder overstyringBuilderFor = informasjonBuilder.getOverstyringBuilderFor(arbeidsgiver, yrkesaktivitet1.getArbeidsforholdRef().orElse(ArbeidsforholdRef.ref(null)));
        overstyringBuilderFor.medHandling(ArbeidsforholdHandlingType.IKKE_BRUK);
        informasjonBuilder.leggTil(overstyringBuilderFor);

        inntektArbeidYtelseRepository.lagre(behandling, informasjonBuilder);
        inntektArbeidYtelseGrunnlag = inntektArbeidYtelseRepository.hentAggregat(behandling, null);
        assertThat(inntektArbeidYtelseGrunnlag.getAktørArbeidFørStp().iterator().next().getYrkesaktiviteter()).hasSize(0);
    }

    @Test
    public void skal_lagre_ned_inntekt_arbeid_ytelser() {
        final Personinfo personinfo = new Personinfo.Builder()
            .medNavn("Navn navnesen")
            .medAktørId(AKTØRID)
            .medFødselsdato(LocalDate.now().minusYears(20))
            .medLandkode(Landkoder.NOR)
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medPersonIdent(new PersonIdent("12345678901"))
            .medForetrukketSpråk(Språkkode.nb)
            .build();
        final Fagsak fagsak = Fagsak.opprettNy(NavBruker.opprettNy(personinfo));
        fagsakRepository.opprettNy(fagsak);
        final Behandling.Builder builder = Behandling.forFørstegangssøknad(fagsak);
        final Behandling behandling = builder.build();
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));

        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = inntektArbeidYtelseRepository.opprettBuilderFor(behandling, VersjonType.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder(AKTØRID);
        AktørInntektEntitet.InntektBuilder inntektBuilder = aktørInntektBuilder.getInntektBuilder(InntektsKilde.INNTEKT_OPPTJENING, new Opptjeningsnøkkel(AREBIDSFORHOLD_ID.getReferanse(), null, null));
        InntektEntitet.InntektspostBuilder inntektspostBuilder = inntektBuilder.getInntektspostBuilder();

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = inntektArbeidYtelseAggregatBuilder.getAktørArbeidBuilder(AKTØRID);
        YrkesaktivitetBuilder yrkesaktivitetBuilder = aktørArbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(new Opptjeningsnøkkel(AREBIDSFORHOLD_ID.getReferanse(), null, null),
            ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);

        InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder = inntektArbeidYtelseAggregatBuilder.getAktørYtelseBuilder(AKTØRID);
        aktørYtelseBuilder.leggTilYtelse(lagYtelse(aktørYtelseBuilder));

        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();
        YrkesaktivitetEntitet.PermisjonBuilder permisjonBuilder = yrkesaktivitetBuilder.getPermisjonBuilder();

        LocalDate fraOgMed = LocalDate.now().minusWeeks(1);
        LocalDate tilOgMed = LocalDate.now().plusMonths(1);

        Permisjon permisjon = permisjonBuilder
            .medProsentsats(BigDecimal.valueOf(100))
            .medPeriode(fraOgMed, tilOgMed)
            .medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.UDEFINERT)
            .build();

        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale = aktivitetsAvtaleBuilder
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fraOgMed, tilOgMed))
            .medProsentsats(BigDecimal.TEN)
            .medAntallTimer(BigDecimal.valueOf(20.4d))
            .medSisteLønnsendringsdato(fraOgMed)
            .medAntallTimerFulltid(BigDecimal.valueOf(10.2d));

        Virksomhet virksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr("21542512")
            .medNavn("Virksomheten")
            .medRegistrert(fraOgMed.minusYears(2L))
            .medOppstart(fraOgMed.minusYears(1L))
            .oppdatertOpplysningerNå()
            .build();

        repositoryRule.getEntityManager().persist(virksomhet);

        Yrkesaktivitet yrkesaktivitet = yrkesaktivitetBuilder
            .medArbeidType(ArbeidType.UDEFINERT)
            .medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet))
            .medArbeidsforholdId(AREBIDSFORHOLD_ID)
            .leggTilAktivitetsAvtale(aktivitetsAvtale)
            .leggTilPermisjon(permisjon)
            .build();

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeid = aktørArbeidBuilder
            .leggTilYrkesaktivitet(yrkesaktivitetBuilder);

        InntektEntitet.InntektspostBuilder inntektspost = inntektspostBuilder
            .medBeløp(BigDecimal.TEN)
            .medPeriode(fraOgMed, tilOgMed)
            .medInntektspostType(InntektspostType.YTELSE)
            .medYtelse(OffentligYtelseType.UDEFINERT);

        inntektBuilder
            .leggTilInntektspost(inntektspost)
            .medArbeidsgiver(yrkesaktivitet.getArbeidsgiver())
            .medInntektsKilde(InntektsKilde.INNTEKT_OPPTJENING);

        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntekt = aktørInntektBuilder
            .leggTilInntekt(inntektBuilder);

        inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntekt);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørArbeid(aktørArbeid);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørYtelse(aktørYtelseBuilder);

        inntektArbeidYtelseRepository.lagre(behandling, inntektArbeidYtelseAggregatBuilder);

        InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag = inntektArbeidYtelseRepository.hentAggregat(behandling, null);

        assertThat(inntektArbeidYtelseGrunnlag.getAktørInntektForFørStp()).hasSize(1);
        assertThat(inntektArbeidYtelseGrunnlag.getAktørInntektForFørStp().iterator().next().getAktørId()).isEqualTo(AKTØRID);
        assertThat(inntektArbeidYtelseGrunnlag.getAktørInntektForFørStp().iterator().next().getInntektPensjonsgivende()).hasSize(1);
        assertThat(inntektArbeidYtelseGrunnlag.getAktørInntektForFørStp().iterator().next().getInntektPensjonsgivende().iterator().next().getArbeidsgiver().getVirksomhet()).isEqualTo(virksomhet);
        assertThat(inntektArbeidYtelseGrunnlag.getAktørInntektForFørStp().iterator().next().getInntektPensjonsgivende().iterator().next().getInntektspost()).hasSize(1);
        assertThat(inntektArbeidYtelseGrunnlag.getAktørInntektForFørStp().iterator().next().getInntektPensjonsgivende().iterator().next().getInntektspost().iterator().next().getBeløp().getVerdi()).isEqualTo
            (BigDecimal.TEN);

        assertThat(inntektArbeidYtelseGrunnlag.getAktørArbeidFørStp()).hasSize(1);
        assertThat(inntektArbeidYtelseGrunnlag.getAktørArbeidFørStp().iterator().next().getAktørId()).isEqualTo(AKTØRID);
        assertThat(inntektArbeidYtelseGrunnlag.getAktørArbeidFørStp().iterator().next().getYrkesaktiviteter()).hasSize(1);
        assertThat(inntektArbeidYtelseGrunnlag.getAktørArbeidFørStp().iterator().next().getYrkesaktiviteter().iterator().next().getArbeidsgiver().getVirksomhet()).isEqualTo(virksomhet);

        assertThatForAktørYtelse(inntektArbeidYtelseGrunnlag);

        final InntektArbeidYtelseAggregatBuilder aggregatBuilder = inntektArbeidYtelseRepository.opprettBuilderFor(behandling, VersjonType.REGISTER);

        final InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder arbeidBuilder = aggregatBuilder.getAktørArbeidBuilder(AKTØRID);
        final YrkesaktivitetBuilder builder1 = arbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(new Opptjeningsnøkkel(AREBIDSFORHOLD_ID.getReferanse(), null, null),
            ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        builder1.leggTilAktivitetsAvtale(builder1.getAktivitetsAvtaleBuilder()
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(LocalDate.now().minusDays(100), LocalDate.now()))
            .medProsentsats(BigDecimal.TEN));

        arbeidBuilder.leggTilYrkesaktivitet(builder1);

        inntektArbeidYtelseRepository.lagre(behandling, aggregatBuilder);

        InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag2 = inntektArbeidYtelseRepository.hentAggregat(behandling, null);

        assertThat(inntektArbeidYtelseGrunnlag2).isNotNull();
        assertThat(inntektArbeidYtelseGrunnlag2).isNotEqualTo(inntektArbeidYtelseGrunnlag);

    }

    @Test
    public void skal_lagre_slette_inntekter_fra_treet() {
        final Personinfo personinfo = new Personinfo.Builder()
            .medNavn("Navn navnesen")
            .medAktørId(AKTØRID)
            .medFødselsdato(LocalDate.now().minusYears(20))
            .medLandkode(Landkoder.NOR)
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medPersonIdent(new PersonIdent("12345678901"))
            .medForetrukketSpråk(Språkkode.nb)
            .build();
        final Fagsak fagsak = Fagsak.opprettNy(NavBruker.opprettNy(personinfo));
        fagsakRepository.opprettNy(fagsak);
        final Behandling.Builder builder = Behandling.forFørstegangssøknad(fagsak);
        final Behandling behandling = builder.build();
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));

        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = inntektArbeidYtelseRepository.opprettBuilderFor(behandling, VersjonType.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder(AKTØRID);
        AktørInntektEntitet.InntektBuilder inntektBuilder = aktørInntektBuilder.getInntektBuilder(InntektsKilde.INNTEKT_OPPTJENING, new Opptjeningsnøkkel(AREBIDSFORHOLD_ID.getReferanse(), null, null));
        InntektEntitet.InntektspostBuilder inntektspostBuilder = inntektBuilder.getInntektspostBuilder();

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = inntektArbeidYtelseAggregatBuilder.getAktørArbeidBuilder(AKTØRID);
        YrkesaktivitetBuilder yrkesaktivitetBuilder = aktørArbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(new Opptjeningsnøkkel(AREBIDSFORHOLD_ID.getReferanse(), null, null),
            ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);

        InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder = inntektArbeidYtelseAggregatBuilder.getAktørYtelseBuilder(AKTØRID);
        aktørYtelseBuilder.leggTilYtelse(lagYtelse(aktørYtelseBuilder));

        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();
        YrkesaktivitetEntitet.PermisjonBuilder permisjonBuilder = yrkesaktivitetBuilder.getPermisjonBuilder();

        LocalDate fraOgMed = LocalDate.now().minusWeeks(1);
        LocalDate tilOgMed = LocalDate.now().plusMonths(1);

        Permisjon permisjon = permisjonBuilder
            .medProsentsats(BigDecimal.valueOf(100))
            .medPeriode(fraOgMed, tilOgMed)
            .medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.UDEFINERT)
            .build();

        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale = aktivitetsAvtaleBuilder
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fraOgMed, tilOgMed))
            .medProsentsats(BigDecimal.TEN)
            .medAntallTimer(BigDecimal.valueOf(20.4d))
            .medSisteLønnsendringsdato(fraOgMed)
            .medAntallTimerFulltid(BigDecimal.valueOf(10.2d));

        Virksomhet virksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr("21542512")
            .medNavn("Virksomheten")
            .medRegistrert(fraOgMed.minusYears(2L))
            .medOppstart(fraOgMed.minusYears(1L))
            .oppdatertOpplysningerNå()
            .build();

        repositoryRule.getEntityManager().persist(virksomhet);

        Yrkesaktivitet yrkesaktivitet = yrkesaktivitetBuilder
            .medArbeidType(ArbeidType.UDEFINERT)
            .medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet))
            .medArbeidsforholdId(AREBIDSFORHOLD_ID)
            .leggTilAktivitetsAvtale(aktivitetsAvtale)
            .leggTilPermisjon(permisjon)
            .build();

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeid = aktørArbeidBuilder
            .leggTilYrkesaktivitet(yrkesaktivitetBuilder);

        InntektEntitet.InntektspostBuilder inntektspost = inntektspostBuilder
            .medBeløp(BigDecimal.TEN)
            .medPeriode(fraOgMed, tilOgMed)
            .medInntektspostType(InntektspostType.YTELSE)
            .medYtelse(PensjonTrygdType.BIL);

        inntektBuilder
            .leggTilInntektspost(inntektspost)
            .medArbeidsgiver(yrkesaktivitet.getArbeidsgiver())
            .medInntektsKilde(InntektsKilde.INNTEKT_OPPTJENING);

        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntekt = aktørInntektBuilder
            .leggTilInntekt(inntektBuilder);

        inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntekt);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørArbeid(aktørArbeid);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørYtelse(aktørYtelseBuilder);

        inntektArbeidYtelseRepository.lagre(behandling, inntektArbeidYtelseAggregatBuilder);

        Behandling oppdatertBehandling = behandlingRepository.hentBehandling(behandling.getId());

        InntektArbeidYtelseAggregatBuilder builderUtenInntekt = inntektArbeidYtelseRepository.opprettBuilderFor(oppdatertBehandling, VersjonType.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder fjernInntetkBuilder = builderUtenInntekt.getAktørInntektBuilder(AKTØRID);
        fjernInntetkBuilder.fjernInntekterFraKilde(InntektsKilde.INNTEKT_OPPTJENING);

        inntektArbeidYtelseRepository.lagre(oppdatertBehandling, builderUtenInntekt);

        InntektArbeidYtelseGrunnlag assertGrunnlag = inntektArbeidYtelseRepository.hentAggregat(behandling, null);

        Optional<AktørInntekt> aktørInntektOpt = assertGrunnlag.getAktørInntektForFørStp().stream().findFirst();
        AktørInntekt inntektForAktør = aktørInntektOpt.get();
        List<Inntekt> inntektPensjonsgivende = inntektForAktør.getInntektPensjonsgivende();
        assertThat(inntektPensjonsgivende).isEmpty();
    }

    private void assertThatForAktørYtelse(InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag) {
        assertThat(inntektArbeidYtelseGrunnlag.getAktørYtelseFørStp(AKTØRID)).isPresent();
        final Collection<Ytelse> ytelser = inntektArbeidYtelseGrunnlag.getAktørYtelseFørStp(AKTØRID).get().getYtelser();
        assertThat(ytelser).hasSize(1);
        Ytelse ytelse = ytelser.iterator().next();
        assertThat(ytelse.getPeriode().getFomDato()).isEqualTo(FOM_DATO);
        assertThat(ytelse.getPeriode().getTomDato()).isEqualTo(TOM_DATO);

        assertThat(ytelse.getYtelseGrunnlag()).isPresent();
        YtelseGrunnlag ytelseGrunnlag = ytelse.getYtelseGrunnlag().get();
        assertThat(ytelseGrunnlag.getOpprinneligIdentdato().get()).isEqualTo(OPPRINNELIG_IDENTDATO);
        assertThat(ytelseGrunnlag.getInntektsgrunnlagProsent().get().getVerdi()).isEqualTo(new BigDecimal(99.00));
        assertThat(ytelseGrunnlag.getDekningsgradProsent().get().getVerdi()).isEqualTo(new BigDecimal(98.00));

        assertThat(ytelseGrunnlag.getYtelseStørrelse()).hasSize(1);
        YtelseStørrelse ytelseStørrelse = ytelseGrunnlag.getYtelseStørrelse().iterator().next();
        assertThat(ytelseStørrelse.getBeløp().getVerdi()).isEqualTo(new BigDecimal(100000.50));

        Optional<Virksomhet> virksomhetOpt = ytelseStørrelse.getVirksomhet();
        assertThat(virksomhetOpt).hasValueSatisfying(virksomhet -> {
            assertThat(virksomhet.getOrgnr()).isEqualTo("41414141");
            assertThat(virksomhet.getNavn()).isEqualTo("YtelseVirksomheten");
        });

        Collection<YtelseAnvist> ytelseAnvist = ytelse.getYtelseAnvist();
        assertThat(ytelseAnvist).hasSize(1);
        YtelseAnvist anvist = ytelseAnvist.iterator().next();
        assertThat(anvist.getAnvistFOM()).isEqualTo(ANVIST_FOM);
        assertThat(anvist.getAnvistTOM()).isEqualTo(ANVIST_TOM);
        assertThat(anvist.getDagsats().get()).isEqualTo(new BigDecimal(500.00));
        assertThat(anvist.getUtbetalingsgradProsent().get()).isEqualTo(new BigDecimal(180.00));
    }

    private YtelseBuilder lagYtelse(InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder) {
        VirksomhetEntitet ytelseVirksomheten = new VirksomhetEntitet.Builder()
            .medOrgnr("41414141")
            .medNavn("YtelseVirksomheten")
            .oppdatertOpplysningerNå()
            .build();
        repositoryRule.getEntityManager().persist(ytelseVirksomheten);
        Saksnummer sakId = new Saksnummer("1200094");
        YtelseBuilder ytelselseBuilder = aktørYtelseBuilder.getYtelselseBuilderForType(Fagsystem.FPSAK, RelatertYtelseType.SYKEPENGER, sakId);
        ytelselseBuilder.tilbakestillAnvisteYtelser();
        return ytelselseBuilder.medKilde(Fagsystem.INFOTRYGD)
            .medYtelseType(RelatertYtelseType.FORELDREPENGER)
            .medBehandlingsTema(TemaUnderkategori.UDEFINERT)
            .medStatus(RelatertYtelseTilstand.AVSLUTTET)
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(FOM_DATO, TOM_DATO))
            .medSaksnummer(sakId)
            .medYtelseGrunnlag(
                ytelselseBuilder.getGrunnlagBuilder()
                    .medOpprinneligIdentdato(OPPRINNELIG_IDENTDATO)
                    .medInntektsgrunnlagProsent(new BigDecimal(99.00))
                    .medDekningsgradProsent(new BigDecimal(98.00))
                    .medYtelseStørrelse(YtelseStørrelseBuilder.ny()
                        .medBeløp(new BigDecimal(100000.50))
                        .medVirksomhet(ytelseVirksomheten)
                        .build())
                    .build())
            .medYtelseAnvist(ytelselseBuilder.getAnvistBuilder()
                .medAnvistPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(ANVIST_FOM, ANVIST_TOM))
                .medDagsats(new BigDecimal(500.00))
                .medUtbetalingsgradProsent(new BigDecimal(180.00))
                .build());
    }

    @Test
    public void skal_lagre_ned_oppgitt_opptjening() {
        final Personinfo personinfo = new Personinfo.Builder()
            .medNavn("Navn navnesen")
            .medAktørId(AKTØRID)
            .medFødselsdato(LocalDate.now().minusYears(20))
            .medLandkode(Landkoder.NOR)
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medPersonIdent(new PersonIdent("12345678901"))
            .medForetrukketSpråk(Språkkode.nb)
            .build();
        final Fagsak fagsak = Fagsak.opprettNy(NavBruker.opprettNy(personinfo));
        fagsakRepository.opprettNy(fagsak);
        final Behandling.Builder builder = Behandling.forFørstegangssøknad(fagsak);
        final Behandling behandling = builder.build();
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));

        LocalDate fraOgMed = LocalDate.now().minusMonths(1);
        LocalDate tilOgMed = LocalDate.now().plusMonths(1);
        DatoIntervallEntitet periode = DatoIntervallEntitet.fraOgMedTilOgMed(fraOgMed, tilOgMed);

        OppgittOpptjeningBuilder.EgenNæringBuilder egenNæringBuilder = OppgittOpptjeningBuilder.EgenNæringBuilder.ny();
        UtenlandskVirksomhetEntitet svenska_stat = new UtenlandskVirksomhetEntitet(Landkoder.SWE, "Svenska Stat");
        egenNæringBuilder
            .medPeriode(periode)
            .medUtenlandskVirksomhet(svenska_stat)
            .medBegrunnelse("Vet ikke")
            .medBruttoInntekt(BigDecimal.valueOf(100000))
            .medRegnskapsførerNavn("Jacob")
            .medRegnskapsførerTlf("+46678456345")
            .medVirksomhetType(VirksomhetType.FISKE);

        OppgittOpptjeningBuilder.OppgittArbeidsforholdBuilder oppgittArbeidsforholdBuilder = OppgittOpptjeningBuilder.OppgittArbeidsforholdBuilder.ny();
        oppgittArbeidsforholdBuilder
            .medPeriode(periode)
            .medArbeidType(ArbeidType.MARITIMT_ARBEIDSFORHOLD)
            .medErUtenlandskInntekt(true)
            .medUtenlandskVirksomhet(svenska_stat);

        OppgittOpptjeningBuilder oppgittOpptjeningBuilder = OppgittOpptjeningBuilder.ny();
        oppgittOpptjeningBuilder
            .leggTilAnnenAktivitet(new AnnenAktivitetEntitet(periode, ArbeidType.MILITÆR_ELLER_SIVILTJENESTE))
            .leggTilEgneNæringer(Arrays.asList(egenNæringBuilder))
            .leggTilOppgittArbeidsforhold(oppgittArbeidsforholdBuilder);

        //Act
        inntektArbeidYtelseRepository.lagre(behandling, oppgittOpptjeningBuilder);

        //Assert
        InntektArbeidYtelseGrunnlag grunnlag = inntektArbeidYtelseRepository.hentAggregat(behandling, null);
        Optional<OppgittOpptjening> oppgittOpptjeningOpt = grunnlag.getOppgittOpptjening();
        assertThat(oppgittOpptjeningOpt).isPresent();
        if (oppgittOpptjeningOpt.isPresent()) {
            OppgittOpptjening oppgittOpptjening = oppgittOpptjeningOpt.get();
            assertThat(oppgittOpptjening.getAnnenAktivitet()).hasSize(1);
            assertThat(oppgittOpptjening.getEgenNæring()).hasSize(1);
            assertThat(oppgittOpptjening.getOppgittArbeidsforhold()).hasSize(1);
        }
    }

    @Test
    public void skal_kunne_lagre_overstyrt() {
        final Personinfo personinfo = new Personinfo.Builder()
            .medNavn("Navn navnesen")
            .medAktørId(AKTØRID)
            .medFødselsdato(LocalDate.now().minusYears(20))
            .medLandkode(Landkoder.NOR)
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medPersonIdent(new PersonIdent("12345678901"))
            .medForetrukketSpråk(Språkkode.nb)
            .build();
        final Fagsak fagsak = Fagsak.opprettNy(NavBruker.opprettNy(personinfo));
        fagsakRepository.opprettNy(fagsak);
        final Behandling.Builder builder = Behandling.forFørstegangssøknad(fagsak);
        final Behandling behandling = builder.build();
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));

        Virksomhet virksomhet = opprettVirksomhet();
        final Arbeidsgiver virksomhet1 = Arbeidsgiver.virksomhet(virksomhet);
        InntektArbeidYtelseAggregatBuilder bekreftet = opprettInntektArbeidYtelseAggregatForYrkesaktivitet(AKTØRID, AREBIDSFORHOLD_ID.getReferanse(), virksomhet1);
        inntektArbeidYtelseRepository.lagre(behandling, bekreftet);

        LocalDate fraOgMed = LocalDate.now().minusWeeks(2);
        LocalDate tilOgMed = LocalDate.now().plusMonths(2);
        InntektArbeidYtelseAggregatBuilder saksbehandlet = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonType.SAKSBEHANDLET);
        final Optional<ArbeidsforholdInformasjon> informasjon = inntektArbeidYtelseRepository.hentArbeidsforholdInformasjon(behandling);
        String arbeidsforholdId = informasjon.get().finnForEkstern(virksomhet1, AREBIDSFORHOLD_ID).getReferanse();
        final InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = saksbehandlet.getAktørArbeidBuilder(AKTØRID);
        YrkesaktivitetBuilder yrkesaktivitetBuilder = aktørArbeidBuilder
            .getYrkesaktivitetBuilderForNøkkelAvType(new Opptjeningsnøkkel(arbeidsforholdId, null, null), ArbeidType.UDEFINERT);
        yrkesaktivitetBuilder
            .leggTilAktivitetsAvtale(yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder()
                .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fraOgMed, tilOgMed))
                .medProsentsats(BigDecimal.TEN)
                .medAntallTimer(BigDecimal.valueOf(20.4d))
                .medAntallTimerFulltid(BigDecimal.valueOf(10.2d)));
        aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitetBuilder);
        saksbehandlet.leggTilAktørArbeid(aktørArbeidBuilder);
        //Act
        inntektArbeidYtelseRepository.lagre(behandling, saksbehandlet);

        InntektArbeidYtelseGrunnlag grunnlag = inntektArbeidYtelseRepository.hentAggregat(behandling, null);

        assertThat(grunnlag.getOpplysningerFørSkjæringstidspunkt()).isPresent();
        assertThat(grunnlag.getOpplysningerFørSkjæringstidspunkt().get().getAktørArbeid().iterator().next().getYrkesaktiviteter().iterator().next().getAktivitetsAvtaler()).hasSize(1);
        assertThat(grunnlag.harBlittSaksbehandlet()).isTrue();
        assertThat(grunnlag.getSaksbehandletVersjon().get().getAktørArbeid().iterator().next().getYrkesaktiviteter().iterator().next().getAktivitetsAvtaler()).hasSize(1);
    }

    private Virksomhet opprettVirksomhet() {
        Virksomhet virksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr("21542512")
            .medNavn("Virksomheten")
            .medRegistrert(LocalDate.now().minusYears(2L))
            .medOppstart(LocalDate.now().minusYears(1L))
            .oppdatertOpplysningerNå()
            .build();
        VirksomhetRepository virksomhetRepository = repositoryProvider.getVirksomhetRepository();
        virksomhetRepository.lagre(virksomhet);
        return virksomhet;
    }

    private InntektArbeidYtelseAggregatBuilder opprettInntektArbeidYtelseAggregatForYrkesaktivitet(AktørId aktørId, String arbeidsforhold, Arbeidsgiver virksomhet1) {
        InntektArbeidYtelseAggregatBuilder builder = InntektArbeidYtelseAggregatBuilder
            .oppdatere(Optional.empty(), VersjonType.REGISTER);

        LocalDate fraOgMed = LocalDate.now().minusWeeks(1);
        LocalDate tilOgMed = LocalDate.now().plusMonths(1);

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = builder.getAktørArbeidBuilder(aktørId);
        YrkesaktivitetBuilder yrkesaktivitetBuilder = aktørArbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(new Opptjeningsnøkkel(arbeidsforhold, null, null),
            ArbeidType.UDEFINERT);
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();
        YrkesaktivitetEntitet.PermisjonBuilder permisjonBuilder = yrkesaktivitetBuilder.getPermisjonBuilder();

        Permisjon permisjon = permisjonBuilder
            .medProsentsats(BigDecimal.valueOf(100))
            .medPeriode(fraOgMed, tilOgMed)
            .medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.UDEFINERT)
            .build();

        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale = aktivitetsAvtaleBuilder
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fraOgMed, tilOgMed))
            .medProsentsats(BigDecimal.TEN)
            .medAntallTimer(BigDecimal.valueOf(20.4d))
            .medAntallTimerFulltid(BigDecimal.valueOf(10.2d));

        yrkesaktivitetBuilder
            .medArbeidType(ArbeidType.UDEFINERT)
            .medArbeidsgiver(virksomhet1)
            .medArbeidsforholdId(AREBIDSFORHOLD_ID)
            .leggTilAktivitetsAvtale(aktivitetsAvtale)
            .leggTilPermisjon(permisjon);

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeid = aktørArbeidBuilder
            .leggTilYrkesaktivitet(yrkesaktivitetBuilder);

        builder.leggTilAktørArbeid(aktørArbeid);

        return builder;
    }
}
