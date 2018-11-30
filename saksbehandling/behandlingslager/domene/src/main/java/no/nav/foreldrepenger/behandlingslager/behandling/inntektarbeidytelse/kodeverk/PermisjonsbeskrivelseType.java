package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "PermisjonsbeskrivelseType")
@DiscriminatorValue(PermisjonsbeskrivelseType.DISCRIMINATOR)
public class PermisjonsbeskrivelseType extends Kodeliste{
    public static final String DISCRIMINATOR = "PERMISJONSBESKRIVELSE_TYPE"; //$NON-NLS-1$

    public static final PermisjonsbeskrivelseType UDEFINERT = new PermisjonsbeskrivelseType("-"); //$NON-NLS-1$
    public static final PermisjonsbeskrivelseType UTDANNINGSPERMISJON = new PermisjonsbeskrivelseType("UTDANNINGSPERMISJON"); //$NON-NLS-1$

    private PermisjonsbeskrivelseType(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public PermisjonsbeskrivelseType() {
        //hibernate
    }
}
