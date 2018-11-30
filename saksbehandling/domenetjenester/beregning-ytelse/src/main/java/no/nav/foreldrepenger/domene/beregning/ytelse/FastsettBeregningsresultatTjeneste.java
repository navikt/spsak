package no.nav.foreldrepenger.domene.beregning.ytelse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.domene.beregning.regelmodell.BeregningsresultatRegelmodell;
import no.nav.foreldrepenger.domene.beregning.regler.RegelFastsettBeregningsresultat;
import no.nav.foreldrepenger.domene.beregning.ytelse.adapter.MapBeregningsresultatFraRegelTilVL;
import no.nav.foreldrepenger.domene.beregning.ytelse.adapter.MapBeregningsresultatFraVLTilRegel;
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
public class FastsettBeregningsresultatTjeneste {

    private JacksonJsonConfig jacksonJsonConfig = new JacksonJsonConfig();
    private MapBeregningsresultatFraVLTilRegel mapBeregningsresultatFraVLTilRegel;
    private BehandlingRepositoryProvider repositoryProvider;

    FastsettBeregningsresultatTjeneste() {
        //NOSONAR
    }

    @Inject
    public FastsettBeregningsresultatTjeneste(MapBeregningsresultatFraVLTilRegel mapBeregningsresultatFraVLTilRegel,
            BehandlingRepositoryProvider repositoryProvider) {
        this.mapBeregningsresultatFraVLTilRegel = mapBeregningsresultatFraVLTilRegel;
        this.repositoryProvider = repositoryProvider;
    }

    public BeregningsresultatFP fastsettBeregningsresultat(Beregningsgrunnlag beregningsgrunnlag, UttakResultatEntitet uttakResultat, Behandling behandling) {
        // Map til regelmodell
        BeregningsresultatRegelmodell regelmodell = mapBeregningsresultatFraVLTilRegel.mapFra(beregningsgrunnlag, uttakResultat, behandling);
        // Kalle regel
        RegelFastsettBeregningsresultat regel = new RegelFastsettBeregningsresultat();
        no.nav.foreldrepenger.domene.beregning.regelmodell.BeregningsresultatFP outputContainer = no.nav.foreldrepenger.domene.beregning.regelmodell.BeregningsresultatFP.builder().build();
        Evaluation evaluation = regel.evaluer(regelmodell, outputContainer);
        RegelResultat regelResultat = RegelmodellOversetter.getRegelResultat(evaluation);

        // Map tilbake til domenemodell fra regelmodell
        BeregningsresultatFP beregningsresultatFP = BeregningsresultatFP.builder()
            .medRegelInput(toJson(regelmodell))
            .medRegelSporing(regelResultat.getRegelSporing())
            .build();

        MapBeregningsresultatFraRegelTilVL.mapFra(outputContainer, beregningsresultatFP, repositoryProvider.getVirksomhetRepository());

        return beregningsresultatFP;
    }

    private String toJson(BeregningsresultatRegelmodell grunnlag) {
        JacksonJsonConfig var10000 = this.jacksonJsonConfig;
        FastsettBeregningsresultatFeil var10002 = FastsettBeregningsresultatFeil.FACTORY;
        return var10000.toJson(grunnlag, var10002::jsonMappingFeilet);
    }

    interface FastsettBeregningsresultatFeil extends DeklarerteFeil {
        FastsettBeregningsresultatFeil FACTORY = FeilFactory.create(FastsettBeregningsresultatFeil.class); // NOSONAR ok med konstant

        @TekniskFeil(feilkode = "FP-563791",
            feilmelding = "JSON mapping feilet",
            logLevel = LogLevel.ERROR)
        Feil jsonMappingFeilet(JsonProcessingException var1);
    }
}
