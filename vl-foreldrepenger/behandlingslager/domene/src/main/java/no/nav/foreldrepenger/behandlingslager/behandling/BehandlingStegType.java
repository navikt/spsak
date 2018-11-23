package no.nav.foreldrepenger.behandlingslager.behandling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.VurderingspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.VurderingspunktDefinisjon.Type;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkTabell;

@Entity(name = "BehandlingStegType")
@Table(name = "BEHANDLING_STEG_TYPE")
public class BehandlingStegType extends KodeverkTabell {

    // Steg koder som deles av Foreldrepenger og Engangsstønad
    public static final BehandlingStegType VARSEL_REVURDERING = new BehandlingStegType("VRSLREV"); //$NON-NLS-1$
    public static final BehandlingStegType INNHENT_REGISTEROPP = new BehandlingStegType("INREG"); //$NON-NLS-1$
    public static final BehandlingStegType KONTROLLER_FAKTA = new BehandlingStegType("KOFAK"); //$NON-NLS-1$
    public static final BehandlingStegType SØKERS_RELASJON_TIL_BARN = new BehandlingStegType("VURDERSRB"); //$NON-NLS-1$
    public static final BehandlingStegType VURDER_MEDLEMSKAPVILKÅR = new BehandlingStegType("VURDERMV"); //$NON-NLS-1$
    public static final BehandlingStegType BEREGN_YTELSE = new BehandlingStegType("BERYT"); //$NON-NLS-1$
    public static final BehandlingStegType FATTE_VEDTAK = new BehandlingStegType("FVEDSTEG"); //$NON-NLS-1$
    public static final BehandlingStegType IVERKSETT_VEDTAK = new BehandlingStegType("IVEDSTEG"); //$NON-NLS-1$
    public static final BehandlingStegType FORESLÅ_VEDTAK = new BehandlingStegType("FORVEDSTEG"); //$NON-NLS-1$
    public static final BehandlingStegType KONTROLLERER_SØKERS_OPPLYSNINGSPLIKT = new BehandlingStegType("VURDEROP"); //$NON-NLS-1$
    public static final BehandlingStegType KLAGE_NFP = new BehandlingStegType("KLAGEUI"); //$NON-NLS-1$
    public static final BehandlingStegType KLAGE_NK = new BehandlingStegType("KLAGEOI"); //$NON-NLS-1$
    public static final BehandlingStegType REGISTRER_SØKNAD = new BehandlingStegType("REGSØK"); //$NON-NLS-1$
    public static final BehandlingStegType VURDER_INNSYN = new BehandlingStegType("VURDINNSYN"); //$NON-NLS-1$
    public static final BehandlingStegType INNHENT_PERSONOPPLYSNINGER = new BehandlingStegType("INPER"); //$NON-NLS-1$
    public static final BehandlingStegType VURDER_KOMPLETTHET = new BehandlingStegType("VURDERKOMPLETT"); //$NON-NLS-1$
    public static final BehandlingStegType VURDER_SAMLET = new BehandlingStegType("VURDERSAMLET"); //$NON-NLS-1$
    public static final BehandlingStegType SIMULER_OPPDRAG = new BehandlingStegType("SIMOPP"); //$NON-NLS-1$

    // Kun for Foreldrepenger
    public static final BehandlingStegType VURDER_OPPTJENINGSVILKÅR = new BehandlingStegType("VURDER_OPPTJ"); //$NON-NLS-1$
    public static final BehandlingStegType FORESLÅ_BEREGNINGSGRUNNLAG = new BehandlingStegType("FORS_BERGRUNN"); //$NON-NLS-1$
    public static final BehandlingStegType FASTSETT_BEREGNINGSGRUNNLAG = new BehandlingStegType("FAST_BERGRUNN"); //$NON-NLS-1$
    public static final BehandlingStegType KONTROLLER_FAKTA_BEREGNING = new BehandlingStegType("KOFAKBER"); //$NON-NLS-1$
    public static final BehandlingStegType KONTROLLER_FAKTA_ARBEIDSFORHOLD = new BehandlingStegType("KOARB"); //$NON-NLS-1$
    public static final BehandlingStegType FASTSETT_OPPTJENINGSPERIODE = new BehandlingStegType("VURDER_OPPTJ_PERIODE"); //$NON-NLS-1$
    public static final BehandlingStegType KONTROLLER_LØPENDE_MEDLEMSKAP = new BehandlingStegType("KOFAK_LOP_MEDL"); //$NON-NLS-1$

    // Kun for Engangsstønad
    public static final BehandlingStegType VURDER_SØKNADSFRISTVILKÅR = new BehandlingStegType("VURDERSFV"); //$NON-NLS-1$

    @Valid
    @Size(max = 10)
    @OneToMany(mappedBy = "behandlingSteg")
    protected List<VurderingspunktDefinisjon> vurderingspunkter = new ArrayList<>();

    /**
     * Definisjon av hvilken status behandlingen skal rapporteres som når dette steget er aktivt.
     */
    @Valid
    @ManyToOne
    @JoinColumnOrFormula(column = @JoinColumn(name = "behandling_status_def", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + BehandlingStatus.DISCRIMINATOR + "'"))
    private BehandlingStatus definertBehandlingStatus;

    protected BehandlingStegType() {
        // Hibernate trenger denne
    }

    protected BehandlingStegType(String kode) {
        super(kode);
    }

    public BehandlingStatus getDefinertBehandlingStatus() {
        validerBehandlingStatusHentet();
        return definertBehandlingStatus;
    }

    private void validerBehandlingStatusHentet() {
        if (definertBehandlingStatus == null) {
            throw new IllegalArgumentException(
                "Denne koden er ikke hentet fra databasen, kan ikke brukes til å konfigurere steg (kun skriving):" + this); //$NON-NLS-1$
        }
    }

    public List<VurderingspunktDefinisjon> getVurderingspunkter() {
        return Collections.unmodifiableList(vurderingspunkter);
    }

    public Optional<VurderingspunktDefinisjon> getVurderingspunktInngang() {
        return Optional.ofNullable(finnVurderingspunkt(VurderingspunktDefinisjon.Type.INNGANG));
    }

    public List<AksjonspunktDefinisjon> getAksjonspunktDefinisjonerInngang() {
        Optional<VurderingspunktDefinisjon> vurd = getVurderingspunktInngang();
        if (!vurd.isPresent()) {
            return Collections.emptyList();
        } else {
            return vurd.get().getAksjonspunktDefinisjoner();
        }
    }

    public Optional<VurderingspunktDefinisjon> getVurderingspunktUtgang() {
        return Optional.ofNullable(finnVurderingspunkt(VurderingspunktDefinisjon.Type.UTGANG));
    }

    public List<AksjonspunktDefinisjon> getAksjonspunktDefinisjonerUtgang() {
        Optional<VurderingspunktDefinisjon> vurd = getVurderingspunktUtgang();
        if (!vurd.isPresent()) {
            return Collections.emptyList();
        } else {
            return vurd.get().getAksjonspunktDefinisjoner();
        }
    }

    private VurderingspunktDefinisjon finnVurderingspunkt(Type type) {
        List<VurderingspunktDefinisjon> list = vurderingspunkter.stream()
            .filter(v -> Objects.equals(type, v.getType()))
            .collect(Collectors.toList());
        if (list.isEmpty()) {
            return null;
        } else if (list.size() == 1) {
            return list.get(0);
        } else {
            throw new IllegalStateException("Mer enn en definisjon matcher type : " + type.getDbKode() + ": " + list); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

}
