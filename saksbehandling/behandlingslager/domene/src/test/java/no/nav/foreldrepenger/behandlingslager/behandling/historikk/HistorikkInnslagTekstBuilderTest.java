package no.nav.foreldrepenger.behandlingslager.behandling.historikk;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;

public class HistorikkInnslagTekstBuilderTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repositoryRule.getEntityManager());
    private AksjonspunktRepository aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();

    @Test
    public void testHistorikkinnslagTekstSakRetur() {
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setType(HistorikkinnslagType.SAK_RETUR);

        HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder();
        Map<SkjermlenkeType, List<HistorikkinnslagTotrinnsvurdering>> vurdering = new HashMap<>();

        List<HistorikkinnslagTotrinnsvurdering> vurderingUtenVilkar = new ArrayList<>();

        HistorikkinnslagTotrinnsvurdering vurderPåNytt = new HistorikkinnslagTotrinnsvurdering();
        vurderPåNytt.setGodkjent(false);
        vurderPåNytt.setBegrunnelse("Må vurderes igjen. Se på dokumentasjon.");

        vurderPåNytt.setAksjonspunktDefinisjon(aksjonspunktRepository.finnAksjonspunktDefinisjon(AksjonspunktDefinisjon.MANUELL_VURDERING_AV_MEDLEMSKAP.getKode()));
        LocalDateTime now = LocalDateTime.now();
        vurderPåNytt.setAksjonspunktSistEndret(now);
        vurdering.put(SkjermlenkeType.PUNKT_FOR_MEDLEMSKAP, Collections.singletonList(vurderPåNytt));

        HistorikkinnslagTotrinnsvurdering godkjent = new HistorikkinnslagTotrinnsvurdering();
        godkjent.setGodkjent(true);
        godkjent.setAksjonspunktDefinisjon(aksjonspunktRepository.finnAksjonspunktDefinisjon(AksjonspunktDefinisjon.KONTROLLER_REVURDERINGSBEHANDLING.getKode()));
        godkjent.setAksjonspunktSistEndret(now.plusSeconds(1));


        HistorikkinnslagTotrinnsvurdering vurderPåNytt2 = new HistorikkinnslagTotrinnsvurdering();
        vurderPåNytt2.setGodkjent(false);
        vurderPåNytt2.setAksjonspunktDefinisjon(aksjonspunktRepository.finnAksjonspunktDefinisjon(AksjonspunktDefinisjon.KONTROLLER_REVURDERINGSBEHANDLING.getKode()));
        vurderPåNytt2.setBegrunnelse("Ikke enig.");
        vurderPåNytt2.setAksjonspunktSistEndret(now.plusSeconds(2));
        vurdering.put(SkjermlenkeType.FAKTA_OM_MEDLEMSKAP, Arrays.asList(godkjent, vurderPåNytt2));

        List<HistorikkinnslagDel> deler = tekstBuilder
            .medTotrinnsvurdering(vurdering, vurderingUtenVilkar)
            .medHendelse(HistorikkinnslagType.SAK_RETUR)
            .build(historikkinnslag);

        assertThat(deler).hasSize(2);
        HistorikkinnslagDel historikkinnslagDel = deler.get(0);
        List<HistorikkinnslagTotrinnsvurdering> aksjonspunkter = historikkinnslagDel.getTotrinnsvurderinger(aksjonspunktRepository);
        assertThat(aksjonspunkter).hasSize(1);
        HistorikkinnslagTotrinnsvurdering aksjonspunkt = aksjonspunkter.get(0);
        assertThat(aksjonspunkt.erGodkjent()).as("godkjent").isFalse();
        assertThat(aksjonspunkt.getBegrunnelse()).as("begrunnelse").isEqualTo("Må vurderes igjen. Se på dokumentasjon.");
    }

}
