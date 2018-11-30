package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "FagsystemUnderkategori")
@DiscriminatorValue(FagsystemUnderkategori.DISCRIMINATOR)
public class FagsystemUnderkategori extends Kodeliste {

    public static final String DISCRIMINATOR = "FAGSYSTEM_UNDERKATEGORI";
    public static final FagsystemUnderkategori INFOTRYGD_SAK = new FagsystemUnderkategori("INFOTRYGD_SAK"); //$NON-NLS-1$
    public static final FagsystemUnderkategori INFOTRYGD_VEDTAK = new FagsystemUnderkategori("INFOTRYGD_VEDTAK"); //$NON-NLS-1$
    public static final FagsystemUnderkategori UDEFINERT = new FagsystemUnderkategori("-"); //$NON-NLS-1$

    FagsystemUnderkategori() {
        // Hibernate trenger den
    }

    private FagsystemUnderkategori(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
