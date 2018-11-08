package no.nav.foreldrepenger.behandlingslager.behandling.historikk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "HistorikkOpplysningType")
@DiscriminatorValue(HistorikkOpplysningType.DISCRIMINATOR)
public class HistorikkOpplysningType extends Kodeliste {
    public static final String DISCRIMINATOR = "HISTORIKK_OPPLYSNING_TYPE"; //$NON-NLS-1$

    public static final HistorikkOpplysningType UDEFINIERT = new HistorikkOpplysningType("-");

    public static final HistorikkOpplysningType ANTALL_BARN = new HistorikkOpplysningType("ANTALL_BARN");
    public static final HistorikkOpplysningType TPS_ANTALL_BARN = new HistorikkOpplysningType("TPS_ANTALL_BARN");
    public static final HistorikkOpplysningType FODSELSDATO = new HistorikkOpplysningType("FODSELSDATO");
    public static final HistorikkOpplysningType UTTAK_PERIODE_FOM = new HistorikkOpplysningType("UTTAK_PERIODE_FOM");
    public static final HistorikkOpplysningType UTTAK_PERIODE_TOM = new HistorikkOpplysningType("UTTAK_PERIODE_TOM");

    public HistorikkOpplysningType() {
        //
    }

    public HistorikkOpplysningType(String kode) {
        super(kode, DISCRIMINATOR);
    }

}
