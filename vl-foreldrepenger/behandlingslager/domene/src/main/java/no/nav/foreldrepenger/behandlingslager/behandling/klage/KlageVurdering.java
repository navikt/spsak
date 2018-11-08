package no.nav.foreldrepenger.behandlingslager.behandling.klage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "KlageVurdering")
@DiscriminatorValue(KlageVurdering.DISCRIMINATOR)
public class KlageVurdering extends Kodeliste {

    public static final String DISCRIMINATOR = "KLAGEVURDERING";
    private static final Map<String, KlageVurdering> REG_KODER = new HashMap<>();


    public static final KlageVurdering OPPHEVE_YTELSESVEDTAK = new KlageVurdering("OPPHEVE_YTELSESVEDTAK"); //$NON-NLS-1$
    public static final KlageVurdering STADFESTE_YTELSESVEDTAK = new KlageVurdering("STADFESTE_YTELSESVEDTAK"); //$NON-NLS-1$
    public static final KlageVurdering MEDHOLD_I_KLAGE = new KlageVurdering("MEDHOLD_I_KLAGE"); //$NON-NLS-1$
    public static final KlageVurdering AVVIS_KLAGE = new KlageVurdering("AVVIS_KLAGE"); //$NON-NLS-1$

    private KlageVurdering() {
        // for hibernate
    }

    private KlageVurdering(String kode) {
        super(kode, DISCRIMINATOR);
        REG_KODER.putIfAbsent(kode, this);
    }
    

    /** Kun til invortes bruk i tester. Ingen garanti for at dette dekker alle konstanter. */
    public static Map<String, KlageVurdering> getHardkodedeKonstanter() {
        return Collections.unmodifiableMap(REG_KODER);
    }
}
