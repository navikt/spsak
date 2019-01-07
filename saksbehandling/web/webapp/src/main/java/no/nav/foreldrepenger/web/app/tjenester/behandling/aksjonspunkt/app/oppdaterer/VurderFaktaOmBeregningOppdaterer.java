package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.FaktaOmBeregningTilfelle;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsgiverHistorikkinnslagTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VurderFaktaOmBeregningDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

@ApplicationScoped
@DtoTilServiceAdapter(dto = VurderFaktaOmBeregningDto.class, adapter = AksjonspunktOppdaterer.class)
public class VurderFaktaOmBeregningOppdaterer implements AksjonspunktOppdaterer<VurderFaktaOmBeregningDto> {

    private VurderNyoppstartetFLOppdaterer vurderNyoppstartetFLOppdaterer;
    private FastsettBruttoBeregningsgrunnlagFLOppdaterer fastsettBruttoBeregningsgrunnlagFLOppdaterer;
    private VurderSelvstendigNæringsdrivendeNyIArbeidslivetOppdaterer vurderSelvstendigNæringsdrivendeNyIArbeidslivetOppdaterer;
    private VurderTidsbegrensetArbeidsforholdOppdaterer vurderTidsbegrensetArbeidsforholdOppdaterer;
    private FastsettMånedsinntektUtenInntektsmeldingOppdaterer fastsettMånedsinntektUtenInntektsmeldingOppdaterer;
    private FastsettMånedsinntektATogFLiSammeOrganisasjonOppdaterer fastsettMånedsinntektATogFLiSammeOrganisasjonOppdaterer;
    private FastsettEndretBeregningsgrunnlagOppdaterer fastsettEndretBeregningsgrunnlagOppdaterer;
    private FastsettBGTilstøtendeYtelseOppdaterer fastsettBGTilstøtendeYtelseOppdaterer;
    private VurderLønnsendringOppdaterer vurderLønnsendringOppdaterer;
    private HistorikkTjenesteAdapter historikkAdapter;
    private AksjonspunktRepository aksjonspunktRepository;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private VurderTilstøtendeYtelseOgEndretBeregninsgrunnlagOppdaterer vurderTilstøtendeYtelseOgEndretBeregningsgrunnlagOppdaterer;


    @Inject
    public VurderFaktaOmBeregningOppdaterer(HistorikkTjenesteAdapter historikkAdapter,
                                            GrunnlagRepositoryProvider repositoryProvider,
                                            ResultatRepositoryProvider resultatRepositoryProvider,
                                            ArbeidsgiverHistorikkinnslagTjeneste arbeidsgiverHistorikkinnslagTjeneste) {
        this.fastsettEndretBeregningsgrunnlagOppdaterer = new FastsettEndretBeregningsgrunnlagOppdaterer(repositoryProvider, resultatRepositoryProvider, historikkAdapter, arbeidsgiverHistorikkinnslagTjeneste);
        this.vurderTidsbegrensetArbeidsforholdOppdaterer = new VurderTidsbegrensetArbeidsforholdOppdaterer(repositoryProvider, historikkAdapter, arbeidsgiverHistorikkinnslagTjeneste);
        this.vurderSelvstendigNæringsdrivendeNyIArbeidslivetOppdaterer = new VurderSelvstendigNæringsdrivendeNyIArbeidslivetOppdaterer(repositoryProvider, historikkAdapter);
        this.fastsettBruttoBeregningsgrunnlagFLOppdaterer = new FastsettBruttoBeregningsgrunnlagFLOppdaterer(repositoryProvider, historikkAdapter);
        this.vurderNyoppstartetFLOppdaterer = new VurderNyoppstartetFLOppdaterer(repositoryProvider, historikkAdapter);
        this.fastsettMånedsinntektUtenInntektsmeldingOppdaterer = new FastsettMånedsinntektUtenInntektsmeldingOppdaterer(historikkAdapter, arbeidsgiverHistorikkinnslagTjeneste);
        this.vurderLønnsendringOppdaterer = new VurderLønnsendringOppdaterer(repositoryProvider, historikkAdapter);
        this.fastsettMånedsinntektATogFLiSammeOrganisasjonOppdaterer = new FastsettMånedsinntektATogFLiSammeOrganisasjonOppdaterer(repositoryProvider, historikkAdapter, arbeidsgiverHistorikkinnslagTjeneste);
        this.fastsettBGTilstøtendeYtelseOppdaterer = new FastsettBGTilstøtendeYtelseOppdaterer(repositoryProvider, resultatRepositoryProvider, historikkAdapter, arbeidsgiverHistorikkinnslagTjeneste);
        this.historikkAdapter = historikkAdapter;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.beregningsgrunnlagRepository = resultatRepositoryProvider.getBeregningsgrunnlagRepository();
        this.vurderTilstøtendeYtelseOgEndretBeregningsgrunnlagOppdaterer = new VurderTilstøtendeYtelseOgEndretBeregninsgrunnlagOppdaterer(repositoryProvider, resultatRepositoryProvider, historikkAdapter, arbeidsgiverHistorikkinnslagTjeneste);
    }

    @Override
    public OppdateringResultat oppdater(VurderFaktaOmBeregningDto dto, Behandling behandling) {

        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagRepository.hentAggregat(behandling);
        Beregningsgrunnlag nyttBeregningsgrunnlag = beregningsgrunnlag.dypKopi();
        List<FaktaOmBeregningTilfelle> tilfeller = dto.getFaktaOmBeregningTilfeller();

        tilfeller.forEach(tilfelle -> {
            if (FaktaOmBeregningTilfelle.VURDER_NYOPPSTARTET_FL.equals(tilfelle)){
                vurderNyoppstartetFLOppdaterer.oppdater(dto.getVurderNyoppstartetFL(), behandling, nyttBeregningsgrunnlag);
            } else if (FaktaOmBeregningTilfelle.FASTSETT_MAANEDSINNTEKT_FL.equals(tilfelle)){
                fastsettBruttoBeregningsgrunnlagFLOppdaterer.oppdater(dto.getFastsettMaanedsinntektFL(), behandling, nyttBeregningsgrunnlag);
            } else if (FaktaOmBeregningTilfelle.VURDER_SN_NY_I_ARBEIDSLIVET.equals(tilfelle)){
                vurderSelvstendigNæringsdrivendeNyIArbeidslivetOppdaterer.oppdater(dto.getVurderNyIArbeidslivet(), behandling, nyttBeregningsgrunnlag);
            } else if (FaktaOmBeregningTilfelle.VURDER_TIDSBEGRENSET_ARBEIDSFORHOLD.equals(tilfelle)){
                vurderTidsbegrensetArbeidsforholdOppdaterer.oppdater(dto.getVurderTidsbegrensetArbeidsforhold(), behandling, nyttBeregningsgrunnlag);
            } else if (FaktaOmBeregningTilfelle.VURDER_LØNNSENDRING.equals(tilfelle)){
                vurderLønnsendringOppdaterer.oppdater(dto.getVurdertLonnsendring(), behandling, nyttBeregningsgrunnlag);
            } else if (FaktaOmBeregningTilfelle.FASTSETT_MÅNEDSLØNN_ARBEIDSTAKER_UTEN_INNTEKTSMELDING.equals(tilfelle)){
                fastsettMånedsinntektUtenInntektsmeldingOppdaterer.oppdater(dto.getFastsatteLonnsendringer(), nyttBeregningsgrunnlag);
            } else if (FaktaOmBeregningTilfelle.VURDER_AT_OG_FL_I_SAMME_ORGANISASJON.equals(tilfelle)){
                fastsettMånedsinntektATogFLiSammeOrganisasjonOppdaterer.oppdater(dto.getVurderATogFLiSammeOrganisasjon(), behandling, nyttBeregningsgrunnlag);
            }
        });

        // Håndtere spesielle kombinasjoner
        håndterKombinasjoner(dto, behandling, nyttBeregningsgrunnlag, tilfeller);

        lagHistorikkInnslag(dto, behandling);
        beregningsgrunnlagRepository.lagre(behandling, nyttBeregningsgrunnlag, BeregningsgrunnlagTilstand.KOFAKBER_UT);

        return OppdateringResultat.utenOveropp();
    }

    private void håndterKombinasjoner(VurderFaktaOmBeregningDto dto, Behandling behandling, Beregningsgrunnlag nyttBeregningsgrunnlag, List<FaktaOmBeregningTilfelle> tilfeller) {
        if (tilfeller.contains(FaktaOmBeregningTilfelle.TILSTØTENDE_YTELSE) || tilfeller.contains(FaktaOmBeregningTilfelle.FASTSETT_ENDRET_BEREGNINGSGRUNNLAG)) {
            if (tilfeller.contains(FaktaOmBeregningTilfelle.TILSTØTENDE_YTELSE) && tilfeller.contains(FaktaOmBeregningTilfelle.FASTSETT_ENDRET_BEREGNINGSGRUNNLAG)) {
                vurderTilstøtendeYtelseOgEndretBeregningsgrunnlagOppdaterer.oppdater(dto.getTilstotendeYtelseOgEndretBG(), behandling, nyttBeregningsgrunnlag);
            } else {
                tilfeller.forEach(tilfelle -> oppdaterForTilfellerUtenKombinasjon(dto, behandling, nyttBeregningsgrunnlag, tilfelle));
            }
        }
    }

    private void oppdaterForTilfellerUtenKombinasjon(VurderFaktaOmBeregningDto dto, Behandling behandling, Beregningsgrunnlag nyttBeregningsgrunnlag, FaktaOmBeregningTilfelle tilfelle) {
        if (FaktaOmBeregningTilfelle.FASTSETT_ENDRET_BEREGNINGSGRUNNLAG.equals(tilfelle)){
            fastsettEndretBeregningsgrunnlagOppdaterer.oppdater(dto.getFastsettEndringBeregningsgrunnlag(), behandling, nyttBeregningsgrunnlag);
        } else if (FaktaOmBeregningTilfelle.TILSTØTENDE_YTELSE.equals(tilfelle)) {
            fastsettBGTilstøtendeYtelseOppdaterer.oppdater(dto.getTilstøtendeYtelse(), behandling, nyttBeregningsgrunnlag);
        }
    }

    private void lagHistorikkInnslag(VurderFaktaOmBeregningDto dto, Behandling behandling) {
        historikkAdapter.tekstBuilder().ferdigstillHistorikkinnslagDel();
        List<HistorikkinnslagDel> historikkDeler = historikkAdapter.tekstBuilder().getHistorikkinnslagDeler();
        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());
        settBegrunnelse(dto, behandling, historikkDeler, aksjonspunktDefinisjon);
        settSkjermlenkeOmIkkjeSatt(behandling, historikkDeler, aksjonspunktDefinisjon);
    }

    private void settBegrunnelse(VurderFaktaOmBeregningDto dto, Behandling behandling, List<HistorikkinnslagDel> historikkDeler, AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        Boolean erBegrunnelseSatt = historikkDeler.stream().anyMatch(historikkDel -> historikkDel.getBegrunnelse().isPresent());
        if (!erBegrunnelseSatt) {
            boolean erBegrunnelseEndret = aksjonspunktRepository.sjekkErBegrunnelseForAksjonspunktEndret(behandling,
                aksjonspunktDefinisjon, dto.getBegrunnelse());
            if (erBegrunnelseEndret){
                historikkAdapter.tekstBuilder().medBegrunnelse(dto.getBegrunnelse(), erBegrunnelseEndret);
                historikkAdapter.tekstBuilder().ferdigstillHistorikkinnslagDel();
            }
        }
    }

    private void settSkjermlenkeOmIkkjeSatt(Behandling behandling, List<HistorikkinnslagDel> historikkDeler, AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        Boolean erSkjermlenkeSatt = historikkDeler.stream().anyMatch(historikkDel -> historikkDel.getSkjermlenke().isPresent());
        if (!erSkjermlenkeSatt && !historikkDeler.isEmpty()) {
            historikkAdapter.tekstBuilder().medSkjermlenke(aksjonspunktDefinisjon, behandling);
        }
    }

}
