package no.nav.foreldrepenger.domene.beregningsgrunnlag;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;

@ApplicationScoped
public class OpprettBeregningsgrunnlagTjeneste {
    private FastsettSkjæringstidspunktOgStatuser fastsettSkjæringstidspunktOgStatuser;
    private FastsettInntektskategoriFraSøknadTjeneste fastsettInntektskategoriFraSøknadTjeneste;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private BeregningsgrunnlagFraTilstøtendeYtelseTjeneste beregningsgrunnlagFraTilstøtendeYtelseTjeneste;
    private FastsettBeregningsgrunnlagPeriodeTjeneste fastsettBeregningsgrunnlagPerioderTjeneste;
    private HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste;

    protected OpprettBeregningsgrunnlagTjeneste() {
        // for CDI proxy
    }


    @Inject
    public OpprettBeregningsgrunnlagTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                             FastsettSkjæringstidspunktOgStatuser fastsettSkjæringstidspunktOgStatuser,
                                             FastsettInntektskategoriFraSøknadTjeneste fastsettInntektskategoriFraSøknadTjeneste,
                                             BeregningsgrunnlagFraTilstøtendeYtelseTjeneste beregningsgrunnlagFraTilstøtendeYtelseTjeneste,
                                             FastsettBeregningsgrunnlagPeriodeTjeneste fastsettBeregningsgrunnlagPerioderTjeneste,
                                             HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste) {
        this.beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
        this.fastsettSkjæringstidspunktOgStatuser = fastsettSkjæringstidspunktOgStatuser;
        this.fastsettInntektskategoriFraSøknadTjeneste = fastsettInntektskategoriFraSøknadTjeneste;
        this.beregningsgrunnlagFraTilstøtendeYtelseTjeneste = beregningsgrunnlagFraTilstøtendeYtelseTjeneste;
        this.fastsettBeregningsgrunnlagPerioderTjeneste = fastsettBeregningsgrunnlagPerioderTjeneste;
        this.hentGrunnlagsdataTjeneste = hentGrunnlagsdataTjeneste;
    }

    /**
     * Henter inn grunnlagsdata om nødvendig
     * Oppretter og bygger beregningsgrunnlag for behandlingen
     * Oppretter perioder og andeler på beregningsgrunnlag
     * Setter inntektskategori på andeler
     * Splitter perioder basert på refusjon, gradering og naturalytelse.
     *
     * @param behandling
     */
    public void opprettOgLagreBeregningsgrunnlag(Behandling behandling) {
        // Innhent grunnlagsdata
        if (hentGrunnlagsdataTjeneste.vurderOmNyesteGrunnlagsdataSkalHentes(behandling)) {
            hentGrunnlagsdataTjeneste.innhentInntektsInformasjonBeregningOgSammenligning(behandling);
        }
        Beregningsgrunnlag beregningsgrunnlag = fastsettSkjæringstidspunktOgStatuser.fastsettSkjæringstidspunktOgStatuser(behandling);
        fastsettInntektskategoriFraSøknadTjeneste.fastsettInntektskategori(beregningsgrunnlag, behandling);
        if (harTilstøtendeYtelse(beregningsgrunnlag)) {
            beregningsgrunnlagFraTilstøtendeYtelseTjeneste.opprettBeregningsgrunnlagFraTilstøtendeYtelse(behandling, beregningsgrunnlag);
        }
        fastsettBeregningsgrunnlagPerioderTjeneste.fastsettPerioder(behandling, beregningsgrunnlag);
        kopierFraGjeldendeBeregningsgrunnlag(behandling, beregningsgrunnlag);
        beregningsgrunnlagRepository.lagre(behandling, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPRETTET);
        settGjeldendeBeregningsgrunnlag(behandling, beregningsgrunnlag);
    }

    /**
     * Setter gjeldende beregningsgrunnlag for faktaavklaring
     *
     * @param behandling
     * @param beregningsgrunnlag aktivt beregningsgrunnlag
     */
    private void settGjeldendeBeregningsgrunnlag(Behandling behandling, Beregningsgrunnlag beregningsgrunnlag) {
        Optional<Beregningsgrunnlag> gjeldendeBeregningsgrunnlag = hentGrunnlagsdataTjeneste.hentGjeldendeBeregningsgrunnlag(behandling);
        Beregningsgrunnlag nyttBG = beregningsgrunnlag.dypKopi();
        gjeldendeBeregningsgrunnlag.ifPresent(bg -> Beregningsgrunnlag.builder(nyttBG).medGjeldendeBeregningsgrunnlag(bg).build());
        beregningsgrunnlagRepository.lagre(behandling, nyttBG, BeregningsgrunnlagTilstand.OPPRETTET);
    }

    /**
     * Kopier informasjon fra gjeldende beregningsgrunnlag i tilfeller hvor det ikke er gjort endringer som påvirker beregningsgrunnlaget
     */
    private void kopierFraGjeldendeBeregningsgrunnlag(Behandling behandling, Beregningsgrunnlag beregningsgrunnlag) {
        if (!hentGrunnlagsdataTjeneste.vurderOmNyesteGrunnlagsdataSkalHentes(behandling)) {
            hentGrunnlagsdataTjeneste.hentGjeldendeBeregningsgrunnlag(behandling).ifPresent(gjeldendeBG ->
                KopierBeregningsgrunnlag.kopierOverstyrteVerdier(gjeldendeBG, beregningsgrunnlag));
        }
    }

    private boolean harTilstøtendeYtelse(Beregningsgrunnlag beregningsgrunnlag) {
        return beregningsgrunnlag.getAktivitetStatuser().stream().anyMatch(status ->
            AktivitetStatus.TILSTØTENDE_YTELSE.equals(status.getAktivitetStatus()));
    }
}
