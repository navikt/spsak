package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.historikk.HistorikkTjenesteAdapter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import no.nav.foreldrepenger.behandling.revurdering.impl.RevurderingTjenesteProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.TotrinnRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.TotrinnRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.TotrinnTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.VurderÅrsakTotrinnsvurdering;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.personopplysning.BasisPersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.vedtak.VedtakTjeneste;
import no.nav.foreldrepenger.domene.vedtak.impl.FatterVedtakAksjonspunkt;
import no.nav.foreldrepenger.domene.vedtak.impl.VedtakTjenesteImpl;
import no.nav.foreldrepenger.vedtakslager.LagretVedtakRepository;
import no.nav.foreldrepenger.vedtakslager.LagretVedtakRepositoryImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.vedtak.aksjonspunkt.FatterVedtakAksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.vedtak.aksjonspunkt.ForeslåVedtakAksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.vedtak.aksjonspunkt.dto.AksjonspunktGodkjenningDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.vedtak.aksjonspunkt.dto.FatterVedtakAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.vedtak.aksjonspunkt.dto.ForeslaVedtakAksjonspunktDto;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class AksjonspunktOppdatererTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());

    private AksjonspunktRepository aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
    private LagretVedtakRepository lagretVedtakRepository = new LagretVedtakRepositoryImpl(repoRule.getEntityManager());

    private FatterVedtakAksjonspunkt fatterVedtakAksjonspunkt;
    private HistorikkRepository historikkRepository = new HistorikkRepositoryImpl(repoRule.getEntityManager());
    private TotrinnRepository totrinnRepository;

    @Mock
    private BasisPersonopplysningTjeneste personopplysningTjeneste;

    @Inject
    private VedtakTjeneste vedtakTjeneste;

    @Before
    public void setup() {
        RevurderingTjenesteProvider revurderingTjenesteProvider = new RevurderingTjenesteProvider();
        totrinnRepository = new TotrinnRepositoryImpl(repoRule.getEntityManager());

        TotrinnTjeneste totrinnTjeneste = new TotrinnTjenesteImpl(repositoryProvider, resultatRepositoryProvider, totrinnRepository);

        VedtakTjeneste vedtakTjeneste = new VedtakTjenesteImpl(lagretVedtakRepository, repositoryProvider, revurderingTjenesteProvider, totrinnTjeneste);
        fatterVedtakAksjonspunkt = new FatterVedtakAksjonspunkt(repositoryProvider, vedtakTjeneste, totrinnTjeneste);
    }

    @Test
    public void bekreft_foreslå_vedtak_aksjonspkt_setter_ansvarlig_saksbehandler() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();

        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);

        ForeslaVedtakAksjonspunktDto dto = new ForeslaVedtakAksjonspunktDto("begrunnelse", null, null, false) {
        };
        ForeslåVedtakAksjonspunktOppdaterer foreslaVedtakAksjonspunktOppdaterer = new ForeslåVedtakAksjonspunktOppdaterer(
            repositoryProvider, mock(HistorikkTjenesteAdapter.class), new TotrinnTjenesteImpl(repositoryProvider, resultatRepositoryProvider, totrinnRepository), vedtakTjeneste) {
            @Override
            protected String getCurrentUserId() {
                // return test verdi
                return "hello";
            }
        };
        foreslaVedtakAksjonspunktOppdaterer
            .oppdater(dto, behandling, VilkårResultat.builder());
        assertThat(behandling.getAnsvarligSaksbehandler()).isEqualTo("hello");
    }

    @Test
    public void oppdaterer_aksjonspunkt_med_beslutters_vurdering_ved_totrinnskontroll() {

        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();

        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);

        leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.AVKLAR_OM_ER_BOSATT);

        AksjonspunktGodkjenningDto aksGodkjDto = new AksjonspunktGodkjenningDto();
        aksGodkjDto.setArsaker(new HashSet<>(Collections.singletonList(VurderÅrsak.FEIL_FAKTA)).stream().map(VurderÅrsak::new)
            .collect(Collectors.toSet()));
        aksGodkjDto.setGodkjent(false);
        String besluttersBegrunnelse = "Må ha bedre dokumentasjon.";
        aksGodkjDto.setBegrunnelse(besluttersBegrunnelse);
        aksGodkjDto.setAksjonspunktKode(AksjonspunktDefinisjon.AVKLAR_OM_ER_BOSATT);

        FatterVedtakAksjonspunktDto aksjonspunktDto = new FatterVedtakAksjonspunktDto("", Collections.singletonList(aksGodkjDto));
        new FatterVedtakAksjonspunktOppdaterer(repositoryProvider, fatterVedtakAksjonspunkt).oppdater(aksjonspunktDto, behandling,
            VilkårResultat.builder());

        Collection<Totrinnsvurdering> totrinnsvurderinger = totrinnRepository.hentTotrinnaksjonspunktvurderinger(behandling);
        assertThat(totrinnsvurderinger.size()).isEqualTo(1);
        Totrinnsvurdering totrinnsvurdering = totrinnsvurderinger.iterator().next();

        assertThat(totrinnsvurdering.isGodkjent()).isFalse();
        assertThat(totrinnsvurdering.getBegrunnelse()).isEqualTo(besluttersBegrunnelse);
        assertThat(totrinnsvurdering.getVurderPåNyttÅrsaker().size()).isEqualTo(1);
        VurderÅrsakTotrinnsvurdering vurderPåNyttÅrsak = totrinnsvurdering.getVurderPåNyttÅrsaker().iterator().next();
        assertThat(vurderPåNyttÅrsak.getÅrsaksType()).isEqualTo(VurderÅrsak.FEIL_FAKTA);
    }

    @Test
    public void oppdaterer_aksjonspunkt_med_godkjent_totrinnskontroll() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);

        leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.AVKLAR_OM_ER_BOSATT);

        AksjonspunktGodkjenningDto aksGodkjDto = new AksjonspunktGodkjenningDto();
        aksGodkjDto.setGodkjent(true);
        aksGodkjDto.setAksjonspunktKode(AksjonspunktDefinisjon.AVKLAR_OM_ER_BOSATT);

        FatterVedtakAksjonspunktDto aksjonspunktDto = new FatterVedtakAksjonspunktDto("", Collections.singletonList(aksGodkjDto));
        new FatterVedtakAksjonspunktOppdaterer(repositoryProvider, fatterVedtakAksjonspunkt).oppdater(aksjonspunktDto, behandling, VilkårResultat.builder());

        Collection<Totrinnsvurdering> totrinnsvurderinger = totrinnRepository.hentTotrinnaksjonspunktvurderinger(behandling);
        assertThat(totrinnsvurderinger.size()).isEqualTo(1);
        Totrinnsvurdering totrinnsvurdering = totrinnsvurderinger.iterator().next();

        assertThat(totrinnsvurdering.isGodkjent()).isTrue();
        assertThat(totrinnsvurdering.getBegrunnelse()).isNullOrEmpty();
        assertThat(totrinnsvurdering.getVurderPåNyttÅrsaker()).isEmpty();
    }

    protected Aksjonspunkt leggTilAksjonspunkt(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        return aksjonspunktRepository.leggTilAksjonspunkt(behandling, aksjonspunktDefinisjon,
            BehandlingStegType.KONTROLLER_FAKTA);
    }

}
