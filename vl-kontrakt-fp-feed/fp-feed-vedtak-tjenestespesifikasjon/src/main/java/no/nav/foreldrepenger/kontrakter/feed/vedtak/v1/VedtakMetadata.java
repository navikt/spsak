package no.nav.foreldrepenger.kontrakter.feed.vedtak.v1;

import java.time.ZonedDateTime;

public class VedtakMetadata {

    private ZonedDateTime opprettetDato;

    public ZonedDateTime getOpprettetDato() {
        return opprettetDato;
    }

    private VedtakMetadata(Builder builder) {
        opprettetDato = builder.opprettetDato;
    }

    public static class Builder {
        private ZonedDateTime opprettetDato;

        public Builder medOpprettetDato(ZonedDateTime val) {
            opprettetDato = val;
            return this;
        }

        public VedtakMetadata build() {
            return new VedtakMetadata(this);
        }
    }

}
