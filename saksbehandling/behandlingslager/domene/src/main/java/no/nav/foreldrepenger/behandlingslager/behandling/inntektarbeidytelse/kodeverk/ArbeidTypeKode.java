package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "ArbeidTypeKode")
@DiscriminatorValue(ArbeidTypeKode.DISCRIMINATOR)
class ArbeidTypeKode extends Kodeliste {

    public static final String DISCRIMINATOR = "ARBEID_TYPE_KODE";

    public static final ArbeidTypeKode UDEFINERT = new ArbeidTypeKode("-"); //$NON-NLS-1$

    private ArbeidTypeKode(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public ArbeidTypeKode() {
        //hibernate
    }

    protected ArbeidTypeKode(String kode, String discriminator) {
        super(kode, discriminator);
    }
}
