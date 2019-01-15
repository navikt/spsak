package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.aksjonspunkt;

import java.math.BigDecimal;
import java.util.List;

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
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.aksjonspunkt.dto.FastsettBruttoBeregningsgrunnlagSNDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = FastsettBruttoBeregningsgrunnlagSNDto.class, adapter = AksjonspunktOppdaterer.class)
public class FastsettBruttoBeregningsgrunnlagSNOppdaterer implements AksjonspunktOppdaterer<FastsettBruttoBeregningsgrunnlagSNDto> {

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private HistorikkTjenesteAdapter historikkAdapter;
    private AksjonspunktRepository aksjonspunktRepository;

    FastsettBruttoBeregningsgrunnlagSNOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public FastsettBruttoBeregningsgrunnlagSNOppdaterer(GrunnlagRepositoryProvider repositoryProvider,
                                                        ResultatRepositoryProvider resultatRepositoryProvider,
                                                        HistorikkTjenesteAdapter historikkAdapter) {
        this.beregningsgrunnlagRepository = resultatRepositoryProvider.getBeregningsgrunnlagRepository();
        this.historikkAdapter = historikkAdapter;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
    }

    @Override
    public OppdateringResultat oppdater(FastsettBruttoBeregningsgrunnlagSNDto dto, Behandling behandling) {
        Integer bruttoBeregningsgrunnlag = dto.getBruttoBeregningsgrunnlag();
        if (bruttoBeregningsgrunnlag != null) {
            Beregningsgrunnlag grunnlag = beregningsgrunnlagRepository.hentAggregat(behandling);
            Beregningsgrunnlag nyttGrunnlag = Kopimaskin.deepCopy(grunnlag);
            List<BeregningsgrunnlagPeriode> bgPerioder = nyttGrunnlag.getBeregningsgrunnlagPerioder();
            for (BeregningsgrunnlagPeriode bgPeriode : bgPerioder) {
                BeregningsgrunnlagPrStatusOgAndel bgAndel = bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                    .filter(bpsa -> AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE.equals(bpsa.getAktivitetStatus()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Mangler BeregningsgrunnlagPrStatusOgAndel[SELVSTENDIG_NÆRINGSDRIVENDE] for behandling " + behandling.getId()));

                BeregningsgrunnlagPrStatusOgAndel.builder(bgAndel)
                    .medOverstyrtPrÅr(BigDecimal.valueOf(bruttoBeregningsgrunnlag))
                    .build(bgPeriode);
            }
            lagHistorikkInnslag(dto, behandling);
            beregningsgrunnlagRepository.lagre(behandling, nyttGrunnlag, BeregningsgrunnlagTilstand.FASTSATT_INN);
        }
        return OppdateringResultat.utenOveropp();
    }

    private void lagHistorikkInnslag(FastsettBruttoBeregningsgrunnlagSNDto dto, Behandling behandling) {
        HistorikkInnslagTekstBuilder historikkDelBuilder = historikkAdapter.tekstBuilder();
        historikkDelBuilder.ferdigstillHistorikkinnslagDel();
        oppdaterVedEndretVerdi(historikkDelBuilder, dto.getBruttoBeregningsgrunnlag());

        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());

        boolean erBegrunnelseEndret = aksjonspunktRepository.sjekkErBegrunnelseForAksjonspunktEndret(behandling, aksjonspunktDefinisjon, dto.getBegrunnelse());
        historikkDelBuilder.medBegrunnelse(dto.getBegrunnelse(), erBegrunnelseEndret);
    }

    private void oppdaterVedEndretVerdi(HistorikkInnslagTekstBuilder historikkDelBuilder, Integer bruttoNæringsInntekt) {
        historikkDelBuilder.medEndretFelt(HistorikkEndretFeltType.BRUTTO_NAERINGSINNTEKT, null, bruttoNæringsInntekt);
    }
}
