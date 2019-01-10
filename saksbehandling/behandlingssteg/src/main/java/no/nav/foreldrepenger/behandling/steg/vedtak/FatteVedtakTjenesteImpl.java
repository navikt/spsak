package no.nav.foreldrepenger.behandling.steg.vedtak;

import static java.lang.Boolean.TRUE;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.domene.vedtak.VedtakTjeneste;

@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class FatteVedtakTjenesteImpl implements FatteVedtakTjeneste {

    public static final String UTVIKLER_FEIL_VEDTAK = "Utvikler-feil: Vedtak kan ikke fattes, behandlingsresultat er ";
    private static final Set<BehandlingResultatType> VEDTAKSTILSTANDER_REVURDERING = new HashSet<>(
        Arrays.asList(BehandlingResultatType.AVSLÅTT, BehandlingResultatType.INNVILGET,
            BehandlingResultatType.OPPHØR, BehandlingResultatType.FORELDREPENGER_ENDRET,
            BehandlingResultatType.INGEN_ENDRING));
    private static final Set<BehandlingResultatType> VEDTAKSTILSTANDER = new HashSet<>(
        Arrays.asList(BehandlingResultatType.AVSLÅTT, BehandlingResultatType.INNVILGET));
    private BehandlingRepository behandlingRepository;
    private VedtakTjeneste vedtakTjeneste;
    private TotrinnTjeneste totrinnTjeneste;
    private BehandlingVedtakTjeneste behandlingVedtakTjeneste;

    protected FatteVedtakTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    FatteVedtakTjenesteImpl(BehandlingRepository behandlingRepository, VedtakTjeneste vedtakTjeneste,
                            TotrinnTjeneste totrinnTjeneste,
                            BehandlingVedtakTjeneste behandlingVedtakTjeneste) {
        this.behandlingRepository = behandlingRepository;
        this.vedtakTjeneste = vedtakTjeneste;
        this.totrinnTjeneste = totrinnTjeneste;
        this.behandlingVedtakTjeneste = behandlingVedtakTjeneste;
    }

    @Override
    public BehandleStegResultat fattVedtak(BehandlingskontrollKontekst kontekst, Behandling behandling) {
        verifiserBehandlingsresultat(behandling);

        if (behandling.isToTrinnsBehandling()) {
            Collection<Totrinnsvurdering> totrinnaksjonspunktvurderinger = totrinnTjeneste.hentTotrinnaksjonspunktvurderinger(behandling);
            if (sendesTilbakeTilSaksbehandler(totrinnaksjonspunktvurderinger)) {
                List<AksjonspunktDefinisjon> aksjonspunktDefinisjoner = totrinnaksjonspunktvurderinger.stream()
                    .filter(a -> !TRUE.equals(a.isGodkjent()))
                    .map(Totrinnsvurdering::getAksjonspunktDefinisjon).collect(Collectors.toList());

                return BehandleStegResultat.tilbakeførtMedAksjonspunkter(aksjonspunktDefinisjoner);
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
        Optional<Behandlingsresultat> behandlingsresultat = behandlingRepository.hentResultatHvisEksisterer(behandling.getId());
        if (behandling.erRevurdering()) {
            if (!VEDTAKSTILSTANDER_REVURDERING.contains(behandlingsresultat.get().getBehandlingResultatType())) {
                throw new IllegalStateException(
                    UTVIKLER_FEIL_VEDTAK // $NON-NLS-1$
                        + (behandlingsresultat.get().getBehandlingResultatType().getNavn()));
            }
        } else if (behandlingsresultat.isEmpty() || !VEDTAKSTILSTANDER.contains(behandlingsresultat.get().getBehandlingResultatType())) {
            throw new IllegalStateException(
                UTVIKLER_FEIL_VEDTAK // $NON-NLS-1$
                    + (behandlingsresultat.isEmpty() ? "null" : behandlingsresultat.get().getBehandlingResultatType().getNavn()));
        }
    }

    private void opprettLagretVedtak(Behandling behandling) {
        // FIXME SP: oppretter vedtak på noe vis
    }

}
