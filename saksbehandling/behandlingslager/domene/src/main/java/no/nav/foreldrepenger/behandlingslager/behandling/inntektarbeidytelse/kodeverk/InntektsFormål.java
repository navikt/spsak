package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "InntektsFormål")
@DiscriminatorValue(InntektsFormål.DISCRIMINATOR)
public class InntektsFormål extends Kodeliste {

    public static final String DISCRIMINATOR = "INNTEKTS_FORMAAL"; //$NON-NLS-1$

    public static final InntektsFormål UDEFINERT = new InntektsFormål("-"); //$NON-NLS-1$
    public static final InntektsFormål FORMAAL_FORELDREPENGER = new InntektsFormål("FORMAAL_FORELDREPENGER"); //$NON-NLS-1$
    public static final InntektsFormål FORMAAL_PGI = new InntektsFormål("FORMAAL_PGI"); //$NON-NLS-1$

    private InntektsFormål(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public InntektsFormål() {
        //hibernate
    }
}
