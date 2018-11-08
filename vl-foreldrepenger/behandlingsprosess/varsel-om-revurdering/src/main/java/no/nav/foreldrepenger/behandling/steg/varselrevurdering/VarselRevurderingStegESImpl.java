package no.nav.foreldrepenger.behandling.steg.varselrevurdering;

import static java.util.Collections.singletonList;
import static no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType.RE_AVVIK_ANTALL_BARN;
import static no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType.RE_MANGLER_FØDSEL;
import static no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType.RE_MANGLER_FØDSEL_I_PERIODE;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_SATT_PÅ_VENT_REVURDERING;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.VARSEL_REVURDERING_ETTERKONTROLL;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.VARSEL_REVURDERING_MANUELL;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.RevurderingVarslingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveBehandlingKobling;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveBehandlingKoblingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerApplikasjonTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BestillBrevDto;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.OppgaveTjeneste;

@BehandlingStegRef(kode = "VRSLREV")
@BehandlingTypeRef
@FagsakYtelseTypeRef("ES")
@ApplicationScoped
public class VarselRevurderingStegESImpl implements VarselRevurderingSteg {

    private BehandlingRepository behandlingRepository;
    private OppgaveBehandlingKoblingRepository oppgaveBehandlingKoblingRepository;
    private OppgaveTjeneste oppgaveTjeneste;
    private DokumentBestillerApplikasjonTjeneste dokumentBestillerApplikasjonTjeneste;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    @Inject
    public VarselRevurderingStegESImpl(BehandlingRepository behandlingRepository,
                                       OppgaveBehandlingKoblingRepository oppgaveBehandlingKoblingRepository, OppgaveTjeneste oppgaveTjeneste,
                                       DokumentBestillerApplikasjonTjeneste dokumentBestillerApplikasjonTjeneste,
        /* FIXME (FLUORITT): midlertidig tar inn BehandlingskontrollTjeneste, trengs for å sette på vent. */
                                       BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        this.behandlingRepository = behandlingRepository;
        this.oppgaveBehandlingKoblingRepository = oppgaveBehandlingKoblingRepository;
        this.oppgaveTjeneste = oppgaveTjeneste;
        this.dokumentBestillerApplikasjonTjeneste = dokumentBestillerApplikasjonTjeneste;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        if (harSendtVarsel(kontekst.getBehandlingId())) {
            return BehandleStegResultat.utførtUtenAksjonspunkter();
        }

        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());
        if (behandling.getBehandlingÅrsaker().isEmpty()) {
            throw VarselRevurderingStegFeil.FACTORY.manglerBehandlingsårsakPåRevurdering().toException();
        }

        BehandlingÅrsakType behandlingÅrsakType = behandling.getBehandlingÅrsaker().get(0).getBehandlingÅrsakType();

        if (RE_AVVIK_ANTALL_BARN.equals(behandlingÅrsakType) || RE_MANGLER_FØDSEL_I_PERIODE.equals(behandlingÅrsakType)) {
            return BehandleStegResultat.utførtMedAksjonspunkter(singletonList(VARSEL_REVURDERING_ETTERKONTROLL));
        }

        if (RE_MANGLER_FØDSEL.equals(behandlingÅrsakType)) {
            sendVarselOmRevurdering(behandling);
            settBehandlingPåVent(behandling);
            return BehandleStegResultat.utførtUtenAksjonspunkter();
        }

        // Manuelt opprettet revurdering
        return BehandleStegResultat.utførtMedAksjonspunkter(singletonList(VARSEL_REVURDERING_MANUELL));
    }

    private void sendVarselOmRevurdering(Behandling behandling) {
        BestillBrevDto bestillBrevDto = new BestillBrevDto(behandling.getId(), DokumentMalType.REVURDERING_DOK);
        bestillBrevDto.setÅrsakskode(RevurderingVarslingÅrsak.BARN_IKKE_REGISTRERT_FOLKEREGISTER.getKode());
        dokumentBestillerApplikasjonTjeneste.bestillDokument(bestillBrevDto, HistorikkAktør.VEDTAKSLØSNINGEN);
    }

    private void settBehandlingPåVent(Behandling behandling) {
        // FIXME (TOPAS): Her bør refaktoreres siden dette er del av seg. Bør returnere via BehandleStegResultat

        behandlingskontrollTjeneste.settBehandlingPåVent(behandling, AUTO_SATT_PÅ_VENT_REVURDERING,
            BehandlingStegType.VARSEL_REVURDERING, null, null);

        avsluttOppgave(behandling);
    }

    /**
     * @deprecated FIXME (TOPAS): Bør flyttes til event listener tilsvarende OppgaveTjeneste#observerBehandlingStatus
     * @param behandling en Behandling
     */
    @Deprecated
    private void avsluttOppgave(Behandling behandling) {
        List<OppgaveBehandlingKobling> oppgaver = oppgaveBehandlingKoblingRepository.hentOppgaverRelatertTilBehandling(behandling.getId());
        OppgaveBehandlingKobling.getAktivOppgaveMedÅrsak(behandling.getBehandleOppgaveÅrsak(), oppgaver).ifPresent(oppgave ->
            oppgaveTjeneste.avslutt(behandling.getId(), oppgave.getOppgaveÅrsak()));
    }

    private boolean harSendtVarsel(Long behandlingId) {
        return dokumentBestillerApplikasjonTjeneste.erDokumentProdusert(behandlingId, DokumentMalType.REVURDERING_DOK);
    }
}
