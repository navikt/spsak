package no.nav.foreldrepenger.behandling.steg.vedtak;

import static java.lang.Boolean.TRUE;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.behandlingslager.lagretvedtak.LagretVedtak;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.OppgaveTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl.OpprettOppgaveForBehandlingSendtTilbakeTask;
import no.nav.foreldrepenger.domene.vedtak.VedtakTjeneste;
import no.nav.foreldrepenger.domene.vedtak.xml.VedtakXmlFeil;
import no.nav.foreldrepenger.domene.vedtak.xml.VedtakXmlTjeneste;
import no.nav.foreldrepenger.vedtakslager.LagretVedtakRepository;
import no.nav.vedtak.feil.FeilFactory;

abstract class FatteVedtakTjenesteImpl implements FatteVedtakTjeneste {

    public static final String UTVIKLER_FEIL_VEDTAK = "Utvikler-feil: Vedtak kan ikke fattes, behandlingsresultat er ";
    private static final Set<BehandlingResultatType> VEDTAKSTILSTANDER_REVURDERING = new HashSet<>(
        Arrays.asList(BehandlingResultatType.AVSLÅTT, BehandlingResultatType.INNVILGET,
            BehandlingResultatType.OPPHØR, BehandlingResultatType.FORELDREPENGER_ENDRET,
            BehandlingResultatType.INGEN_ENDRING));
    private static final Set<BehandlingResultatType> VEDTAKSTILSTANDER = new HashSet<>(
        Arrays.asList(BehandlingResultatType.AVSLÅTT, BehandlingResultatType.INNVILGET));
    private static final Set<BehandlingResultatType> VEDTAKSTILSTANDER_KLAGE = new HashSet<>(
        Arrays.asList(BehandlingResultatType.KLAGE_AVVIST, BehandlingResultatType.KLAGE_MEDHOLD
            , BehandlingResultatType.KLAGE_YTELSESVEDTAK_OPPHEVET, BehandlingResultatType.KLAGE_YTELSESVEDTAK_STADFESTET));
    private static final Set<BehandlingResultatType> VEDTAKSTILSTANDER_INNSYN = new HashSet<>(
        Arrays.asList(BehandlingResultatType.INNSYN_AVVIST, BehandlingResultatType.INNSYN_DELVIS_INNVILGET, BehandlingResultatType.INNSYN_INNVILGET));
    private LagretVedtakRepository lagretVedtakRepository;
    private VedtakXmlTjeneste vedtakXmlTjeneste;
    private VedtakTjeneste vedtakTjeneste;
    private OppgaveTjeneste oppgaveTjeneste;
    private TotrinnTjeneste totrinnTjeneste;
    private BehandlingVedtakTjeneste behandlingVedtakTjeneste;

    protected FatteVedtakTjenesteImpl() {
        // for CDI proxy
    }

    FatteVedtakTjenesteImpl(LagretVedtakRepository vedtakRepository,
                            VedtakXmlTjeneste vedtakXmlTjeneste,
                            VedtakTjeneste vedtakTjeneste,
                            OppgaveTjeneste oppgaveTjeneste,
                            TotrinnTjeneste totrinnTjeneste,
                            BehandlingVedtakTjeneste behandlingVedtakTjeneste) {
        this.lagretVedtakRepository = vedtakRepository;
        this.vedtakXmlTjeneste = vedtakXmlTjeneste;
        this.vedtakTjeneste = vedtakTjeneste;
        this.oppgaveTjeneste = oppgaveTjeneste;
        this.totrinnTjeneste = totrinnTjeneste;
        this.behandlingVedtakTjeneste = behandlingVedtakTjeneste;
    }

    @Override
    public BehandleStegResultat fattVedtak(BehandlingskontrollKontekst kontekst, Behandling behandling) {
        verifiserBehandlingsresultat(behandling);

        if (behandling.isToTrinnsBehandling()) {
            Collection<Totrinnsvurdering> totrinnaksjonspunktvurderinger = totrinnTjeneste.hentTotrinnaksjonspunktvurderinger(behandling);
            if (sendesTilbakeTilSaksbehandler(totrinnaksjonspunktvurderinger)) {
                oppgaveTjeneste.avsluttOppgaveOgStartTask(behandling, OppgaveÅrsak.GODKJENNE_VEDTAK, OpprettOppgaveForBehandlingSendtTilbakeTask.TASKTYPE);
                List<AksjonspunktDefinisjon> aksjonspunktDefinisjoner = totrinnaksjonspunktvurderinger.stream()
                    .filter(a -> !TRUE.equals(a.isGodkjent()))
                    .map(Totrinnsvurdering::getAksjonspunktDefinisjon).collect(Collectors.toList());

                return BehandleStegResultat.tilbakeførtMedAksjonspunkter(aksjonspunktDefinisjoner);
            } else {
                oppgaveTjeneste.opprettTaskAvsluttOppgave(behandling);
            }
        } else {
            vedtakTjeneste.lagHistorikkinnslagFattVedtak(behandling);
        }

        behandlingVedtakTjeneste.opprettBehandlingVedtak(kontekst, behandling);

        opprettLagretVedtak(behandling);

        // Ingen nye aksjonspunkt herfra
        return BehandleStegResultat.utførtUtenAksjonspunkter();
    }

    private boolean sendesTilbakeTilSaksbehandler(Collection<Totrinnsvurdering> medTotrinnskontroll) {
        return medTotrinnskontroll.stream()
            .anyMatch(a -> !TRUE.equals(a.isGodkjent()));
    }

    private void verifiserBehandlingsresultat(Behandling behandling) {
        Behandlingsresultat behandlingsresultat = behandling.getBehandlingsresultat();
        if (behandling.erKlage()) {
            if (!VEDTAKSTILSTANDER_KLAGE.contains(behandlingsresultat.getBehandlingResultatType())) {
                throw new IllegalStateException(
                    UTVIKLER_FEIL_VEDTAK //$NON-NLS-1$
                        + (behandlingsresultat.getBehandlingResultatType().getNavn()));
            }
        } else if (behandling.erInnsyn()) {
            if (!VEDTAKSTILSTANDER_INNSYN.contains(behandlingsresultat.getBehandlingResultatType())) {
                throw new IllegalStateException(
                    UTVIKLER_FEIL_VEDTAK //$NON-NLS-1$
                        + (behandlingsresultat.getBehandlingResultatType().getNavn()));
            }
        } else if (behandling.erRevurdering()) {
            if (!VEDTAKSTILSTANDER_REVURDERING.contains(behandlingsresultat.getBehandlingResultatType())) {
                throw new IllegalStateException(
                    UTVIKLER_FEIL_VEDTAK //$NON-NLS-1$
                        + (behandlingsresultat.getBehandlingResultatType().getNavn()));
            }
        } else if (behandlingsresultat == null || !VEDTAKSTILSTANDER.contains(behandlingsresultat.getBehandlingResultatType())) {
            throw new IllegalStateException(
                UTVIKLER_FEIL_VEDTAK //$NON-NLS-1$
                    + (behandlingsresultat == null ? "null" : behandlingsresultat.getBehandlingResultatType().getNavn()));
        }
    }

    private void opprettLagretVedtak(Behandling behandling) {
        if (behandling.erInnsyn()) {
            return;
        }
        if (!erKlarForVedtak(behandling)) {
            throw FeilFactory.create(VedtakXmlFeil.class).behandlingErIFeilTilstand(behandling.getId(), behandling.getStatus().getBeskrivelse())
                .toException();
        }
        LagretVedtak lagretVedtak = LagretVedtak.builder()
            .medVedtakType(vedtakTjeneste.finnLagretVedtakType(behandling))
            .medBehandlingId(behandling.getId())
            .medFagsakId(behandling.getFagsakId())
            .medXmlClob(vedtakXmlTjeneste.opprettVedtakXml(behandling.getId()))
            .build();
        lagretVedtakRepository.lagre(lagretVedtak);
    }

    private boolean erKlarForVedtak(Behandling behandling) {
        return behandling.erKlage() || BehandlingStatus.FATTER_VEDTAK.equals(behandling.getStatus());
    }
}
