package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.util.Objects;

public enum PeriodeVurderingType {
    PERIODE_OK,
    ENDRE_PERIODE,
    UAVKLART_PERIODE,
    IKKE_VURDERT;

    public static boolean avklart(PeriodeVurderingType periodeVurderingType) {
        return Objects.equals(periodeVurderingType, PERIODE_OK) || Objects.equals(periodeVurderingType, ENDRE_PERIODE);
    }
}
