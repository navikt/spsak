package no.nav.foreldrepenger.domene.beregningsgrunnlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallMerknad;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.regelmodelltilvl.MapBeregningsgrunnlagFraRegelTilVL;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.jackson.JacksonJsonConfig;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.RegelMerknad;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.RegelmodellOversetter;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.fastsette.RegelFullføreBeregningsgrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

@ApplicationScoped
public class FullføreBeregningsgrunnlag {

    private MapBeregningsgrunnlagFraVLTilRegel oversetterTilRegel;
    private MapBeregningsgrunnlagFraRegelTilVL oversetterFraRegel;

    private JacksonJsonConfig jacksonJsonConfig = new JacksonJsonConfig();

    FullføreBeregningsgrunnlag() {
        //for CDI proxy
    }

    @Inject
    public FullføreBeregningsgrunnlag(MapBeregningsgrunnlagFraVLTilRegel oversetterTilRegel,
                                      MapBeregningsgrunnlagFraRegelTilVL oversetterFraRegel) {
        this.oversetterTilRegel = oversetterTilRegel;
        this.oversetterFraRegel = oversetterFraRegel;
    }

    public Beregningsgrunnlag fullføreBeregningsgrunnlag(Behandling behandling, Beregningsgrunnlag beregningsgrunnlag) {
        // Oversetter foreslått Beregningsgrunnlag -> regelmodell
        no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag beregningsgrunnlagRegel = oversetterTilRegel.map(behandling, beregningsgrunnlag);

        // Evaluerer hver BeregningsgrunnlagPeriode fra foreslått Beregningsgrunnlag
        List<RegelResultat> regelResultater = new ArrayList<>();
        String input = toJson(beregningsgrunnlagRegel);
        for (BeregningsgrunnlagPeriode periode : beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder()) {
            RegelFullføreBeregningsgrunnlag regel = new RegelFullføreBeregningsgrunnlag(periode);
            Evaluation evaluation = regel.evaluer(periode);
            regelResultater.add(RegelmodellOversetter.getRegelResultat(evaluation));
        }

        // Oversett endelig resultat av regelmodell til fastsatt Beregningsgrunnlag  (+ spore input -> evaluation)
        Beregningsgrunnlag fastsattBeregningsgrunnlag = oversetterFraRegel.mapFastsettBeregningsgrunnlag(beregningsgrunnlagRegel, input, regelResultater, beregningsgrunnlag);
        vurderVilkår(behandling, input, regelResultater);
        return fastsattBeregningsgrunnlag;
    }

    private void vurderVilkår(Behandling behandling, String input,
                              List<RegelResultat> regelResultater) {
        //FIXME(TOPAS) : Hvilken periode brukes for regelsporing o.l i vilkårsresultatet?
        boolean vilkårOppfylt = erVilkårOppfylt(regelResultater);
        opprettVilkårsResultat(behandling, input, regelResultater.get(0), vilkårOppfylt);

        if (!vilkårOppfylt) {
            Behandlingsresultat behandlingsresultat = behandling.getBehandlingsresultat();
            Behandlingsresultat.builderEndreEksisterende(behandlingsresultat).medBehandlingResultatType(BehandlingResultatType.AVSLÅTT);
        }
    }

    private boolean erVilkårOppfylt(List<RegelResultat> regelResultater) {
        List<String> merknadList = regelResultater.stream()
            .flatMap(regelResultat -> regelResultat.getMerknader().stream())
            .map(RegelMerknad::getMerknadKode)
            .collect(Collectors.toList());

        if (merknadList.isEmpty()) {
            return true;
        } else if (merknadList.contains(Avslagsårsak.FOR_LAVT_BEREGNINGSGRUNNLAG.getKode())) {
            return false;
        } else {
            throw new IllegalStateException("Ugyldig merknad i resultat fra regelevaluering");
        }
    }

    // TODO (TOPAS): Se på spesifik merknad, properties, regelinput, regelevaluering
    private void opprettVilkårsResultat(Behandling behandling, String input,
                                        RegelResultat regelResultat, boolean oppfylt) {
        Properties props = new Properties();
        VilkårResultat.Builder builder = VilkårResultat
            .builderFraEksisterende(behandling.getBehandlingsresultat().getVilkårResultat())
            .medVilkårResultatType(oppfylt ? VilkårResultatType.INNVILGET : VilkårResultatType.AVSLÅTT)
            .leggTilVilkårResultat(
                VilkårType.BEREGNINGSGRUNNLAGVILKÅR,
                oppfylt ? VilkårUtfallType.OPPFYLT : VilkårUtfallType.IKKE_OPPFYLT,
                oppfylt ? VilkårUtfallMerknad.UDEFINERT : VilkårUtfallMerknad.VM_1041,
                props,
                oppfylt ? null : Avslagsårsak.FOR_LAVT_BEREGNINGSGRUNNLAG,
                false,
                false,
                regelResultat.getRegelSporing(),
                input);
        builder.buildFor(behandling);
    }

    private String toJson(no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag beregningsgrunnlagRegel) {
        return jacksonJsonConfig.toJson(beregningsgrunnlagRegel, RegelFeil.FEILFACTORY::kanIkkeSerialisereRegelinput);
    }

    interface RegelFeil extends DeklarerteFeil {
        RegelFeil FEILFACTORY = FeilFactory.create(RegelFeil.class);

        @TekniskFeil(feilkode = "FP-380602", feilmelding = "Kunne ikke serialisere regelinput for beregningsgrunnlag.", logLevel = LogLevel.WARN)
        Feil kanIkkeSerialisereRegelinput(JsonProcessingException e);
    }
}
