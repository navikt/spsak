package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "RelatertYtelseSakstype")
@DiscriminatorValue(RelatertYtelseSakstype.DISCRIMINATOR)
public class RelatertYtelseSakstype extends Kodeliste {
    public static final String DISCRIMINATOR = "RELATERT_YTELSE_SAKSTYPE";

    public static final RelatertYtelseSakstype SØKNAD = new RelatertYtelseSakstype("S"); //$NON-NLS-1$

    /* Legger inn udefinert kode.  Må gjerne erstattes av noe annet dersom starttilstand er kjent. */
    public static final RelatertYtelseSakstype UDEFINERT = new RelatertYtelseSakstype("-"); //$NON-NLS-1$


    RelatertYtelseSakstype() {
        // Hibernate trenger den
    }

    public RelatertYtelseSakstype(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
