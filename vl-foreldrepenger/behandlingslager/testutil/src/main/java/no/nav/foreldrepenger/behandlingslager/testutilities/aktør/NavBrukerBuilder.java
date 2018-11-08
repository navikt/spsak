package no.nav.foreldrepenger.behandlingslager.testutilities.aktør;

import no.nav.foreldrepenger.domene.typer.AktørId;
import static no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn.KVINNE;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;

public class NavBrukerBuilder {

    private static AtomicLong counter = new AtomicLong(100000L);
    private NavBruker bruker;

    private AktørId aktørId = new AktørId(counter.getAndIncrement());
    private Personinfo personinfo;
    private NavBrukerKjønn kjønn;
    private Språkkode språkkode;

    public NavBrukerBuilder() {
        // default ctor
    }

    public NavBrukerBuilder medAktørId(AktørId aktørId) {
        this.aktørId = aktørId;
        return this;
    }

    public NavBrukerBuilder medBruker(NavBruker bruker) {
        this.bruker = bruker;
        return this;
    }

    public NavBrukerBuilder medPersonInfo(Personinfo personinfo) {
        this.personinfo = personinfo;
        return this;
    }

    public NavBrukerBuilder medKjønn(NavBrukerKjønn kjønn) {
        this.kjønn = kjønn;
        return this;
    }

    public NavBrukerBuilder medForetrukketSpråk(Språkkode språkkode) {
        this.språkkode = språkkode;
        return this;
    }

    public NavBrukerKjønn getKjønn() {
        return kjønn;
    }

    public Språkkode getSpråkkode() {
        return språkkode;
    }

    public AktørId getAktørId() {
        if(bruker != null) {
            return bruker.getAktørId();
        }
        return aktørId;
    }

    public NavBruker build() {
        if (bruker != null) {
            return bruker;
        }
        if (personinfo == null) {

            personinfo = new NavPersoninfoBuilder()
                .medAktørId(aktørId)
                .medKjønn(Optional.ofNullable(kjønn).orElse(KVINNE))
                .medForetrukketSpråk(språkkode)
                .build();
        }
        return NavBruker.opprettNy(personinfo);
    }
}
