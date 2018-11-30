package no.nav.foreldrepenger.domene.mottak.hendelser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRevurderingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.hendelser.ForretningshendelseType;
import no.nav.foreldrepenger.domene.mottak.Behandlingsoppretter;
import no.nav.foreldrepenger.domene.mottak.ForretningshendelseHåndterer;
import no.nav.foreldrepenger.domene.mottak.ForretningshendelsestypeRef;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.HistorikkinnslagTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl.Kompletthetskontroller;
import no.nav.foreldrepenger.domene.mottak.ytelse.YtelseForretningshendelse;
import no.nav.foreldrepenger.domene.registerinnhenting.Endringskontroller;


@ForretningshendelsestypeRef(ForretningshendelsestypeRef.YTELSE_HENDELSE)
@ApplicationScoped
public class YtelseForretningshendelseHåndterer implements ForretningshendelseHåndterer<YtelseForretningshendelse> {

    private static Map<ForretningshendelseType, BehandlingÅrsakType> HENDELSE_TIL_BEHANDLINGSÅRSAK = new HashMap<>();
    static {
        HENDELSE_TIL_BEHANDLINGSÅRSAK.put(ForretningshendelseType.YTELSE_INNVILGET, BehandlingÅrsakType.RE_TILSTØTENDE_YTELSE_INNVILGET);
        HENDELSE_TIL_BEHANDLINGSÅRSAK.put(ForretningshendelseType.YTELSE_ENDRET, BehandlingÅrsakType.RE_ENDRING_BEREGNINGSGRUNNLAG);
        HENDELSE_TIL_BEHANDLINGSÅRSAK.put(ForretningshendelseType.YTELSE_OPPHØRT, BehandlingÅrsakType.RE_TILSTØTENDE_YTELSE_OPPHØRT);
        HENDELSE_TIL_BEHANDLINGSÅRSAK.put(ForretningshendelseType.YTELSE_ANNULERT, BehandlingÅrsakType.RE_TILSTØTENDE_YTELSE_OPPHØRT);
    }

    private FagsakRepository fagsakRepository;
    private Behandlingsoppretter behandlingsoppretter;
    private BehandlingRepository behandlingRepository;
    private HistorikkRepository historikkRepository;
    private Kompletthetskontroller kompletthetskontroller;
    private BehandlingRevurderingRepository behandlingRevurderingRepository;
    private Endringskontroller endringskontroller;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;

    public YtelseForretningshendelseHåndterer() {
        //Criminal Diamonds Inc.
    }

    @Inject
    public YtelseForretningshendelseHåndterer(BehandlingRepositoryProvider provider,
                                              Behandlingsoppretter behandlingsoppretter,
                                              Kompletthetskontroller kompletthetskontroller,
                                              Endringskontroller endringskontroller,
                                              BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                              HistorikkinnslagTjeneste historikkinnslagTjeneste) {
        this.fagsakRepository = provider.getFagsakRepository();
        this.behandlingRevurderingRepository = provider.getBehandlingRevurderingRepository();
        this.behandlingRepository = provider.getBehandlingRepository();
        this.historikkRepository = provider.getHistorikkRepository();
        this.behandlingsoppretter = behandlingsoppretter;
        this.kompletthetskontroller = kompletthetskontroller;
        this.endringskontroller = endringskontroller;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
    }


    @Override
    public List<Fagsak> finnRelaterteFagsaker(YtelseForretningshendelse forretningshendelse) {
        return fagsakRepository.hentForBruker(forretningshendelse.getAktørId()).stream()
            .filter(fagsak -> !FagsakStatus.AVSLUTTET.equals(fagsak.getStatus()))
            .filter(fagsak -> fagsak.getYtelseType().equals(FagsakYtelseType.FORELDREPENGER))
            .collect(Collectors.toList());
    }

    @Override
    public void håndterÅpenBehandling(Behandling åpenBehandling, ForretningshendelseType forretningshendelseType) {
        BehandlingÅrsakType behandlingÅrsakType = finnBehandlingÅrsakType(forretningshendelseType);
        settBehandlingÅrsakPåBehandling(åpenBehandling, behandlingÅrsakType);
        opprettHistorikkinnslag(åpenBehandling, behandlingÅrsakType);
        kompletthetskontroller.vurderNyForretningshendelse(åpenBehandling);
    }

    @Override
    public void håndterAvsluttetBehandling(Behandling avsluttetBehandling, ForretningshendelseType forretningshendelseType) {
        BehandlingÅrsakType behandlingÅrsakType = finnBehandlingÅrsakType(forretningshendelseType);
        if (BehandlingStatus.IVERKSETTER_VEDTAK.equals(avsluttetBehandling.getStatus()) && ForretningshendelseType.YTELSE_OPPHØRT.equals(forretningshendelseType)) {
            settBehandlingÅrsakPåBehandling(avsluttetBehandling, behandlingÅrsakType);
            opprettHistorikkinnslag(avsluttetBehandling, behandlingÅrsakType);
            // Re-kjør steg IVERKSETT_VEDTAK, da steget har logikk som oppdager at ytelsen har opphørt
            endringskontroller.spolTilSteg(avsluttetBehandling, BehandlingStegType.IVERKSETT_VEDTAK);
            return;
        }
        behandlingsoppretter.opprettRevurdering(avsluttetBehandling.getFagsak(), behandlingÅrsakType);
    }

    @Override
    public void håndterKøetBehandling(Fagsak fagsak, ForretningshendelseType forretningshendelseType) {
        Optional<Behandling> køetBehandlingOpt = behandlingRevurderingRepository.finnKøetYtelsesbehandling(fagsak.getId());
        if (køetBehandlingOpt.isPresent()) {
            // Oppdateringer fanges opp etter at behandling tas av kø, ettersom den vil passere steg innhentregisteropplysninger
            return;
        }
        BehandlingÅrsakType eksternÅrsak = finnBehandlingÅrsakType(forretningshendelseType);
        Behandling køetBehandling = behandlingsoppretter.opprettKøetBehandling(fagsak, eksternÅrsak);
        historikkinnslagTjeneste.opprettHistorikkinnslagForVenteFristRelaterteInnslag(køetBehandling, HistorikkinnslagType.BEH_KØET, null, Venteårsak.VENT_ÅPEN_BEHANDLING);
        kompletthetskontroller.oppdaterKompletthetForKøetBehandling(køetBehandling);
    }

    private BehandlingÅrsakType finnBehandlingÅrsakType(ForretningshendelseType forretningshendelseType) {
        return HENDELSE_TIL_BEHANDLINGSÅRSAK.getOrDefault(forretningshendelseType, BehandlingÅrsakType.UDEFINERT);
    }

    private void settBehandlingÅrsakPåBehandling(Behandling behandling, BehandlingÅrsakType behandlingÅrsakType) {
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling.getId());
        BehandlingÅrsak.builder(behandlingÅrsakType).buildFor(behandling);
        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
    }

    private void opprettHistorikkinnslag(Behandling behandling, BehandlingÅrsakType behandlingÅrsakType) {
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setType(HistorikkinnslagType.NYE_REGOPPLYSNINGER);
        historikkinnslag.setBehandlingId(behandling.getId());
        historikkinnslag.setFagsakId(behandling.getFagsakId());
        historikkinnslag.setAktør(HistorikkAktør.VEDTAKSLØSNINGEN);

        HistorikkInnslagTekstBuilder builder = new HistorikkInnslagTekstBuilder();
        builder.medHendelse(HistorikkinnslagType.NYE_REGOPPLYSNINGER);
        builder.medBegrunnelse(behandlingÅrsakType);
        builder.build(historikkinnslag);
        historikkRepository.lagre(historikkinnslag);
    }
}

