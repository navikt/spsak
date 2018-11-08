package no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt;

import java.time.LocalDateTime;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface AksjonspunktRepository extends BehandlingslagerRepository {

    AksjonspunktDefinisjon finnAksjonspunktDefinisjon(String kode);

    List<AksjonspunktDefinisjon> hentAksjonspunktDefinisjonAvType(AksjonspunktType aksjonspunktType);

    void setTilAvbrutt(Aksjonspunkt aksjonspunkt);

    boolean setTilUtført(Aksjonspunkt aksjonspunkt, String begrunnelse);

    void deaktiver(Aksjonspunkt aksjonspunkt);

    void settInaktivSomSlettet(Aksjonspunkt aksjonspunkt);

    void reaktiver(Aksjonspunkt aksjonspunkt);

    void setReåpnet(Aksjonspunkt aksjonspunkt);

    void setToTrinnsBehandlingKreves(Aksjonspunkt aksjonspunkt);

    void fjernToTrinnsBehandlingKreves(Aksjonspunkt aksjonspunkt);

    Aksjonspunkt leggTilAksjonspunkt(Behandling behandling, AksjonspunktDefinisjon def);

    Aksjonspunkt leggTilAksjonspunkt(Behandling behandling, AksjonspunktDefinisjon def, BehandlingStegType steg);

    void setFrist(Aksjonspunkt ap, LocalDateTime fristTid, Venteårsak venteårsak);

    void fjernAksjonspunkt(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon);

    /**
     * @deprecated Skal ikke ha dette per Aksjonspunkt
     */
    @Deprecated
    void setSlettingVedRegisterinnhenting(Aksjonspunkt aksjonspunkt, boolean slettes);

    Aksjonspunkt settBehandlingPåVent(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon, BehandlingStegType stegType,
                                      LocalDateTime fristTid, Venteårsak venteårsak);

    AksjonspunktStatus finnAksjonspunktStatus(String kode);

    void setPeriode(Aksjonspunkt ap, DatoIntervallEntitet periode);

    void setToTrinnsBehandlingKreves(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon);

    /** Sjekk om begrunnelse er endret. */
    boolean sjekkErBegrunnelseForAksjonspunktEndret(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon, String begrunnelse);

    void setTilManueltOpprettet(Aksjonspunkt aksjonspunkt);

    void kopierAlleAksjonspunkterOgSettDemInaktive(Behandling opprinneligBehandling, Behandling nyBehandling);

}
