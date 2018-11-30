package no.nav.foreldrepenger.behandlingslager.behandling;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "BehandlingTema")
@DiscriminatorValue(BehandlingTema.DISCRIMINATOR)
public class BehandlingTema extends Kodeliste {

    public static final String DISCRIMINATOR = "BEHANDLING_TEMA";

    /**
     * Konstanter for å skrive ned kodeverdi. For å hente ut andre data konfigurert, må disse leses fra databasen (eks.
     * for å hente offisiell kode for et Nav kodeverk).
     */
    public static final BehandlingTema SYKEPENGER = new BehandlingTema("ab0061");

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

    public static BehandlingTema fraFagsak(@SuppressWarnings("unused") Fagsak fagsak) {
        // FIXME SP : finnes noe logikk for å avlede BehandlingTema fra Fagsak?
        return SYKEPENGER;
    }

}
