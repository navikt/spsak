package no.nav.foreldrepenger.domene.inngangsvilkaar.impl;

import java.util.List;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.domene.inngangsvilkaar.Inngangsvilkår;
import no.nav.foreldrepenger.domene.inngangsvilkaar.InngangsvilkårTjeneste;
import no.nav.foreldrepenger.domene.inngangsvilkaar.VilkårTypeRef.VilkårTypeRefLiteral;

@ApplicationScoped
public class InngangsvilkårTjenesteImpl implements InngangsvilkårTjeneste {

    private Instance<Inngangsvilkår> alleInngangsvilkår;
    private BehandlingRepository behandlingRepository;
    private AksjonspunktRepository aksjonspunktRepository;
    private VilkårKodeverkRepository vilkårKodeverkRepository;

    InngangsvilkårTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public InngangsvilkårTjenesteImpl(@Any Instance<Inngangsvilkår> alleInngangsvilkår, GrunnlagRepositoryProvider repositoryProvider) {
        this.alleInngangsvilkår = alleInngangsvilkår;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.vilkårKodeverkRepository = repositoryProvider.getVilkårKodeverkRepository();
    }

    @Override
    public Inngangsvilkår finnVilkår(VilkårType vilkårType) {

        Instance<Inngangsvilkår> selected = alleInngangsvilkår.select(new VilkårTypeRefLiteral(vilkårType.getKode()));
        if (selected.isAmbiguous()) {
            throw new IllegalArgumentException("Mer enn en implementasjon funnet for vilkårtype:" + vilkårType);
        } else if (selected.isUnsatisfied()) {
            throw new IllegalArgumentException("Ingen implementasjoner funnet for vilkårtype:" + vilkårType);
        }
        Inngangsvilkår minInstans = selected.get();
        if (minInstans.getClass().isAnnotationPresent(Dependent.class)) {
            throw new IllegalStateException("Kan ikke ha @Dependent scope bean ved Instance lookup dersom en ikke også håndtere lifecycle selv: " + minInstans.getClass());
        }
        return minInstans;
    }

    @Override
    public void overstyrAksjonspunktForSøkersopplysningsplikt(Long behandlingId, VilkårUtfallType utfall, BehandlingskontrollKontekst kontekst) {
        Avslagsårsak avslagsårsak = Avslagsårsak.MANGLENDE_DOKUMENTASJON;
        AksjonspunktDefinisjon vedtakUtenTotrinnskontroll = AksjonspunktDefinisjon.VEDTAK_UTEN_TOTRINNSKONTROLL;
        VilkårType vilkårType = VilkårType.SØKERSOPPLYSNINGSPLIKT;
        AksjonspunktDefinisjon søkersOpplysningsplikt = AksjonspunktDefinisjon.SØKERS_OPPLYSNINGSPLIKT_OVST;

        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        VilkårResultat vilkårResultat = behandling.getBehandlingsresultat().getVilkårResultat();
        VilkårResultat.Builder builder = VilkårResultat.builderFraEksisterende(vilkårResultat);
        List<Aksjonspunkt> åpneAksjonspunkter = behandling.getÅpneAksjonspunkter();

        if (Objects.equals(VilkårUtfallType.OPPFYLT, utfall)) {
            // Rydd opp i aksjonspunkt tidligere opprettet i forbindelse med overstyring av søkers opplysningsplikt
            aksjonspunktRepository.fjernAksjonspunkt(behandling, vedtakUtenTotrinnskontroll);
            builder.overstyrVilkår(vilkårType, utfall, null);

            if (!finnesOverstyrteAvviste(vilkårResultat, vilkårType)) {
                builder.medVilkårResultatType(VilkårResultatType.IKKE_FASTSATT);
            }
            builder.buildFor(behandling);
            behandlingRepository.lagre(behandling.getBehandlingsresultat().getVilkårResultat(), kontekst.getSkriveLås());
            behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
        } else {
            // Hvis det trengs: Rydd opp i aksjonspunkt som gjelder forslag til vedtak før vi hopper frem til
            // FORESLÅ_VEDTAK-steget
            åpneAksjonspunkter.stream()
                    .filter(a -> !a.getAksjonspunktDefinisjon().getKode().equals(søkersOpplysningsplikt.getKode())/* ikke ta med seg selv*/)
                    .forEach(a -> aksjonspunktRepository.setTilAvbrutt(a));
            aksjonspunktRepository.leggTilAksjonspunkt(behandling, vedtakUtenTotrinnskontroll);

            builder.overstyrVilkår(vilkårType, utfall, avslagsårsak);
            builder.medVilkårResultatType(VilkårResultatType.AVSLÅTT);
            builder.buildFor(behandling);
            behandlingRepository.lagre(behandling.getBehandlingsresultat().getVilkårResultat(), kontekst.getSkriveLås());
            behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
        }
    }

    @Override
    public void overstyrAksjonspunkt(Long behandlingId, VilkårType vilkårType, VilkårUtfallType utfall, String avslagsårsakKode, BehandlingskontrollKontekst kontekst) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        VilkårResultat vilkårResultat = behandling.getBehandlingsresultat().getVilkårResultat();
        VilkårResultat.Builder builder = VilkårResultat.builderFraEksisterende(vilkårResultat);

        Avslagsårsak avslagsårsak = finnAvslagsårsak(avslagsårsakKode, utfall);
        builder.overstyrVilkår(vilkårType, utfall, avslagsårsak);
        if (utfall.equals(VilkårUtfallType.IKKE_OPPFYLT)) {
            builder.medVilkårResultatType(VilkårResultatType.AVSLÅTT);
        } else if (utfall.equals(VilkårUtfallType.OPPFYLT)) {
            if (!finnesOverstyrteAvviste(vilkårResultat, vilkårType)) {
                builder.medVilkårResultatType(VilkårResultatType.IKKE_FASTSATT);
            }
        }
        builder.buildFor(behandling);
        behandlingRepository.lagre(behandling.getBehandlingsresultat().getVilkårResultat(), kontekst.getSkriveLås());
        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
    }

    private boolean finnesOverstyrteAvviste(VilkårResultat vilkårResultat, VilkårType vilkårType) {
        return vilkårResultat.getVilkårene().stream()
                .filter(vilkår -> !vilkår.getVilkårType().equals(vilkårType))
                .anyMatch(vilkår -> vilkår.erOverstyrt() && vilkår.erIkkeOppfylt());
    }

    private Avslagsårsak finnAvslagsårsak(String avslagsÅrsakKode, VilkårUtfallType utfall) {
        Avslagsårsak avslagsårsak;
        if (avslagsÅrsakKode == null || utfall.equals(VilkårUtfallType.OPPFYLT)) {
            avslagsårsak = Avslagsårsak.UDEFINERT;
        } else {
            avslagsårsak = vilkårKodeverkRepository.finnAvslagÅrsak(avslagsÅrsakKode);
        }
        return avslagsårsak;
    }
}
