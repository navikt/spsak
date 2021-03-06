package no.nav.foreldrepenger.behandling.aksjonspunkt;

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.EntityManager;

import no.nav.foreldrepenger.behandling.historikk.HistorikkTjenesteAdapter;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;

public class AbstractOverstyringshåndtererTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private EntityManager em = repoRule.getEntityManager();

    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(em);
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());

    private AksjonspunktRepository aksjonspunktRepository = new AksjonspunktRepositoryImpl(em);

    @SuppressWarnings("unchecked")
    @Test
    public void skal_reaktivere_inaktivt_aksjonspunkt() throws Exception {
        Behandling behandling = ScenarioMorSøkerEngangsstønad.forDefaultAktør().lagre(repositoryProvider, resultatRepositoryProvider);
        AksjonspunktDefinisjon aksjonspunktDefinisjon = AksjonspunktDefinisjon.OVERSTYRING_AV_OPPTJENINGSVILKÅRET;
        Aksjonspunkt ap = aksjonspunktRepository.leggTilAksjonspunkt(behandling, aksjonspunktDefinisjon);
        aksjonspunktRepository.setTilUtført(ap, "OK");
        aksjonspunktRepository.deaktiver(ap);

        OverstyringAksjonspunktDto dto = new OverstyringAksjonspunktDto() {
            @Override
            public String getAvslagskode() {
                return null;
            }

            @Override
            public String getKode() {
                return aksjonspunktDefinisjon.getKode();
            }

            @Override
            public boolean getErVilkarOk() {
                return false;
            }
        };

        new TestOversyringshåndterer().håndterAksjonspunktForOverstyring(dto, behandling);

        assertThat(behandling.getAksjonspunktFor(aksjonspunktDefinisjon).erAktivt()).isTrue();
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
