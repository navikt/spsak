package no.nav.foreldrepenger.domene.feed;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "VedtakUtgaaendeHendelse")
@DiscriminatorValue(VedtakUtgåendeHendelse.FEED_NAVN_VEDTAK)
@SekvensnummerNavn(value=VedtakUtgåendeHendelse.SEQ_GENERATOR_NAVN)
public class VedtakUtgåendeHendelse extends UtgåendeHendelse {

    static final String FEED_NAVN_VEDTAK = "VEDTAK_FP";
    static final String SEQ_GENERATOR_NAVN = "SEQ_" + FEED_NAVN_VEDTAK;

    private VedtakUtgåendeHendelse() {
        super();
    }    
    
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String type;
        private String payload;
        private String aktørId;
        private String kildeId;

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public Builder aktørId(String aktørId) {
            this.aktørId = aktørId;
            return this;
        }
        
        public Builder kildeId(String kildeId) {
            this.kildeId = kildeId;
            return this;
        }  

        public VedtakUtgåendeHendelse build() {
            VedtakUtgåendeHendelse hendelse = new VedtakUtgåendeHendelse();
            hendelse.setType(type);
            hendelse.setPayload(payload);
            hendelse.setAktørId(aktørId);
            hendelse.setKildeId(kildeId);
            return hendelse;
        }
    }
}
