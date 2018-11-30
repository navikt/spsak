package no.nav.foreldrepenger.behandlingslager.testutilities.aktør;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.domene.typer.AktørId;

public class NavBrukerBuilder {

    private NavBruker bruker;
    private Språkkode språkkode = Språkkode.nb;

    public NavBrukerBuilder() {
        // default ctor
    }

    public NavBrukerBuilder medBruker(NavBruker bruker) {
        this.bruker = bruker;
        return this;
    }

    public Språkkode getSpråkkode() {
        return språkkode;
    }

    public AktørId getAktørId() {
        return bruker.getAktørId();
    }

    public NavBruker build() {
        return bruker;
    }
    
    public NavBrukerBuilder medForetrukketSpråk(Språkkode språkkode) {
        this.språkkode = språkkode;
        return this;
    }

    public NavBruker build(AktørId aktørId) {
        return NavBruker.opprettNy(new NavPersoninfoBuilder()
            .medAktørId(aktørId)
            .medForetrukketSpråk(språkkode)
            .build());
    }
}
