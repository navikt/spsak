package no.nav.foreldrepenger.behandlingslager.behandling.historikk;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "HistorikkAvklartSoeknadsperiodeType")
@DiscriminatorValue(HistorikkAvklartSoeknadsperiodeType.DISCRIMINATOR)
public class HistorikkAvklartSoeknadsperiodeType extends Kodeliste {

    public static final String DISCRIMINATOR = "HISTORIKK_AVKLART_SOEKNADSPERIODE_TYPE"; //$NON-NLS-1$

    public static final HistorikkAvklartSoeknadsperiodeType UTSETTELSE_ARBEID = new HistorikkAvklartSoeknadsperiodeType("UTSETTELSE_ARBEID");

    HistorikkAvklartSoeknadsperiodeType() {
        // Hibernate
    }

    private HistorikkAvklartSoeknadsperiodeType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
