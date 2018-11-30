package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse;

public class AktørYtelseEndring {
    private final boolean ytelserFpsakEndret;
    private final boolean ytelserEksterneEndret;

    AktørYtelseEndring(boolean ytelserFpsakEndret, boolean ytelserEksterneEndret) {
        this.ytelserFpsakEndret = ytelserFpsakEndret;
        this.ytelserEksterneEndret = ytelserEksterneEndret;
    }

    public boolean erEndret() {
        return ytelserFpsakEndret || ytelserEksterneEndret;
    }

    public boolean erEksterneRegistreEndret() {
        return ytelserEksterneEndret;
    }
}
