package no.nav.foreldrepenger.beregningsgrunnlag;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Ytelse;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.beregningsgrunnlag.adapter.regelmodelltilvl.MapBeregningsgrunnlagFraTilstøtendeYtelseFraRegelTilVL;
import no.nav.foreldrepenger.beregningsgrunnlag.adapter.vltilregelmodell.MapBeregningsgrunnlagFraTilstøtendeYtelseFraVLTilRegel;
import no.nav.foreldrepenger.beregningsgrunnlag.jackson.JacksonJsonConfig;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagFraTilstøtendeYtelse;
import no.nav.foreldrepenger.beregningsgrunnlag.ytelse.RegelOpprettBeregningsgrunnlagsandelerForTilstøtendeYtelse;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningInntektArbeidYtelseTjeneste;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

@ApplicationScoped
public class BeregningsgrunnlagFraTilstøtendeYtelseTjenesteImpl implements BeregningsgrunnlagFraTilstøtendeYtelseTjeneste {
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;
    private OpptjeningInntektArbeidYtelseTjeneste opptjeningInntektArbeidYtelseTjeneste;
    private MapBeregningsgrunnlagFraTilstøtendeYtelseFraVLTilRegel mapBeregningsgrunnlagFraTilstøtendeYtelseFraVLTilRegel;
    private MapBeregningsgrunnlagFraTilstøtendeYtelseFraRegelTilVL mapBeregningsgrunnlagFraTilstøtendeYtelseFraRegelTilVL;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private JacksonJsonConfig jacksonJsonConfig = new JacksonJsonConfig();

    public BeregningsgrunnlagFraTilstøtendeYtelseTjenesteImpl() {
    }

    @Inject
    public BeregningsgrunnlagFraTilstøtendeYtelseTjenesteImpl(BehandlingRepositoryProvider behandlingRepositoryProvider, OpptjeningInntektArbeidYtelseTjeneste opptjeningInntektArbeidYtelseTjeneste,
                                                              MapBeregningsgrunnlagFraTilstøtendeYtelseFraVLTilRegel mapBeregningsgrunnlagFraTilstøtendeYtelseFraVLTilRegel,
                                                              MapBeregningsgrunnlagFraTilstøtendeYtelseFraRegelTilVL mapBeregningsgrunnlagFraTilstøtendeYtelseFraRegelTilVL,
                                                              SkjæringstidspunktTjeneste skjæringstidspunktTjeneste) {
        this.inntektArbeidYtelseRepository = behandlingRepositoryProvider.getInntektArbeidYtelseRepository();
        this.opptjeningInntektArbeidYtelseTjeneste = opptjeningInntektArbeidYtelseTjeneste;
        this.mapBeregningsgrunnlagFraTilstøtendeYtelseFraVLTilRegel = mapBeregningsgrunnlagFraTilstøtendeYtelseFraVLTilRegel;
        this.mapBeregningsgrunnlagFraTilstøtendeYtelseFraRegelTilVL = mapBeregningsgrunnlagFraTilstøtendeYtelseFraRegelTilVL;
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
    }

    @Override
    public Beregningsgrunnlag opprettBeregningsgrunnlagFraTilstøtendeYtelse(Behandling behandling, Beregningsgrunnlag beregningsgrunnlag) {
        InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag = inntektArbeidYtelseRepository.hentAggregat(behandling,
            skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling));
        List<Ytelse> sammenhengendeYtelser = opptjeningInntektArbeidYtelseTjeneste.hentSammenhengendeInfotrygdYtelserFørSkjæringstidspunktForOppjening(behandling);
        if (sammenhengendeYtelser.isEmpty()){
            throw new IllegalStateException("Fant ingen tidligere ytelse for behandling: " + behandling.getId() + ", aktørId: " + behandling.getAktørId());
        }
        BeregningsgrunnlagFraTilstøtendeYtelse bgFraTY =
            mapBeregningsgrunnlagFraTilstøtendeYtelseFraVLTilRegel.map(behandling, beregningsgrunnlag, inntektArbeidYtelseGrunnlag, sammenhengendeYtelser);

        String regelInput = toJson(bgFraTY);
        Evaluation evaluation = new RegelOpprettBeregningsgrunnlagsandelerForTilstøtendeYtelse().evaluer(bgFraTY);
        RegelResultat regelResultat = RegelmodellOversetter.getRegelResultat(evaluation);
        String regelLogg = regelResultat.getRegelSporing();

        return mapBeregningsgrunnlagFraTilstøtendeYtelseFraRegelTilVL.map(beregningsgrunnlag, bgFraTY, regelInput, regelLogg);
    }

    private String toJson(BeregningsgrunnlagFraTilstøtendeYtelse beregningsgrunnlag) {
        return jacksonJsonConfig.toJson(beregningsgrunnlag, RegelFeil.FEILFACTORY::kanIkkeSerialisereRegelinput);
    }

    interface RegelFeil extends DeklarerteFeil {
        RegelFeil FEILFACTORY = FeilFactory.create(RegelFeil.class);

        @TekniskFeil(feilkode = "FP-370601", feilmelding = "Kunne ikke serialisere regelinput for beregningsgrunnlag fra tilstøtende ytelse.", logLevel = LogLevel.WARN)
        Feil kanIkkeSerialisereRegelinput(JsonProcessingException e);
    }
}
