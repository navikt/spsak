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
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
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

    public RyddVilkårTyper(BehandlingStegModell modell, GrunnlagRepositoryProvider repositoryProvider, Behandling behandling, BehandlingskontrollKontekst kontekst) {
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
        Optional<Behandlingsresultat> behandlingsresultat = behandlingRepository.hentResultatHvisEksisterer(behandling.getId());
        if (behandlingsresultat.isEmpty() ||
            Objects.equals(behandlingsresultat.get().getBehandlingResultatType(), BehandlingResultatType.IKKE_FASTSATT)) {
            return;
        }

        Behandlingsresultat behandlingsresultat1 = behandlingsresultat.get();
        Behandlingsresultat.builderEndreEksisterende(behandlingsresultat1).medBehandlingResultatType(BehandlingResultatType.IKKE_FASTSATT);
        behandlingRepository.lagre(behandlingsresultat1, kontekst.getSkriveLås());
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
        Optional<Behandlingsresultat> behandlingsresultatOpt = behandlingRepository.hentResultatHvisEksisterer(behandling.getId());
        Optional<VilkårResultat> vilkårResultatOpt = behandlingsresultatOpt
            .map(Behandlingsresultat::getVilkårResultat)
            .filter(inng -> !inng.erOverstyrt());
        if (vilkårResultatOpt.isEmpty()) {
            return;
        }

        VilkårResultat vilkårResultat = vilkårResultatOpt.get();
        Behandlingsresultat behandlingsresultat = behandlingsresultatOpt.get();
        VilkårResultat.Builder builder = VilkårResultat.builderFraEksisterende(vilkårResultat);
        if (!vilkårResultat.getVilkårResultatType().equals(IKKE_FASTSATT)) {
            builder.medVilkårResultatType(IKKE_FASTSATT);
        }
        builder.buildFor(behandlingsresultat);
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandlingsresultat.getVilkårResultat(), lås);
        behandlingRepository.lagre(behandlingsresultat, lås);
    }

    private void nullstillVilkår(List<VilkårType> vilkårTyper) {
        Optional<Behandlingsresultat> behandlingsresultat = behandlingRepository.hentResultatHvisEksisterer(behandling.getId());
        Optional<VilkårResultat> vilkårResultatOpt = behandlingsresultat
            .map(Behandlingsresultat::getVilkårResultat);
        if (vilkårResultatOpt.isEmpty()) {
            return;
        }
        VilkårResultat vilkårResultat = vilkårResultatOpt.get();

        List<Vilkår> vilkårSomSkalNullstilles = vilkårResultat.getVilkårene().stream()
            .filter(v -> vilkårTyper.contains(v.getVilkårType()))
            .collect(toList());
        if (vilkårSomSkalNullstilles.isEmpty()) {
            return;
        }
        Behandlingsresultat behandlingsresultatet = behandlingsresultat.get();
        VilkårResultat.Builder builder = VilkårResultat.builderFraEksisterende(vilkårResultat);
        vilkårSomSkalNullstilles.forEach(vilkår -> builder.nullstillVilkår(vilkår.getVilkårType(), vilkår.getVilkårUtfallOverstyrt()));
        builder.buildFor(behandlingsresultatet);
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandlingsresultatet.getVilkårResultat(), lås);
        behandlingRepository.lagre(behandlingsresultatet, lås);
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
