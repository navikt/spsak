package no.nav.foreldrepenger.behandlingskontroll.transisjoner;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.Set;

import javax.persistence.EntityManager;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellRepository;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTransisjonEvent;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.StegTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.ReaktiveringStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;

public class BehandlingskontrollRevurderingTransisjonEventObserverTest {

    private static final TransisjonIdentifikator TRANSISJON = TransisjonIdentifikator.forId("revurdering-fremoverhopp-til-VURDER_OPPTJ_PERIODE");
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final EntityManager entityManager = repoRule.getEntityManager();

    GrunnlagRepositoryProvider grunnlagRepositoryProvider = new GrunnlagRepositoryProviderImpl(entityManager);
    ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(entityManager);
    AksjonspunktRepository aksjonspunktRepository = grunnlagRepositoryProvider.getAksjonspunktRepository();
    BehandlingRepository behandlingRepository = grunnlagRepositoryProvider.getBehandlingRepository();
    BehandlingModellRepository behandlingModellRepository = new BehandlingModellRepository(entityManager);

    BehandlingskontrollRevurderingTransisjonEventObserver transisjonEventObserver = new BehandlingskontrollRevurderingTransisjonEventObserver(aksjonspunktRepository, behandlingRepository, behandlingModellRepository);

    @Test
    public void skal_slette_aksjonspunkt_som_ikke_er_manuelt_opprettet_og_som_er_i_eller_etter_steget_det_hoppes_til() {
        Behandling behandling = ScenarioMorSøkerForeldrepenger.forDefaultAktør().lagre(grunnlagRepositoryProvider, resultatRepositoryProvider);
        Fagsak fagsak = behandling.getFagsak();
        Aksjonspunkt ap1 = aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.AVKLAR_LOVLIG_OPPHOLD);
        Aksjonspunkt ap3 = aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.FORESLÅ_VEDTAK);
        aksjonspunktRepository.setTilUtført(ap1, "test");
        aksjonspunktRepository.setTilUtført(ap3, "test");
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, lås);

        Behandling revurdering = Behandling.fraTidligereBehandling(behandling, BehandlingType.REVURDERING).build();
        aksjonspunktRepository.kopierAlleAksjonspunkterOgSettDemInaktive(behandling, revurdering);
        BehandlingLås låsR = behandlingRepository.taSkriveLås(revurdering);
        behandlingRepository.lagre(revurdering, låsR);

        assertThat(revurdering.getAlleAksjonspunkterInklInaktive()).hasSize(2);

        BehandlingLås låsEvent = behandlingRepository.taSkriveLås(revurdering);
        BehandlingskontrollKontekst kontekstEvent = new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), låsEvent);
        Optional<StegTilstand> fraTilstand = Optional.of(new StegTilstand(BehandlingStegType.REGISTRER_SØKNAD, BehandlingStegStatus.INNGANG));
        BehandlingStegType tilSteg = BehandlingStegType.KONTROLLER_FAKTA;

        //act
        BehandlingTransisjonEvent event = new BehandlingTransisjonEvent(kontekstEvent, TRANSISJON, fraTilstand, tilSteg, true);
        transisjonEventObserver.observerBehandlingSteg(event);

        //assert
        Set<Aksjonspunkt> aper = revurdering.getAlleAksjonspunkterInklInaktive();
        assertThat(aper).hasSize(2);
        assertThat(aper.stream().filter(ap -> ap.getReaktiveringStatus().equals(ReaktiveringStatus.SLETTET)).collect(toList())).hasSize(2);
        assertThat(aper.stream().filter(ap -> !ap.getReaktiveringStatus().equals(ReaktiveringStatus.SLETTET)).collect(toList())).isEmpty();
    }

    @Test
    public void skal_reaktiver_og_gjenåpne_aksjonspunkt_som_er_manuelt_opprettet_og_som_er_i_eller_etter_steget_det_hoppes_til() {
        Behandling behandling = ScenarioMorSøkerForeldrepenger.forDefaultAktør().lagre(grunnlagRepositoryProvider, resultatRepositoryProvider);
        Fagsak fagsak = behandling.getFagsak();
        Aksjonspunkt ap1 = aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.AVKLAR_LOVLIG_OPPHOLD);
        Aksjonspunkt ap3 = aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.FORESLÅ_VEDTAK_MANUELT);
        aksjonspunktRepository.setTilManueltOpprettet(ap1);
        aksjonspunktRepository.setTilManueltOpprettet(ap3);
        aksjonspunktRepository.setTilUtført(ap1, "test");
        aksjonspunktRepository.setTilUtført(ap3, "test");
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, lås);

        Behandling revurdering = Behandling.fraTidligereBehandling(behandling, BehandlingType.REVURDERING).build();
        aksjonspunktRepository.kopierAlleAksjonspunkterOgSettDemInaktive(behandling, revurdering);
        BehandlingLås låsR = behandlingRepository.taSkriveLås(revurdering);
        behandlingRepository.lagre(revurdering, låsR);

        assertThat(revurdering.getAlleAksjonspunkterInklInaktive()).hasSize(2);

        BehandlingLås låsEvent = behandlingRepository.taSkriveLås(revurdering);
        BehandlingskontrollKontekst kontekstEvent = new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), låsEvent);
        Optional<StegTilstand> fraTilstand = Optional.of(new StegTilstand(BehandlingStegType.REGISTRER_SØKNAD, BehandlingStegStatus.INNGANG));
        BehandlingStegType tilSteg = BehandlingStegType.KONTROLLER_FAKTA;

        //act
        BehandlingTransisjonEvent event = new BehandlingTransisjonEvent(kontekstEvent, TRANSISJON, fraTilstand, tilSteg, true);
        transisjonEventObserver.observerBehandlingSteg(event);

        //assert
        assertThat(revurdering.getAlleAksjonspunkterInklInaktive()).hasSize(2);
        assertThat(revurdering.getAksjonspunktMedDefinisjonOptional(AksjonspunktDefinisjon.AVKLAR_LOVLIG_OPPHOLD)).isPresent();
        Optional<Aksjonspunkt> apForeslå = revurdering.getAksjonspunktMedDefinisjonOptional(AksjonspunktDefinisjon.FORESLÅ_VEDTAK_MANUELT);
        assertThat(apForeslå).isPresent();
        assertThat(apForeslå.get().getStatus()).isEqualTo(AksjonspunktStatus.OPPRETTET);
    }

}
