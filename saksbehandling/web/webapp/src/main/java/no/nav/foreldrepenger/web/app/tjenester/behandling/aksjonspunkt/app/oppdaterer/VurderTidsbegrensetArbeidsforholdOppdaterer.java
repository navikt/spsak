package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltVerdiType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsgiverHistorikkinnslagTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VurderTidsbegrensetArbeidsforholdDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VurderteArbeidsforholdDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

@ApplicationScoped
public class VurderTidsbegrensetArbeidsforholdOppdaterer {

    private HistorikkTjenesteAdapter historikkAdapter;
    private AksjonspunktRepository aksjonspunktRepository;
    private ArbeidsgiverHistorikkinnslagTjeneste arbeidsgiverHistorikkinnslagTjeneste;

    VurderTidsbegrensetArbeidsforholdOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public VurderTidsbegrensetArbeidsforholdOppdaterer(GrunnlagRepositoryProvider repositoryProvider,
                                                       HistorikkTjenesteAdapter historikkAdapter,
                                                       ArbeidsgiverHistorikkinnslagTjeneste arbeidsgiverHistorikkinnslagTjeneste) {
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.historikkAdapter = historikkAdapter;
        this.arbeidsgiverHistorikkinnslagTjeneste = arbeidsgiverHistorikkinnslagTjeneste;
    }

    public void oppdater(VurderTidsbegrensetArbeidsforholdDto dto, Behandling behandling, Beregningsgrunnlag nyttBeregningsgrunnlag) {
        BeregningsgrunnlagPeriode periode = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<VurderteArbeidsforholdDto> fastsatteArbeidsforhold = dto.getFastsatteArbeidsforhold();
        for (VurderteArbeidsforholdDto arbeidsforhold : fastsatteArbeidsforhold) {
            BeregningsgrunnlagPrStatusOgAndel korrektAndel = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(a -> a.getAndelsnr().equals(arbeidsforhold.getAndelsnr()))
                .findFirst()
                .orElseThrow(() -> VurderTidsbegrensetArbeidsforholdOppdatererFeil.FACTORY.finnerIkkeAndelFeil(behandling.getId()).toException());
            BGAndelArbeidsforhold.Builder bgAndelArbeidsforhold = BGAndelArbeidsforhold
                .builder(korrektAndel.getBgAndelArbeidsforhold().orElse(null))
                .medTidsbegrensetArbeidsforhold(arbeidsforhold.isTidsbegrensetArbeidsforhold());
            BeregningsgrunnlagPrStatusOgAndel
                .builder(korrektAndel)
                .medBGAndelArbeidsforhold(bgAndelArbeidsforhold);
            lagHistorikkInnslag(arbeidsforhold, korrektAndel, behandling);
        }
    }

    private void lagHistorikkInnslag(VurderteArbeidsforholdDto arbeidsforhold, BeregningsgrunnlagPrStatusOgAndel andel, Behandling behandling) {
        oppdaterVedEndretVerdi(HistorikkEndretFeltType.ENDRING_TIDSBEGRENSET_ARBEIDSFORHOLD, arbeidsforhold, andel);
        List<HistorikkinnslagDel> historikkDeler = historikkAdapter.tekstBuilder().getHistorikkinnslagDeler();
        Boolean erSkjermlenkeSatt = historikkDeler.stream().anyMatch(historikkDel -> historikkDel.getSkjermlenke().isPresent());
        if (!erSkjermlenkeSatt) {
            historikkAdapter.tekstBuilder().medSkjermlenke(aksjonspunktRepository.finnAksjonspunktDefinisjon(AksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN.getKode()), behandling);
        }
        historikkAdapter.tekstBuilder().ferdigstillHistorikkinnslagDel();
    }

    private HistorikkEndretFeltVerdiType konvertBooleanTilFaktaEndretVerdiType(Boolean endringTidsbegrensetArbeidsforhold) {
        if (endringTidsbegrensetArbeidsforhold == null) {
            return null;
        }
        return endringTidsbegrensetArbeidsforhold ? HistorikkEndretFeltVerdiType.TIDSBEGRENSET_ARBEIDSFORHOLD : HistorikkEndretFeltVerdiType.IKKE_TIDSBEGRENSET_ARBEIDSFORHOLD;
    }

    private void oppdaterVedEndretVerdi(HistorikkEndretFeltType historikkEndretFeltType, VurderteArbeidsforholdDto arbeidsforhold, BeregningsgrunnlagPrStatusOgAndel andel) {
        String arbeidsforholdInfo = arbeidsgiverHistorikkinnslagTjeneste.lagHistorikkinnslagTekstForBeregningsgrunnlag(andel);
        HistorikkEndretFeltVerdiType opprinneligVerdi = konvertBooleanTilFaktaEndretVerdiType(arbeidsforhold.isOpprinneligVerdi());
        HistorikkEndretFeltVerdiType nyVerdi = konvertBooleanTilFaktaEndretVerdiType(arbeidsforhold.isTidsbegrensetArbeidsforhold());
        if (opprinneligVerdi != nyVerdi) {
            historikkAdapter.tekstBuilder().medEndretFelt(historikkEndretFeltType, arbeidsforholdInfo, opprinneligVerdi, nyVerdi);
        }
    }

    private interface VurderTidsbegrensetArbeidsforholdOppdatererFeil extends DeklarerteFeil {

        VurderTidsbegrensetArbeidsforholdOppdatererFeil FACTORY = FeilFactory.create(VurderTidsbegrensetArbeidsforholdOppdatererFeil.class);

        @TekniskFeil(feilkode = "FP-238175", feilmelding = "Finner ikke andelen for eksisterende grunnlag. Behandling %s", logLevel = LogLevel.WARN)
        Feil finnerIkkeAndelFeil(long behandlingId);
    }


}
