package no.nav.foreldrepenger.behandlingslager.behandling.totrinn;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.BatchSize;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "Totrinnsvurdering")
@Table(name = "TOTRINNSVURDERING")
public class Totrinnsvurdering extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TOTRINNSVURDERING")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "behandling_id", nullable = false, updatable = false)
    private Behandling behandling;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @BatchSize(size = 10)
    @JoinColumn(name = "aksjonspunkt_def", nullable = false, updatable = false)
    private AksjonspunktDefinisjon aksjonspunktDefinisjon;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "totrinnsvurdering")
    private Set<VurderÅrsakTotrinnsvurdering> vurderPåNyttÅrsaker = new HashSet<>();

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "godkjent", nullable = false)
    private Boolean godkjent;

    @Column(name = "begrunnelse")
    private String begrunnelse;


    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;


    public Set<VurderÅrsakTotrinnsvurdering> getVurderPåNyttÅrsaker() {
        return vurderPåNyttÅrsaker;
    }

    public Boolean isGodkjent() {
        return godkjent;
    }

    public boolean isAktiv() {
        return aktiv;
    }

    public Behandling getBehandling() {
        return behandling;
    }

    public AksjonspunktDefinisjon getAksjonspunktDefinisjon() {
        return aksjonspunktDefinisjon;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public void setAktiv(boolean aktiv) {
        this.aktiv = aktiv;
    }


    public static class Builder {
        private Totrinnsvurdering totrinnsvurderingMal;

        public Builder(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon) {
            totrinnsvurderingMal = new Totrinnsvurdering();
            totrinnsvurderingMal.aksjonspunktDefinisjon = aksjonspunktDefinisjon;
            totrinnsvurderingMal.behandling = behandling;
        }

        public Totrinnsvurdering.Builder medGodkjent(boolean godkjent) {
            totrinnsvurderingMal.godkjent = godkjent;
            return this;
        }


        public Totrinnsvurdering.Builder medBegrunnelse(String begrunnelse) {
            totrinnsvurderingMal.begrunnelse = begrunnelse;
            return this;
        }

        public Totrinnsvurdering.Builder medVurderÅrsak(VurderÅrsak vurderÅrsak) {
            VurderÅrsakTotrinnsvurdering vurderPåNyttÅrsak = new VurderÅrsakTotrinnsvurdering(vurderÅrsak, totrinnsvurderingMal);
            totrinnsvurderingMal.vurderPåNyttÅrsaker.add(vurderPåNyttÅrsak);
            return this;
        }


        public Totrinnsvurdering build() {
            verifyStateForBuild();
            return totrinnsvurderingMal;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(totrinnsvurderingMal.aksjonspunktDefinisjon, "aksjonspunktDefinisjon");
            Objects.requireNonNull(totrinnsvurderingMal.behandling, "behandling");

        }
    }
}
