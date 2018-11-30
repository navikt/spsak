package no.nav.foreldrepenger.behandlingslager.uttak;

import java.util.Optional;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "PeriodeResultatAarsak")
@DiscriminatorValue(PeriodeResultatÅrsak.DISCRIMINATOR)
public class PeriodeResultatÅrsak extends Kodeliste {
    public static final String DISCRIMINATOR = "PERIODE_RESULTAT_AARSAK";

    public static final PeriodeResultatÅrsak UKJENT = new PeriodeResultatÅrsak("-");

    @Transient
    private String lovReferanse;

    PeriodeResultatÅrsak() {
        // For hibernate
    }

    PeriodeResultatÅrsak(String kode) {
        super(kode, DISCRIMINATOR);
    }

    protected PeriodeResultatÅrsak(String kode, String discriminator) {
        super(kode, discriminator);
    }

    public Optional<String> getLovReferanse(FagsakYtelseType fagsakYtelseType) {
        if (lovReferanse == null) {
            lovReferanse = getJsonField("fagsakYtelseType", fagsakYtelseType.getKode(), "lovreferanse"); //$NON-NLS-1$
        }
        return Optional.ofNullable(lovReferanse);
    }
}
