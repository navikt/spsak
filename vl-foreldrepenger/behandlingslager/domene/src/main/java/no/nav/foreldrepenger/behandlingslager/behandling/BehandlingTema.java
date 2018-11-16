package no.nav.foreldrepenger.behandlingslager.behandling;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "BehandlingTema")
@DiscriminatorValue(BehandlingTema.DISCRIMINATOR)
public class BehandlingTema extends Kodeliste {

    public static final String DISCRIMINATOR = "BEHANDLING_TEMA";

    /**
     * Konstanter for å skrive ned kodeverdi. For å hente ut andre data konfigurert, må disse leses fra databasen (eks.
     * for å hente offisiell kode for et Nav kodeverk).
     */
    public static final BehandlingTema ENGANGSSTØNAD = new BehandlingTema("ENGST"); //$NON-NLS-1$
    public static final BehandlingTema ENGANGSSTØNAD_FØDSEL = new BehandlingTema("ENGST_FODS"); //$NON-NLS-1$
    public static final BehandlingTema ENGANGSSTØNAD_ADOPSJON = new BehandlingTema("ENGST_ADOP"); //$NON-NLS-1$

    public static final BehandlingTema FORELDREPENGER = new BehandlingTema("FORP"); //$NON-NLS-1$
    public static final BehandlingTema FORELDREPENGER_ADOPSJON = new BehandlingTema("FORP_ADOP"); //$NON-NLS-1$
    public static final BehandlingTema FORELDREPENGER_FØDSEL = new BehandlingTema("FORP_FODS"); //$NON-NLS-1$

    /**
     * Alle kodeverk må ha en verdi, det kan ikke være null i databasen. Denne koden gjør samme nytten.
     */
    public static final BehandlingTema UDEFINERT = new BehandlingTema("-"); //$NON-NLS-1$

    BehandlingTema() {
        // Hibernate trenger den
    }

    private BehandlingTema(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public static boolean gjelderEngangsstønad(BehandlingTema behandlingTema) {
        return ENGANGSSTØNAD_ADOPSJON.equals(behandlingTema) || ENGANGSSTØNAD_FØDSEL.equals(behandlingTema) || ENGANGSSTØNAD.equals(behandlingTema);
    }

    public static boolean gjelderForeldrepenger(BehandlingTema behandlingTema) {
        return FORELDREPENGER_ADOPSJON.equals(behandlingTema) || FORELDREPENGER_FØDSEL.equals(behandlingTema) || FORELDREPENGER.equals(behandlingTema);
    }

    public static boolean ikkeSpesifikkHendelse(BehandlingTema behandlingTema) {
        return FORELDREPENGER.equals(behandlingTema) || ENGANGSSTØNAD.equals(behandlingTema) || UDEFINERT.equals(behandlingTema);
    }

    public static boolean gjelderSammeYtelse(BehandlingTema tema1, BehandlingTema tema2) {
        return (gjelderForeldrepenger(tema1) && gjelderForeldrepenger(tema2))
            || (gjelderEngangsstønad(tema1) && gjelderEngangsstønad(tema2));
    }

    public static BehandlingTema fraFagsak(Fagsak fagsak) {
        FagsakYtelseType ytelseType = fagsak.getYtelseType();

        if (ytelseType.gjelderForeldrepenger()) {
            return FORELDREPENGER;
        }

        return UDEFINERT;
    }

}
