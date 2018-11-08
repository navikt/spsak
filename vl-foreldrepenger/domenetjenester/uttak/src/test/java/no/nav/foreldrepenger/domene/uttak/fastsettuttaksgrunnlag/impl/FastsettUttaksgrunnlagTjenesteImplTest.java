package no.nav.foreldrepenger.domene.uttak.fastsettuttaksgrunnlag.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordeling;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class FastsettUttaksgrunnlagTjenesteImplTest {

    @Rule
    public RepositoryRule repositoryRule = new UnittestRepositoryRule();
    public BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());

    @Test
    public void skal_kopiere_søknadsperioder_fra_forrige_behandling_hvis_forrige_behandling_ikke_har_uttaksresultat() {
        FastsettUttaksgrunnlagTjenesteImpl tjeneste = new FastsettUttaksgrunnlagTjenesteImpl(repositoryProvider,
            mock(EndringsdatoFørstegangsbehandlingUtleder.class),
            mock(EndringsdatoRevurderingUtleder.class));

        LocalDate førsteUttaksdato = LocalDate.now();
        OppgittPeriode periode = OppgittPeriodeBuilder.ny().medPeriode(førsteUttaksdato, LocalDate.now().plusDays(10))
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .build();
        OppgittFordeling oppgittFordelingForrigeBehandling = new OppgittFordelingEntitet(Collections.singletonList(periode), true);

        ScenarioFarSøkerForeldrepenger førstegangsbehandlingScenario = ScenarioFarSøkerForeldrepenger.forFødsel();
        førstegangsbehandlingScenario.medFordeling(oppgittFordelingForrigeBehandling);
        førstegangsbehandlingScenario.medFødselAdopsjonsdato(Collections.singletonList(LocalDate.now()));
        førstegangsbehandlingScenario.medBehandlingType(BehandlingType.FØRSTEGANGSSØKNAD);
        Behandling førstegangsbehandling = førstegangsbehandlingScenario.lagre(repositoryProvider);

        ScenarioFarSøkerForeldrepenger revurdering = ScenarioFarSøkerForeldrepenger.forFødsel();
        revurdering.medOriginalBehandling(førstegangsbehandling, BehandlingÅrsakType.RE_OPPLYSNINGER_OM_OPPTJENING);
        revurdering.medFødselAdopsjonsdato(Collections.singletonList(LocalDate.now()));
        revurdering.medBehandlingType(BehandlingType.REVURDERING);
        revurdering.medFordeling(new OppgittFordelingEntitet(Collections.emptyList(), true));

        Behandling revurderingBehandling = revurdering.lagre(repositoryProvider);

        tjeneste.fastsettUttaksgrunnlag(revurderingBehandling);

        YtelseFordelingAggregat forrigeBehandlingFordeling = repositoryProvider.getYtelsesFordelingRepository().hentAggregat(førstegangsbehandling);
        YtelseFordelingAggregat resultat = repositoryProvider.getYtelsesFordelingRepository().hentAggregat(revurderingBehandling);

        assertThat(resultat.getOppgittFordeling().getOppgittePerioder()).isEqualTo(forrigeBehandlingFordeling.getOppgittFordeling().getOppgittePerioder());
        assertThat(resultat.getOppgittFordeling().getErAnnenForelderInformert()).isEqualTo(forrigeBehandlingFordeling.getOppgittFordeling().getErAnnenForelderInformert());
    }
}
