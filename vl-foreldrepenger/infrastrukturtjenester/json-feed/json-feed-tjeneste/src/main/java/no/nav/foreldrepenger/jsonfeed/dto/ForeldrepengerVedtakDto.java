package no.nav.foreldrepenger.jsonfeed.dto;

import java.util.List;

import no.nav.foreldrepenger.kontrakter.feed.felles.FeedElement;

public class ForeldrepengerVedtakDto {
    private boolean harFlereElementer;
    private List<FeedElement> elementer;
    
    public ForeldrepengerVedtakDto(boolean harFlereElementer, List<FeedElement> elementer) {
        super();
        this.harFlereElementer = harFlereElementer;
        this.elementer = elementer;
    }

    public boolean isHarFlereElementer() {
        return harFlereElementer;
    }

    public List<FeedElement> getElementer() {
        return elementer;
    }
    
    
    

}
