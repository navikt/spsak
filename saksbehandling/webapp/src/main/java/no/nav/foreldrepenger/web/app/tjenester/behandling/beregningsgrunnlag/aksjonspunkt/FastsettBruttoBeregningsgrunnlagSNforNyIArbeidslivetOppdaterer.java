package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.aksjonspunkt;

import java.math.BigDecimal;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.behandling.aksjonspunkt.DtoTilServiceAdapter;
import no.nav.foreldrepenger.behandling.aksjonspunkt.OppdateringResultat;
import no.nav.foreldrepenger.behandling.historikk.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.behandlingslager.Kopimaskin;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.aksjonspunkt.dto.FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto.class, adapter = AksjonspunktOppdaterer.class)
public class FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetOppdaterer implements AksjonspunktOppdaterer<FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto> {

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private HistorikkTjenesteAdapter historikkAdapter;
    private AksjonspunktRepository aksjonspunktRepository;

    FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetOppdaterer(GrunnlagRepositoryProvider repositoryProvider,
                                                                          ResultatRepositoryProvider resultatRepositoryProvider,
                                                                          HistorikkTjenesteAdapter historikkAdapter) {
        this.beregningsgrunnlagRepository = resultatRepositoryProvider.getBeregningsgrunnlagRepository();
        this.historikkAdapter = historikkAdapter;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
    }

    @Override
    public OppdateringResultat oppdater(FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto dto, Behandling behandling) {
        Integer bruttoBeregningsgrunnlag = dto.getBruttoBeregningsgrunnlag();
        if (bruttoBeregningsgrunnlag != null) {
            Beregningsgrunnlag grunnlag = beregningsgrunnlagRepository.hentAggregat(behandling);
            Beregningsgrunnlag nyttGrunnlag = Kopimaskin.deepCopy(grunnlag);
            nyttGrunnlag.getBeregningsgrunnlagPerioder().forEach(bgPeriode -> {
                BeregningsgrunnlagPrStatusOgAndel bgAndel = bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                    .filter(bpsa -> bpsa.getAktivitetStatus().erSelvstendigNæringsdrivende())
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Mangler andel for selvstendig næringsdrivende (eller kombinasjon med SN) for behandling "  + behandling.getId()));

                lagHistorikkInnslag(dto, behandling);

                BeregningsgrunnlagPrStatusOgAndel.builder(bgAndel)
                    .medOverstyrtPrÅr(BigDecimal.valueOf(bruttoBeregningsgrunnlag))
                    .build(bgPeriode);
            });
            beregningsgrunnlagRepository.lagre(behandling, nyttGrunnlag, BeregningsgrunnlagTilstand.FASTSATT_INN);
        }
        return OppdateringResultat.utenOveropp();
    }

    private void lagHistorikkInnslag(FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto dto, Behandling behandling) {
        oppdaterVedEndretVerdi(dto.getBruttoBeregningsgrunnlag());
        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());
        boolean erBegrunnelseEndret = aksjonspunktRepository.sjekkErBegrunnelseForAksjonspunktEndret(behandling, aksjonspunktDefinisjon, dto.getBegrunnelse());
        historikkAdapter.tekstBuilder()
            .medBegrunnelse(dto.getBegrunnelse(), erBegrunnelseEndret)
            .medSkjermlenke(aksjonspunktDefinisjon, behandling);
    }

    private void oppdaterVedEndretVerdi(Integer bruttoNæringsInntekt) {
        historikkAdapter.tekstBuilder().medEndretFelt(HistorikkEndretFeltType.BRUTTO_NAERINGSINNTEKT, null, bruttoNæringsInntekt);
    }
}
