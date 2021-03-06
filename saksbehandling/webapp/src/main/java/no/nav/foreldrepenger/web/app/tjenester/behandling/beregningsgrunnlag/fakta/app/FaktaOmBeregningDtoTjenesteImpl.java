package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.FaktaOmBeregningTilfelle;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.FaktaOmBeregningTilfelleTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.KontrollerFaktaBeregningTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.ATogFLISammeOrganisasjonDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.BeregningsgrunnlagArbeidsforholdDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.EndringBeregningsgrunnlagDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.FaktaOmBeregningAndelDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.FaktaOmBeregningDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.KortvarigeArbeidsforholdDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.TilstøtendeYtelseDto;

@ApplicationScoped
public class FaktaOmBeregningDtoTjenesteImpl implements FaktaOmBeregningDtoTjeneste {

    private KontrollerFaktaBeregningTjeneste kontrollerFaktaBeregningTjeneste;
    private EndringBeregningsgrunnlagDtoTjeneste endringBeregningsgrunnlagDtoTjeneste;
    private TilstøtendeYtelseDtoTjeneste tilstøtendeYtelseDtoTjeneste;
    private FaktaOmBeregningAndelDtoTjeneste faktaOmBeregningAndelDtoTjeneste;
    private FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste;
    private BeregningsgrunnlagDtoUtil dtoUtil;

    FaktaOmBeregningDtoTjenesteImpl() {
        // Hibernate
    }

    @Inject
    public FaktaOmBeregningDtoTjenesteImpl(KontrollerFaktaBeregningTjeneste kontrollerFaktaBeregningTjeneste,
                                           FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste,
                                           EndringBeregningsgrunnlagDtoTjeneste endringBeregningsgrunnlagDtoTjeneste,
                                           TilstøtendeYtelseDtoTjeneste tilstøtendeYtelseDtoTjeneste,
                                           FaktaOmBeregningAndelDtoTjeneste faktaOmBeregningAndelDtoTjeneste,
                                           BeregningsgrunnlagDtoUtil dtoUtil) {
        this.kontrollerFaktaBeregningTjeneste = kontrollerFaktaBeregningTjeneste;
        this.faktaOmBeregningTilfelleTjeneste = faktaOmBeregningTilfelleTjeneste;
        this.endringBeregningsgrunnlagDtoTjeneste = endringBeregningsgrunnlagDtoTjeneste;
        this.tilstøtendeYtelseDtoTjeneste = tilstøtendeYtelseDtoTjeneste;
        this.faktaOmBeregningAndelDtoTjeneste = faktaOmBeregningAndelDtoTjeneste;
        this.dtoUtil = dtoUtil;
    }

    @Override
    public Optional<FaktaOmBeregningDto> lagFaktaOmBeregningDto(Behandling behandling, Beregningsgrunnlag beregningsgrunnlag) {
        if (!harAksjonspunktForVurderFaktaATFL(behandling)){
            return Optional.empty();
        }
        List<FaktaOmBeregningTilfelle> tilfeller = beregningsgrunnlag.getFaktaOmBeregningTilfeller();
        if (tilfeller.isEmpty()) {
            tilfeller = faktaOmBeregningTilfelleTjeneste.finnTilfellerForFellesAksjonspunkt(behandling);
            if (tilfeller.isEmpty()) {
                return Optional.empty();
            }
        }
        FaktaOmBeregningDto faktaOmBeregningDto = new FaktaOmBeregningDto();
        faktaOmBeregningDto.setFaktaOmBeregningTilfeller(tilfeller);
        utledDtoerForTilfeller(faktaOmBeregningDto, tilfeller, behandling, beregningsgrunnlag);
        return Optional.of(faktaOmBeregningDto);
    }

    private boolean harAksjonspunktForVurderFaktaATFL(Behandling behandling) {
        return behandling.getAksjonspunktMedDefinisjonOptional(AksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN).isPresent();
    }

    private void utledDtoerForTilfeller(FaktaOmBeregningDto faktaOmBeregningDto, List<FaktaOmBeregningTilfelle> tilfeller, Behandling behandling, Beregningsgrunnlag beregningsgrunnlag) {
        if (tilfeller.contains(FaktaOmBeregningTilfelle.VURDER_TIDSBEGRENSET_ARBEIDSFORHOLD)) {
            List<KortvarigeArbeidsforholdDto> arbeidsforholdDto = lagKortvarigeArbeidsforholdDto(behandling)
                .orElseThrow(() -> new IllegalStateException("Fant ingen kortvarige arbeidsforhold"));
            faktaOmBeregningDto.setKortvarigeArbeidsforhold(arbeidsforholdDto);
        }
        if (tilfeller.contains(FaktaOmBeregningTilfelle.VURDER_NYOPPSTARTET_FL) && faktaOmBeregningDto.getFrilansAndel() == null) {
            faktaOmBeregningAndelDtoTjeneste.lagFrilansAndelDto(beregningsgrunnlag).ifPresent(faktaOmBeregningDto::setFrilansAndel);
        }
        if (tilfeller.contains(FaktaOmBeregningTilfelle.TILSTØTENDE_YTELSE)) {
            Optional<TilstøtendeYtelseDto> tilstøtendeYtelseOpt = tilstøtendeYtelseDtoTjeneste.lagTilstøtendeYtelseDto(behandling, beregningsgrunnlag);
            faktaOmBeregningDto.setTilstøtendeYtelse(tilstøtendeYtelseOpt.orElse(null));
        }
        if (tilfeller.contains(FaktaOmBeregningTilfelle.VURDER_AT_OG_FL_I_SAMME_ORGANISASJON)) {
            List<ATogFLISammeOrganisasjonDto> aTogFLISammeOrganisasjonDto = faktaOmBeregningAndelDtoTjeneste.lagATogFLISAmmeOrganisasjonListe(behandling);
            if (faktaOmBeregningDto.getFrilansAndel() == null) {
                faktaOmBeregningAndelDtoTjeneste.lagFrilansAndelDto(beregningsgrunnlag).ifPresent(faktaOmBeregningDto::setFrilansAndel);
            }
            faktaOmBeregningDto.setATogFLISammeOrganisasjonListe(aTogFLISammeOrganisasjonDto);
        }
        if (tilfeller.contains(FaktaOmBeregningTilfelle.VURDER_LØNNSENDRING)) {
            Optional<List<FaktaOmBeregningAndelDto>> arbeidsforholdUtenInntektsmeldingDtoList = faktaOmBeregningAndelDtoTjeneste.lagArbeidsforholdUtenInntektsmeldingDtoList(behandling);
            arbeidsforholdUtenInntektsmeldingDtoList.ifPresent(faktaOmBeregningDto::setArbeidsforholdMedLønnsendringUtenIM);
        }
        if (tilfeller.contains(FaktaOmBeregningTilfelle.FASTSETT_ENDRET_BEREGNINGSGRUNNLAG)) {
            EndringBeregningsgrunnlagDto endringAvBeregningsgrunnlagDto = endringBeregningsgrunnlagDtoTjeneste
                .lagEndringAvBeregningsgrunnlagDto(behandling, beregningsgrunnlag)
                .orElseThrow(() -> new IllegalStateException("Fant ingen endret beregningsgrunnlag"));
            faktaOmBeregningDto.setEndringBeregningsgrunnlag(endringAvBeregningsgrunnlagDto);
        }
    }

    private Optional<List<KortvarigeArbeidsforholdDto>> lagKortvarigeArbeidsforholdDto(Behandling behandling) {
        Map<BeregningsgrunnlagPrStatusOgAndel, Yrkesaktivitet> kortvarige = kontrollerFaktaBeregningTjeneste.hentAndelerForKortvarigeArbeidsforhold(behandling);
        if (kortvarige.isEmpty()) {
            return Optional.empty();
        } else {
            List<KortvarigeArbeidsforholdDto> arbeidsforholdList = kortvarige.entrySet().stream()
                .map(entry -> mapFraYrkesaktivitet(entry.getKey()))
                .collect(Collectors.toList());
            return Optional.of(arbeidsforholdList);
        }
    }

    private KortvarigeArbeidsforholdDto mapFraYrkesaktivitet(BeregningsgrunnlagPrStatusOgAndel prStatusOgAndel) {
        KortvarigeArbeidsforholdDto beregningArbeidsforhold = new KortvarigeArbeidsforholdDto();
        beregningArbeidsforhold.setErTidsbegrensetArbeidsforhold(prStatusOgAndel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getErTidsbegrensetArbeidsforhold).orElse(null));
        beregningArbeidsforhold.setAndelsnr(prStatusOgAndel.getAndelsnr());
        Optional<BeregningsgrunnlagArbeidsforholdDto> arbDto = dtoUtil.lagArbeidsforholdDto(prStatusOgAndel);
        arbDto.ifPresent(beregningArbeidsforhold::setArbeidsforhold);
        return beregningArbeidsforhold;
    }
}
