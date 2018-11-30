package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsgiverHistorikkinnslagTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FastsettBeregningsgrunnlagATFLDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.InntektPrAndelDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

@ApplicationScoped
@DtoTilServiceAdapter(dto = FastsettBeregningsgrunnlagATFLDto.class, adapter = AksjonspunktOppdaterer.class)
public class FastsettBeregningsgrunnlagATFLOppdaterer implements AksjonspunktOppdaterer<FastsettBeregningsgrunnlagATFLDto> {

    private AksjonspunktRepository aksjonspunktRepository;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private HistorikkTjenesteAdapter historikkAdapter;
    private ArbeidsgiverHistorikkinnslagTjeneste arbeidsgiverHistorikkinnslagTjeneste;
    FastsettBeregningsgrunnlagATFLOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public FastsettBeregningsgrunnlagATFLOppdaterer(BehandlingRepositoryProvider repositoryProvider,
                                                    HistorikkTjenesteAdapter historikkAdapter,
                                                    ArbeidsgiverHistorikkinnslagTjeneste arbeidsgiverHistorikkinnslagTjeneste) {
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
        this.historikkAdapter = historikkAdapter;
        this.arbeidsgiverHistorikkinnslagTjeneste = arbeidsgiverHistorikkinnslagTjeneste;
    }

    @Override
    public OppdateringResultat oppdater(FastsettBeregningsgrunnlagATFLDto dto, Behandling behandling) {

        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagRepository.hentAggregat(behandling);
        Beregningsgrunnlag nyttBeregningsgrunnlag = beregningsgrunnlag.dypKopi();
        List<BeregningsgrunnlagPeriode> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        List<InntektPrAndelDto> fastsattInntektListe = dto.getInntektPrAndelList();
        List<BeregningsgrunnlagPrStatusOgAndel> frilanserList = null;
        BeregningsgrunnlagPeriode førstePeriode = perioder.get(0);
        List<BeregningsgrunnlagPrStatusOgAndel> arbeidstakerList = førstePeriode.getBeregningsgrunnlagPrStatusOgAndelList()
            .stream()
            .filter(andel -> andel.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSTAKER))
            .collect(Collectors.toList());
        if (fastsattInntektListe != null && !arbeidstakerList.isEmpty()) {
            for (InntektPrAndelDto inntekPrAndel : fastsattInntektListe) {
                BeregningsgrunnlagPrStatusOgAndel korresponderendeAndelIFørstePeriode = arbeidstakerList.stream()
                    .filter(andel -> andel.getAndelsnr().equals(inntekPrAndel.getAndelsnr()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Fant ingen korresponderende andel med andelsnr " + inntekPrAndel.getAndelsnr() + " i første periode for behandling " + behandling.getId()));
                for (BeregningsgrunnlagPeriode periode : perioder) {
                    Optional<BeregningsgrunnlagPrStatusOgAndel> korresponderendeAndelOpt = finnRiktigAndel(korresponderendeAndelIFørstePeriode, periode);
                    korresponderendeAndelOpt.ifPresent(andel-> BeregningsgrunnlagPrStatusOgAndel.builder(andel)
                        .medOverstyrtPrÅr(BigDecimal.valueOf(inntekPrAndel.getInntekt())));
                }
            }
        }
        if (dto.getInntektFrilanser() != null) {
            for (BeregningsgrunnlagPeriode periode : perioder) {
                frilanserList = periode.getBeregningsgrunnlagPrStatusOgAndelList()
                    .stream()
                    .filter(andel -> andel.getAktivitetStatus().equals(AktivitetStatus.FRILANSER))
                    .collect(Collectors.toList());
                frilanserList.forEach(prStatusOgAndel ->
                    BeregningsgrunnlagPrStatusOgAndel.builder(prStatusOgAndel).medOverstyrtPrÅr(BigDecimal.valueOf(dto.getInntektFrilanser())));
            }
        }
        lagHistorikkInnslag(dto, behandling, fastsattInntektListe, arbeidstakerList, frilanserList);
        håndterEventueltOverflødigAksjonspunkt(behandling);
        beregningsgrunnlagRepository.lagre(behandling, nyttBeregningsgrunnlag, BeregningsgrunnlagTilstand.FASTSATT_INN);

        return OppdateringResultat.utenOveropp();
    }

    private Optional<BeregningsgrunnlagPrStatusOgAndel> finnRiktigAndel(BeregningsgrunnlagPrStatusOgAndel andelIFørstePeriode, BeregningsgrunnlagPeriode periode) {
        return periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(andel -> andel.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSTAKER))
            .filter(andel -> andel.equals(andelIFørstePeriode)).findFirst();
    }

    private void lagHistorikkInnslag(FastsettBeregningsgrunnlagATFLDto dto, Behandling behandling, List<InntektPrAndelDto> fastsattInntektListe, List<BeregningsgrunnlagPrStatusOgAndel> arbeidstakerList, List<BeregningsgrunnlagPrStatusOgAndel> frilanserList) {

        oppdaterVedEndretVerdi(fastsattInntektListe, arbeidstakerList, frilanserList, dto.getInntektFrilanser());

        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());

        historikkAdapter.tekstBuilder()
            .medBegrunnelse(dto.getBegrunnelse(), aksjonspunktRepository.sjekkErBegrunnelseForAksjonspunktEndret(behandling, aksjonspunktDefinisjon, dto.getBegrunnelse()))
            .medSkjermlenke(aksjonspunktDefinisjon, behandling);
    }

    private void oppdaterVedEndretVerdi(List<InntektPrAndelDto> overstyrtList, List<BeregningsgrunnlagPrStatusOgAndel> arbeidstakerList, List<BeregningsgrunnlagPrStatusOgAndel> frilanserList, Integer inntektFrilanser) {
        if (arbeidstakerList.stream().noneMatch(bgpsa -> bgpsa.getAktivitetStatus().equals(AktivitetStatus.FRILANSER))) {
            historikkAdapter.tekstBuilder().medResultat(HistorikkResultatType.BEREGNET_AARSINNTEKT);
        }

        if (inntektFrilanser != null && !frilanserList.isEmpty()) {
            historikkAdapter.tekstBuilder().medEndretFelt(HistorikkEndretFeltType.FRILANS_INNTEKT, null, inntektFrilanser);
        }

        if (overstyrtList != null) {
            oppdaterForOverstyrt(overstyrtList, arbeidstakerList);
        }

    }

    private void oppdaterForOverstyrt(List<InntektPrAndelDto> overstyrtList, List<BeregningsgrunnlagPrStatusOgAndel> arbeidstakerList) {
        for (BeregningsgrunnlagPrStatusOgAndel prStatus : arbeidstakerList) {
            Optional<InntektPrAndelDto> overstyrt = overstyrtList.stream().filter(andelInntekt -> andelInntekt.getAndelsnr().equals(prStatus.getAndelsnr())).findFirst();
            if (overstyrt.isPresent()) {
                String visningsNavn = arbeidsgiverHistorikkinnslagTjeneste.lagHistorikkinnslagTekstForBeregningsgrunnlag(prStatus);
                historikkAdapter.tekstBuilder().medEndretFelt(HistorikkEndretFeltType.INNTEKT_FRA_ARBEIDSFORHOLD, visningsNavn, null, overstyrt.get().getInntekt());
            }
        }
    }

    /*
    Ved tilbakehopp hender det at avbrutte aksjonspunkter gjenopprettes feilaktig. Denne funksjonen sørger for å rydde opp
    i dette for det ene scenarioet det er mulig i beregningsmodulen. Også implementert i det motsatte tilfellet.
    Se https://jira.adeo.no/browse/PFP-2042 for mer informasjon.
     */
    private void håndterEventueltOverflødigAksjonspunkt(Behandling behandling) {
        Optional<Aksjonspunkt> overflødigAksjonspunkt = behandling.getAksjonspunkter().stream()
            .filter(ap -> AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_TIDSBEGRENSET_ARBEIDSFORHOLD.equals(ap.getAksjonspunktDefinisjon()))
            .filter(Aksjonspunkt::erOpprettet)
            .findFirst();
        overflødigAksjonspunkt.ifPresent(ap -> aksjonspunktRepository.setTilAvbrutt(ap));
    }
}
