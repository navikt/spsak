package no.nav.foreldrepenger.behandlingslager.behandling.historikk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "HistorikkBegrunnelseType")
@DiscriminatorValue(HistorikkBegrunnelseType.DISCRIMINATOR)
public class HistorikkBegrunnelseType extends Kodeliste {
    public static final String DISCRIMINATOR = "HISTORIKK_BEGRUNNELSE_TYPE"; //$NON-NLS-1$

    

    public static final HistorikkBegrunnelseType SAKSBEH_START_PA_NYTT = new HistorikkBegrunnelseType("SAKSBEH_START_PA_NYTT");
    public static final HistorikkBegrunnelseType BEH_STARTET_PA_NYTT = new HistorikkBegrunnelseType("BEH_STARTET_PA_NYTT");

    public HistorikkBegrunnelseType() {
        //
    }

    private HistorikkBegrunnelseType(String kode) {
        super(kode, DISCRIMINATOR);
    }

}
