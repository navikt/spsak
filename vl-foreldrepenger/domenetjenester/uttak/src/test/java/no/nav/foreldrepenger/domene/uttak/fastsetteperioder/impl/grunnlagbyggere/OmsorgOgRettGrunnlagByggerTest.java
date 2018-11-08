package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl.grunnlagbyggere;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittRettighetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PeriodeAleneOmsorgEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PerioderAleneOmsorgEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlagBuilder;

public class OmsorgOgRettGrunnlagByggerTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());

    @Test
    public void skalLeggeTilHvemSomHarRett_SøkerMorHarRettAnnenForeldreHarIkkeRett() {
        Behandling behandling = morMedRett(true, false);

        FastsettePeriodeGrunnlag grunnlag = byggGrunnlag(behandling);
        assertThat(grunnlag.isMorRett()).isTrue();
        assertThat(grunnlag.isFarRett()).isFalse();
    }

    @Test
    public void skalLeggeTilHvemSomHarRett_SøkerMorHarRettAnnenForeldreHarRett() {
        Behandling behandling = morMedRett(true, true);

        FastsettePeriodeGrunnlag grunnlag = byggGrunnlag(behandling);

        assertThat(grunnlag.isMorRett()).isTrue();
        assertThat(grunnlag.isFarRett()).isTrue();
    }

    @Test
    public void skalLeggeTilHvemSomHarRett_SøkerFarHarRettAnnenForeldreHarRett() {
        Behandling behandling = farMedRett(true, true);

        FastsettePeriodeGrunnlag grunnlag = byggGrunnlag(behandling);

        assertThat(grunnlag.isMorRett()).isTrue();
        assertThat(grunnlag.isFarRett()).isTrue();
    }

    @Test
    public void skalLeggeTilHvemSomHarRett_SøkerFarHarRettAnnenForeldreHarIkkeRett() {
        Behandling behandling = farMedRett(true, false);

        FastsettePeriodeGrunnlag grunnlag = byggGrunnlag(behandling);

        assertThat(grunnlag.isMorRett()).isFalse();
        assertThat(grunnlag.isFarRett()).isTrue();
    }

    @Test
    public void skalLeggeTilHvemSomHarRett_SøkerFarHarIkkeRettAnnenForeldreHarIkkeRett() {
        Behandling behandling = farMedRett(false, false);

        FastsettePeriodeGrunnlag grunnlag = byggGrunnlag(behandling);

        assertThat(grunnlag.isMorRett()).isFalse();
        assertThat(grunnlag.isFarRett()).isFalse();
    }

    @Test
    public void skalLeggeTilHvemSomHarRett_SøkerFarHarIkkeRettAnnenForeldreHarRett() {
        Behandling behandling = farMedRett(false, true);

        FastsettePeriodeGrunnlag grunnlag = byggGrunnlag(behandling);

        assertThat(grunnlag.isMorRett()).isTrue();
        assertThat(grunnlag.isFarRett()).isFalse();
    }

    @Test
    public void skalLeggeTilHvemSomHarRett_SøkerMorHarIkkeRettAnnenForeldreHarRett() {
        Behandling behandling = morMedRett(false, true);

        FastsettePeriodeGrunnlag grunnlag = byggGrunnlag(behandling);

        assertThat(grunnlag.isMorRett()).isFalse();
        assertThat(grunnlag.isFarRett()).isTrue();
    }

    @Test
    public void skalLeggeHarAleneomsorgHvisAleneomsorg() {
        Behandling behandling = medAleneomsorg(true);

        FastsettePeriodeGrunnlag grunnlag = byggGrunnlag(behandling);

        assertThat(grunnlag.harAleneomsorg()).isTrue();
    }

    @Test
    public void skalIkkeLeggeTilHarAleneomsorgHvisIkkeAleneomsorg() {
        Behandling behandling = medAleneomsorg(false);

        FastsettePeriodeGrunnlag grunnlag = byggGrunnlag(behandling);

        assertThat(grunnlag.harAleneomsorg()).isFalse();
    }

    private Behandling medAleneomsorg(boolean harAleneomsorg) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        scenario.medFordeling(new OppgittFordelingEntitet(Collections.emptyList(), true));
        scenario.medBekreftetHendelse().medFødselsDato(LocalDate.now().minusWeeks(2));
        scenario.medBehandlingVedtak()
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medAnsvarligSaksbehandler("saksbehandler")
            .medVedtaksdato(LocalDate.now());
        scenario.medOppgittRettighet(new OppgittRettighetEntitet(true, true, false));
        if (harAleneomsorg) {
            PerioderAleneOmsorgEntitet perioderAleneOmsorg = new PerioderAleneOmsorgEntitet();
            perioderAleneOmsorg.leggTil(new PeriodeAleneOmsorgEntitet(LocalDate.now(), LocalDate.now()));
            scenario.medPeriodeMedAleneomsorg(perioderAleneOmsorg);
        }

        return scenario.lagre(repositoryProvider);
    }

    private OmsorgOgRettGrunnlagBygger grunnlagBygger() {
        return new OmsorgOgRettGrunnlagBygger();
    }

    private Behandling morMedRett(boolean søkerHarRett, boolean annenForelderHarRett) {
        return scenarioMedRett(ScenarioMorSøkerForeldrepenger.forFødsel(), søkerHarRett, annenForelderHarRett);
    }

    private Behandling farMedRett(boolean søkerHarRett, boolean annenForelderHarRett) {
        return scenarioMedRett(ScenarioFarSøkerForeldrepenger.forFødsel(), søkerHarRett, annenForelderHarRett);
    }

    private Behandling scenarioMedRett(AbstractTestScenario scenario, boolean søkerRett, boolean annenForelderHarRett) {
        scenario.medFordeling(new OppgittFordelingEntitet(Collections.emptyList(), true));
        scenario.medBekreftetHendelse().medFødselsDato(LocalDate.now().minusWeeks(2));
        scenario.medBehandlingVedtak()
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medAnsvarligSaksbehandler("saksbehandler")
            .medVedtaksdato(LocalDate.now());
        scenario.medOppgittRettighet(new OppgittRettighetEntitet(annenForelderHarRett, true, false));

        Behandling behandling = scenario.lagre(repositoryProvider);
        if (!søkerRett) {
            Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder()
                .medBehandlingResultatType(BehandlingResultatType.AVSLÅTT)
                .buildFor(behandling);
            VilkårResultat vilkårResultat = VilkårResultat.builder().medVilkårResultatType(VilkårResultatType.AVSLÅTT).buildFor(behandlingsresultat);
            behandlingsresultat.medOppdatertVilkårResultat(vilkårResultat);
            behandling.setBehandlingresultat(behandlingsresultat);
        }
        return behandling;
    }

    private FastsettePeriodeGrunnlag byggGrunnlag(Behandling behandling) {
        OmsorgOgRettGrunnlagBygger bygger = grunnlagBygger();
        YtelseFordelingAggregat yfa = repositoryProvider.getYtelsesFordelingRepository().hentAggregat(behandling);
        FastsettePeriodeGrunnlagBuilder grunnlagBuilder = FastsettePeriodeGrunnlagBuilder.create();
        bygger.byggGrunnlag(grunnlagBuilder, behandling, yfa);

        return grunnlagBuilder.build();
    }

}
