package no.nav.foreldrepenger.kontrakter.feed.infotrygd.v1;

import java.time.LocalDateTime;

public class InfotrygdMetadata {

    private LocalDateTime opprettetDato;

    public LocalDateTime getOpprettetDato() {
        return opprettetDato;
    }

    private InfotrygdMetadata(Builder builder) {
        opprettetDato = builder.opprettetDato;
    }

    public static class Builder {
        private LocalDateTime opprettetDato;

        public Builder medOpprettetDato(LocalDateTime val) {
            opprettetDato = val;
            return this;
        }

        public InfotrygdMetadata build() {
            return new InfotrygdMetadata(this);
        }
    }

}
