package no.nav.foreldrepenger.kontrakter.feed.felles;

import java.util.Objects;

public class FeedElement {

    private String type;

    private long sekvensId;

    private long koblingId;

    private Object innhold;

    private Object metadata;

    public FeedElement() {
        // Default Constructor for Jackson
    }

    private FeedElement(Builder builder) {
        type = builder.type;
        sekvensId = builder.sekvensId;
        koblingId = builder.koblingId;
        innhold = builder.innhold;
        metadata = builder.metadata;
    }

    public String getType() {
        return type;
    }

    public long getSekvensId() {
        return sekvensId;
    }

    public long getKoblingId() {
        return koblingId;
    }

    public Object getInnhold() {
        return innhold;
    }

    public Object getMetadata() {
        return metadata;
    }


    public static class Builder {
        private String type;
        private long sekvensId;
        private long koblingId;

        private Object innhold;
        private Object metadata;

        public Builder medType(String val) {
            type = val;
            return this;
        }

        public Builder medSekvensId(long val) {
            sekvensId = val;
            return this;
        }

        public Builder medKoblingId(long val) {
            koblingId  = val;
            return this;
        }

        public Builder medInnhold(Object val) {
            innhold = val;
            return this;
        }

        public Builder medMetadata(Object val) {
            metadata = val;
            return this;
        }

        public FeedElement build() {
        	Objects.requireNonNull(type, "type"); //$NON-NLS-1$
        	Objects.requireNonNull(sekvensId, "sekvensId"); //$NON-NLS-1$
        	Objects.requireNonNull(innhold, "innhold"); //$NON-NLS-1$        	
            return new FeedElement(this);
        }
    }

}
