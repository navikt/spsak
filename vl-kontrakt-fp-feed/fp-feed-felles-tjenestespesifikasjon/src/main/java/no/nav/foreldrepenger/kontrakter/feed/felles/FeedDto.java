package no.nav.foreldrepenger.kontrakter.feed.felles;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class FeedDto {

    private String tittel;

    private Boolean inneholderFlereElementer;
    private List<FeedElement> elementer;

    public FeedDto() {
        // Default Constructor for Jackson
    }

    private FeedDto(Builder builder) {
        tittel = builder.tittel;
        inneholderFlereElementer = builder.inneholderFlereElementer;
        elementer = builder.elementer;
    }

    public String getTittel() {
        return tittel;
    }

    public Boolean getInneholderFlereElementer() {
        return inneholderFlereElementer;
    }

    public List<FeedElement> getElementer() {
        return elementer;
    }

    public static class Builder {
        private String tittel;
        private Boolean inneholderFlereElementer;
        private List<FeedElement> elementer;

        public Builder medTittel(String val) {
            this.tittel = val;
            return this;
        }

        public Builder medInneholderFlereElementer(Boolean val) {
            this.inneholderFlereElementer = val;
            return this;
        }

        public Builder medElementer(List<FeedElement> values) {
            this.elementer = values;
            return this;
        }
        
        public Builder leggTilElementer(FeedElement... elementer) {
        	if (this.elementer == null) {
        		this.elementer = new LinkedList<>();
        	}
        	this.elementer.addAll(Arrays.asList(elementer));
            return this;
        }
        
        public FeedDto build() {
        	Objects.requireNonNull(tittel, "tittel"); //$NON-NLS-1$
        	Objects.requireNonNull(elementer, "elementer"); //$NON-NLS-1$
            return new FeedDto(this);
        }
    }
}
