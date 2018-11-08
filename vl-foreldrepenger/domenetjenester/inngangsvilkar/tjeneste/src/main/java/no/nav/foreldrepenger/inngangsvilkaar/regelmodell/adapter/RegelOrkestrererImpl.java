package no.nav.foreldrepenger.inngangsvilkaar.regelmodell.adapter;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static no.nav.foreldrepenger.inngangsvilkaar.regelmodell.adapter.RegelintegrasjonFeil.FEILFACTORY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.inngangsvilkår.RegelOrkestrerer;
import no.nav.foreldrepenger.behandlingslager.inngangsvilkår.RegelResultat;
import no.nav.foreldrepenger.behandlingslager.inngangsvilkår.VilkårData;
import no.nav.foreldrepenger.inngangsvilkaar.Inngangsvilkår;
import no.nav.foreldrepenger.inngangsvilkaar.InngangsvilkårTjeneste;
import no.nav.vedtak.util.Objects;


@ApplicationScoped
class RegelOrkestrererImpl implements RegelOrkestrerer {

    private InngangsvilkårTjeneste inngangsvilkårTjeneste;

    RegelOrkestrererImpl() {
        // For CDI
    }

    @Inject
    public RegelOrkestrererImpl(InngangsvilkårTjeneste inngangsvilkårTjeneste) {
        this.inngangsvilkårTjeneste = inngangsvilkårTjeneste;
    }

    @Override
    public RegelResultat vurderInngangsvilkår(List<VilkårType> vilkårHåndtertAvSteg, Behandling behandling) {
        VilkårResultat vilkårResultat = behandling.getBehandlingsresultat().getVilkårResultat();
        List<Vilkår> matchendeVilkårPåBehandling = vilkårResultat.getVilkårene().stream()
            .filter(v -> vilkårHåndtertAvSteg.contains(v.getVilkårType()))
            .collect(toList());
        validerMaksEttVilkår(matchendeVilkårPåBehandling);

        Vilkår vilkår = matchendeVilkårPåBehandling.isEmpty() ? null : matchendeVilkårPåBehandling.get(0);
        if (vilkår == null) {
            // Intet vilkår skal eksekveres i regelmotor, men sikrer at det samlede inngangsvilkår-utfallet blir korrekt
            // ved å utlede det fra alle vilkårsutfallene
            Set<VilkårUtfallType> alleUtfall = hentAlleVilkårsutfall(vilkårResultat);
            VilkårResultatType inngangsvilkårUtfall = utledInngangsvilkårUtfall(alleUtfall);
            oppdaterBehandlingMedVilkårresultat(behandling, inngangsvilkårUtfall);
            return new RegelResultat(vilkårResultat, emptyList(), emptyMap());
        }

        VilkårData vilkårDataResultat = kjørRegelmotor(behandling, vilkår);

        // Ekstraresultat
        HashMap<VilkårType, Object> ekstraResultater = new HashMap<>();
        if (vilkårDataResultat.getEkstraVilkårresultat() != null) {
            ekstraResultater.put(vilkårDataResultat.getVilkårType(), vilkårDataResultat.getEkstraVilkårresultat());
        }

        // Inngangsvilkårutfall utledet fra alle vilkårsutfallene
        Set<VilkårUtfallType> alleUtfall = sammenslåVilkårUtfall(vilkårResultat, vilkårDataResultat);
        VilkårResultatType inngangsvilkårUtfall = utledInngangsvilkårUtfall(alleUtfall);
        oppdaterBehandlingMedVilkårresultat(behandling, vilkårDataResultat, inngangsvilkårUtfall);

        // Aksjonspunkter
        List<AksjonspunktDefinisjon> aksjonspunktDefinisjoner = new ArrayList<>();
        if (!vilkår.erOverstyrt()) {
            // TODO (essv): PKMANTIS-1988 Sjekk med Anita om AP for manuell vurdering skal (gjen)opprettes dersom allerede overstyrt
            aksjonspunktDefinisjoner = vilkårDataResultat.getApDefinisjoner();
        }

        return new RegelResultat(vilkårResultat, aksjonspunktDefinisjoner, ekstraResultater);
    }

    private Set<VilkårUtfallType> hentAlleVilkårsutfall(VilkårResultat vilkårResultat) {
        return vilkårResultat.getVilkårene().stream()
            .map(Vilkår::getGjeldendeVilkårUtfall)
            .collect(toSet());
    }

    private void validerMaksEttVilkår(List<Vilkår> vilkårSomSkalBehandle) {
        Objects.check(vilkårSomSkalBehandle.size() <= 1, "Kun ett vilkår skal evalueres per regelkall. " +
            "Her angis vilkår: " + vilkårSomSkalBehandle.stream().map(v -> v.getVilkårType().getKode()).collect(Collectors.joining(",")));
    }

    protected VilkårData vurderVilkår(VilkårType vilkårType, Behandling behandling) {
        Inngangsvilkår inngangsvilkår = inngangsvilkårTjeneste.finnVilkår(vilkårType);
        return inngangsvilkår.vurderVilkår(behandling);
    }

    private Set<VilkårUtfallType> sammenslåVilkårUtfall(VilkårResultat vilkårResultat,
                                                        VilkårData vdRegelmotor) {
        Map<VilkårType, Vilkår> vilkårTyper = vilkårResultat.getVilkårene().stream()
            .collect(toMap(v -> v.getVilkårType(), v -> v));
        Map<VilkårType, VilkårUtfallType> vilkårUtfall = vilkårResultat.getVilkårene().stream()
            .collect(toMap(v -> v.getVilkårType(), v -> v.getGjeldendeVilkårUtfall()));

        Vilkår matchendeVilkår = vilkårTyper.get(vdRegelmotor.getVilkårType());
        java.util.Objects.requireNonNull(matchendeVilkår, "skal finnes match"); //$NON-NLS-1$
        // Utfall fra automatisk regelvurdering skal legges til settet av utfall, dersom vilkår ikke er manuelt vurdert
        if (!(matchendeVilkår.erManueltVurdert() || matchendeVilkår.erOverstyrt())) {
            vilkårUtfall.put(vdRegelmotor.getVilkårType(), vdRegelmotor.getUtfallType());
        }

        return new HashSet<>(vilkårUtfall.values());
    }

    private VilkårData kjørRegelmotor(Behandling behandling, Vilkår vilkår) {
        return vurderVilkår(vilkår.getVilkårType(), behandling);
    }

    VilkårResultatType utledInngangsvilkårUtfall(Collection<VilkårUtfallType> vilkårene) {
        boolean oppfylt = vilkårene.stream()
            .anyMatch(utfall -> utfall.equals(VilkårUtfallType.OPPFYLT));
        boolean ikkeOppfylt = vilkårene.stream()
            .anyMatch(vilkår -> vilkår.equals(VilkårUtfallType.IKKE_OPPFYLT));
        boolean ikkeVurdert = vilkårene.stream()
            .anyMatch(vilkår -> vilkår.equals(VilkårUtfallType.IKKE_VURDERT));

        // Enkeltutfallene per vilkår sammenstilles til et samlet vilkårsresultat.
        // Høyest rangerte enkeltutfall ift samlet vilkårsresultat sjekkes først, deretter nest høyeste osv.
        VilkårResultatType vilkårResultatType;
        if (ikkeOppfylt) {
            vilkårResultatType = VilkårResultatType.AVSLÅTT;
        } else if (ikkeVurdert) {
            vilkårResultatType = VilkårResultatType.IKKE_FASTSATT;
        } else if (oppfylt) {
            vilkårResultatType = VilkårResultatType.INNVILGET;
        } else {
            throw FEILFACTORY.kanIkkeUtledeVilkårsresultatFraRegelmotor().toException();
        }

        return vilkårResultatType;
    }

    private void oppdaterBehandlingMedVilkårresultat(Behandling behandling, VilkårResultatType inngangsvilkårUtfall) {
        VilkårResultat.Builder builder = VilkårResultat
            .builderFraEksisterende(behandling.getBehandlingsresultat().getVilkårResultat())
            .medVilkårResultatType(inngangsvilkårUtfall);
        builder.buildFor(behandling);
    }

    private void oppdaterBehandlingMedVilkårresultat(Behandling behandling,
                                                     VilkårData vilkårData, VilkårResultatType inngangsvilkårUtfall) {

        VilkårResultat.Builder builder = VilkårResultat
            .builderFraEksisterende(behandling.getBehandlingsresultat().getVilkårResultat())
            .medVilkårResultatType(inngangsvilkårUtfall);
        builder.leggTilVilkårResultat(vilkårData.getVilkårType(),
            vilkårData.getUtfallType(), vilkårData.getVilkårUtfallMerknad(), vilkårData.getMerknadParametere(),
            vilkårData.getAvslagsårsak(), false, vilkårData.erOverstyrt(),
            vilkårData.getRegelEvaluering(), vilkårData.getRegelInput());

        builder.buildFor(behandling);
    }
}
