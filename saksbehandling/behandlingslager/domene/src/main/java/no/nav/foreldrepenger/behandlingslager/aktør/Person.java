package no.nav.foreldrepenger.behandlingslager.aktør;

import no.nav.foreldrepenger.domene.typer.AktørId;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class Person extends Aktør {

    public Person(AktørId aktørId) {
        super(aktørId);
    }

}
