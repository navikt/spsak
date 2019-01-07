package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VurderLønnsendringDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

@ApplicationScoped
public class VurderLønnsendringOppdaterer {

    private HistorikkTjenesteAdapter historikkAdapter;
    private AksjonspunktRepository aksjonspunktRepository;

    VurderLønnsendringOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public VurderLønnsendringOppdaterer(GrunnlagRepositoryProvider repositoryProvider,
                                        HistorikkTjenesteAdapter historikkAdapter) {
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.historikkAdapter = historikkAdapter;
    }

    public void oppdater(VurderLønnsendringDto dto, Behandling behandling, Beregningsgrunnlag nyttBeregningsgrunnlag) {
        List<BeregningsgrunnlagPrStatusOgAndel> arbeidstakerAndeler = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .map(BeregningsgrunnlagPeriode::getBeregningsgrunnlagPrStatusOgAndelList).flatMap(Collection::stream)
            .filter(bpsa -> bpsa.getAktivitetStatus().erArbeidstaker())
            .collect(Collectors.toList());

        Boolean opprinneligVerdiErLønnsendring = hentOpprinneligVerdiErLønnsendring(arbeidstakerAndeler);
        if (dto.erLønnsendringIBeregningsperioden()) {
            arbeidstakerAndeler.forEach(andel ->{
                BGAndelArbeidsforhold.Builder bgAndelArbeidsforhold = BGAndelArbeidsforhold
                    .builder(andel.getBgAndelArbeidsforhold().orElse(null))
                    .medLønnsendringIBeregningsperioden(true);
                BeregningsgrunnlagPrStatusOgAndel.builder(andel)
                    .medBGAndelArbeidsforhold(bgAndelArbeidsforhold)
                    .medFastsattAvSaksbehandler(true);
                });
        } else {
            arbeidstakerAndeler.forEach(bgAndel ->{
                BGAndelArbeidsforhold.Builder bgAndelArbeidsforhold = BGAndelArbeidsforhold
                    .builder(bgAndel.getBgAndelArbeidsforhold().orElse(null))
                    .medLønnsendringIBeregningsperioden(false);
                BeregningsgrunnlagPrStatusOgAndel.builder(bgAndel)
                    .medBGAndelArbeidsforhold(bgAndelArbeidsforhold)
                    .medFastsattAvSaksbehandler(false)
                    .medBeregnetPrÅr(null);
                });
            }
        lagHistorikkinnslag(behandling, dto, opprinneligVerdiErLønnsendring);
    }

    private Boolean hentOpprinneligVerdiErLønnsendring(List<BeregningsgrunnlagPrStatusOgAndel> arbeidstakerAndeler) {
        return arbeidstakerAndeler.stream()
            .map(BeregningsgrunnlagPrStatusOgAndel::getBgAndelArbeidsforhold)
            .map(Optional::get)
            .filter(a -> a.erLønnsendringIBeregningsperioden() != null)
            .anyMatch(BGAndelArbeidsforhold::erLønnsendringIBeregningsperioden);

    }

    private void lagHistorikkinnslag(Behandling behandling, VurderLønnsendringDto dto, Boolean opprinneligVerdiErLønnsendring) {
        HistorikkInnslagTekstBuilder tekstBuilder = historikkAdapter.tekstBuilder();
        if (!dto.erLønnsendringIBeregningsperioden().equals(opprinneligVerdiErLønnsendring)) {
            tekstBuilder
                .medEndretFelt(HistorikkEndretFeltType.LØNNSENDRING_I_PERIODEN, opprinneligVerdiErLønnsendring, dto.erLønnsendringIBeregningsperioden());
        }
        List<HistorikkinnslagDel> historikkDeler = tekstBuilder.getHistorikkinnslagDeler();
        Boolean erSkjermlenkeSatt = historikkDeler.stream().anyMatch(historikkDel -> historikkDel.getSkjermlenke().isPresent());
        if (!erSkjermlenkeSatt) {
            tekstBuilder
                .medSkjermlenke(aksjonspunktRepository.finnAksjonspunktDefinisjon(AksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN.getKode()), behandling);
        }
        tekstBuilder.ferdigstillHistorikkinnslagDel();
    }
}
