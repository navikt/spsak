package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "TypeFiske")
@DiscriminatorValue(TypeFiske.DISCRIMINATOR)
public class TypeFiske extends Kodeliste {

    public static final String DISCRIMINATOR = "TYPE_FISKE";

    public static final TypeFiske UDEFINERT = new TypeFiske("-"); //$NON-NLS-1$

    public static final TypeFiske BLAD_A = new TypeFiske("BLAD_A"); //$NON-NLS-1$
    public static final TypeFiske LOTT = new TypeFiske("LOTT"); //$NON-NLS-1$
    public static final TypeFiske BLAD_B = new TypeFiske("BLAD_B"); //$NON-NLS-1$
    public static final TypeFiske HYRE = new TypeFiske("HYRE"); //$NON-NLS-1$

    TypeFiske() {
        // Hibernate trenger en
    }

    private TypeFiske(String kode) {
        super(kode, DISCRIMINATOR);
    }

}
