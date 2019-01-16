package no.nav.foreldrepenger.domene.vedtak.impl;

import static no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeTjeneste.finnSkjermlenkeType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.revurdering.RevurderingTjenesteProvider;
import no.nav.foreldrepenger.behandling.revurdering.fp.RevurderingTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagTotrinnsvurdering;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.behandlingslager.lagretvedtak.LagretVedtak;
import no.nav.foreldrepenger.behandlingslager.lagretvedtak.LagretVedtakMedBehandlingType;
import no.nav.foreldrepenger.behandlingslager.lagretvedtak.LagretVedtakType;
import no.nav.foreldrepenger.domene.vedtak.VedtakTjeneste;
import no.nav.foreldrepenger.vedtakslager.LagretVedtakRepository;

@ApplicationScoped
public class VedtakTjenesteImpl implements VedtakTjeneste {

    private HistorikkRepository historikkRepository;
    private LagretVedtakRepository lagretVedtakRepository;
    private RevurderingTjenesteProvider revurderingTjenesteProvider;
    private TotrinnTjeneste totrinnTjeneste;
    private BehandlingRepository behandlingRepository;

    VedtakTjenesteImpl() {
        // CDI
    }

    @Inject
    public VedtakTjenesteImpl(LagretVedtakRepository lagretVedtakRepository,
                              GrunnlagRepositoryProvider grunnlagRepositoryProvider,
                              RevurderingTjenesteProvider revurderingTjenesteProvider, TotrinnTjeneste totrinnTjeneste) {
        this.historikkRepository = grunnlagRepositoryProvider.getHistorikkRepository();
        this.behandlingRepository = grunnlagRepositoryProvider.getBehandlingRepository();
        this.lagretVedtakRepository = lagretVedtakRepository;
        this.revurderingTjenesteProvider = revurderingTjenesteProvider;
        this.totrinnTjeneste = totrinnTjeneste;
    }

    @Override
    public List<LagretVedtakMedBehandlingType> hentLagreteVedtakPåFagsak(Long fagsakId) {
        return lagretVedtakRepository.hentLagreteVedtakPåFagsak(fagsakId);
    }

    @Override
    public LagretVedtak hentLagretVedtak(Long lagretVedtakId) {
        return lagretVedtakRepository.hentLagretVedtak(lagretVedtakId);
    }

    @Override
    public void lagHistorikkinnslagFattVedtak(Behandling behandling) {
        if (behandling.isToTrinnsBehandling()) {
            Collection<Totrinnsvurdering> totrinnsvurderings = totrinnTjeneste.hentTotrinnaksjonspunktvurderinger(behandling);
            if (sendesTilbakeTilSaksbehandler(totrinnsvurderings)) {
                lagHistorikkInnslagVurderPåNytt(behandling, totrinnsvurderings);
                return;
            }
        }
        lagHistorikkInnslagVedtakFattet(behandling);
    }

    @Override
    public LagretVedtakType finnLagretVedtakType(Behandling behandling) {
        // FIXME SP - Trenger noe annet?
        return LagretVedtakType.FODSEL;
    }

    private boolean sendesTilbakeTilSaksbehandler(Collection<Totrinnsvurdering> medTotrinnskontroll) {
        return medTotrinnskontroll.stream()
            .anyMatch(a -> !Boolean.TRUE.equals(a.isGodkjent()));
    }

    private void lagHistorikkInnslagVedtakFattet(Behandling behandling) {
        boolean erUendretUtfall = getRevurderingTjeneste(behandling).erRevurderingMedUendretUtfall(behandling);
        HistorikkinnslagType historikkinnslagType = erUendretUtfall ? HistorikkinnslagType.UENDRET_UTFALL : HistorikkinnslagType.VEDTAK_FATTET;
        HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder()
            .medHendelse(historikkinnslagType)
            .medSkjermlenke(SkjermlenkeType.VEDTAK);
        if (!erUendretUtfall) {
            tekstBuilder.medResultat(utledVedtakResultatType(behandling));
        }
        Historikkinnslag innslag = new Historikkinnslag();
        innslag.setAktør(behandling.isToTrinnsBehandling() ? HistorikkAktør.BESLUTTER : HistorikkAktør.VEDTAKSLØSNINGEN);
        innslag.setType(historikkinnslagType);
        innslag.setBehandling(behandling);
        tekstBuilder.build(innslag);

        historikkRepository.lagre(innslag);
    }

    private RevurderingTjeneste getRevurderingTjeneste(Behandling behandling) {
        return revurderingTjenesteProvider.finnRevurderingTjenesteFor(behandling.getFagsak());
    }

    private void lagHistorikkInnslagVurderPåNytt(Behandling behandling, Collection<Totrinnsvurdering> medTotrinnskontroll) {
        Map<SkjermlenkeType, List<HistorikkinnslagTotrinnsvurdering>> vurdering = new HashMap<>();
        List<HistorikkinnslagTotrinnsvurdering> vurderingUtenLenke = new ArrayList<>();

        for (Totrinnsvurdering ttv : medTotrinnskontroll) {
            HistorikkinnslagTotrinnsvurdering totrinnsVurdering = lagHistorikkinnslagTotrinnsvurdering(ttv);
            LocalDateTime sistEndret = ttv.getEndretTidspunkt() != null ? ttv.getEndretTidspunkt() : ttv.getOpprettetTidspunkt();
            totrinnsVurdering.setAksjonspunktSistEndret(sistEndret);
            SkjermlenkeType skjermlenkeType = finnSkjermlenkeType(ttv.getAksjonspunktDefinisjon(), behandling);
            if (skjermlenkeType != SkjermlenkeType.UDEFINERT) {
                List<HistorikkinnslagTotrinnsvurdering> aksjonspktVurderingListe = vurdering.computeIfAbsent(skjermlenkeType,
                    k -> new ArrayList<>());
                aksjonspktVurderingListe.add(totrinnsVurdering);
            } else {
                vurderingUtenLenke.add(totrinnsVurdering);
            }
        }

        HistorikkInnslagTekstBuilder delBuilder = new HistorikkInnslagTekstBuilder()
            .medHendelse(HistorikkinnslagType.SAK_RETUR)
            .medTotrinnsvurdering(vurdering, vurderingUtenLenke);

        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setBehandling(behandling);
        historikkinnslag.setAktør(HistorikkAktør.BESLUTTER);
        historikkinnslag.setType(HistorikkinnslagType.SAK_RETUR);
        delBuilder.build(historikkinnslag);
        historikkRepository.lagre(historikkinnslag);
    }

    private HistorikkinnslagTotrinnsvurdering lagHistorikkinnslagTotrinnsvurdering(Totrinnsvurdering ttv) {
        HistorikkinnslagTotrinnsvurdering totrinnsVurdering = new HistorikkinnslagTotrinnsvurdering();
        totrinnsVurdering.setAksjonspunktDefinisjon(ttv.getAksjonspunktDefinisjon());
        totrinnsVurdering.setBegrunnelse(ttv.getBegrunnelse());
        totrinnsVurdering.setGodkjent(Boolean.TRUE.equals(ttv.isGodkjent()));
        return totrinnsVurdering;
    }

    @Override
    public VedtakResultatType utledVedtakResultatType(Behandling behandling) {
        Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(behandling.getId());
        BehandlingResultatType resultatType = behandlingsresultat.getBehandlingResultatType();
        return utledVedtakResultatType(behandling, resultatType);
    }

    private VedtakResultatType utledVedtakResultatType(Behandling behandling, BehandlingResultatType resultatType) {
        if (BehandlingResultatType.INGEN_ENDRING.equals(resultatType)) {
            Optional<Behandling> originalBehandlingOpt = behandling.getOriginalBehandling();
            if (originalBehandlingOpt.isPresent() && behandlingRepository.hentResultatHvisEksisterer(originalBehandlingOpt.get().getId()).isPresent()) {
                return utledVedtakResultatType(originalBehandlingOpt.get());
            }
        }
        if (BehandlingResultatType.getInnvilgetKoder().contains(resultatType)) {
            return VedtakResultatType.INNVILGET;
        }
        if (BehandlingResultatType.OPPHØR.equals(resultatType)) {
            return VedtakResultatType.OPPHØR;
        }
        return VedtakResultatType.AVSLAG;
    }

}
