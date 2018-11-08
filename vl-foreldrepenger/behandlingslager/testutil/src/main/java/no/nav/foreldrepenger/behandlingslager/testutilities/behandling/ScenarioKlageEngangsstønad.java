package no.nav.foreldrepenger.behandlingslager.testutilities.behandling;

import static no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType.KLAGE_NFP;
import static no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType.KLAGE_NK;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.FORESLÅ_VEDTAK;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.MANUELL_VURDERING_AV_KLAGE_NFP;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.MANUELL_VURDERING_AV_KLAGE_NK;
import static no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdering.STADFESTE_YTELSESVEDTAK;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.InternalManipulerBehandlingImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageAvvistÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageMedholdÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdering;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurderingResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdertAv;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.vedtak.felles.testutilities.Whitebox;
import no.nav.vedtak.util.FPDateUtil;

/**
 * Default test scenario builder for Klage Engangssøknad. Kan opprettes for gitt standard Scenario Engangssøknad
 * <p>
 * Oppretter en avsluttet behandling ved hjelp av Scenario Builder.
 * <p>
 * Kan bruke settere (evt. legge til) for å tilpasse utgangspunktet.
 * <p>
 * Mer avansert bruk er ikke gitt at kan bruke denne
 * klassen.
 * <p>
 * Alle scenarioene som har NK resultat, har NFP resultat stadfestet.
 */
@SuppressWarnings("deprecation")
public class ScenarioKlageEngangsstønad {

    public static ScenarioKlageEngangsstønad forUtenVurderingResultat(AbstractTestScenario<?> abstractTestScenario) {
        return new ScenarioKlageEngangsstønad().setup(abstractTestScenario).medBehandlingStegStart(KLAGE_NFP);
    }

    public static ScenarioKlageEngangsstønad forMedholdNFP(AbstractTestScenario<?> abstractTestScenario) {
        return new ScenarioKlageEngangsstønad().setup(abstractTestScenario, KlageVurdering.MEDHOLD_I_KLAGE);
    }

    public static ScenarioKlageEngangsstønad forAvvistNFP(AbstractTestScenario<?> abstractTestScenario) {
        return new ScenarioKlageEngangsstønad().setup(abstractTestScenario, KlageVurdering.AVVIS_KLAGE);
    }

    public static ScenarioKlageEngangsstønad forStadfestetNFP(AbstractTestScenario<?> abstractTestScenario) {
        return new ScenarioKlageEngangsstønad().setup(abstractTestScenario, STADFESTE_YTELSESVEDTAK).medBehandlingStegStart(KLAGE_NK);
    }

    public static ScenarioKlageEngangsstønad forMedholdNK(AbstractTestScenario<?> abstractTestScenario) {
        return new ScenarioKlageEngangsstønad().setup(abstractTestScenario, STADFESTE_YTELSESVEDTAK, KlageVurdering.MEDHOLD_I_KLAGE);
    }

    public static ScenarioKlageEngangsstønad forAvvistNK(AbstractTestScenario<?> abstractTestScenario) {
        return new ScenarioKlageEngangsstønad().setup(abstractTestScenario, STADFESTE_YTELSESVEDTAK, KlageVurdering.AVVIS_KLAGE);
    }

    public static ScenarioKlageEngangsstønad forOpphevetNK(AbstractTestScenario<?> abstractTestScenario) {
        return new ScenarioKlageEngangsstønad().setup(abstractTestScenario, STADFESTE_YTELSESVEDTAK, KlageVurdering.OPPHEVE_YTELSESVEDTAK);
    }

    public static ScenarioKlageEngangsstønad forStadfestetNK(AbstractTestScenario<?> abstractTestScenario) {
        return new ScenarioKlageEngangsstønad().setup(abstractTestScenario, STADFESTE_YTELSESVEDTAK, STADFESTE_YTELSESVEDTAK);
    }

    private Map<AksjonspunktDefinisjon, BehandlingStegType> opprettedeAksjonspunktDefinisjoner = new HashMap<>();
    private Map<AksjonspunktDefinisjon, BehandlingStegType> utførteAksjonspunktDefinisjoner = new HashMap<>();

    private AbstractTestScenario<?> abstractTestScenario;

    private KlageVurdering vurderingNFP;
    private KlageVurdering vurderingNK;
    private String behandlendeEnhet;

    private Behandling klageBehandling;
    private BehandlingStegType startSteg;

    private KlageVurderingResultat.Builder vurderingResultatNFP = KlageVurderingResultat.builder();
    private KlageVurderingResultat.Builder vurderingResultatNK = KlageVurderingResultat.builder();
    private BehandlingVedtak behandlingVedtak;

    private ScenarioKlageEngangsstønad() {
    }

    private ScenarioKlageEngangsstønad setup(AbstractTestScenario<?> abstractTestScenario) {
        this.abstractTestScenario = abstractTestScenario;

        //default steg (kan bli overskrevet av andre setup metoder som kaller denne)
        this.startSteg = KLAGE_NFP;

        this.opprettedeAksjonspunktDefinisjoner.put(MANUELL_VURDERING_AV_KLAGE_NFP, KLAGE_NFP);
        return this;
    }

    private ScenarioKlageEngangsstønad setup(AbstractTestScenario<?> abstractTestScenario, KlageVurdering resultatTypeNFP) {
        setup(abstractTestScenario);
        this.vurderingNFP = resultatTypeNFP;

        this.opprettedeAksjonspunktDefinisjoner.remove(MANUELL_VURDERING_AV_KLAGE_NFP);
        this.utførteAksjonspunktDefinisjoner.put(MANUELL_VURDERING_AV_KLAGE_NFP, KLAGE_NFP);

        //default steg (kan bli overskrevet av andre setup metoder som kaller denne)
        if(resultatTypeNFP.equals(STADFESTE_YTELSESVEDTAK)){
            this.startSteg = KLAGE_NK;
            this.opprettedeAksjonspunktDefinisjoner.put(MANUELL_VURDERING_AV_KLAGE_NK, KLAGE_NK);
        }else {
            this.startSteg = BehandlingStegType.FORESLÅ_VEDTAK;
            this.opprettedeAksjonspunktDefinisjoner.put(FORESLÅ_VEDTAK, BehandlingStegType.FORESLÅ_VEDTAK);
        }
        //setter default resultat NFP trenger kanskje en utledning fra resultattype
        this.vurderingResultatNFP.medKlageAvvistÅrsak(KlageAvvistÅrsak.UDEFINERT).medBegrunnelse("DEFAULT")
            .medKlageVurdering(KlageVurdering.AVVIS_KLAGE).medVedtaksdatoPåklagdBehandling(LocalDate.now(FPDateUtil.getOffset()));
        return this;
    }

    private ScenarioKlageEngangsstønad setup(AbstractTestScenario<?> abstractTestScenario, KlageVurdering resultatTypeNFP, KlageVurdering resultatTypeNK) {
        setup(abstractTestScenario,resultatTypeNFP);
        this.vurderingNK = resultatTypeNK;

        this.opprettedeAksjonspunktDefinisjoner.remove(MANUELL_VURDERING_AV_KLAGE_NK);
        this.utførteAksjonspunktDefinisjoner.put(MANUELL_VURDERING_AV_KLAGE_NK, KLAGE_NK);

        //default steg (de fleste scenarioene starter her. De resterende overstyrer i static metoden)
        this.startSteg = BehandlingStegType.FORESLÅ_VEDTAK;
        this.opprettedeAksjonspunktDefinisjoner.put(FORESLÅ_VEDTAK, BehandlingStegType.FORESLÅ_VEDTAK);

        //setter default resultat NFP trenger kanskje en utledning fra resultattype
        this.vurderingResultatNK.medKlageAvvistÅrsak(KlageAvvistÅrsak.UDEFINERT).medBegrunnelse("DEFAULT")
            .medKlageVurdering(KlageVurdering.AVVIS_KLAGE).medVedtaksdatoPåklagdBehandling(LocalDate.now(FPDateUtil.getOffset()));
        return this;
    }

    public Behandling lagre(BehandlingRepositoryProvider repositoryProvider) {
        if (klageBehandling != null) {
            throw new IllegalStateException("build allerede kalt.  Hent Behandling via getBehandling eller opprett nytt scenario.");
        }
        abstractTestScenario.buildAvsluttet(repositoryProvider.getBehandlingRepository(), repositoryProvider);
        return buildKlage(repositoryProvider);
    }

    private Behandling buildKlage(BehandlingRepositoryProvider repositoryProvider) {
        Fagsak fagsak = abstractTestScenario.getFagsak();

        // oppprett og lagre behandling
        Behandling.Builder builder = Behandling.forKlage(fagsak);

        if (behandlendeEnhet != null){
            builder.medBehandlendeEnhet(new OrganisasjonsEnhet(behandlendeEnhet, null));
        }

        klageBehandling = builder.build();
        BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
        BehandlingLås lås = behandlingRepository.taSkriveLås(klageBehandling);
        behandlingRepository.lagre(klageBehandling, lås);
        if (vurderingNFP != null) {
            behandlingRepository.lagre(vurderingResultatNFP.medKlageVurdertAv(KlageVurdertAv.NFP).medKlageVurdering(vurderingNFP)
                .medVedtaksdatoPåklagdBehandling(LocalDate.now(FPDateUtil.getOffset())).medBehandling(klageBehandling).build(), lås);
        }
        if (vurderingNK != null) {
            behandlingRepository.lagre(vurderingResultatNK.medKlageVurdertAv(KlageVurdertAv.NK).medKlageVurdering(vurderingNK)
                .medVedtaksdatoPåklagdBehandling(LocalDate.now(FPDateUtil.getOffset())).medBehandling(klageBehandling).build(), lås);
        }
        if (vurderingNFP != null) {
            Behandlingsresultat.builder().medBehandlingResultatType(
                BehandlingResultatType.tolkBehandlingResultatType(vurderingNK != null ? vurderingNK : vurderingNFP))
                .buildFor(klageBehandling);
        }else{
            Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.IKKE_FASTSATT)
                .buildFor(klageBehandling);
        }

        utførteAksjonspunktDefinisjoner.forEach((apDef, stegType) ->
            repositoryProvider.getAksjonspunktRepository().leggTilAksjonspunkt(klageBehandling, apDef, stegType)
        );

        klageBehandling.getAksjonspunkter().forEach(punkt ->  repositoryProvider.getAksjonspunktRepository().setTilUtført(punkt,"Test"));

        opprettedeAksjonspunktDefinisjoner.forEach((apDef, stegType) ->
            repositoryProvider.getAksjonspunktRepository().leggTilAksjonspunkt(klageBehandling, apDef, stegType)
        );

        klageBehandling.getAksjonspunkter().forEach(punkt -> Whitebox.setInternalState(punkt, "id", AbstractTestScenario.nyId()));

        if (startSteg != null) {
            new InternalManipulerBehandlingImpl(repositoryProvider).forceOppdaterBehandlingSteg(klageBehandling, startSteg);
        }

        return klageBehandling;
    }

    public ScenarioKlageEngangsstønad medKlageAvvistÅrsak(KlageAvvistÅrsak klageAvvistÅrsak) {
        vurderingResultatNFP.medKlageAvvistÅrsak(klageAvvistÅrsak);
        vurderingResultatNK.medKlageAvvistÅrsak(klageAvvistÅrsak);
        return this;
    }

    public ScenarioKlageEngangsstønad medKlageMedholdÅrsak(KlageMedholdÅrsak klageMedholdÅrsak) {
        vurderingResultatNFP.medKlageMedholdÅrsak(klageMedholdÅrsak);
        vurderingResultatNK.medKlageMedholdÅrsak(klageMedholdÅrsak);
        return this;
    }

    public ScenarioKlageEngangsstønad medBegrunnelse(String begrunnelse) {
        vurderingResultatNFP.medBegrunnelse(begrunnelse);
        vurderingResultatNK.medBegrunnelse(begrunnelse);
        return this;
    }

    public ScenarioKlageEngangsstønad medBehandlendeEnhet(String behandlendeEnhet) {
        this.behandlendeEnhet = behandlendeEnhet;
        return this;
    }

    public BehandlingRepository mockBehandlingRepository() {
        BehandlingRepository behandlingRepository = abstractTestScenario.mockBehandlingRepository();
        when(behandlingRepository.hentBehandling(klageBehandling.getId())).thenReturn(klageBehandling);
        return behandlingRepository;
    }

    public BehandlingRepositoryProvider mockBehandlingRepositoryProvider() {
        mockBehandlingRepository();
        return abstractTestScenario.mockBehandlingRepositoryProvider();
    }

    public Behandling lagMocked() {
        //pga det ikke går ann å flytte steg hvis mocket så settes startsteg til null
        startSteg = null;
        BehandlingRepositoryProvider repositoryProvider = abstractTestScenario.mockBehandlingRepositoryProvider();
        lagre(repositoryProvider);
        Whitebox.setInternalState(klageBehandling, "id", AbstractTestScenario.nyId());
        Whitebox.setInternalState(klageBehandling.getType(), "ekstraData", "{ \"behandlingstidFristUker\" : 12, \"behandlingstidVarselbrev\" : \"N\" }");
        return klageBehandling;
    }


    public Fagsak getFagsak(){
        return abstractTestScenario.getFagsak();
    }

    public ScenarioKlageEngangsstønad medAksjonspunkt(AksjonspunktDefinisjon apDef, BehandlingStegType stegType) {
        opprettedeAksjonspunktDefinisjoner.put(apDef, stegType);
        return this;
    }

    public ScenarioKlageEngangsstønad medUtførtAksjonspunkt(AksjonspunktDefinisjon apDef, BehandlingStegType stegType) {
        utførteAksjonspunktDefinisjoner.put(apDef, stegType);
        return this;
    }

    public ScenarioKlageEngangsstønad medBehandlingStegStart(BehandlingStegType startSteg) {
        this.startSteg = startSteg;
        return this;
    }

    public BehandlingVedtak mockBehandlingVedtak() {
        if (behandlingVedtak == null) {
            behandlingVedtak = Mockito.mock(BehandlingVedtak.class);
            when(abstractTestScenario.mockBehandlingRepositoryProvider().getBehandlingVedtakRepository().hentBehandlingvedtakForBehandlingId(klageBehandling.getId())).thenReturn(Optional.of(behandlingVedtak));
        }
        return behandlingVedtak;
    }


}
