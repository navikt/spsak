package no.nav.foreldrepenger.domene.ytelsefordeling.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PeriodeUttakDokumentasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PeriodeUttakDokumentasjonEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PerioderUttakDokumentasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.UttakDokumentasjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.ytelsefordeling.YtelseFordelingTjeneste;

public class YtelseFordelingTjenesteImplTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private YtelseFordelingTjeneste tjeneste = new YtelseFordelingTjenesteImpl(repositoryProvider);


    @Test
    public void test_lagring_perioderuttakdokumentasjon() {
        final LocalDate enDag = LocalDate.of(2018, 3, 15);
        List<PeriodeUttakDokumentasjon> dokumentasjonPerioder = Arrays.asList(
            new PeriodeUttakDokumentasjonEntitet(enDag, enDag.plusDays(1), UttakDokumentasjonType.SYK_SØKER),
            new PeriodeUttakDokumentasjonEntitet(enDag.plusDays(4), enDag.plusDays(7), UttakDokumentasjonType.SYK_SØKER)
        );

        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        Behandling behandling = scenario.lagre(repositoryProvider);

        OppgittPeriode opprinnelig = OppgittPeriodeBuilder.ny()
            .medPeriode(enDag, enDag.plusDays(7))
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .build();

        OppgittFordelingEntitet oppgittPerioder = new OppgittFordelingEntitet(Collections.singletonList(opprinnelig), true);

        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, oppgittPerioder);


        OppgittPeriode ny = OppgittPeriodeBuilder.ny()
            .medPeriode(enDag, enDag.plusDays(7))
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .build();

        tjeneste.overstyrSøknadsperioder(behandling, Collections.singletonList(ny), dokumentasjonPerioder);

        Optional<PerioderUttakDokumentasjon> perioderUttak = tjeneste.hentAggregat(behandling).getPerioderUttakDokumentasjon();

        assertThat(perioderUttak).isNotNull();
        List<PeriodeUttakDokumentasjon> perioder = perioderUttak.get().getPerioder();
        assertThat(perioder).isNotEmpty();
        assertThat(perioder).hasSize(2);
    }
}
