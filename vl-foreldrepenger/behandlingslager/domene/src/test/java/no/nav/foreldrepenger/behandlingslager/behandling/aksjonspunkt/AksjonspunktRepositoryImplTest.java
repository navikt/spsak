package no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt;

import static java.util.Arrays.asList;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_MANUELT_SATT_PÅ_VENT;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_TERMINBEKREFTELSE;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.OVERSTYRING_AV_ADOPSJONSVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.BasicBehandlingBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class AksjonspunktRepositoryImplTest {

    private static final String BEGRUNNELSE = "Begrunnelse";
    private static final LocalDateTime FRIST_TID = LocalDateTime.now().plusDays(1);

    @Rule
    public RepositoryRule repoRule = new UnittestRepositoryRule();

    private EntityManager entityManager = repoRule.getEntityManager();
    private BasicBehandlingBuilder basicBehandlingBuilder;

    private AksjonspunktRepository aksjonspunktRepository;

    @Before
    public void before() {
        basicBehandlingBuilder = new BasicBehandlingBuilder(entityManager);
        aksjonspunktRepository = new AksjonspunktRepositoryImpl(entityManager);
    }

    @Test
    public void skal_kopiere_aksjonspunkt_med_alle_felter_fra_eksisterende_behandling_og_markere_som_inaktivt() {
        // Arrange
        Fagsak fagsak = basicBehandlingBuilder.opprettFagsak(FagsakYtelseType.FORELDREPENGER);
        Behandling opprinneligBehandling = basicBehandlingBuilder.opprettOgLagreFørstegangssøknad(fagsak);
        Behandling nyBehandling = basicBehandlingBuilder.opprettOgLagreFørstegangssøknad(fagsak);

        Aksjonspunkt ap = aksjonspunktRepository.leggTilAksjonspunkt(opprinneligBehandling, AVKLAR_TERMINBEKREFTELSE, BehandlingStegType.KONTROLLER_FAKTA);
        aksjonspunktRepository.setFrist(ap, FRIST_TID, Venteårsak.AVV_FODSEL);
        aksjonspunktRepository.setToTrinnsBehandlingKreves(ap);
        aksjonspunktRepository.setTilUtført(ap, BEGRUNNELSE);

        // Act
        aksjonspunktRepository.kopierAlleAksjonspunkterOgSettDemInaktive(opprinneligBehandling, nyBehandling);

        // Assert
        assertThat(nyBehandling.getAlleAksjonspunkterInklInaktive()).hasSize(1);
        Aksjonspunkt kopiertAp = nyBehandling.getAlleAksjonspunkterInklInaktive().iterator().next();
        assertThat(kopiertAp.getAksjonspunktDefinisjon()).isEqualTo(AVKLAR_TERMINBEKREFTELSE);
        assertThat(kopiertAp.getBegrunnelse()).isEqualTo(BEGRUNNELSE);
        assertThat(kopiertAp.getVenteårsak()).isEqualTo(Venteårsak.AVV_FODSEL);
        assertThat(kopiertAp.getFristTid()).isEqualTo(FRIST_TID);
        assertThat(kopiertAp.getStatus()).isEqualTo(AksjonspunktStatus.UTFØRT);
        assertThat(kopiertAp.isToTrinnsBehandling()).isTrue();
        assertThat(kopiertAp.getBehandlingStegFunnet()).isEqualTo(BehandlingStegType.KONTROLLER_FAKTA);
        assertThat(kopiertAp.getSlettesVedRegisterinnhenting()).isTrue();
        assertThat(kopiertAp.erManueltOpprettet()).isFalse();
        assertThat(kopiertAp.erAktivt()).isFalse();
        assertThat(kopiertAp.erRevurdering()).isTrue();
    }

    @Test
    public void skal_kopiere_alle_aksjonspunkter() {
        // Arrange
        Fagsak fagsak = basicBehandlingBuilder.opprettFagsak(FagsakYtelseType.FORELDREPENGER);
        Behandling opprinneligBehandling = basicBehandlingBuilder.opprettOgLagreFørstegangssøknad(fagsak);
        Behandling nyBehandling = basicBehandlingBuilder.opprettOgLagreFørstegangssøknad(fagsak);

        aksjonspunktRepository.leggTilAksjonspunkt(opprinneligBehandling, AVKLAR_TERMINBEKREFTELSE);
        aksjonspunktRepository.leggTilAksjonspunkt(opprinneligBehandling, SJEKK_MANGLENDE_FØDSEL);
        aksjonspunktRepository.leggTilAksjonspunkt(opprinneligBehandling, OVERSTYRING_AV_ADOPSJONSVILKÅRET);

        // Act
        aksjonspunktRepository.kopierAlleAksjonspunkterOgSettDemInaktive(opprinneligBehandling, nyBehandling);

        // Assert
        assertThat(nyBehandling.getAlleAksjonspunkterInklInaktive()).hasSize(3);
        List<AksjonspunktDefinisjon> apDefinisjoner = nyBehandling.getAlleAksjonspunkterInklInaktive().stream()
            .map(Aksjonspunkt::getAksjonspunktDefinisjon).collect(Collectors.toList());
        assertThat(apDefinisjoner).containsAll(asList(AVKLAR_TERMINBEKREFTELSE, SJEKK_MANGLENDE_FØDSEL, OVERSTYRING_AV_ADOPSJONSVILKÅRET));
    }

    @Test
    public void skal_ikke_kopiere_autopunkter() {
        // Arrange
        Fagsak fagsak = basicBehandlingBuilder.opprettFagsak(FagsakYtelseType.FORELDREPENGER);
        Behandling opprinneligBehandling = basicBehandlingBuilder.opprettOgLagreFørstegangssøknad(fagsak);
        Behandling nyBehandling = basicBehandlingBuilder.opprettOgLagreFørstegangssøknad(fagsak);

        aksjonspunktRepository.leggTilAksjonspunkt(opprinneligBehandling, AUTO_MANUELT_SATT_PÅ_VENT);
        aksjonspunktRepository.leggTilAksjonspunkt(opprinneligBehandling, SJEKK_MANGLENDE_FØDSEL);

        // Act
        aksjonspunktRepository.kopierAlleAksjonspunkterOgSettDemInaktive(opprinneligBehandling, nyBehandling);

        // Assert
        assertThat(nyBehandling.getAlleAksjonspunkterInklInaktive()).hasSize(1);
        List<AksjonspunktDefinisjon> apDefinisjoner = nyBehandling.getAlleAksjonspunkterInklInaktive().stream()
            .map(Aksjonspunkt::getAksjonspunktDefinisjon).collect(Collectors.toList());
        assertThat(apDefinisjoner).containsOnly(SJEKK_MANGLENDE_FØDSEL);
    }

    @Test
    public void skal_ikke_kopiere_avbrutte_aksjonspunkter() {
        // Arrange
        Fagsak fagsak = basicBehandlingBuilder.opprettFagsak(FagsakYtelseType.FORELDREPENGER);
        Behandling opprinneligBehandling = basicBehandlingBuilder.opprettOgLagreFørstegangssøknad(fagsak);
        Behandling nyBehandling = basicBehandlingBuilder.opprettOgLagreFørstegangssøknad(fagsak);

        aksjonspunktRepository.leggTilAksjonspunkt(opprinneligBehandling, AVKLAR_TERMINBEKREFTELSE);
        aksjonspunktRepository.leggTilAksjonspunkt(opprinneligBehandling, SJEKK_MANGLENDE_FØDSEL);
        aksjonspunktRepository.setTilAvbrutt(opprinneligBehandling.getAksjonspunktFor(SJEKK_MANGLENDE_FØDSEL));

        // Act
        aksjonspunktRepository.kopierAlleAksjonspunkterOgSettDemInaktive(opprinneligBehandling, nyBehandling);

        // Assert
        assertThat(nyBehandling.getAlleAksjonspunkterInklInaktive()).hasSize(1);
        List<AksjonspunktDefinisjon> apDefinisjoner = nyBehandling.getAlleAksjonspunkterInklInaktive().stream()
            .map(Aksjonspunkt::getAksjonspunktDefinisjon).collect(Collectors.toList());
        assertThat(apDefinisjoner).containsOnly(AVKLAR_TERMINBEKREFTELSE);
    }
}
