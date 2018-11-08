package no.nav.foreldrepenger.behandlingslager.behandling.klage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "KlageAvvistÅrsak")
@DiscriminatorValue(KlageAvvistÅrsak.DISCRIMINATOR)
public class KlageAvvistÅrsak extends Kodeliste {

    public static final String DISCRIMINATOR = "KLAGE_AVVIST_AARSAK";
    private static final Map<String, KlageAvvistÅrsak> REG_KODER = new HashMap<>();

    public static final KlageAvvistÅrsak KLAGET_FOR_SENT = new KlageAvvistÅrsak("KLAGET_FOR_SENT"); //$NON-NLS-1$
    public static final KlageAvvistÅrsak KLAGE_UGYLDIG = new KlageAvvistÅrsak("KLAGE_UGYLDIG"); //$NON-NLS-1$
    public static final KlageAvvistÅrsak UDEFINERT = new KlageAvvistÅrsak("-"); //$NON-NLS-1$

    KlageAvvistÅrsak() {
        // for hibernate
    }

    private KlageAvvistÅrsak(String kode) {
        super(kode, DISCRIMINATOR);
        REG_KODER.put(kode, this);
    }
    
    /** Kun til invortes bruk i tester. Ingen garanti for at dette dekker alle konstanter. */
    public static Map<String, KlageAvvistÅrsak> getHardkodedeKonstanter() {
        return Collections.unmodifiableMap(REG_KODER);
    }
}
