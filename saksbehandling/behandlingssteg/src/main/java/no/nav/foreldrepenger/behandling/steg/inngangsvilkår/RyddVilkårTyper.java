package no.nav.foreldrepenger.behandling.steg.inngangsvilkår;

import static java.util.stream.Collectors.toList;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType.IKKE_FASTSATT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.MEDLEMSKAPSVILKÅRET;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;

class RyddVilkårTyper {

    private BehandlingStegModell modell;
    private BehandlingRepository behandlingRepository;
    private AksjonspunktRepository aksjonspunktRepository;
    private final Behandling behandling;
    private final BehandlingskontrollKontekst kontekst;

    static Map<VilkårType, Consumer<RyddVilkårTyper>> OPPRYDDER_FOR_AVKLARTE_DATA = new HashMap<>();
    private MedlemskapRepository medlemskapRepository;


    static {
        OPPRYDDER_FOR_AVKLARTE_DATA.put(MEDLEMSKAPSVILKÅRET, r -> r.medlemskapRepository.slettAvklarteMedlemskapsdata(r.behandling, r.kontekst.getSkriveLås()));
    }

    public RyddVilkårTyper(BehandlingStegModell modell, BehandlingRepositoryProvider repositoryProvider, Behandling behandling, BehandlingskontrollKontekst kontekst) {
        this.modell = modell;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.medlemskapRepository = repositoryProvider.getMedlemskapRepository();
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.behandling = behandling;
        this.kontekst = kontekst;
    }

    public void ryddVedOverhoppFramover(List<VilkårType> vilkårTyper) {
        slettAvklarteFakta(vilkårTyper);
        nullstillVilkår(vilkårTyper);
    }

    public void ryddVedTilbakeføring(List<VilkårType> vilkårTyper) {
        nullstillInngangsvilkår();
        nullstillVilkår(vilkårTyper);
        nullstillVedtaksresultat();

        // De generelle reglene klarer ikke å håndtere aksjonspunkt for søknadsfrist i alle caser
        spesialhåndterSøknadsfrist();
    }

    private void nullstillVedtaksresultat() {
        Behandlingsresultat behandlingsresultat = behandling.getBehandlingsresultat();
        if (behandlingsresultat == null ||
            Objects.equals(behandlingsresultat.getBehandlingResultatType(), BehandlingResultatType.IKKE_FASTSATT)) {
            return;
        }

        Behandlingsresultat.builderEndreEksisterende(behandling.getBehandlingsresultat()).medBehandlingResultatType(BehandlingResultatType.IKKE_FASTSATT);
        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
    }

    private void slettAvklarteFakta(List<VilkårType> vilkårTyper) {
        vilkårTyper.forEach(vilkårType -> {
            Consumer<RyddVilkårTyper> ryddVilkårConsumer = OPPRYDDER_FOR_AVKLARTE_DATA.get(vilkårType);
            if (ryddVilkårConsumer != null) {
                ryddVilkårConsumer.accept(this);
            }
        });
    }

    private void nullstillInngangsvilkår() {
        Optional<VilkårResultat> vilkårResultatOpt = Optional.ofNullable(behandling.getBehandlingsresultat())
            .map(Behandlingsresultat::getVilkårResultat)
            .filter(inng -> !inng.erOverstyrt());
        if (!vilkårResultatOpt.isPresent()) {
            return;
        }

        VilkårResultat vilkårResultat = vilkårResultatOpt.get();
        VilkårResultat.Builder builder = VilkårResultat.builderFraEksisterende(vilkårResultat);
        if (!vilkårResultat.getVilkårResultatType().equals(IKKE_FASTSATT)) {
            builder.medVilkårResultatType(IKKE_FASTSATT);
        }
        builder.buildFor(behandling);
    }

    private void nullstillVilkår(List<VilkårType> vilkårTyper) {
        Optional<VilkårResultat> vilkårResultatOpt = Optional.ofNullable(behandling.getBehandlingsresultat())
            .map(Behandlingsresultat::getVilkårResultat);
        if (!vilkårResultatOpt.isPresent()) {
            return;
        }
        VilkårResultat vilkårResultat = vilkårResultatOpt.get();

        List<Vilkår> vilkårSomSkalNullstilles = vilkårResultat.getVilkårene().stream()
            .filter(v -> vilkårTyper.contains(v.getVilkårType()))
            .collect(toList());
        if (vilkårSomSkalNullstilles.isEmpty()) {
            return;
        }

        VilkårResultat.Builder builder = VilkårResultat.builderFraEksisterende(vilkårResultat);
        vilkårSomSkalNullstilles.forEach(vilkår -> builder.nullstillVilkår(vilkår.getVilkårType(), vilkår.getVilkårUtfallOverstyrt()));
        builder.buildFor(behandling);
    }

    private void spesialhåndterSøknadsfrist() {
        if(Objects.equals(modell.getBehandlingStegType(), BehandlingStegType.VURDER_MEDLEMSKAPVILKÅR)) {
            // Ved tilbakehopp til steg hvor man kan endre skjæringstidspunkt må
            // MANUELL_VURDERING_AV_SØKNADSFRISTVILKÅRET nullstilles, ettersom det ikke alltid fanges av generelle regl.
            behandling.getÅpneAksjonspunkter().stream()
                .filter(a -> Objects.equals(a.getAksjonspunktDefinisjon(), AksjonspunktDefinisjon.AVKLAR_LOVLIG_OPPHOLD))
                .forEach(a -> aksjonspunktRepository.fjernAksjonspunkt(behandling, a.getAksjonspunktDefinisjon()));
        }
    }

}
