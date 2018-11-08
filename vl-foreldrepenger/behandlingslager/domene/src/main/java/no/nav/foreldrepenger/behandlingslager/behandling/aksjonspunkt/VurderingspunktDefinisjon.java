package no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkTabell;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity(name = "VurderingspunktDef")
@Table(name = "VURDERINGSPUNKT_DEF")
public class VurderingspunktDefinisjon extends KodeverkTabell {

    public enum Type {
        INNGANG("INN"), //$NON-NLS-1$
        UTGANG("UT"); //$NON-NLS-1$
        private final String dbKode;

        private Type(String dbKode) {
            this.dbKode = dbKode;
        }

        public String getDbKode() {
            return dbKode;
        }

        public static Type getType(String kode) {
            switch (kode) {
                case "INN": //$NON-NLS-1$
                    return INNGANG;
                case "UT": //$NON-NLS-1$
                    return UTGANG;
                default:
                    throw new IllegalArgumentException("Ukjent kode: " + kode); //$NON-NLS-1$
            }
        }
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "behandling_steg", nullable = false, updatable = false, insertable = false)
    private BehandlingStegType behandlingSteg;

    @Column(name = "vurderingspunkt_type", nullable = false, updatable = false, insertable = false)
    private String type;

    @OneToMany(mappedBy = "vurderingspunktDefinisjon")
    protected List<AksjonspunktDefinisjon> aksjonspunktDefinisjoner = new ArrayList<>();

    protected VurderingspunktDefinisjon() {
        // for hibernate
    }

    protected VurderingspunktDefinisjon(String kode, Type type) {
        super(kode);
        this.type = type.getDbKode();
    }

    public VurderingspunktDefinisjon.Type getType() {
        return VurderingspunktDefinisjon.Type.getType(type);
    }

    public List<AksjonspunktDefinisjon> getAksjonspunktDefinisjoner() {
        return Collections.unmodifiableList(aksjonspunktDefinisjoner);
    }
    
    public BehandlingStegType getBehandlingSteg() {
        return behandlingSteg;
    }

}
