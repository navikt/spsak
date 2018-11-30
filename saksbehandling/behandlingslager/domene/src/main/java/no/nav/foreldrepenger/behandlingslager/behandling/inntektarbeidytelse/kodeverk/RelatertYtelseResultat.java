package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "RelatertYtelseResultat")
@DiscriminatorValue(RelatertYtelseResultat.DISCRIMINATOR)
public class RelatertYtelseResultat extends Kodeliste {
    public static final String DISCRIMINATOR = "RELATERT_YTELSE_RESULTAT";

    public static final RelatertYtelseResultat INNVILGET = new RelatertYtelseResultat("I"); //$NON-NLS-1$

    /* Legger inn udefinert kode.  Må gjerne erstattes av noe annet dersom starttilstand er kjent. */
    public static final RelatertYtelseResultat UDEFINERT = new RelatertYtelseResultat("-"); //$NON-NLS-1$

    RelatertYtelseResultat() {
        // Hibernate trenger den
    }

    public RelatertYtelseResultat(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
