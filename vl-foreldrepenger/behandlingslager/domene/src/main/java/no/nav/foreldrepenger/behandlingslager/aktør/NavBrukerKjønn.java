package no.nav.foreldrepenger.behandlingslager.aktør;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "NavBrukerKjønn")
@DiscriminatorValue(NavBrukerKjønn.DISCRIMINATOR)
public class NavBrukerKjønn extends Kodeliste {

    public static final String DISCRIMINATOR = "BRUKER_KJOENN";
    public static final NavBrukerKjønn KVINNE = new NavBrukerKjønn("K");
    public static final NavBrukerKjønn MANN = new NavBrukerKjønn("M");

    public static final NavBrukerKjønn UDEFINERT = new NavBrukerKjønn("-");

    private static final Map<String, NavBrukerKjønn> TILGJENGELIGE = new HashMap<>();

    static {
        TILGJENGELIGE.put(KVINNE.getKode(), KVINNE);
        TILGJENGELIGE.put(MANN.getKode(), MANN);
    }


    NavBrukerKjønn() {
        // For Hibernate
    }

    public NavBrukerKjønn(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public static NavBrukerKjønn fraKode(String kode) {
        if (TILGJENGELIGE.containsKey(kode)) {
            return TILGJENGELIGE.get(kode);
        }
        return UDEFINERT;
    }
}
