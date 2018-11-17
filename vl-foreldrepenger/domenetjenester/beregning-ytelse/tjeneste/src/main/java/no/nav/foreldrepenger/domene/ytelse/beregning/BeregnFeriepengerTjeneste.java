package no.nav.foreldrepenger.domene.ytelse.beregning;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFeriepenger;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.beregning.regelmodell.feriepenger.BeregningsresultatFeriepengerRegelModell;
import no.nav.foreldrepenger.beregning.regler.feriepenger.RegelBeregnFeriepenger;
import no.nav.foreldrepenger.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.foreldrepenger.beregningsgrunnlag.jackson.JacksonJsonConfig;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.foreldrepenger.domene.ytelse.beregning.adapter.MapBeregningsresultatFeriepengerFraRegelTilVL;
import no.nav.foreldrepenger.domene.ytelse.beregning.adapter.MapBeregningsresultatFeriepengerFraVLTilRegel;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

@ApplicationScoped
public class BeregnFeriepengerTjeneste {

    private JacksonJsonConfig jacksonJsonConfig = new JacksonJsonConfig();

    @Inject
    public BeregnFeriepengerTjeneste() {
        //NOSONAR
    }

    public void beregnFeriepenger(Behandling behandling, BeregningsresultatFP beregningsresultatFP, Beregningsgrunnlag beregningsgrunnlag) {

        BeregningsresultatFeriepengerRegelModell regelModell = MapBeregningsresultatFeriepengerFraVLTilRegel.mapFra(beregningsgrunnlag, behandling, beregningsresultatFP);
        String regelInput = toJson(regelModell);

        RegelBeregnFeriepenger regelBeregnFeriepenger = new RegelBeregnFeriepenger();
        Evaluation evaluation = regelBeregnFeriepenger.evaluer(regelModell);
        RegelResultat regelResultat = RegelmodellOversetter.getRegelResultat(evaluation);

        BeregningsresultatFeriepenger beregningsresultatFeriepenger = BeregningsresultatFeriepenger.builder()
            .medFeriepengerRegelInput(regelInput)
            .medFeriepengerRegelSporing(regelResultat.getRegelSporing())
            .build(beregningsresultatFP);

        MapBeregningsresultatFeriepengerFraRegelTilVL.mapFra(beregningsresultatFP, regelModell, beregningsresultatFeriepenger);
    }

    private String toJson(BeregningsresultatFeriepengerRegelModell grunnlag) {
        JacksonJsonConfig var10000 = this.jacksonJsonConfig;
        BeregnFeriepengerFeil var10002 = BeregnFeriepengerFeil.FACTORY;
        return var10000.toJson(grunnlag, var10002::jsonMappingFeilet);
    }

    interface BeregnFeriepengerFeil extends DeklarerteFeil {
        BeregnFeriepengerFeil FACTORY = FeilFactory.create(BeregnFeriepengerFeil.class); // NOSONAR ok med konstant

        @TekniskFeil(feilkode = "FP-985762", feilmelding = "JSON mapping feilet", logLevel = LogLevel.ERROR)
        Feil jsonMappingFeilet(JsonProcessingException var1);
    }
}
