package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.aksjonspunkt;

import java.math.BigDecimal;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.behandling.aksjonspunkt.DtoTilServiceAdapter;
import no.nav.foreldrepenger.behandling.aksjonspunkt.OppdateringResultat;
import no.nav.foreldrepenger.behandling.historikk.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltVerdiType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.aksjonspunkt.dto.VurderVarigEndringEllerNyoppstartetSNDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = VurderVarigEndringEllerNyoppstartetSNDto.class, adapter = AksjonspunktOppdaterer.class)
public class VurderVarigEndringEllerNyoppstartetSNOppdaterer implements AksjonspunktOppdaterer<VurderVarigEndringEllerNyoppstartetSNDto> {

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private HistorikkTjenesteAdapter historikkAdapter;
    private AksjonspunktRepository aksjonspunktRepository;

    private static final AksjonspunktDefinisjon FASTSETTBRUTTOSNKODE = AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_SELVSTENDIG_NÆRINGSDRIVENDE;

    VurderVarigEndringEllerNyoppstartetSNOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public VurderVarigEndringEllerNyoppstartetSNOppdaterer(GrunnlagRepositoryProvider repositoryProvider,
                                                           ResultatRepositoryProvider resultatRepositoryProvider,
                                                           HistorikkTjenesteAdapter historikkAdapter) {
        this.beregningsgrunnlagRepository = resultatRepositoryProvider.getBeregningsgrunnlagRepository();
        this.historikkAdapter = historikkAdapter;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
    }

    @Override
    public OppdateringResultat oppdater(VurderVarigEndringEllerNyoppstartetSNDto dto, Behandling behandling) {
        beregningsgrunnlagRepository.hentAggregat(behandling);

        boolean erVarigEndrettNæring = dto.getErVarigEndretNaering();
        if (erVarigEndrettNæring) {
            aksjonspunktRepository.leggTilAksjonspunkt(behandling,FASTSETTBRUTTOSNKODE, BehandlingStegType.FORESLÅ_BEREGNINGSGRUNNLAG);
        } else {
            if (behandling.getAksjonspunktMedDefinisjonOptional(FASTSETTBRUTTOSNKODE).isPresent()) {
                aksjonspunktRepository.fjernAksjonspunkt(behandling, FASTSETTBRUTTOSNKODE);
            }
            Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagRepository.hentAggregat(behandling);
            Beregningsgrunnlag nyttBeregningsgrunnlag = beregningsgrunnlag.dypKopi();
            List<BeregningsgrunnlagPeriode> bgPerioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
            for (BeregningsgrunnlagPeriode bgPeriode : bgPerioder) {
                BeregningsgrunnlagPrStatusOgAndel bgAndel = bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                    .filter(bpsa -> AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE.equals(bpsa.getAktivitetStatus()))
                    .findFirst().orElseThrow(() -> new IllegalStateException("Kunne ikke finne BeregningsgrunnlagPrStatusOgAndel for SELVSTENDIG_NÆRINGSDRIVENDE"));
                BigDecimal overstyrtPrÅr = bgAndel.getOverstyrtPrÅr();
                if (overstyrtPrÅr != null) {
                    BeregningsgrunnlagPrStatusOgAndel.builder(bgAndel)
                        .medOverstyrtPrÅr(null)
                        .build(bgPeriode);
                }
            }
            beregningsgrunnlagRepository.lagre(behandling, nyttBeregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT);
        }

        lagHistorikkInnslag(dto, behandling, erVarigEndrettNæring);

        return OppdateringResultat.utenOveropp();
    }

    private void lagHistorikkInnslag(VurderVarigEndringEllerNyoppstartetSNDto dto, Behandling behandling, boolean erVarigEndrettNæring) {

        oppdaterVedEndretVerdi(HistorikkEndretFeltType.ENDRING_NAERING, konvertBooleanTilFaktaEndretVerdiType(erVarigEndrettNæring));

        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());

        historikkAdapter.tekstBuilder()
            .medBegrunnelse(dto.getBegrunnelse(), aksjonspunktRepository.sjekkErBegrunnelseForAksjonspunktEndret(behandling, aksjonspunktDefinisjon, dto.getBegrunnelse()))
            .medSkjermlenke(aksjonspunktDefinisjon, behandling);
    }

    private HistorikkEndretFeltVerdiType konvertBooleanTilFaktaEndretVerdiType(Boolean endringNæring) {
        if (endringNæring == null) {
            return null;
        }
        return endringNæring ? HistorikkEndretFeltVerdiType.VARIG_ENDRET_NAERING : HistorikkEndretFeltVerdiType.INGEN_VARIG_ENDRING_NAERING;
    }

    private boolean oppdaterVedEndretVerdi(HistorikkEndretFeltType historikkEndretFeltType, HistorikkEndretFeltVerdiType bekreftet) {
        historikkAdapter.tekstBuilder().medEndretFelt(historikkEndretFeltType, null, bekreftet);
        return true;
    }
}
