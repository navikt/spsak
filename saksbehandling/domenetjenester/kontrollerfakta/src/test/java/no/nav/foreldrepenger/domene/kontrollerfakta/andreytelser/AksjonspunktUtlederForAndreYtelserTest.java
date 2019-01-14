package no.nav.foreldrepenger.domene.kontrollerfakta.andreytelser;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.SykefraværBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.SykefraværPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.SykefraværPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class AksjonspunktUtlederForAndreYtelserTest {

    public static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now().minusMonths(1);
    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repositoryRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repositoryRule.getEntityManager());
    private InntektArbeidYtelseRepository repository = repositoryProvider.getInntektArbeidYtelseRepository();

    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider, resultatRepositoryProvider);
    private AksjonspunktUtlederForAndreYtelser utleder = new AksjonspunktUtlederForAndreYtelser(repositoryProvider, skjæringstidspunktTjeneste);
    private Behandling behandling;

    @Before
    public void setUp() throws Exception {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        scenario.removeDodgyDefaultInntektArbeidYTelse();
        SykefraværBuilder sfBuilder = scenario.getSykefraværBuilder();
        SykefraværPeriodeBuilder builder = sfBuilder.periodeBuilder();
        builder.medType(SykefraværPeriodeType.SYKEMELDT)
            .medPeriode(SKJÆRINGSTIDSPUNKT, LocalDate.now())
            .medArbeidsgiver(Arbeidsgiver.person(new AktørId("1234")));
        sfBuilder.leggTil(builder);
        scenario.medSykefravær(sfBuilder);

        behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
    }


    @Test
    public void skal_ikke_gi_aksjonspunkt_hvis_ytelse_avsluttet_før_siste_2_måneder() {
        opprettYtelse(SKJÆRINGSTIDSPUNKT.minusMonths(10), SKJÆRINGSTIDSPUNKT.minusDays(70), Fagsystem.ARENA, RelatertYtelseType.ARBEIDSAVKLARINGSPENGER);
        List<AksjonspunktDefinisjon> aksjonspunktResultat = utleder.utledAksjonspunkterFor(behandling).stream().map(AksjonspunktResultat::getAksjonspunktDefinisjon).collect(Collectors.toList());

        assertThat(aksjonspunktResultat).isEmpty();
    }

    @Test
    public void skal_gi_aksjonspunkt_hvis_ytelse_siste_2_måneder_uten_overlapp() {
        opprettYtelse(SKJÆRINGSTIDSPUNKT.minusMonths(10), SKJÆRINGSTIDSPUNKT.minusMonths(1), Fagsystem.ARENA, RelatertYtelseType.ARBEIDSAVKLARINGSPENGER);

        List<AksjonspunktDefinisjon> aksjonspunktResultat = utleder.utledAksjonspunkterFor(behandling).stream().map(AksjonspunktResultat::getAksjonspunktDefinisjon).collect(Collectors.toList());

        assertThat(aksjonspunktResultat).containsOnly(AksjonspunktDefinisjon.VURDER_ANDRE_YTELSER);
    }

    @Test
    public void skal_ikke_gi_aksjonspunkt_hvis_ytelse_siste_2_måneder_uten_overlapp_hvis_sykepenger() {
        opprettYtelse(SKJÆRINGSTIDSPUNKT.minusMonths(10), SKJÆRINGSTIDSPUNKT.minusMonths(1), Fagsystem.INFOTRYGD, RelatertYtelseType.SYKEPENGER);

        List<AksjonspunktDefinisjon> aksjonspunktResultat = utleder.utledAksjonspunkterFor(behandling)
            .stream()
            .map(AksjonspunktResultat::getAksjonspunktDefinisjon)
            .collect(Collectors.toList());

        assertThat(aksjonspunktResultat).containsOnly();
    }

    @Test
    public void skal_gi_aksjonspunkt_hvis_ytelse_siste_2_måneder_med_overlapp() {
        opprettYtelse(SKJÆRINGSTIDSPUNKT.minusMonths(10), SKJÆRINGSTIDSPUNKT.plusMonths(1), Fagsystem.ARENA, RelatertYtelseType.ARBEIDSAVKLARINGSPENGER);

        List<AksjonspunktDefinisjon> aksjonspunktResultat = utleder.utledAksjonspunkterFor(behandling).stream().map(AksjonspunktResultat::getAksjonspunktDefinisjon).collect(Collectors.toList());

        assertThat(aksjonspunktResultat).containsOnly(AksjonspunktDefinisjon.VURDER_ANDRE_YTELSER);
    }

    @Test
    public void ingen_aksjonspunkt_hvis_ikke_ytelse_siste_2_måneder() {
        List<AksjonspunktDefinisjon> aksjonspunktResultat = utleder.utledAksjonspunkterFor(behandling).stream().map(AksjonspunktResultat::getAksjonspunktDefinisjon).collect(Collectors.toList());

        assertThat(aksjonspunktResultat).isEmpty();
    }

    private void opprettYtelse(LocalDate fomDato, LocalDate tomDato, Fagsystem fagsystem, RelatertYtelseType relatertYtelseType) {
        InntektArbeidYtelseAggregatBuilder builder = repository.opprettBuilderFor(behandling, VersjonType.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder = builder.getAktørYtelseBuilder(behandling.getAktørId());
        YtelseBuilder ytelseBuilder = aktørYtelseBuilder.getYtelselseBuilderForType(fagsystem, relatertYtelseType, Saksnummer.arena("12345"));
        ytelseBuilder.medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fomDato, tomDato))
            .medStatus(RelatertYtelseTilstand.AVSLUTTET);
        aktørYtelseBuilder.leggTilYtelse(ytelseBuilder);
        builder.leggTilAktørYtelse(aktørYtelseBuilder);

        repository.lagre(behandling, builder);
    }


}
