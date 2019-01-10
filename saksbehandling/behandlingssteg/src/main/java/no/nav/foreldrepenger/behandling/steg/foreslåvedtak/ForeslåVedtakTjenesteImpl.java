package no.nav.foreldrepenger.behandling.steg.foreslåvedtak;

import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårKodeverkRepository;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

abstract class ForeslåVedtakTjenesteImpl implements ForeslåVedtakTjeneste {

    private VilkårKodeverkRepository vilkårKodeverkRepository;
    private SjekkMotEksisterendeOppgaverTjeneste sjekkMotEksisterendeOppgaverTjeneste;

    protected ForeslåVedtakTjenesteImpl() {
        //CDI proxy
    }

    ForeslåVedtakTjenesteImpl(GrunnlagRepositoryProvider provider, SjekkMotEksisterendeOppgaverTjeneste sjekkMotEksisterendeOppgaverTjeneste) {
        this.vilkårKodeverkRepository = provider.getVilkårKodeverkRepository();
        this.sjekkMotEksisterendeOppgaverTjeneste = sjekkMotEksisterendeOppgaverTjeneste;
    }

    @Override
    public BehandleStegResultat foreslåVedtak(Behandling behandling) {
        foreslåAutomatisertVedtak(behandling);

        List<AksjonspunktDefinisjon> aksjonspunktDefinisjoner = sjekkMotEksisterendeOppgaverTjeneste.sjekkMotEksisterendeGsakOppgaver(behandling.getAktørId(), behandling);

        Optional<Aksjonspunkt> vedtakUtenTotrinnskontroll = behandling.getAksjonspunktMedDefinisjonOptional(AksjonspunktDefinisjon.VEDTAK_UTEN_TOTRINNSKONTROLL);
        if (vedtakUtenTotrinnskontroll.isPresent() && vedtakUtenTotrinnskontroll.get().erÅpentAksjonspunkt()) {
            return BehandleStegResultat.utførtMedAksjonspunkter(aksjonspunktDefinisjoner);
        }

        if (skalUtføreTotrinnsbehandling(behandling)) {
            if (!behandling.isToTrinnsBehandling()) {
                behandling.setToTrinnsBehandling();
            }
            aksjonspunktDefinisjoner.add(AksjonspunktDefinisjon.FORESLÅ_VEDTAK);
        } else {
            behandling.nullstillToTrinnsBehandling();
            if (BehandlingType.REVURDERING.equals(behandling.getType())) {
                aksjonspunktDefinisjoner.add(AksjonspunktDefinisjon.FORESLÅ_VEDTAK_MANUELT);
            }
        }

        return aksjonspunktDefinisjoner.isEmpty() ?
            BehandleStegResultat.utførtUtenAksjonspunkter() : BehandleStegResultat.utførtMedAksjonspunkter(aksjonspunktDefinisjoner);
    }

    protected abstract boolean sjekkVilkårAvslått(Behandlingsresultat behandlingsresultat);

    protected abstract void foreslåAutomatisertVedtak(Behandling behandling);

    private boolean skalUtføreTotrinnsbehandling(Behandling behandling) {
        return !behandling.harÅpentAksjonspunktMedType(AksjonspunktDefinisjon.VEDTAK_UTEN_TOTRINNSKONTROLL) &&
            behandling.harAksjonspunktMedTotrinnskontroll();
    }

    Avslagsårsak finnAvslagsårsak(Vilkår vilkår) {
        Avslagsårsak avslagsårsak = vilkår.getGjeldendeAvslagsårsak();
        if (avslagsårsak == null) {
            if (vilkår.getVilkårUtfallMerknad() == null) {
                return vilkårKodeverkRepository.finnEnesteAvslagÅrsak(vilkår.getVilkårType().getKode())
                    .orElseThrow(() -> ForeslåVedtakTjenesteFeil.FEILFACTORY
                        .kanIkkeUtledeAvslagsårsakUtfallMerknadMangler(vilkår.getVilkårType().getKode()).toException());
            }
            avslagsårsak = vilkårKodeverkRepository.finnAvslagÅrsak(vilkår.getVilkårUtfallMerknad().getKode());
            if (avslagsårsak == null) {
                throw ForeslåVedtakTjenesteFeil.FEILFACTORY.kanIkkeUtledeAvslagsårsakFraUtfallMerknad(vilkår.getVilkårUtfallMerknad().getKode())
                    .toException();
            }
        }
        return avslagsårsak;
    }

    private interface ForeslåVedtakTjenesteFeil extends DeklarerteFeil {
        ForeslåVedtakTjenesteFeil FEILFACTORY = FeilFactory.create(ForeslåVedtakTjenesteFeil.class); // NOSONAR ok med konstant
        // i interface her

        @TekniskFeil(feilkode = "FP-411108", feilmelding = "Kan ikke utlede avslagsårsak fra utfallmerknad %s.", logLevel = LogLevel.ERROR)
        Feil kanIkkeUtledeAvslagsårsakFraUtfallMerknad(String kode);

        @TekniskFeil(feilkode = "FP-411109", feilmelding = "Kan ikke utlede avslagsårsak, utfallmerknad mangler i vilkår %s.", logLevel = LogLevel.ERROR)
        Feil kanIkkeUtledeAvslagsårsakUtfallMerknadMangler(String kode);
    }
}
