package no.nav.foreldrepenger.behandlingslager.behandling.historikk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "HistorikkResultatType")
@DiscriminatorValue(HistorikkResultatType.DISCRIMINATOR)
public class HistorikkResultatType extends Kodeliste {
    public static final String DISCRIMINATOR = "HISTORIKK_RESULTAT_TYPE"; //$NON-NLS-1$
    
    public static final HistorikkResultatType BEREGNET_AARSINNTEKT = new HistorikkResultatType("BEREGNET_AARSINNTEKT");
    

    public HistorikkResultatType() {
        //
    }

    private HistorikkResultatType(String kode) {
        super(kode, DISCRIMINATOR);
    }

}
