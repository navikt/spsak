package no.nav.foreldrepenger.domene.beregning.ytelse.adapter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.domene.beregning.regelmodell.BeregningsresultatRegelmodell;
import no.nav.foreldrepenger.domene.beregning.regelmodell.UttakResultat;

@ApplicationScoped
public class MapBeregningsresultatFraVLTilRegel {

    private MapUttakResultatFraVLTilRegel mapUttakResultatFraVLTilRegel;

    MapBeregningsresultatFraVLTilRegel() {
        //Skal ikke instansieres
    }

    @Inject
    public MapBeregningsresultatFraVLTilRegel(MapUttakResultatFraVLTilRegel mapUttakResultatFraVLTilRegel) {
        this.mapUttakResultatFraVLTilRegel = mapUttakResultatFraVLTilRegel;
    }

    public BeregningsresultatRegelmodell mapFra(Beregningsgrunnlag vlBeregningsgrunnlag, UttakResultatEntitet vlUttakResultatPlan, Behandling behandling) {
        no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.Beregningsgrunnlag regelBeregningsgrunnlag = MapBeregningsgrunnlagFraVLTilRegel.map(vlBeregningsgrunnlag);
        UttakResultat regelUttakResultat = mapUttakResultatFraVLTilRegel.mapFra(vlUttakResultatPlan, behandling);
        return new BeregningsresultatRegelmodell(regelBeregningsgrunnlag, regelUttakResultat);
    }
}
