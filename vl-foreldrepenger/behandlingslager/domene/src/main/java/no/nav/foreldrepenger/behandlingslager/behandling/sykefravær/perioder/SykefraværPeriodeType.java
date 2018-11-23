package no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "SykefraværPeriodeType")
@DiscriminatorValue(SykefraværPeriodeType.DISCRIMINATOR)
public class SykefraværPeriodeType extends Kodeliste {

    public static final String DISCRIMINATOR = "SYKEFRAVÆR_PERIODE_TYPE";

    public static final SykefraværPeriodeType SYKEMELDT = new SykefraværPeriodeType("SYKEMELDT"); //$NON-NLS-1$
    public static final SykefraværPeriodeType EGENMELDING = new SykefraværPeriodeType("EGENMELDING"); //$NON-NLS-1$
    public static final SykefraværPeriodeType UDEFINERT = new SykefraværPeriodeType("-"); //$NON-NLS-1$

    SykefraværPeriodeType() {
        // Hibernate trenger en
    }

    private SykefraværPeriodeType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
