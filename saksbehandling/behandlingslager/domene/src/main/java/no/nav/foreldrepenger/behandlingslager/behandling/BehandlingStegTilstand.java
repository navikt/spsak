package no.nav.foreldrepenger.behandlingslager.behandling;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.diff.IndexKey;

@Entity(name = "BehandlingStegTilstand")
@Table(name = "BEHANDLING_STEG_TILSTAND")
public class BehandlingStegTilstand extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BEHANDLING_STEG_TILSTAND")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "behandling_steg", nullable = false, updatable = false)
    private BehandlingStegType behandlingSteg;

    @ManyToOne(optional = false)
    @JoinColumn(name = "behandling_id", nullable = false, updatable = false)
    private Behandling behandling;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "behandling_steg_status", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + BehandlingStegStatus.DISCRIMINATOR + "'"))
    private BehandlingStegStatus behandlingStegStatus = BehandlingStegStatus.UDEFINERT;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    BehandlingStegTilstand() {
        // for hibernate
    }

    public BehandlingStegTilstand(Behandling behandling, BehandlingStegType behandlingSteg) {
        this(behandling, behandlingSteg, BehandlingStegStatus.UDEFINERT);
    }

    public BehandlingStegTilstand(Behandling behandling, BehandlingStegType behandlingSteg, BehandlingStegStatus stegStatus) {
        this.behandling = behandling;
        this.behandlingSteg = behandlingSteg;
        this.setBehandlingStegStatus(stegStatus);
    }

    public Long getId() {
        return id;
    }
    
    @Override
    public String getIndexKey() {
        return IndexKey.createKey(behandlingSteg, behandlingStegStatus);
    }

    public Long getBehandlingId() {
        return behandling.getId();
    }

    public BehandlingStegType getStegType() {
        return behandlingSteg;
    }

    public BehandlingStegStatus getStatus() {
        return Objects.equals(BehandlingStegStatus.UDEFINERT, behandlingStegStatus) ? null : behandlingStegStatus;
    }

    /**
     * Set BehandlingStegStatus direkte. Kun for invortes bruk.
     *
     * @param behandlingStegStatus - ny status
     */
    void setBehandlingStegStatus(BehandlingStegStatus behandlingStegStatus) {
        this.behandlingStegStatus = behandlingStegStatus == null ? BehandlingStegStatus.UDEFINERT : behandlingStegStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BehandlingStegTilstand)) {
            return false;
        }
        BehandlingStegTilstand that = (BehandlingStegTilstand) o;
        return Objects.equals(getStegType(), that.getStegType()) &&
            Objects.equals(getStatus(), that.getStatus());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStegType(), getStatus());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<behandlingId=" + behandling.getId()
            + ", steg=" + getStegType()
            + ", stegStatus=" + getStatus()
            + ">";
    }

    public boolean erVedInngangAvSteg() {
        return this.behandlingStegStatus!=null && this.behandlingStegStatus.erVedInngang();
    }
    
    public boolean erVedUtgangAvSteg() {
        return this.behandlingStegStatus!=null && this.behandlingStegStatus.erVedUtgang();
    }
}
