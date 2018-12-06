package no.nav.foreldrepenger.behandlingslager.behandling.totrinn;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;
import no.nav.foreldrepenger.behandlingslager.diff.IndexKey;


/**
 * Tilbakemelding fra beslutter for å be saksbehandler vurdere et aksjonspunkt på nytt.
 */
@Entity(name = "VurderÅrsakTotrinnsvurdering")
@Table(name = "VURDER_AARSAK_TTVURDERING")
public class VurderÅrsakTotrinnsvurdering extends BaseEntitet implements IndexKey {

    @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VURDER_AARSAK_TTVURDERING")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "aarsak_type", referencedColumnName = "kode", nullable = false, updatable=false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + VurderÅrsak.DISCRIMINATOR
            + "'")) })
    private VurderÅrsak årsaksType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "totrinnsvurdering_id", nullable = false, updatable=false)
    private Totrinnsvurdering totrinnsvurdering;

    VurderÅrsakTotrinnsvurdering() {
        // for Hibernate
    }

    public VurderÅrsakTotrinnsvurdering(VurderÅrsak type, Totrinnsvurdering totrinnsvurdering) {
        this.totrinnsvurdering = totrinnsvurdering;
        this.årsaksType = type;
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(årsaksType);
    }

    public VurderÅrsak getÅrsaksType() {
        return årsaksType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VurderÅrsakTotrinnsvurdering)) {
            return false;
        }
        VurderÅrsakTotrinnsvurdering that = (VurderÅrsakTotrinnsvurdering) o;
        return Objects.equals(getÅrsaksType(), that.getÅrsaksType()) &&
            Objects.equals(totrinnsvurdering, that.totrinnsvurdering);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getÅrsaksType(), totrinnsvurdering);
    }
}
