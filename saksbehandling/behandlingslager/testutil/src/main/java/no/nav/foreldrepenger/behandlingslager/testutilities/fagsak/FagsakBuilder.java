package no.nav.foreldrepenger.behandlingslager.testutilities.fagsak;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.aktør.NavBrukerBuilder;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

/**
 * Builder for å bygge enkle fagsaker. Primært for test.
 */
public class FagsakBuilder {

    private Saksnummer saksnummer;

    private NavBrukerBuilder brukerBuilder = new NavBrukerBuilder();

    private Fagsak fagsak;

    private FagsakBuilder() {
    }

    private FagsakBuilder(Fagsak fagsak) {
        this.fagsak = fagsak;
    }

    public FagsakBuilder medSaksnummer(Saksnummer saksnummer) {
        validerFagsakIkkeSatt();
        this.saksnummer = saksnummer;
        return this;
    }

    private void validerFagsakIkkeSatt() {
        if (fagsak != null) {
            throw new IllegalStateException("Fagsak er allerede konfigurert, kan ikke overstyre her");
        }
    }

    public NavBrukerBuilder getBrukerBuilder() {
        return brukerBuilder;
    }

    public FagsakBuilder medBruker(NavBruker bruker) {
        validerFagsakIkkeSatt();
        brukerBuilder.medBruker(bruker);
        return this;
    }

    public static FagsakBuilder enkel(Fagsak fagsak) {
        return new FagsakBuilder(fagsak);
    }

    public static FagsakBuilder nyFagsak() {
        return new FagsakBuilder();
    }

    public Fagsak build() {

        if (fagsak != null) {
            return fagsak;
        } else {
            fagsak = Fagsak.opprettNy(brukerBuilder.build(), saksnummer);
            return fagsak;
        }

    }
}
