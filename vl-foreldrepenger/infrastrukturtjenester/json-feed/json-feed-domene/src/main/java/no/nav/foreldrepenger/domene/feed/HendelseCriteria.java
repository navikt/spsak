package no.nav.foreldrepenger.domene.feed;

import java.util.Objects;

public class HendelseCriteria {
    private Long sisteLestSekvensId;
    private String type;
    private String aktørId;
    private Long maxAntall;

    public Long getSisteLestSekvensId() {
        return sisteLestSekvensId;
    }

    public String getType() {
        return type;
    }

    public String getAktørId() {
        return aktørId;
    }

    public Long getMaxAntall() {
        return maxAntall;
    }
    
    public Long getSisteSekvensId() {
        return sisteLestSekvensId + maxAntall;
    }
    
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long sisteLestSekvensId;
        private String type;
        private String aktørId;
        private Long maxAntall;

        public Builder medSisteLestSekvensId(Long sisteLestSekvensId) {
            this.sisteLestSekvensId = sisteLestSekvensId;
            return this;
        }

        public Builder medType(String type) {
            this.type = type;
            return this;
        }

        public Builder medAktørId(String aktørId) {
            this.aktørId = aktørId;
            return this;
        }

        public Builder medMaxAntall(Long maxAntall) {
            this.maxAntall = maxAntall;
            return this;
        }

        public HendelseCriteria build() {
            HendelseCriteria hc = new HendelseCriteria();
            Objects.requireNonNull(maxAntall);
            Objects.requireNonNull(sisteLestSekvensId);
            hc.sisteLestSekvensId = sisteLestSekvensId;
            hc.type = type;
            hc.aktørId = aktørId;
            hc.maxAntall = maxAntall;
            return hc;
        }
    }
}
