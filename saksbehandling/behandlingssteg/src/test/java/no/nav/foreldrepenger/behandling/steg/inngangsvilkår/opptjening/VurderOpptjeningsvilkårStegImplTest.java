package no.nav.foreldrepenger.behandling.steg.inngangsvilkår.opptjening;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.AktørInntektEntitet.InntektBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektEntitet.InntektspostBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Opptjeningsnøkkel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektspostType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.sykemelding.SykemeldingBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.sykemelding.SykemeldingerBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.inngangsvilkaar.RegelOrkestrerer;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Prosentsats;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

@RunWith(CdiRunner.class)
public class VurderOpptjeningsvilkårStegImplTest {

    private static final AktørId AKTØR_ID = new AktørId("99");

    @Rule
    public final RepositoryRule repoRule = new UnittestRepositoryRule();

    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());

    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    private VirksomhetRepository virksomhetRepository = new VirksomhetRepositoryImpl(repoRule.getEntityManager());

    @Inject
    public RegelOrkestrerer regelOrkestrerer;

    private final LocalDate idag = LocalDate.now();
    private final LocalDate fraOgMed = idag.minusDays(200);
    private final LocalDate tilOgMed = idag.plusDays(200);

    private VirksomhetEntitet virksomhetA;
    private VirksomhetEntitet virksomhetB;

    @Before
    public void setup() {
        virksomhetA = new VirksomhetEntitet.Builder().medNavn("OrgA").medOrgnr("100").oppdatertOpplysningerNå().build();
        virksomhetRepository.lagre(virksomhetA);

        virksomhetB = new VirksomhetEntitet.Builder().medNavn("OrgB").medOrgnr("200").oppdatertOpplysningerNå().build();
        virksomhetRepository.lagre(virksomhetB);
    }

    @Test
    public void skal_lagre_resultat_av_opptjeningsvilkår() throws Exception {

        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        AktørId aktørId = AKTØR_ID;
        scenario.medBruker(aktørId, NavBrukerKjønn.KVINNE);

        scenario.leggTilVilkår(VilkårType.OPPTJENINGSPERIODEVILKÅR, VilkårUtfallType.IKKE_VURDERT); // må kjøres opp for
                                                                                                    // å få Opptjening
        scenario.leggTilVilkår(VilkårType.OPPTJENINGSVILKÅRET, VilkårUtfallType.IKKE_VURDERT); // vurderer faktisk
                                                                                               // opptjening
        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseBuilder = scenario.getInntektArbeidYtelseScenarioTestBuilder().getKladd();
        // Virksomhet A
        lagAktørArbeid(inntektArbeidYtelseBuilder, aktørId, virksomhetA, fraOgMed, tilOgMed, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        for (LocalDate dt = fraOgMed; dt.isBefore(tilOgMed); dt = dt.plusMonths(2)) {
            // lag inntekt annen hver måned
            lagInntekt(inntektArbeidYtelseBuilder, aktørId, virksomhetA, dt, dt.plusMonths(1));
        }
        // Virksomhet B
        lagAktørArbeid(inntektArbeidYtelseBuilder, aktørId, virksomhetB, fraOgMed, tilOgMed, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        lagInntekt(inntektArbeidYtelseBuilder, aktørId, virksomhetB, fraOgMed, idag);
        SykemeldingerBuilder builder = scenario.getSykemeldingerBuilder();
        SykemeldingBuilder sykemeldingBuilder = builder.sykemeldingBuilder("ASDF-ASDF-ASDF");
        sykemeldingBuilder.medPeriode(idag, idag.plusDays(36))
            .medArbeidsgiver(Arbeidsgiver.person(new AktørId(1234L)))
            .medGrad(new Prosentsats(100));
        builder.medSykemelding(sykemeldingBuilder);
        scenario.medSykemeldinger(builder);
        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        Fagsak fagsak = behandling.getFagsak();
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(),behandlingRepository.taSkriveLås(behandling));

        // Act
        // opprett opptjening
        new FastsettOpptjeningsperiodeStegImpl(repositoryProvider, resultatRepositoryProvider, regelOrkestrerer)
                .utførSteg(kontekst);

        // vurder vilkåret
        new VurderOpptjeningsvilkårStegImpl(repositoryProvider, resultatRepositoryProvider, regelOrkestrerer)
                .utførSteg(kontekst);
    }

    private void lagAktørArbeid(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder, AktørId aktørId, Virksomhet virksomhet,
            LocalDate fom, LocalDate tom, ArbeidType arbeidType) {
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = inntektArbeidYtelseAggregatBuilder
                .getAktørArbeidBuilder(aktørId);
        Opptjeningsnøkkel opptjeningsnøkkel = Opptjeningsnøkkel.forOrgnummer(virksomhet.getOrgnr());

        YrkesaktivitetBuilder yrkesaktivitetBuilder = aktørArbeidBuilder
                .getYrkesaktivitetBuilderForNøkkelAvType(opptjeningsnøkkel, arbeidType);
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();

        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale = aktivitetsAvtaleBuilder.
                medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom))
                .medProsentsats(BigDecimal.TEN);
        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(aktivitetsAvtale)
                .medArbeidType(arbeidType)
                .medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet));

        aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitetBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
    }

    private void lagInntekt(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder, AktørId aktørId, Virksomhet virksomhet,
            LocalDate fom, LocalDate tom) {
        Opptjeningsnøkkel opptjeningsnøkkel = Opptjeningsnøkkel.forOrgnummer(virksomhet.getOrgnr());

        AktørInntektBuilder aktørInntektBuilder = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder(aktørId);

        InntektBuilder inntektBuilder = aktørInntektBuilder.getInntektBuilder(InntektsKilde.INNTEKT_OPPTJENING, opptjeningsnøkkel);
        InntektEntitet.InntektspostBuilder inntektspost = InntektspostBuilder.ny()
                .medBeløp(BigDecimal.TEN)
            .medPeriode(fom, tom)
                .medInntektspostType(InntektspostType.LØNN);
        inntektBuilder.leggTilInntektspost(inntektspost);

        aktørInntektBuilder.leggTilInntekt(inntektBuilder);

        inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntektBuilder);

    }
}
