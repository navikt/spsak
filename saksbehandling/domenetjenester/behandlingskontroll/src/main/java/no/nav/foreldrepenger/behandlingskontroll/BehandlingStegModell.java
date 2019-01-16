package no.nav.foreldrepenger.behandlingskontroll;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.Instance;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.VurderingspunktDefinisjon;

/**
 * Modellerer ett behandlingssteg, inklusiv hvilke aksjonspunkter må løses før/etter steget.
 * Dersom det ved kjøring oppdages aksjonspunkter som ikke er registrert må disse løses før utgang av et
 * behandlingssteg.
 */
public class BehandlingStegModell {
    private Instance<BehandlingSteg> bean;
    private BehandlingSteg steg;
    private BehandlingStegType behandlingStegType;

    /**
     * Aksjonspunkter som må løses ved inngang til behandlingsteg.
     */
    private final Set<String> inngangAksjonpunktKoder = new LinkedHashSet<>();

    /**
     * Aksjonspunkter som må løses ved utgang fra behandlingsteg.
     */
    private final Set<String> utgangAksjonpunktKoder = new LinkedHashSet<>();

    /** Hver steg modell må tilhøre en BehandlingModell som beskriver hvordan de henger sammen. */
    private BehandlingModellImpl behandlingModell;

    /**
     * Holder for å referere til en konkret, men lazy-initialisert CDI implementasjon av et {@link BehandlingSteg}.
     */
    BehandlingStegModell(BehandlingModellImpl behandlingModell, Instance<BehandlingSteg> bean, BehandlingStegType stegType) {
        Objects.requireNonNull(behandlingModell, "behandlingModell"); //$NON-NLS-1$
        Objects.requireNonNull(bean, "bean"); //$NON-NLS-1$
        Objects.requireNonNull(stegType, "stegType"); //$NON-NLS-1$
        this.bean = bean;
        this.behandlingModell = behandlingModell;
        this.behandlingStegType = stegType;
    }

    /** Direkte injisering av {@link BehandlingSteg}. For testing. */
    BehandlingStegModell(BehandlingModellImpl behandlingModell, BehandlingSteg steg, BehandlingStegType stegType) {
        Objects.requireNonNull(behandlingModell, "behandlingModell"); //$NON-NLS-1$
        Objects.requireNonNull(steg, "steg"); //$NON-NLS-1$ // NOSONAR
        Objects.requireNonNull(stegType, "stegType"); //$NON-NLS-1$ // NOSONAR
        this.steg = steg;
        this.behandlingModell = behandlingModell;
        this.behandlingStegType = stegType;
    }

    public BehandlingModellImpl getBehandlingModell() {
        return behandlingModell;
    }

    Set<String> getInngangAksjonpunktKoder() {
        return Collections.unmodifiableSet(inngangAksjonpunktKoder);
    }

    Set<String> getUtgangAksjonpunktKoder() {
        return Collections.unmodifiableSet(utgangAksjonpunktKoder);
    }

    private void initSteg() {
        if (steg == null) {
            try {
                steg = bean.get();
            } catch (InjectionException e) {
                throw new IllegalStateException(
                        "Mangler steg definert for stegKode=" + behandlingStegType + " [behandlingType=" //$NON-NLS-1$ //$NON-NLS-2$
                                + behandlingModell.getBehandlingType() + ", fagsakYtelseType=" + behandlingModell.getFagsakYtelseType() //$NON-NLS-1$ //$NON-NLS-2$
                                + "]",
                        e);
            }
        }
    }

    void leggTilAksjonspunktVurderingUtgang(String kode) {
        behandlingModell.validerErIkkeAlleredeMappet(kode);
        utgangAksjonpunktKoder.add(kode);
    }

    void leggTilAksjonspunktVurderingInngang(String kode) {
        behandlingModell.validerErIkkeAlleredeMappet(kode);
        inngangAksjonpunktKoder.add(kode);
    }

    void destroy() {
        if (bean != null && steg != null) {
            bean.destroy(steg);
        }
    }

    /**
         * Type kode for dette steget.
         */
    public BehandlingStegType getBehandlingStegType() {
        return behandlingStegType;
    }

    /**
         * Forventet status når behandling er i steget.
         */
    public String getForventetStatus() {
        return behandlingStegType.getDefinertBehandlingStatus().getKode();
    }

    /**
         * Implementasjon av et gitt steg i behandlingen.
         */
    public BehandlingSteg getSteg() {
        initSteg();
        return steg;
    }

    /**
     * Avleder status behandlingsteg bør settes i gitt et sett med aksjonpunkter. Tar kun hensyn til aksjonpunkter
     * som gjelder dette steget.
     */
    public Optional<BehandlingStegStatus> avledStatus(Collection<String> aksjonspunkter) {

        if (!Collections.disjoint(aksjonspunkter, inngangAksjonpunktKoder)) { // NOSONAR
            return Optional.of(BehandlingStegStatus.INNGANG);
        } else if (!Collections.disjoint(aksjonspunkter, utgangAksjonpunktKoder)) { // NOSONAR
            return Optional.of(BehandlingStegStatus.UTGANG);
        } else {
            return Optional.empty();
        }
    }

    protected void leggTilVurderingspunktUtgang(Optional<VurderingspunktDefinisjon> vurderingspunkt) {
        if (vurderingspunkt.isPresent()) {
            vurderingspunkt.get().getAksjonspunktDefinisjoner().forEach(ad -> leggTilAksjonspunktVurderingUtgang(ad.getKode()));
        }
    }

    protected void leggTilVurderingspunktInngang(Optional<VurderingspunktDefinisjon> vurderingspunkt) {
        if (vurderingspunkt.isPresent()) {
            vurderingspunkt.get().getAksjonspunktDefinisjoner().forEach(ad -> leggTilAksjonspunktVurderingInngang(ad.getKode()));
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + behandlingStegType.getKode() + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "inngangAksjonspunkter=" + inngangAksjonpunktKoder + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "utgangAksjonspunkter=" + utgangAksjonpunktKoder + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "impl=" + steg //$NON-NLS-1$
                + ">"; //$NON-NLS-1$
    }
}
