package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.overstyring;

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.EntityManager;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.overstyring.OverstyringAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.overstyring.OverstyringOpptjeningsvilkåretDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

public class AbstractOverstyringshåndtererTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private EntityManager em = repoRule.getEntityManager();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(em);

    private AksjonspunktRepository aksjonspunktRepository = new AksjonspunktRepositoryImpl(em);

    @SuppressWarnings("unchecked")
    @Test
    public void skal_reaktivere_inaktivt_aksjonspunkt() throws Exception {
        Behandling behandling = ScenarioMorSøkerEngangsstønad.forFødsel().lagre(repositoryProvider);
        Aksjonspunkt ap = aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.OVERSTYRING_AV_OPPTJENINGSVILKÅRET);
        aksjonspunktRepository.setTilUtført(ap, "OK");
        aksjonspunktRepository.deaktiver(ap);

        OverstyringAksjonspunktDto dto = new OverstyringOpptjeningsvilkåretDto(false, "ikke opptjeng", "asdf");

        new TestOversyringshåndterer().håndterAksjonspunktForOverstyring(dto, behandling);

        assertThat(behandling.getAksjonspunktFor(AksjonspunktDefinisjon.OVERSTYRING_AV_OPPTJENINGSVILKÅRET).erAktivt()).isTrue();
    }

    @SuppressWarnings("rawtypes")
    private class TestOversyringshåndterer extends AbstractOverstyringshåndterer {

        TestOversyringshåndterer() {
            super(repositoryProvider, Mockito.mock(HistorikkTjenesteAdapter.class), AksjonspunktDefinisjon.OVERSTYRING_AV_OPPTJENINGSVILKÅRET);
        }

        @Override
        protected void lagHistorikkInnslag(Behandling behandling, OverstyringAksjonspunktDto dto) {
            return;
        }

        @Override
        public OppdateringResultat håndterOverstyring(OverstyringAksjonspunktDto dto, Behandling behandling, BehandlingskontrollKontekst kontekst) {
            return OppdateringResultat.utenOveropp();
        }
    }

}
