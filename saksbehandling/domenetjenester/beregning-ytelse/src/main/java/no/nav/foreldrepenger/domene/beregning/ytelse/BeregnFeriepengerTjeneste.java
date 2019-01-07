package no.nav.foreldrepenger.domene.beregning.ytelse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatFeriepenger;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.domene.beregning.regelmodell.feriepenger.BeregningsresultatFeriepengerRegelModell;
import no.nav.foreldrepenger.domene.beregning.regler.feriepenger.RegelBeregnFeriepenger;
import no.nav.foreldrepenger.domene.beregning.ytelse.adapter.MapBeregningsresultatFeriepengerFraRegelTilVL;
import no.nav.foreldrepenger.domene.beregning.ytelse.adapter.MapBeregningsresultatFeriepengerFraVLTilRegel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.jackson.JacksonJsonConfig;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.RegelmodellOversetter;
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

    public void beregnFeriepenger(Behandling behandling, BeregningsresultatPerioder beregningsresultat, Beregningsgrunnlag beregningsgrunnlag) {

        BeregningsresultatFeriepengerRegelModell regelModell = MapBeregningsresultatFeriepengerFraVLTilRegel.mapFra(beregningsgrunnlag, behandling, beregningsresultat);
        String regelInput = toJson(regelModell);

        RegelBeregnFeriepenger regelBeregnFeriepenger = new RegelBeregnFeriepenger();
        Evaluation evaluation = regelBeregnFeriepenger.evaluer(regelModell);
        RegelResultat regelResultat = RegelmodellOversetter.getRegelResultat(evaluation);

        BeregningsresultatFeriepenger beregningsresultatFeriepenger = BeregningsresultatFeriepenger.builder()
            .medFeriepengerRegelInput(regelInput)
            .medFeriepengerRegelSporing(regelResultat.getRegelSporing())
            .build(beregningsresultat);

        MapBeregningsresultatFeriepengerFraRegelTilVL.mapFra(beregningsresultat, regelModell, beregningsresultatFeriepenger);
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
