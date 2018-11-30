package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "RelatertYtelseTilstand")
@DiscriminatorValue(RelatertYtelseTilstand.DISCRIMINATOR)
public class RelatertYtelseTilstand extends Kodeliste {

    public static final String DISCRIMINATOR = "RELATERT_YTELSE_TILSTAND";
    public static final RelatertYtelseTilstand ÅPEN = new RelatertYtelseTilstand("ÅPEN");
    public static final RelatertYtelseTilstand LØPENDE = new RelatertYtelseTilstand("LØPENDE");
    public static final RelatertYtelseTilstand AVSLUTTET = new RelatertYtelseTilstand("AVSLUTTET");
    public static final RelatertYtelseTilstand IKKE_STARTET = new RelatertYtelseTilstand("IKKESTARTET");

    RelatertYtelseTilstand() {
        // Hibernate trenger den
    }

    private RelatertYtelseTilstand(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
