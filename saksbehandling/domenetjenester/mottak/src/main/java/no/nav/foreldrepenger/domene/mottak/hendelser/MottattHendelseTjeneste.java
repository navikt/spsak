package no.nav.foreldrepenger.domene.mottak.hendelser;

import no.nav.foreldrepenger.domene.mottak.hendelser.kontrakt.Hendelse;

public interface MottattHendelseTjeneste {

    boolean erHendelseNy(String uid);

    void registrerHendelse(String uid, Hendelse hendelse);
}
