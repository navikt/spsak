package no.nav.foreldrepenger.domene.beregningsgrunnlag;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.regelmodelltilvl.MapBeregningsgrunnlagFraRegelTilVL;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.jackson.JacksonJsonConfig;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Dekningsgrad;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.skjæringstidspunkt.AktivitetStatusModell;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.skjæringstidspunkt.RegelFastsettSkjæringstidspunkt;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.skjæringstidspunkt.status.RegelFastsettStatusVedSkjæringstidspunkt;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.RegelmodellOversetter;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

@ApplicationScoped
public class FastsettSkjæringstidspunktOgStatuser {

    private MapBeregningsgrunnlagFraVLTilRegel oversetterTilRegel;
    private MapBeregningsgrunnlagFraRegelTilVL oversetterFraRegel;

    private JacksonJsonConfig jacksonJsonConfig = new JacksonJsonConfig();

    FastsettSkjæringstidspunktOgStatuser() {
        // for CDI proxy
    }

    @Inject
    public FastsettSkjæringstidspunktOgStatuser(MapBeregningsgrunnlagFraVLTilRegel oversetterTilRegel, MapBeregningsgrunnlagFraRegelTilVL oversetterFraRegel) {
        this.oversetterTilRegel = oversetterTilRegel;
        this.oversetterFraRegel = oversetterFraRegel;
    }

    public Beregningsgrunnlag fastsettSkjæringstidspunktOgStatuser(Behandling behandling) {
        // Oversetter Opptjening -> regelmodell, hvor også skjæringstidspunkt for Opptjening er lagret
        AktivitetStatusModell regelmodell = oversetterTilRegel.mapForSkjæringstidspunktOgStatuser(behandling);

        // Tar sporingssnapshot av regelmodell, deretter oppdateres modell med fastsatt skjæringstidspunkt for Beregning
        String inputSkjæringstidspunkt = toJson(regelmodell);
        Evaluation evaluationSkjæringstidspunkt = new RegelFastsettSkjæringstidspunkt().evaluer(regelmodell);

        // Tar sporingssnapshot av regelmodell, deretter oppdateres modell med status per beregningsgrunnlag
        String inputStatusFastsetting = toJson(regelmodell);
        Evaluation evaluationStatusFastsetting = new RegelFastsettStatusVedSkjæringstidspunkt().evaluer(regelmodell);

        // Oversett endelig resultat av regelmodell (+ spore input -> evaluation)
        List<String> regelInputer = List.of(inputSkjæringstidspunkt, inputStatusFastsetting);
        List<RegelResultat> regelResultater = List.of(RegelmodellOversetter.getRegelResultat(evaluationSkjæringstidspunkt), RegelmodellOversetter.getRegelResultat(evaluationStatusFastsetting));
        Dekningsgrad dekningsgrad = oversetterTilRegel.mapDekningsgrad(behandling);
        return oversetterFraRegel.mapForSkjæringstidspunktOgStatuser(behandling, regelmodell, dekningsgrad, regelInputer, regelResultater);
    }

    private String toJson(AktivitetStatusModell grunnlag) {
        return jacksonJsonConfig.toJson(grunnlag, RegelFeil.FEILFACTORY::kanIkkeSerialisereRegelinput);
    }

    interface RegelFeil extends DeklarerteFeil {
        RegelFeil FEILFACTORY = FeilFactory.create(RegelFeil.class);

        @TekniskFeil(feilkode = "FP-330602", feilmelding = "Kunne ikke serialisere regelinput for skjæringstidspunkt og status.", logLevel = LogLevel.WARN)
        Feil kanIkkeSerialisereRegelinput(JsonProcessingException e);
    }
}
