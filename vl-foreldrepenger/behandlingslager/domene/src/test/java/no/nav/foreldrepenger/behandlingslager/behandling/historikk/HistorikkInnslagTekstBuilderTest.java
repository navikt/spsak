package no.nav.foreldrepenger.behandlingslager.behandling.historikk;


import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.MANUELL_VURDERING_AV_SØKNADSFRISTVILKÅRET;
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
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;

public class HistorikkInnslagTekstBuilderTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
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

        vurderPåNytt.setAksjonspunktDefinisjon(aksjonspunktRepository.finnAksjonspunktDefinisjon(MANUELL_VURDERING_AV_SØKNADSFRISTVILKÅRET.getKode()));
        vurderPåNytt.setAksjonspunktSistEndret(LocalDateTime.now());
        vurdering.put(SkjermlenkeType.SOEKNADSFRIST, Collections.singletonList(vurderPåNytt));

        HistorikkinnslagTotrinnsvurdering godkjent = new HistorikkinnslagTotrinnsvurdering();
        godkjent.setGodkjent(true);
        godkjent.setAksjonspunktDefinisjon(aksjonspunktRepository.finnAksjonspunktDefinisjon(AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL.getKode()));
        godkjent.setAksjonspunktSistEndret(LocalDateTime.now());


        HistorikkinnslagTotrinnsvurdering vurderPåNytt2 = new HistorikkinnslagTotrinnsvurdering();
        vurderPåNytt2.setGodkjent(false);
        vurderPåNytt2.setAksjonspunktDefinisjon(aksjonspunktRepository.finnAksjonspunktDefinisjon(AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL.getKode()));
        vurderPåNytt2.setBegrunnelse("Ikke enig.");
        vurderPåNytt2.setAksjonspunktSistEndret(LocalDateTime.now());
        vurdering.put(SkjermlenkeType.FAKTA_OM_FOEDSEL, Arrays.asList(godkjent, vurderPåNytt2));

        HistorikkinnslagTotrinnsvurdering vurderPåNytt3 = new HistorikkinnslagTotrinnsvurdering();
        vurderPåNytt3.setGodkjent(false);
        vurderPåNytt3.setAksjonspunktDefinisjon(aksjonspunktRepository.finnAksjonspunktDefinisjon(AksjonspunktDefinisjon.REGISTRER_PAPIRSØKNAD_ENGANGSSTØNAD.getKode()));
        vurderPåNytt3.setBegrunnelse("Ikke enig.");
        vurderPåNytt3.setAksjonspunktSistEndret(LocalDateTime.now());


        vurderingUtenVilkar.add(vurderPåNytt3);

        List<HistorikkinnslagDel> deler = tekstBuilder
            .medTotrinnsvurdering(vurdering, vurderingUtenVilkar)
            .medHendelse(HistorikkinnslagType.SAK_RETUR)
            .build(historikkinnslag);

        assertThat(deler).hasSize(3);
        HistorikkinnslagDel historikkinnslagDel = deler.get(0);
        List<HistorikkinnslagTotrinnsvurdering> aksjonspunkter = historikkinnslagDel.getTotrinnsvurderinger(aksjonspunktRepository);
        assertThat(aksjonspunkter).hasSize(1);
        HistorikkinnslagTotrinnsvurdering aksjonspunkt = aksjonspunkter.get(0);
        assertThat(aksjonspunkt.getAksjonspunktDefinisjon()).as("aksjonspunktKode").isEqualTo(aksjonspunktRepository.finnAksjonspunktDefinisjon(AksjonspunktDefinisjon.REGISTRER_PAPIRSØKNAD_ENGANGSSTØNAD.getKode()));
        assertThat(aksjonspunkt.erGodkjent()).as("godkjent").isFalse();
        assertThat(aksjonspunkt.getBegrunnelse()).as("begrunnelse").isEqualTo("Ikke enig.");
    }

}
