package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity()
@DiscriminatorValue(UttakPeriodeVurderingType.DISCRIMINATOR)
public class UttakPeriodeVurderingType extends Kodeliste {

    public static final String DISCRIMINATOR = "UTTAK_PERIODE_VURDERING_TYPE";

    public static final UttakPeriodeVurderingType PERIODE_OK = new UttakPeriodeVurderingType("PERIODE_OK");
    public static final UttakPeriodeVurderingType PERIODE_OK_ENDRET = new UttakPeriodeVurderingType("PERIODE_OK_ENDRET");
    public static final UttakPeriodeVurderingType PERIODE_KAN_IKKE_AVKLARES = new UttakPeriodeVurderingType("PERIODE_KAN_IKKE_AVKLARES");
    public static final UttakPeriodeVurderingType PERIODE_IKKE_VURDERT = new UttakPeriodeVurderingType("PERIODE_IKKE_VURDERT");

    UttakPeriodeVurderingType() {
        // For hibernate
    }

    UttakPeriodeVurderingType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
