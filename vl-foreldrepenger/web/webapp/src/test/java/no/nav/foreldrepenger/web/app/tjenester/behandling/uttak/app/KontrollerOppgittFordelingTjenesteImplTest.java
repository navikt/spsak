package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.ytelsefordeling.YtelseFordelingTjeneste;
import no.nav.foreldrepenger.domene.ytelsefordeling.impl.YtelseFordelingTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.AvklarFaktaUttakDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.ManuellAvklarFaktaUttakDto;

public class KontrollerOppgittFordelingTjenesteImplTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider behandlingRepositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    VirksomhetRepository virksomhetRepository = new VirksomhetRepositoryImpl(repositoryRule.getEntityManager());
    YtelseFordelingTjeneste ytelseFordelingTjeneste = new YtelseFordelingTjenesteImpl(behandlingRepositoryProvider);

    @Test
    public void skal_lagre_overstyrt_perioder_bekreft_aksjonspunkt() {

        //Scenario med avklar fakta uttak
        ScenarioMorSøkerForeldrepenger scenario = AvklarFaktaTestUtil.opprettScenarioMorSøkerForeldrepenger();
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_FAKTA_UTTAK,
            BehandlingStegType.VURDER_UTTAK);
        scenario.lagre(behandlingRepositoryProvider);
        // Behandling
        Behandling behandling = AvklarFaktaTestUtil.opprettBehandling(scenario);

        AvklarFaktaUttakDto dto = AvklarFaktaTestUtil.opprettDtoAvklarFaktaUttakDto();
        new KontrollerOppgittFordelingTjenesteImpl(ytelseFordelingTjeneste, behandlingRepositoryProvider, virksomhetRepository).avklarFaktaUttaksperiode(dto, behandling);

        YtelseFordelingAggregat ytelseFordelingAggregat = ytelseFordelingTjeneste.hentAggregat(behandling);

        assertThat(ytelseFordelingAggregat).isNotNull();
        List<OppgittPeriode> gjeldendeFordeling = ytelseFordelingAggregat
            .getGjeldendeSøknadsperioder()
            .getOppgittePerioder();

        assertThat(gjeldendeFordeling).isNotEmpty();
        assertThat(gjeldendeFordeling).hasSize(3);
    }

    @Test
    public void skal_lagre_overstyrt_perioder_overstyrings_aksjonspunkt() {

        //Scenario med avklar fakta uttak
        ScenarioMorSøkerForeldrepenger scenario = AvklarFaktaTestUtil.opprettScenarioMorSøkerForeldrepenger();
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.MANUELL_AVKLAR_FAKTA_UTTAK,
            BehandlingStegType.VURDER_UTTAK);
        scenario.lagre(behandlingRepositoryProvider);
        // Behandling
        Behandling behandling = AvklarFaktaTestUtil.opprettBehandling(scenario);

        ManuellAvklarFaktaUttakDto dto = AvklarFaktaTestUtil.opprettDtoManuellAvklarFaktaUttakDto();
        new KontrollerOppgittFordelingTjenesteImpl(ytelseFordelingTjeneste, behandlingRepositoryProvider, virksomhetRepository).manuellAvklarFaktaUttaksperiode(dto, behandling);

        YtelseFordelingAggregat ytelseFordelingAggregat = ytelseFordelingTjeneste.hentAggregat(behandling);

        assertThat(ytelseFordelingAggregat).isNotNull();
        List<OppgittPeriode> gjeldendeFordeling = ytelseFordelingAggregat
            .getGjeldendeSøknadsperioder()
            .getOppgittePerioder();

        assertThat(gjeldendeFordeling).isNotEmpty();
        assertThat(gjeldendeFordeling).hasSize(3);
    }

}
