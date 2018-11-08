package no.nav.foreldrepenger.behandlingslager.behandling.historikk;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "HistorikkAvklartSoeknadsperiodeType")
@DiscriminatorValue(HistorikkAvklartSoeknadsperiodeType.DISCRIMINATOR)
public class HistorikkAvklartSoeknadsperiodeType extends Kodeliste {

    public static final String DISCRIMINATOR = "HISTORIKK_AVKLART_SOEKNADSPERIODE_TYPE"; //$NON-NLS-1$

    public static final HistorikkAvklartSoeknadsperiodeType UDEFINIERT = new HistorikkAvklartSoeknadsperiodeType("-");

    public static final HistorikkAvklartSoeknadsperiodeType GRADERING = new HistorikkAvklartSoeknadsperiodeType("GRADERING");
    public static final HistorikkAvklartSoeknadsperiodeType UTSETTELSE_ARBEID = new HistorikkAvklartSoeknadsperiodeType("UTSETTELSE_ARBEID");
    public static final HistorikkAvklartSoeknadsperiodeType UTSETTELSE_FERIE = new HistorikkAvklartSoeknadsperiodeType("UTSETTELSE_FERIE");
    public static final HistorikkAvklartSoeknadsperiodeType UTSETTELSE_SKYDOM = new HistorikkAvklartSoeknadsperiodeType("UTSETTELSE_SKYDOM");
    public static final HistorikkAvklartSoeknadsperiodeType UTSETTELSE_INSTITUSJON_SØKER = new HistorikkAvklartSoeknadsperiodeType("UTSETTELSE_INSTITUSJON_SØKER");
    public static final HistorikkAvklartSoeknadsperiodeType UTSETTELSE_INSTITUSJON_BARN = new HistorikkAvklartSoeknadsperiodeType("UTSETTELSE_INSTITUSJON_BARN");
    public static final HistorikkAvklartSoeknadsperiodeType NY_SOEKNADSPERIODE = new HistorikkAvklartSoeknadsperiodeType("NY_SOEKNADSPERIODE");
    public static final HistorikkAvklartSoeknadsperiodeType SLETTET_SOEKNASPERIODE = new HistorikkAvklartSoeknadsperiodeType("SLETTET_SOEKNASPERIODE");
    public static final HistorikkAvklartSoeknadsperiodeType OVERFOERING_ALENEOMSORG = new HistorikkAvklartSoeknadsperiodeType("OVERFOERING_ALENEOMSORG");
    public static final HistorikkAvklartSoeknadsperiodeType OVERFOERING_SKYDOM = new HistorikkAvklartSoeknadsperiodeType("OVERFOERING_SKYDOM");
    public static final HistorikkAvklartSoeknadsperiodeType OVERFOERING_INNLEGGELSE = new HistorikkAvklartSoeknadsperiodeType("OVERFOERING_INNLEGGELSE");
    public static final HistorikkAvklartSoeknadsperiodeType OVERFOERING_IKKE_RETT = new HistorikkAvklartSoeknadsperiodeType("OVERFOERING_IKKE_RETT");
    public static final HistorikkAvklartSoeknadsperiodeType UTTAK = new HistorikkAvklartSoeknadsperiodeType("UTTAK");

    HistorikkAvklartSoeknadsperiodeType() {
        //Hibernate
    }

    private HistorikkAvklartSoeknadsperiodeType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
