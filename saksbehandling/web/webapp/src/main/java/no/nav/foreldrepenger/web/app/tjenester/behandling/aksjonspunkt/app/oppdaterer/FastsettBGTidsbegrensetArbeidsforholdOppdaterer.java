package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.behandling.aksjonspunkt.DtoTilServiceAdapter;
import no.nav.foreldrepenger.behandling.aksjonspunkt.OppdateringResultat;
import no.nav.foreldrepenger.behandling.historikk.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsgiverHistorikkinnslagTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FastsatteAndelerTidsbegrensetDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FastsattePerioderTidsbegrensetDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FastsettBGTidsbegrensetArbeidsforholdDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = FastsettBGTidsbegrensetArbeidsforholdDto.class, adapter = AksjonspunktOppdaterer.class)
public class FastsettBGTidsbegrensetArbeidsforholdOppdaterer implements AksjonspunktOppdaterer<FastsettBGTidsbegrensetArbeidsforholdDto> {

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private HistorikkTjenesteAdapter historikkAdapter;
    private AksjonspunktRepository aksjonspunktRepository;
    private ArbeidsgiverHistorikkinnslagTjeneste arbeidsgiverHistorikkinnslagTjeneste;

    FastsettBGTidsbegrensetArbeidsforholdOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public FastsettBGTidsbegrensetArbeidsforholdOppdaterer(ResultatRepositoryProvider resultatRepositoryProvider,
                                                           HistorikkTjenesteAdapter historikkAdapter,
                                                           AksjonspunktRepository aksjonspunktRepository,
                                                           ArbeidsgiverHistorikkinnslagTjeneste arbeidsgiverHistorikkinnslagTjeneste) {
        this.beregningsgrunnlagRepository = resultatRepositoryProvider.getBeregningsgrunnlagRepository();
        this.historikkAdapter = historikkAdapter;
        this.aksjonspunktRepository = aksjonspunktRepository;
        this.arbeidsgiverHistorikkinnslagTjeneste = arbeidsgiverHistorikkinnslagTjeneste;
    }

    @Override
    public OppdateringResultat oppdater(FastsettBGTidsbegrensetArbeidsforholdDto dto, Behandling behandling) {
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagRepository.hentAggregat(behandling);
        Beregningsgrunnlag nyttBeregningsgrunnlag = beregningsgrunnlag.dypKopi();
        List<BeregningsgrunnlagPeriode> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        List<FastsattePerioderTidsbegrensetDto> fastsattePerioder = dto.getFastsatteTidsbegrensedePerioder();
        BigDecimal forrigeFrilansInntekt = null;
        Map<String, List<Integer>> arbeidsforholdInntekterMap = new HashMap<>();
        if (dto.getFrilansInntekt() != null) {
            for (BeregningsgrunnlagPeriode periode : perioder) {
                BeregningsgrunnlagPrStatusOgAndel frilansAndel = periode.getBeregningsgrunnlagPrStatusOgAndelList()
                    .stream()
                    .filter(a -> a.getAktivitetStatus().equals(AktivitetStatus.FRILANSER))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Mangler frilansandel for behandling " + behandling.getId()));
                forrigeFrilansInntekt = frilansAndel.getOverstyrtPrÅr();
                BeregningsgrunnlagPrStatusOgAndel.builder(frilansAndel).medOverstyrtPrÅr(BigDecimal.valueOf(dto.getFrilansInntekt()));
            }
        }
        for (FastsattePerioderTidsbegrensetDto periode: fastsattePerioder) {
            List<BeregningsgrunnlagPeriode> bgPerioderSomSkalFastsettesAvDennePerioden = perioder
                .stream()
                .filter(p -> !p.getBeregningsgrunnlagPeriodeFom().isBefore(periode.getPeriodeFom()))
                .collect(Collectors.toList());
            List<FastsatteAndelerTidsbegrensetDto> fastatteAndeler = periode.getFastsatteTidsbegrensedeAndeler();
            fastatteAndeler.forEach(andel ->
                fastsettAndelerIPeriode(arbeidsforholdInntekterMap, periode, bgPerioderSomSkalFastsettesAvDennePerioden, andel));
        }
        lagHistorikkInnslag(dto, behandling, arbeidsforholdInntekterMap, forrigeFrilansInntekt);
        håndterEventueltOverflødigAksjonspunkt(behandling);
        beregningsgrunnlagRepository.lagre(behandling, nyttBeregningsgrunnlag, BeregningsgrunnlagTilstand.FASTSATT_INN);
        return OppdateringResultat.utenOveropp();
    }

    private void fastsettAndelerIPeriode(Map<String, List<Integer>> arbeidsforholdInntekterMap, FastsattePerioderTidsbegrensetDto periode, List<BeregningsgrunnlagPeriode> bgPerioderSomSkalFastsettesAvDennePerioden, FastsatteAndelerTidsbegrensetDto andel) {
        bgPerioderSomSkalFastsettesAvDennePerioden.forEach(p -> {
            Optional<BeregningsgrunnlagPrStatusOgAndel> korrektAndel = p.getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> a.getAndelsnr().equals(andel.getAndelsnr())).findFirst();
            if (korrektAndel.isPresent()) {
                BeregningsgrunnlagPrStatusOgAndel
                    .builder(korrektAndel.get())
                    .medOverstyrtPrÅr(BigDecimal.valueOf(andel.getBruttoFastsattInntekt()));
                if (skalLageHistorikkinnslag(korrektAndel.get(), periode)) {
                    mapArbeidsforholdInntekter(andel, arbeidsforholdInntekterMap, korrektAndel.get());
                }
            }
        });
    }

    private boolean skalLageHistorikkinnslag(BeregningsgrunnlagPrStatusOgAndel korrektAndel, FastsattePerioderTidsbegrensetDto fastsattArbeidsforhold) {
        // Lager kun historikkinnslag dersom perioden ble eksplisitt fastsatt av saksbehandler.
        // Perioder som "arver" verdier fra foregående periode får ikke historikkinnslag
        BeregningsgrunnlagPeriode bgPeriode = korrektAndel.getBeregningsgrunnlagPeriode();
        return bgPeriode.getBeregningsgrunnlagPeriodeFom().equals(fastsattArbeidsforhold.getPeriodeFom());
    }

    private void mapArbeidsforholdInntekter(FastsatteAndelerTidsbegrensetDto arbeidsforhold, Map<String, List<Integer>> tilHistorikkInnslag, BeregningsgrunnlagPrStatusOgAndel korrektAndel) {
        if (!korrektAndel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsgiver).isPresent()) {
            throw new IllegalStateException("Utviklerfeil: Skal ikke kunne komme hit uten en arbeidsgiver");
        }
        String arbeidsforholdInfo = arbeidsgiverHistorikkinnslagTjeneste.lagHistorikkinnslagTekstForBeregningsgrunnlag(korrektAndel);
        if(tilHistorikkInnslag.containsKey(arbeidsforholdInfo)) {
            List<Integer> inntekter = tilHistorikkInnslag.get(arbeidsforholdInfo);
            inntekter.add(arbeidsforhold.getBruttoFastsattInntekt());
            tilHistorikkInnslag.put(arbeidsforholdInfo, inntekter);
        }else {
            List<Integer> inntekter = new ArrayList<>();
            inntekter.add(arbeidsforhold.getBruttoFastsattInntekt());
            tilHistorikkInnslag.put(arbeidsforholdInfo, inntekter);
        }
    }

    private void lagHistorikkInnslag(FastsettBGTidsbegrensetArbeidsforholdDto dto, Behandling behandling, Map<String, List<Integer>> tilHistorikkInnslag, BigDecimal forrigeFrilansInntekt) {
        oppdaterVedEndretVerdi(HistorikkEndretFeltType.INNTEKT_FRA_ARBEIDSFORHOLD, tilHistorikkInnslag);
        oppdaterFrilansInntektVedEndretVerdi(HistorikkEndretFeltType.FRILANS_INNTEKT, forrigeFrilansInntekt, dto);
        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());

        historikkAdapter.tekstBuilder()
            .medBegrunnelse(dto.getBegrunnelse(),
                aksjonspunktRepository.sjekkErBegrunnelseForAksjonspunktEndret(behandling, aksjonspunktDefinisjon,
                        dto.getBegrunnelse()))
                .medSkjermlenke(aksjonspunktDefinisjon, behandling);
    }

    private void oppdaterVedEndretVerdi(HistorikkEndretFeltType historikkEndretFeltType, Map<String, List<Integer>> tilHistorikkInnslag) {
        for (Map.Entry<String, List<Integer>> entry : tilHistorikkInnslag.entrySet()) {
            String arbeidsforholdInfo = entry.getKey();
            List<Integer> inntekter = entry.getValue();
            historikkAdapter.tekstBuilder().medEndretFelt(historikkEndretFeltType, arbeidsforholdInfo, null, formaterInntekter(inntekter));
        }
    }

    private void oppdaterFrilansInntektVedEndretVerdi(HistorikkEndretFeltType historikkEndretFeltType, BigDecimal forrigeFrilansInntekt, FastsettBGTidsbegrensetArbeidsforholdDto dto) {
        if (forrigeFrilansInntekt != null && dto.getFrilansInntekt() != null) {
            historikkAdapter.tekstBuilder().medEndretFelt(historikkEndretFeltType, forrigeFrilansInntekt, dto.getFrilansInntekt());
        } else if (dto.getFrilansInntekt() != null){
            historikkAdapter.tekstBuilder().medEndretFelt(historikkEndretFeltType, null, dto.getFrilansInntekt());
        }
    }

        /*
    Ved tilbakehopp hender det at avbrutte aksjonspunkter gjenopprettes feilaktig. Denne funksjonen sørger for å rydde opp
    i dette for det ene scenarioet det er mulig i beregningsmodulen. Også implementert i det motsatte tilfellet.
    Se https://jira.adeo.no/browse/PFP-2042 for mer informasjon.
     */
    private void håndterEventueltOverflødigAksjonspunkt(Behandling behandling) {
        Optional<Aksjonspunkt> overflødigAksjonspunkt = behandling.getAksjonspunkter().stream()
            .filter(ap -> AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS.equals(ap.getAksjonspunktDefinisjon()))
            .filter(Aksjonspunkt::erOpprettet)
            .findFirst();
        overflødigAksjonspunkt.ifPresent(ap -> aksjonspunktRepository.setTilAvbrutt(ap));
    }

    private String formaterInntekter(List<Integer> inntekter) {
        if(inntekter.size() > 1) {
            String inntekterString = inntekter.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            return inntekterString.substring(0, inntekterString.lastIndexOf(',')) + " og " + inntekterString.substring(inntekterString.lastIndexOf(',') + 1);
        }
        return inntekter.get(0).toString();
    }

}
