package no.nav.foreldrepenger.domene.beregningsgrunnlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.FaktaOmBeregningTilfelle;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;

@ApplicationScoped
public class FaktaOmBeregningTilfelleTjeneste {
    private KontrollerFaktaBeregningTjeneste kontrollerFaktaBeregningTjeneste;
    private KontrollerFaktaBeregningFrilanserTjeneste kontrollerFaktaBeregningFrilanserTjeneste;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;

    public FaktaOmBeregningTilfelleTjeneste() {
        // For CDI
    }

    @Inject
    public FaktaOmBeregningTilfelleTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                            KontrollerFaktaBeregningTjeneste kontrollerFaktaBeregningTjeneste,
                                            KontrollerFaktaBeregningFrilanserTjeneste kontrollerFaktaBeregningFrilanserTjeneste) {
        this.kontrollerFaktaBeregningTjeneste = kontrollerFaktaBeregningTjeneste;
        this.kontrollerFaktaBeregningFrilanserTjeneste = kontrollerFaktaBeregningFrilanserTjeneste;
        this.beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
    }

    List<FaktaOmBeregningTilfelle> utledOgLagreFaktaOmBeregningTilfeller(Behandling behandling) {
        List<FaktaOmBeregningTilfelle> fellesAksjonspunktTilfeller = finnTilfellerForFellesAksjonspunkt(behandling);
        if (!fellesAksjonspunktTilfeller.isEmpty()) {
            Beregningsgrunnlag bg = beregningsgrunnlagRepository.hentAggregat(behandling).dypKopi();
            Beregningsgrunnlag.builder(bg).leggTilFaktaOmBeregningTilfeller(fellesAksjonspunktTilfeller);
            beregningsgrunnlagRepository.lagre(behandling, bg, BeregningsgrunnlagTilstand.OPPRETTET);
        }
        return fellesAksjonspunktTilfeller;
    }

    public List<FaktaOmBeregningTilfelle> finnTilfellerForFellesAksjonspunkt(Behandling behandling) {
        if (kontrollerFaktaBeregningTjeneste.brukerMedAktivitetStatusTY(behandling)) {
            return finnTilfellerIKombinasjonMedTY(behandling);
        }
        List<FaktaOmBeregningTilfelle> tilfeller = new ArrayList<>();
        Map<BeregningsgrunnlagPrStatusOgAndel, Yrkesaktivitet> kortvarigeAktiviteter = kontrollerFaktaBeregningTjeneste.hentAndelerForKortvarigeArbeidsforhold(behandling);
        if (!kortvarigeAktiviteter.isEmpty()) {
            tilfeller.add(FaktaOmBeregningTilfelle.VURDER_TIDSBEGRENSET_ARBEIDSFORHOLD);
        }
        if (kontrollerFaktaBeregningTjeneste.erNyIArbeidslivetMedAktivitetStatusSN(behandling)) {
            tilfeller.add(FaktaOmBeregningTilfelle.VURDER_SN_NY_I_ARBEIDSLIVET);
        }
        if (kontrollerFaktaBeregningTjeneste.brukerHarHattLønnsendringOgManglerInntektsmelding(behandling)) {
            tilfeller.add(FaktaOmBeregningTilfelle.VURDER_LØNNSENDRING);
        }
        if (kontrollerFaktaBeregningTjeneste.erLønnsendringIBeregningsperioden(behandling)) {
            tilfeller.add(FaktaOmBeregningTilfelle.FASTSETT_MÅNEDSLØNN_ARBEIDSTAKER_UTEN_INNTEKTSMELDING);
        }
        if (kontrollerFaktaBeregningFrilanserTjeneste.erNyoppstartetFrilanser(behandling)) {
            tilfeller.add(FaktaOmBeregningTilfelle.VURDER_NYOPPSTARTET_FL);
        }
        if (kontrollerFaktaBeregningFrilanserTjeneste.harOverstyrtFrilans(behandling)) {
            tilfeller.add(FaktaOmBeregningTilfelle.FASTSETT_MAANEDSINNTEKT_FL);
        }
        settTilfellerForIkkjeBestebergning(behandling, tilfeller);
        return tilfeller;
    }

    private void settTilfellerForIkkjeBestebergning(Behandling behandling, List<FaktaOmBeregningTilfelle> tilfeller) {
        if (kontrollerFaktaBeregningFrilanserTjeneste.erBrukerArbeidstakerOgFrilanserISammeOrganisasjon(behandling)) {
            tilfeller.add(FaktaOmBeregningTilfelle.VURDER_AT_OG_FL_I_SAMME_ORGANISASJON);
        }
        if (kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling)) {
            tilfeller.add(FaktaOmBeregningTilfelle.FASTSETT_ENDRET_BEREGNINGSGRUNNLAG);
        }
    }

    private List<FaktaOmBeregningTilfelle> finnTilfellerIKombinasjonMedTY(Behandling behandling) {
        List<FaktaOmBeregningTilfelle> tilfeller = new ArrayList<>();
        tilfeller.add(FaktaOmBeregningTilfelle.TILSTØTENDE_YTELSE);
        Map<BeregningsgrunnlagPrStatusOgAndel, Yrkesaktivitet> kortvarigeAktiviteter = kontrollerFaktaBeregningTjeneste.hentAndelerForKortvarigeArbeidsforhold(behandling);
        if (!kortvarigeAktiviteter.isEmpty()) {
            tilfeller.add(FaktaOmBeregningTilfelle.VURDER_TIDSBEGRENSET_ARBEIDSFORHOLD);
        }
        if (kontrollerFaktaBeregningTjeneste.erNyIArbeidslivetMedAktivitetStatusSN(behandling)) {
            tilfeller.add(FaktaOmBeregningTilfelle.VURDER_SN_NY_I_ARBEIDSLIVET);
        }
        if (kontrollerFaktaBeregningFrilanserTjeneste.erBrukerArbeidstakerOgFrilanserISammeOrganisasjon(behandling)) {
            tilfeller.add(FaktaOmBeregningTilfelle.VURDER_AT_OG_FL_I_SAMME_ORGANISASJON);
        }
        if (kontrollerFaktaBeregningFrilanserTjeneste.erNyoppstartetFrilanser(behandling)) {
            tilfeller.add(FaktaOmBeregningTilfelle.VURDER_NYOPPSTARTET_FL);
        }
        if (kontrollerFaktaBeregningTjeneste.brukerHarHattLønnsendringOgManglerInntektsmelding(behandling)) {
            tilfeller.add(FaktaOmBeregningTilfelle.VURDER_LØNNSENDRING);
        }
        if (kontrollerFaktaBeregningTjeneste.erLønnsendringIBeregningsperioden(behandling)) {
            tilfeller.add(FaktaOmBeregningTilfelle.FASTSETT_MÅNEDSLØNN_ARBEIDSTAKER_UTEN_INNTEKTSMELDING);
        }
        if (kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling)) {
            tilfeller.add(FaktaOmBeregningTilfelle.FASTSETT_ENDRET_BEREGNINGSGRUNNLAG);
        }
        return tilfeller;
    }
}
